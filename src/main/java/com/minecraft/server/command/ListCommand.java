package com.minecraft.server.command;

/**
 * リストコマンド（スタブ）
 */
public class ListCommand implements Command {
    
    @Override
    public String getName() {
        return "list";
    }
    
    @Override
    public String getDescription() {
        return "オンラインプレイヤーを表示します";
    }
    
    @Override
    public String getUsage() {
        return "/list";
    }
    
    @Override
    public String getPermission() {
        return "";
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage("§aオンラインプレイヤー: 0/20");
        sender.sendMessage("§f(なし)");
        return true;
    }
} 