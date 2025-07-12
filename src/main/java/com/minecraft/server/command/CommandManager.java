package com.minecraft.server.command;

import com.minecraft.server.MinecraftServer;
import com.minecraft.server.util.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * コマンド管理を担当するクラス
 */
public class CommandManager {
    
    private final MinecraftServer server;
    private final Logger logger;
    private final Map<String, Command> commands = new ConcurrentHashMap<>();
    
    public CommandManager(MinecraftServer server) {
        this.server = server;
        this.logger = new Logger("CommandManager");
    }
    
    /**
     * デフォルトコマンドを登録します
     */
    public void registerDefaultCommands() {
        logger.info("デフォルトコマンドを登録中...");
        
        // 基本的なコマンドを登録
        registerCommand(new HelpCommand());
        registerCommand(new StopCommand());
        registerCommand(new ListCommand());
        registerCommand(new SayCommand());
        registerCommand(new TimeCommand());
        registerCommand(new WeatherCommand());
        
        logger.info("デフォルトコマンドの登録が完了しました");
    }
    
    /**
     * コマンドを登録します
     */
    public void registerCommand(Command command) {
        commands.put(command.getName().toLowerCase(), command);
        logger.debug("コマンドを登録しました: " + command.getName());
    }
    
    /**
     * コマンドを実行します
     */
    public boolean executeCommand(String commandLine, CommandSender sender) {
        if (commandLine == null || commandLine.trim().isEmpty()) {
            return false;
        }
        
        String[] parts = commandLine.trim().split("\\s+");
        String commandName = parts[0].toLowerCase();
        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);
        
        Command command = commands.get(commandName);
        if (command == null) {
            sender.sendMessage("§c不明なコマンドです: " + commandName);
            return false;
        }
        
        try {
            if (command.hasPermission(sender)) {
                boolean success = command.execute(sender, args);
                if (!success) {
                    sender.sendMessage("§cコマンドの使用方法: " + command.getUsage());
                }
                return success;
            } else {
                sender.sendMessage("§cこのコマンドを実行する権限がありません");
                return false;
            }
        } catch (Exception e) {
            logger.error("コマンド実行中にエラーが発生しました: " + commandName, e);
            sender.sendMessage("§cコマンド実行中にエラーが発生しました");
            return false;
        }
    }
    
    /**
     * コマンドを取得します
     */
    public Command getCommand(String name) {
        return commands.get(name.toLowerCase());
    }
    
    /**
     * コマンドが存在するかどうかを確認します
     */
    public boolean hasCommand(String name) {
        return commands.containsKey(name.toLowerCase());
    }
    
    /**
     * コマンド数を取得します
     */
    public int getCommandCount() {
        return commands.size();
    }
    
    /**
     * コマンド名のリストを取得します
     */
    public String[] getCommandNames() {
        return commands.keySet().toArray(new String[0]);
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