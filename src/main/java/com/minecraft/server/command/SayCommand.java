package com.minecraft.server.command;

/**
 * 発言コマンド（スタブ）
 */
public class SayCommand implements Command {
    
    @Override
    public String getName() {
        return "say";
    }
    
    @Override
    public String getDescription() {
        return "メッセージをブロードキャストします";
    }
    
    @Override
    public String getUsage() {
        return "/say <メッセージ>";
    }
    
    @Override
    public String getPermission() {
        return "server.say";
    }
    
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            return false;
        }
        
        String message = String.join(" ", args);
        sender.sendMessage("§d[SERVER] " + message);
        return true;
    }
} 