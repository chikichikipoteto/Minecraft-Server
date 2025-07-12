package com.minecraft.server;

import com.minecraft.server.config.ServerConfig;
import com.minecraft.server.network.NetworkManager;
import com.minecraft.server.network.HttpServer;
import com.minecraft.server.world.WorldManager;
import com.minecraft.server.player.PlayerManager;
import com.minecraft.server.plugin.PluginManager;
import com.minecraft.server.command.CommandManager;
import com.minecraft.server.util.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Minecraftサーバーのメインクラス
 */
public class MinecraftServer {
    
    private static final String VERSION = "1.0.0";
    private static final String PROTOCOL_VERSION = "1.20.1";
    
    private final ServerConfig config;
    private final NetworkManager networkManager;
    private final HttpServer httpServer;
    private final WorldManager worldManager;
    private final PlayerManager playerManager;
    private final PluginManager pluginManager;
    private final CommandManager commandManager;
    private final Logger logger;
    private final ScheduledExecutorService scheduler;
    
    private volatile boolean running = false;
    private long startTime;
    
    public MinecraftServer() {
        this.logger = new Logger("MinecraftServer");
        this.config = new ServerConfig();
        this.networkManager = new NetworkManager(this);
        this.httpServer = new HttpServer(this);
        this.worldManager = new WorldManager(this);
        this.playerManager = new PlayerManager(this);
        this.pluginManager = new PluginManager(this);
        this.commandManager = new CommandManager(this);
        this.scheduler = Executors.newScheduledThreadPool(4);
        
        logger.info("Minecraft Server " + VERSION + " を初期化中...");
    }
    
    /**
     * サーバーを起動します
     */
    public void start() {
        if (running) {
            logger.warn("サーバーは既に起動しています");
            return;
        }
        
        try {
            startTime = System.currentTimeMillis();
            running = true;
            
            // ディレクトリ構造を作成
            createDirectories();
            
            // 設定を読み込み
            config.load();
            
            // ワールドマネージャーを初期化
            worldManager.initialize();
            
            // プラグインマネージャーを初期化
            pluginManager.loadPlugins();
            
            // コマンドマネージャーを初期化
            commandManager.registerDefaultCommands();
            
            // ネットワークマネージャーを起動
            networkManager.start();
            
            // HTTPサーバーを起動（Render用）
            httpServer.start();
            
            // メインループを開始
            startMainLoop();
            
            logger.info("Minecraft Server " + VERSION + " が起動しました");
            logger.info("ポート: " + config.getServerPort());
            logger.info("最大プレイヤー数: " + config.getMaxPlayers());
            
        } catch (Exception e) {
            logger.error("サーバー起動中にエラーが発生しました", e);
            stop();
        }
    }
    
    /**
     * サーバーを停止します
     */
    public void stop() {
        if (!running) {
            return;
        }
        
        logger.info("サーバーを停止中...");
        running = false;
        
        try {
            // プレイヤーを切断
            playerManager.disconnectAllPlayers();
            
            // ネットワークマネージャーを停止
            networkManager.stop();
            
            // HTTPサーバーを停止
            httpServer.stop();
            
            // ワールドを保存
            worldManager.saveAllWorlds();
            
            // プラグインを無効化
            pluginManager.disableAllPlugins();
            
            // スケジューラーを停止
            scheduler.shutdown();
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            
            long uptime = System.currentTimeMillis() - startTime;
            logger.info("サーバーが停止しました (稼働時間: " + formatUptime(uptime) + ")");
            
        } catch (Exception e) {
            logger.error("サーバー停止中にエラーが発生しました", e);
        }
    }
    
    /**
     * メインループを開始します
     */
    private void startMainLoop() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                tick();
            } catch (Exception e) {
                logger.error("メインループでエラーが発生しました", e);
            }
        }, 0, 50, TimeUnit.MILLISECONDS); // 20 TPS
    }
    
    /**
     * サーバーの1ティックを処理します
     */
    private void tick() {
        if (!running) {
            return;
        }
        
        // ワールドのティック処理
        worldManager.tick();
        
        // プレイヤーのティック処理
        playerManager.tick();
        
        // プラグインのティック処理
        pluginManager.tick();
    }
    
    /**
     * 必要なディレクトリを作成します
     */
    private void createDirectories() throws IOException {
        String[] dirs = {
            "worlds",
            "plugins",
            "logs",
            "backups",
            "config"
        };
        
        for (String dir : dirs) {
            Path path = Paths.get(dir);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                logger.info("ディレクトリを作成しました: " + dir);
            }
        }
    }
    
    /**
     * 稼働時間をフォーマットします
     */
    private String formatUptime(long uptime) {
        long seconds = uptime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return String.format("%d日 %d時間 %d分", days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format("%d時間 %d分", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%d分 %d秒", minutes, seconds % 60);
        } else {
            return String.format("%d秒", seconds);
        }
    }
    
    // Getter methods
    public ServerConfig getConfig() { return config; }
    public NetworkManager getNetworkManager() { return networkManager; }
    public HttpServer getHttpServer() { return httpServer; }
    public WorldManager getWorldManager() { return worldManager; }
    public PlayerManager getPlayerManager() { return playerManager; }
    public PluginManager getPluginManager() { return pluginManager; }
    public CommandManager getCommandManager() { return commandManager; }
    public Logger getLogger() { return logger; }
    public boolean isRunning() { return running; }
    public String getVersion() { return VERSION; }
    public String getProtocolVersion() { return PROTOCOL_VERSION; }
    
    /**
     * メインメソッド
     */
    public static void main(String[] args) {
        MinecraftServer server = new MinecraftServer();
        
        // シャットダウンフックを追加
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.getLogger().info("シャットダウンシグナルを受信しました");
            server.stop();
        }));
        
        // サーバーを起動
        server.start();
        
        // コンソール入力の処理
        try {
            while (server.isRunning()) {
                int input = System.in.read();
                if (input == -1) {
                    break;
                }
                // コンソールコマンドの処理をここに追加
            }
        } catch (IOException e) {
            server.getLogger().error("コンソール入力の処理中にエラーが発生しました", e);
        }
    }
} 