package com.minecraft.server.world;

import com.minecraft.server.MinecraftServer;
import com.minecraft.server.util.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 個別のワールドを管理するクラス
 */
public class World {
    
    private final MinecraftServer server;
    private final Logger logger;
    private final String name;
    private final Path worldPath;
    private final ChunkManager chunkManager;
    private final WorldGenerator worldGenerator;
    
    private volatile boolean loaded = false;
    private final AtomicLong tickCount = new AtomicLong(0);
    private long lastSaveTime = 0;
    private static final long SAVE_INTERVAL = 6000; // 5分（20 TPS * 60 * 5）
    
    public World(MinecraftServer server, String name) {
        this.server = server;
        this.name = name;
        this.logger = new Logger("World-" + name);
        this.worldPath = Paths.get("worlds", name);
        this.chunkManager = new ChunkManager(this);
        this.worldGenerator = new WorldGenerator(this);
    }
    
    /**
     * ワールドを読み込みます
     */
    public void load() {
        if (loaded) {
            logger.warn("ワールドは既に読み込まれています");
            return;
        }
        
        try {
            logger.info("ワールドを読み込み中: " + name);
            
            // ワールドディレクトリを作成
            createWorldDirectory();
            
            // チャンクマネージャーを初期化
            chunkManager.initialize();
            
            // ワールドジェネレーターを初期化
            worldGenerator.initialize();
            
            loaded = true;
            logger.info("ワールドの読み込みが完了しました: " + name);
            
        } catch (Exception e) {
            logger.error("ワールドの読み込みに失敗しました: " + name, e);
            throw new RuntimeException("Failed to load world: " + name, e);
        }
    }
    
    /**
     * ワールドを保存します
     */
    public void save() {
        if (!loaded) {
            logger.warn("ワールドが読み込まれていないため保存できません: " + name);
            return;
        }
        
        try {
            logger.debug("ワールドを保存中: " + name);
            
            // チャンクを保存
            chunkManager.saveAllChunks();
            
            // ワールドメタデータを保存
            saveWorldMetadata();
            
            lastSaveTime = tickCount.get();
            logger.debug("ワールドの保存が完了しました: " + name);
            
        } catch (Exception e) {
            logger.error("ワールドの保存に失敗しました: " + name, e);
        }
    }
    
    /**
     * ワールドをアンロードします
     */
    public void unload() {
        if (!loaded) {
            return;
        }
        
        try {
            logger.info("ワールドをアンロード中: " + name);
            
            // ワールドを保存
            save();
            
            // チャンクマネージャーをシャットダウン
            chunkManager.shutdown();
            
            loaded = false;
            logger.info("ワールドのアンロードが完了しました: " + name);
            
        } catch (Exception e) {
            logger.error("ワールドのアンロードに失敗しました: " + name, e);
        }
    }
    
    /**
     * ワールドのティック処理を行います
     */
    public void tick() {
        if (!loaded) {
            return;
        }
        
        long currentTick = tickCount.incrementAndGet();
        
        // チャンクのティック処理
        chunkManager.tick();
        
        // 定期的にワールドを保存
        if (currentTick - lastSaveTime >= SAVE_INTERVAL) {
            save();
        }
        
        // ワールド固有のティック処理
        // - 天候の更新
        // - 時間の進行
        // - ワールドイベントの処理
        // など
    }
    
    /**
     * ワールドディレクトリを作成します
     */
    private void createWorldDirectory() throws Exception {
        if (!worldPath.toFile().exists()) {
            worldPath.toFile().mkdirs();
            logger.info("ワールドディレクトリを作成しました: " + worldPath);
        }
    }
    
    /**
     * ワールドメタデータを保存します
     */
    private void saveWorldMetadata() {
        // TODO: ワールドのメタデータ（時間、天候、ゲームルールなど）を保存
        logger.debug("ワールドメタデータを保存しました: " + name);
    }
    
    /**
     * ブロックを取得します
     */
    public int getBlock(int x, int y, int z) {
        if (!loaded) {
            return 0; // 空気ブロック
        }
        
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        int localX = x & 15;
        int localZ = z & 15;
        
        Chunk chunk = chunkManager.getChunk(chunkX, chunkZ);
        if (chunk != null) {
            return chunk.getBlock(localX, y, localZ);
        }
        
        return 0; // 空気ブロック
    }
    
    /**
     * ブロックを設定します
     */
    public void setBlock(int x, int y, int z, int blockId) {
        if (!loaded) {
            return;
        }
        
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        int localX = x & 15;
        int localZ = z & 15;
        
        Chunk chunk = chunkManager.getChunk(chunkX, chunkZ);
        if (chunk != null) {
            chunk.setBlock(localX, y, localZ, blockId);
        }
    }
    
    /**
     * チャンクを取得します
     */
    public Chunk getChunk(int chunkX, int chunkZ) {
        return chunkManager.getChunk(chunkX, chunkZ);
    }
    
    /**
     * チャンクを生成します
     */
    public Chunk generateChunk(int chunkX, int chunkZ) {
        return chunkManager.generateChunk(chunkX, chunkZ);
    }
    
    /**
     * ワールド名を取得します
     */
    public String getName() {
        return name;
    }
    
    /**
     * ワールドパスを取得します
     */
    public Path getWorldPath() {
        return worldPath;
    }
    
    /**
     * ワールドが読み込まれているかどうかを確認します
     */
    public boolean isLoaded() {
        return loaded;
    }
    
    /**
     * ティック数を取得します
     */
    public long getTickCount() {
        return tickCount.get();
    }
    
    /**
     * チャンクマネージャーを取得します
     */
    public ChunkManager getChunkManager() {
        return chunkManager;
    }
    
    /**
     * ワールドジェネレーターを取得します
     */
    public WorldGenerator getWorldGenerator() {
        return worldGenerator;
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