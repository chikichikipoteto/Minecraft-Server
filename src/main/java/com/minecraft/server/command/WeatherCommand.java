package com.minecraft.server.command;

/**
 * 天候コマンド（スタブ）
 */
public class WeatherCommand implements Command {
    
    @Override
    public String getName() {
        return "weather";
    }
    
    @Override
    public String getDescription() {
        return "ワールドの天候を設定します";
    }
    
    @Override
    public String getUsage() {
        return "/weather <clear|rain|thunder>";
    }
    
    @Override
    public String getPermission() {
        return "server.weather";
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            return false;
        }
        
        String weather = args[0];
        sender.sendMessage("§a天候を " + weather + " に設定しました");
        return true;
    }
} 