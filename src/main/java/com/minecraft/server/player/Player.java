package com.minecraft.server.player;

import com.minecraft.server.MinecraftServer;
import com.minecraft.server.util.Logger;

import java.util.UUID;

/**
 * 個別のプレイヤーを管理するクラス
 */
public class Player {
    
    private final MinecraftServer server;
    private final Logger logger;
    private final String username;
    private final UUID uuid;
    
    private volatile boolean connected = false;
    private volatile boolean online = false;
    private long lastActivity = System.currentTimeMillis();
    
    // プレイヤーの位置情報
    private double x, y, z;
    private float yaw, pitch;
    private boolean onGround = true;
    
    // プレイヤーの状態
    private int health = 20;
    private int maxHealth = 20;
    private int foodLevel = 20;
    private float saturation = 5.0f;
    private int experienceLevel = 0;
    private int experiencePoints = 0;
    
    // ゲームモード
    private GameMode gameMode = GameMode.SURVIVAL;
    
    public Player(MinecraftServer server, String username, UUID uuid) {
        this.server = server;
        this.username = username;
        this.uuid = uuid;
        this.logger = new Logger("Player-" + username);
    }
    
    /**
     * プレイヤーを接続します
     */
    public void connect() {
        if (connected) {
            logger.warn("プレイヤーは既に接続しています");
            return;
        }
        
        connected = true;
        online = true;
        lastActivity = System.currentTimeMillis();
        
        // デフォルトワールドにスポーン
        spawnInWorld();
        
        logger.info("プレイヤーが接続しました: " + username);
    }
    
    /**
     * プレイヤーを切断します
     */
    public void disconnect() {
        if (!connected) {
            return;
        }
        
        connected = false;
        online = false;
        
        // プレイヤーデータを保存
        savePlayerData();
        
        logger.info("プレイヤーが切断しました: " + username);
    }
    
    /**
     * プレイヤーのティック処理を行います
     */
    public void tick() {
        if (!online) {
            return;
        }
        
        lastActivity = System.currentTimeMillis();
        
        // プレイヤー固有のティック処理
        // - 体力の回復
        // - 空腹度の減少
        // - エフェクトの更新
        // など
    }
    
    /**
     * ワールドにスポーンします
     */
    private void spawnInWorld() {
        // TODO: スポーン位置の決定
        x = 0.0;
        y = 64.0;
        z = 0.0;
        yaw = 0.0f;
        pitch = 0.0f;
        
        logger.debug("プレイヤーがワールドにスポーンしました: " + username);
    }
    
    /**
     * プレイヤーデータを保存します
     */
    private void savePlayerData() {
        // TODO: プレイヤーデータの保存
        logger.debug("プレイヤーデータを保存しました: " + username);
    }
    
    /**
     * メッセージを送信します
     */
    public void sendMessage(String message) {
        if (!connected) {
            return;
        }
        
        // TODO: 実際のパケット送信
        logger.debug("メッセージを送信: " + message);
    }
    
    /**
     * 位置を更新します
     */
    public void setPosition(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    /**
     * 回転を更新します
     */
    public void setRotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }
    
    /**
     * 体力を設定します
     */
    public void setHealth(int health) {
        this.health = Math.max(0, Math.min(health, maxHealth));
    }
    
    /**
     * ゲームモードを設定します
     */
    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }
    
    // Getter methods
    public String getUsername() { return username; }
    public UUID getUuid() { return uuid; }
    public boolean isConnected() { return connected; }
    public boolean isOnline() { return online; }
    public long getLastActivity() { return lastActivity; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public boolean isOnGround() { return onGround; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getFoodLevel() { return foodLevel; }
    public float getSaturation() { return saturation; }
    public int getExperienceLevel() { return experienceLevel; }
    public int getExperiencePoints() { return experiencePoints; }
    public GameMode getGameMode() { return gameMode; }
    public MinecraftServer getServer() { return server; }
    public Logger getLogger() { return logger; }
    
    /**
     * ゲームモードの列挙型
     */
    public enum GameMode {
        SURVIVAL(0),
        CREATIVE(1),
        ADVENTURE(2),
        SPECTATOR(3);
        
        private final int id;
        
        GameMode(int id) {
            this.id = id;
        }
        
        public int getId() {
            return id;
        }
        
        public static GameMode fromId(int id) {
            for (GameMode mode : values()) {
                if (mode.id == id) {
                    return mode;
                }
            }
            return SURVIVAL;
        }
    }
} 