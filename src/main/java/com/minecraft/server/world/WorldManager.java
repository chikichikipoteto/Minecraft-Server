package com.minecraft.server.world;

import com.minecraft.server.MinecraftServer;
import com.minecraft.server.util.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ワールド管理を担当するクラス
 */
public class WorldManager {
    
    private final MinecraftServer server;
    private final Logger logger;
    private final Map<String, World> worlds = new ConcurrentHashMap<>();
    private World defaultWorld;
    
    public WorldManager(MinecraftServer server) {
        this.server = server;
        this.logger = new Logger("WorldManager");
    }
    
    /**
     * ワールドマネージャーを初期化します
     */
    public void initialize() {
        logger.info("ワールドマネージャーを初期化中...");
        
        // デフォルトワールドを作成
        String defaultWorldName = server.getConfig().getLevelName();
        defaultWorld = createWorld(defaultWorldName);
        
        logger.info("ワールドマネージャーの初期化が完了しました");
        logger.info("デフォルトワールド: " + defaultWorldName);
    }
    
    /**
     * 新しいワールドを作成します
     */
    public World createWorld(String name) {
        if (worlds.containsKey(name)) {
            logger.warn("ワールド " + name + " は既に存在します");
            return worlds.get(name);
        }
        
        World world = new World(server, name);
        worlds.put(name, world);
        
        logger.info("ワールドを作成しました: " + name);
        return world;
    }
    
    /**
     * ワールドを取得します
     */
    public World getWorld(String name) {
        return worlds.get(name);
    }
    
    /**
     * デフォルトワールドを取得します
     */
    public World getDefaultWorld() {
        return defaultWorld;
    }
    
    /**
     * ワールドを削除します
     */
    public void removeWorld(String name) {
        World world = worlds.remove(name);
        if (world != null) {
            world.unload();
            logger.info("ワールドを削除しました: " + name);
        }
    }
    
    /**
     * すべてのワールドを保存します
     */
    public void saveAllWorlds() {
        logger.info("すべてのワールドを保存中...");
        
        for (World world : worlds.values()) {
            try {
                world.save();
            } catch (Exception e) {
                logger.error("ワールド " + world.getName() + " の保存に失敗しました", e);
            }
        }
        
        logger.info("すべてのワールドの保存が完了しました");
    }
    
    /**
     * すべてのワールドをアンロードします
     */
    public void unloadAllWorlds() {
        logger.info("すべてのワールドをアンロード中...");
        
        for (World world : worlds.values()) {
            try {
                world.unload();
            } catch (Exception e) {
                logger.error("ワールド " + world.getName() + " のアンロードに失敗しました", e);
            }
        }
        
        worlds.clear();
        logger.info("すべてのワールドのアンロードが完了しました");
    }
    
    /**
     * ワールドのティック処理を行います
     */
    public void tick() {
        for (World world : worlds.values()) {
            try {
                world.tick();
            } catch (Exception e) {
                logger.error("ワールド " + world.getName() + " のティック処理でエラーが発生しました", e);
            }
        }
    }
    
    /**
     * ワールド数を取得します
     */
    public int getWorldCount() {
        return worlds.size();
    }
    
    /**
     * ワールド名のリストを取得します
     */
    public String[] getWorldNames() {
        return worlds.keySet().toArray(new String[0]);
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