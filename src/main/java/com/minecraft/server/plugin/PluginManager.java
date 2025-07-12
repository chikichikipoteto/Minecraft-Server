package com.minecraft.server.plugin;

import com.minecraft.server.MinecraftServer;
import com.minecraft.server.util.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * プラグイン管理を担当するクラス
 */
public class PluginManager {
    
    private final MinecraftServer server;
    private final Logger logger;
    private final Map<String, Plugin> plugins = new ConcurrentHashMap<>();
    
    public PluginManager(MinecraftServer server) {
        this.server = server;
        this.logger = new Logger("PluginManager");
    }
    
    /**
     * プラグインを読み込みます
     */
    public void loadPlugins() {
        logger.info("プラグインを読み込み中...");
        
        // TODO: プラグインディレクトリからプラグインをスキャンして読み込み
        
        logger.info("プラグインの読み込みが完了しました");
    }
    
    /**
     * プラグインを有効化します
     */
    public void enablePlugin(String name) {
        Plugin plugin = plugins.get(name);
        if (plugin != null && !plugin.isEnabled()) {
            try {
                plugin.onEnable();
                logger.info("プラグインを有効化しました: " + name);
            } catch (Exception e) {
                logger.error("プラグイン " + name + " の有効化に失敗しました", e);
            }
        }
    }
    
    /**
     * プラグインを無効化します
     */
    public void disablePlugin(String name) {
        Plugin plugin = plugins.get(name);
        if (plugin != null && plugin.isEnabled()) {
            try {
                plugin.onDisable();
                logger.info("プラグインを無効化しました: " + name);
            } catch (Exception e) {
                logger.error("プラグイン " + name + " の無効化に失敗しました", e);
            }
        }
    }
    
    /**
     * すべてのプラグインを無効化します
     */
    public void disableAllPlugins() {
        logger.info("すべてのプラグインを無効化中...");
        
        for (Plugin plugin : plugins.values()) {
            if (plugin.isEnabled()) {
                try {
                    plugin.onDisable();
                } catch (Exception e) {
                    logger.error("プラグイン " + plugin.getName() + " の無効化に失敗しました", e);
                }
            }
        }
        
        logger.info("すべてのプラグインの無効化が完了しました");
    }
    
    /**
     * プラグインのティック処理を行います
     */
    public void tick() {
        for (Plugin plugin : plugins.values()) {
            if (plugin.isEnabled()) {
                try {
                    plugin.onTick();
                } catch (Exception e) {
                    logger.error("プラグイン " + plugin.getName() + " のティック処理でエラーが発生しました", e);
                }
            }
        }
    }
    
    /**
     * プラグインを取得します
     */
    public Plugin getPlugin(String name) {
        return plugins.get(name);
    }
    
    /**
     * プラグインが有効かどうかを確認します
     */
    public boolean isPluginEnabled(String name) {
        Plugin plugin = plugins.get(name);
        return plugin != null && plugin.isEnabled();
    }
    
    /**
     * プラグイン数を取得します
     */
    public int getPluginCount() {
        return plugins.size();
    }
    
    /**
     * 有効なプラグイン数を取得します
     */
    public int getEnabledPluginCount() {
        return (int) plugins.values().stream().filter(Plugin::isEnabled).count();
    }
    
    /**
     * プラグイン名のリストを取得します
     */
    public String[] getPluginNames() {
        return plugins.keySet().toArray(new String[0]);
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