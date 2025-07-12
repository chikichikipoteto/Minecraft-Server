package com.minecraft.server.command;

/**
 * コマンドの基本インターフェース
 */
public interface Command {
    
    /**
     * コマンド名を取得します
     */
    String getName();
    
    /**
     * コマンドの説明を取得します
     */
    String getDescription();
    
    /**
     * コマンドの使用方法を取得します
     */
    String getUsage();
    
    /**
     * コマンドの権限を取得します
     */
    String getPermission();
    
    /**
     * コマンドを実行します
     */
    boolean execute(CommandSender sender, String[] args);
    
    /**
     * 送信者がコマンドを実行する権限を持っているかどうかを確認します
     */
    default boolean hasPermission(CommandSender sender) {
        String permission = getPermission();
        if (permission == null || permission.isEmpty()) {
            return true;
        }
        return sender.hasPermission(permission);
    }
} 