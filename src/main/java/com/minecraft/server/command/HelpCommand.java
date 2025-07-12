package com.minecraft.server.command;

/**
 * ヘルプコマンド（スタブ）
 */
public class HelpCommand implements Command {
    
    @Override
    public String getName() {
        return "help";
    }
    
    @Override
    public String getDescription() {
        return "利用可能なコマンドを表示します";
    }
    
    @Override
    public String getUsage() {
        return "/help [ページ]";
    }
    
    @Override
    public String getPermission() {
        return "";
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage("§a=== 利用可能なコマンド ===");
        sender.sendMessage("§f/help §7- このヘルプを表示");
        sender.sendMessage("§f/stop §7- サーバーを停止");
        sender.sendMessage("§f/list §7- オンラインプレイヤーを表示");
        sender.sendMessage("§f/say §7- メッセージをブロードキャスト");
        return true;
    }
} 