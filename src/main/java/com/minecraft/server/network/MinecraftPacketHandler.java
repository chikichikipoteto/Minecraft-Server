package com.minecraft.server.network;

import com.minecraft.server.MinecraftServer;
import com.minecraft.server.util.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.ReadTimeoutException;

import java.net.InetSocketAddress;

/**
 * Minecraftプロトコルのパケットを処理するハンドラー
 */
public class MinecraftPacketHandler extends SimpleChannelInboundHandler<ByteBuf> {
    
    private final MinecraftServer server;
    private final Logger logger;
    private final ConnectionState connectionState;
    private String clientAddress;
    
    public MinecraftPacketHandler(MinecraftServer server) {
        this.server = server;
        this.logger = new Logger("PacketHandler");
        this.connectionState = new ConnectionState();
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        this.clientAddress = address.getAddress().getHostAddress() + ":" + address.getPort();
        
        logger.info("クライアントが接続しました: " + clientAddress);
        
        // 接続状態を初期化
        connectionState.setState(ConnectionState.State.HANDSHAKING);
        
        super.channelActive(ctx);
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("クライアントが切断しました: " + clientAddress);
        
        // プレイヤーをサーバーから削除
        // TODO: プレイヤーマネージャーでプレイヤーを削除
        
        super.channelInactive(ctx);
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        try {
            // パケットIDを読み取り
            int packetId = readVarInt(msg);
            
            // 現在の状態に基づいてパケットを処理
            switch (connectionState.getState()) {
                case HANDSHAKING:
                    handleHandshakePacket(ctx, packetId, msg);
                    break;
                case STATUS:
                    handleStatusPacket(ctx, packetId, msg);
                    break;
                case LOGIN:
                    handleLoginPacket(ctx, packetId, msg);
                    break;
                case PLAY:
                    handlePlayPacket(ctx, packetId, msg);
                    break;
                default:
                    logger.warn("不明な接続状態: " + connectionState.getState());
                    break;
            }
            
        } catch (Exception e) {
            logger.error("パケット処理中にエラーが発生しました", e);
            ctx.close();
        }
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof ReadTimeoutException) {
            logger.warn("クライアントがタイムアウトしました: " + clientAddress);
        } else {
            logger.error("チャンネルでエラーが発生しました: " + clientAddress, cause);
        }
        ctx.close();
    }
    
    /**
     * ハンドシェイクパケットを処理します
     */
    private void handleHandshakePacket(ChannelHandlerContext ctx, int packetId, ByteBuf msg) {
        if (packetId == 0x00) { // Handshake packet
            int protocolVersion = readVarInt(msg);
            String serverAddress = readString(msg);
            int serverPort = msg.readUnsignedShort();
            int nextState = readVarInt(msg);
            
            logger.debug("ハンドシェイク - プロトコル: " + protocolVersion + 
                        ", アドレス: " + serverAddress + ":" + serverPort + 
                        ", 次の状態: " + nextState);
            
            // プロトコルバージョンをチェック
            if (protocolVersion != getProtocolVersion(server.getProtocolVersion())) {
                logger.warn("サポートされていないプロトコルバージョン: " + protocolVersion);
                // TODO: 切断パケットを送信
                return;
            }
            
            // 次の状態に移行
            if (nextState == 1) {
                connectionState.setState(ConnectionState.State.STATUS);
            } else if (nextState == 2) {
                connectionState.setState(ConnectionState.State.LOGIN);
            }
        }
    }
    
    /**
     * ステータスパケットを処理します
     */
    private void handleStatusPacket(ChannelHandlerContext ctx, int packetId, ByteBuf msg) {
        if (packetId == 0x00) { // Request packet
            // サーバー情報を送信
            sendServerStatus(ctx);
        } else if (packetId == 0x01) { // Ping packet
            long payload = msg.readLong();
            // Pongパケットを送信
            sendPong(ctx, payload);
        }
    }
    
    /**
     * ログインパケットを処理します
     */
    private void handleLoginPacket(ChannelHandlerContext ctx, int packetId, ByteBuf msg) {
        if (packetId == 0x00) { // Login Start packet
            String username = readString(msg);
            logger.info("ログイン開始: " + username + " (" + clientAddress + ")");
            
            // TODO: 認証処理
            // 現在はオフラインモードで処理
            
            // ログイン成功パケットを送信
            sendLoginSuccess(ctx, username);
            
            // プレイ状態に移行
            connectionState.setState(ConnectionState.State.PLAY);
            
            // プレイヤーをサーバーに追加
            // TODO: プレイヤーマネージャーでプレイヤーを追加
            
        } else if (packetId == 0x01) { // Encryption Response packet
            // TODO: オンラインモードでの暗号化処理
        }
    }
    
    /**
     * プレイパケットを処理します
     */
    private void handlePlayPacket(ChannelHandlerContext ctx, int packetId, ByteBuf msg) {
        // TODO: プレイ状態のパケット処理
        // - プレイヤー移動
        // - ブロック操作
        // - チャット
        // - インベントリ操作
        // など
        
        logger.debug("プレイパケット受信: 0x" + Integer.toHexString(packetId));
    }
    
    /**
     * サーバーステータスを送信します
     */
    private void sendServerStatus(ChannelHandlerContext ctx) {
        // TODO: 実際のサーバー情報を含むJSONレスポンスを送信
        String response = "{\"version\":{\"name\":\"" + server.getProtocolVersion() + 
                         "\",\"protocol\":" + getProtocolVersion(server.getProtocolVersion()) + 
                         "},\"players\":{\"max\":" + server.getConfig().getMaxPlayers() + 
                         ",\"online\":0},\"description\":{\"text\":\"" + server.getConfig().getMotd() + "\"}}";
        
        // レスポンスパケットを送信
        ByteBuf buffer = ctx.alloc().buffer();
        writeVarInt(buffer, 0x00); // Response packet ID
        writeString(buffer, response);
        ctx.writeAndFlush(buffer);
    }
    
    /**
     * Pongパケットを送信します
     */
    private void sendPong(ChannelHandlerContext ctx, long payload) {
        ByteBuf buffer = ctx.alloc().buffer();
        writeVarInt(buffer, 0x01); // Pong packet ID
        buffer.writeLong(payload);
        ctx.writeAndFlush(buffer);
    }
    
    /**
     * ログイン成功パケットを送信します
     */
    private void sendLoginSuccess(ChannelHandlerContext ctx, String username) {
        ByteBuf buffer = ctx.alloc().buffer();
        writeVarInt(buffer, 0x02); // Login Success packet ID
        writeString(buffer, "00000000-0000-0000-0000-000000000000"); // UUID
        writeString(buffer, username);
        ctx.writeAndFlush(buffer);
    }
    
    // ユーティリティメソッド
    
    /**
     * VarIntを読み取ります
     */
    private int readVarInt(ByteBuf buf) {
        int value = 0;
        int position = 0;
        byte currentByte;
        
        while (true) {
            currentByte = buf.readByte();
            value |= (currentByte & 0x7F) << position;
            
            if ((currentByte & 0x80) == 0) {
                break;
            }
            
            position += 7;
            if (position >= 32) {
                throw new RuntimeException("VarInt is too big");
            }
        }
        
        return value;
    }
    
    /**
     * VarIntを書き込みます
     */
    private void writeVarInt(ByteBuf buf, int value) {
        while (true) {
            if ((value & ~0x7F) == 0) {
                buf.writeByte(value);
                return;
            }
            
            buf.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
    }
    
    /**
     * 文字列を読み取ります
     */
    private String readString(ByteBuf buf) {
        int length = readVarInt(buf);
        if (length > 32767) {
            throw new RuntimeException("String is too long");
        }
        
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
    }
    
    /**
     * 文字列を書き込みます
     */
    private void writeString(ByteBuf buf, String string) {
        byte[] bytes = string.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        writeVarInt(buf, bytes.length);
        buf.writeBytes(bytes);
    }
    
    /**
     * プロトコルバージョンを取得します
     */
    private int getProtocolVersion(String version) {
        // 簡易的なバージョンマッピング
        switch (version) {
            case "1.20.1": return 763;
            case "1.20": return 762;
            case "1.19.4": return 762;
            case "1.19.3": return 761;
            case "1.19.2": return 760;
            case "1.19.1": return 760;
            case "1.19": return 759;
            default: return 763; // デフォルトは1.20.1
        }
    }
    
    /**
     * 接続状態を管理する内部クラス
     */
    private static class ConnectionState {
        public enum State {
            HANDSHAKING,
            STATUS,
            LOGIN,
            PLAY
        }
        
        private State state = State.HANDSHAKING;
        
        public State getState() {
            return state;
        }
        
        public void setState(State state) {
            this.state = state;
        }
    }
} 