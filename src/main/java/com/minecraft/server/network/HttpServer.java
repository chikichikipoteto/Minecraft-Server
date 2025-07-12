package com.minecraft.server.network;

import com.minecraft.server.MinecraftServer;
import com.minecraft.server.util.Logger;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.nio.charset.StandardCharsets;

/**
 * Render用のHTTPヘルスチェックサーバー
 */
public class HttpServer {
    
    private final MinecraftServer server;
    private final Logger logger;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private Channel serverChannel;
    private volatile boolean running = false;
    
    public HttpServer(MinecraftServer server) {
        this.server = server;
        this.logger = new Logger("HttpServer");
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup();
    }
    
    /**
     * HTTPサーバーを起動します
     */
    public void start() throws InterruptedException {
        if (running) {
            logger.warn("HTTPサーバーは既に起動しています");
            return;
        }
        
        try {
            int port = 8080; // Render用のヘルスチェックポート
            
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            
                            // HTTPコーデック
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(65536));
                            
                            // HTTPハンドラー
                            pipeline.addLast(new HttpServerHandler(server));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            
            ChannelFuture future = bootstrap.bind(port);
            serverChannel = future.sync().channel();
            running = true;
            
            logger.info("HTTPサーバーが起動しました - ポート: " + port);
            
        } catch (Exception e) {
            logger.error("HTTPサーバーの起動に失敗しました", e);
            throw e;
        }
    }
    
    /**
     * HTTPサーバーを停止します
     */
    public void stop() {
        if (!running) {
            return;
        }
        
        logger.info("HTTPサーバーを停止中...");
        running = false;
        
        try {
            if (serverChannel != null) {
                serverChannel.close().sync();
            }
            
            // EventLoopGroupをシャットダウン
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            
            // シャットダウンの完了を待機
            bossGroup.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
            workerGroup.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
            
            logger.info("HTTPサーバーが停止しました");
            
        } catch (Exception e) {
            logger.error("HTTPサーバーの停止中にエラーが発生しました", e);
        }
    }
    
    /**
     * サーバーが実行中かどうかを確認します
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * HTTPリクエストハンドラー
     */
    private static class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
        
        private final MinecraftServer server;
        
        public HttpServerHandler(MinecraftServer server) {
            this.server = server;
        }
        
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
            // レスポンスを作成
            FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, 
                HttpResponseStatus.OK,
                createResponseContent()
            );
            
            // ヘッダーを設定
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            
            // レスポンスを送信
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
        
        private io.netty.buffer.ByteBuf createResponseContent() {
            String json = String.format(
                "{\"status\":\"online\",\"version\":\"%s\",\"players\":%d,\"max_players\":%d,\"motd\":\"%s\"}",
                server.getVersion(),
                server.getPlayerManager().getOnlinePlayerCount(),
                server.getConfig().getMaxPlayers(),
                server.getConfig().getMotd()
            );
            
            return io.netty.buffer.Unpooled.copiedBuffer(json, StandardCharsets.UTF_8);
        }
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }
} 