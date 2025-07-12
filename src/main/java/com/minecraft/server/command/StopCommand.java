package com.minecraft.server.command;

/**
 * 停止コマンド（スタブ）
 */
public class StopCommand implements Command {
    
    @Override
    public String getName() {
        return "stop";
    }
    
    @Override
    public String getDescription() {
        return "サーバーを停止します";
    }
    
    @Override
    public String getUsage() {
        return "/stop";
    }
    
    @Override
    public String getPermission() {
        return "server.stop";
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage("§cサーバーを停止中...");
        // TODO: サーバーの停止処理
        return true;
    }
} 