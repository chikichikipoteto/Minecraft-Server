package com.minecraft.server.command;

/**
 * コマンド送信者を表すインターフェース
 */
public interface CommandSender {
    
    /**
     * 送信者の名前を取得します
     */
    String getName();
    
    /**
     * メッセージを送信します
     */
    void sendMessage(String message);
    
    /**
     * 送信者がプレイヤーかどうかを確認します
     */
    boolean isPlayer();
    
    /**
     * 送信者がコンソールかどうかを確認します
     */
    boolean isConsole();
    
    /**
     * 送信者が指定された権限を持っているかどうかを確認します
     */
    boolean hasPermission(String permission);
} 