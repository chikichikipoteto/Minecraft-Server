package com.minecraft.server.command;

/**
 * 時間コマンド（スタブ）
 */
public class TimeCommand implements Command {
    
    @Override
    public String getName() {
        return "time";
    }
    
    @Override
    public String getDescription() {
        return "ワールドの時間を設定します";
    }
    
    @Override
    public String getUsage() {
        return "/time <set|add> <値>";
    }
    
    @Override
    public String getPermission() {
        return "server.time";
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            return false;
        }
        
        String action = args[0];
        String value = args[1];
        
        sender.sendMessage("§a時間を " + action + " " + value + " に設定しました");
        return true;
    }
} 