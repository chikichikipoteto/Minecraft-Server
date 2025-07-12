package com.minecraft.server.plugin;

import com.minecraft.server.MinecraftServer;

/**
 * プラグインの基本インターフェース
 */
public interface Plugin {
    
    /**
     * プラグイン名を取得します
     */
    String getName();
    
    /**
     * プラグインのバージョンを取得します
     */
    String getVersion();
    
    /**
     * プラグインの説明を取得します
     */
    String getDescription();
    
    /**
     * プラグインの作者を取得します
     */
    String getAuthor();
    
    /**
     * プラグインが有効かどうかを確認します
     */
    boolean isEnabled();
    
    /**
     * プラグインが有効化された時に呼び出されます
     */
    void onEnable();
    
    /**
     * プラグインが無効化された時に呼び出されます
     */
    void onDisable();
    
    /**
     * プラグインのティック処理を行います
     */
    default void onTick() {
        // デフォルトでは何もしない
    }
    
    /**
     * サーバーインスタンスを取得します
     */
    MinecraftServer getServer();
} 