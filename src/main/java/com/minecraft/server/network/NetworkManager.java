package com.minecraft.server.network;

import com.minecraft.server.MinecraftServer;
import com.minecraft.server.util.Logger;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

import java.util.concurrent.TimeUnit;

/**
 * ネットワーク通信を管理するクラス
 */
public class NetworkManager {
    
    private final MinecraftServer server;
    private final Logger logger;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private Channel serverChannel;
    private volatile boolean running = false;
    
    public NetworkManager(MinecraftServer server) {
        this.server = server;
        this.logger = new Logger("NetworkManager");
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup();
    }
    
    /**
     * ネットワークサーバーを起動します
     */
    public void start() throws InterruptedException {
        if (running) {
            logger.warn("ネットワークサーバーは既に起動しています");
            return;
        }
        
        try {
            int port = server.getConfig().getServerPort();
            String bindAddress = server.getConfig().getServerIp();
            
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            
                            // タイムアウトハンドラー
                            pipeline.addLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS));
                            pipeline.addLast(new WriteTimeoutHandler(30, TimeUnit.SECONDS));
                            
                            // Minecraftプロトコル用のフレームデコーダー
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(2097152, 0, 3, 0, 3));
                            pipeline.addLast(new LengthFieldPrepender(3));
                            
                            // Minecraftパケットハンドラー
                            pipeline.addLast(new MinecraftPacketHandler(server));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true);
            
            ChannelFuture future;
            if (bindAddress != null && !bindAddress.isEmpty()) {
                future = bootstrap.bind(bindAddress, port);
            } else {
                future = bootstrap.bind(port);
            }
            
            serverChannel = future.sync().channel();
            running = true;
            
            logger.info("ネットワークサーバーが起動しました - ポート: " + port);
            if (bindAddress != null && !bindAddress.isEmpty()) {
                logger.info("バインドアドレス: " + bindAddress);
            }
            
        } catch (Exception e) {
            logger.error("ネットワークサーバーの起動に失敗しました", e);
            throw e;
        }
    }
    
    /**
     * ネットワークサーバーを停止します
     */
    public void stop() {
        if (!running) {
            return;
        }
        
        logger.info("ネットワークサーバーを停止中...");
        running = false;
        
        try {
            if (serverChannel != null) {
                serverChannel.close().sync();
            }
            
            // EventLoopGroupをシャットダウン
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            
            // シャットダウンの完了を待機
            bossGroup.awaitTermination(5, TimeUnit.SECONDS);
            workerGroup.awaitTermination(5, TimeUnit.SECONDS);
            
            logger.info("ネットワークサーバーが停止しました");
            
        } catch (Exception e) {
            logger.error("ネットワークサーバーの停止中にエラーが発生しました", e);
        }
    }
    
    /**
     * サーバーが実行中かどうかを確認します
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * 接続数を取得します
     */
    public int getConnectionCount() {
        // 実際の実装では、接続中のチャンネル数をカウント
        return 0; // 仮の実装
    }
    
    /**
     * サーバーインスタンスを取得します
     */
    public MinecraftServer getServer() {
        return server;
    }
    
    /**
     * ロガーを取得します
     */
    public Logger getLogger() {
        return logger;
    }
} 