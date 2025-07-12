package com.minecraft.server.player;

import com.minecraft.server.MinecraftServer;
import com.minecraft.server.util.Logger;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * プレイヤー管理を担当するクラス
 */
public class PlayerManager {
    
    private final MinecraftServer server;
    private final Logger logger;
    private final Map<UUID, Player> players = new ConcurrentHashMap<>();
    private final Map<String, UUID> playerNames = new ConcurrentHashMap<>();
    
    public PlayerManager(MinecraftServer server) {
        this.server = server;
        this.logger = new Logger("PlayerManager");
    }
    
    /**
     * プレイヤーを追加します
     */
    public Player addPlayer(String username, UUID uuid) {
        if (playerNames.containsKey(username)) {
            logger.warn("プレイヤー " + username + " は既に接続しています");
            return players.get(playerNames.get(username));
        }
        
        Player player = new Player(server, username, uuid);
        players.put(uuid, player);
        playerNames.put(username, uuid);
        
        logger.info("プレイヤーが追加されました: " + username + " (" + uuid + ")");
        return player;
    }
    
    /**
     * プレイヤーを削除します
     */
    public void removePlayer(UUID uuid) {
        Player player = players.remove(uuid);
        if (player != null) {
            playerNames.remove(player.getUsername());
            player.disconnect();
            logger.info("プレイヤーが削除されました: " + player.getUsername());
        }
    }
    
    /**
     * プレイヤーを削除します（ユーザー名指定）
     */
    public void removePlayer(String username) {
        UUID uuid = playerNames.get(username);
        if (uuid != null) {
            removePlayer(uuid);
        }
    }
    
    /**
     * プレイヤーを取得します
     */
    public Player getPlayer(UUID uuid) {
        return players.get(uuid);
    }
    
    /**
     * プレイヤーを取得します（ユーザー名指定）
     */
    public Player getPlayer(String username) {
        UUID uuid = playerNames.get(username);
        if (uuid != null) {
            return players.get(uuid);
        }
        return null;
    }
    
    /**
     * すべてのプレイヤーを切断します
     */
    public void disconnectAllPlayers() {
        logger.info("すべてのプレイヤーを切断中...");
        
        for (Player player : players.values()) {
            try {
                player.disconnect();
            } catch (Exception e) {
                logger.error("プレイヤー " + player.getUsername() + " の切断に失敗しました", e);
            }
        }
        
        players.clear();
        playerNames.clear();
        logger.info("すべてのプレイヤーの切断が完了しました");
    }
    
    /**
     * プレイヤーのティック処理を行います
     */
    public void tick() {
        for (Player player : players.values()) {
            try {
                player.tick();
            } catch (Exception e) {
                logger.error("プレイヤー " + player.getUsername() + " のティック処理でエラーが発生しました", e);
            }
        }
    }
    
    /**
     * オンラインプレイヤー数を取得します
     */
    public int getOnlinePlayerCount() {
        return players.size();
    }
    
    /**
     * 最大プレイヤー数に達しているかどうかを確認します
     */
    public boolean isFull() {
        return getOnlinePlayerCount() >= server.getConfig().getMaxPlayers();
    }
    
    /**
     * プレイヤーがオンラインかどうかを確認します
     */
    public boolean isPlayerOnline(String username) {
        return playerNames.containsKey(username);
    }
    
    /**
     * プレイヤーがオンラインかどうかを確認します（UUID指定）
     */
    public boolean isPlayerOnline(UUID uuid) {
        return players.containsKey(uuid);
    }
    
    /**
     * オンラインプレイヤーのリストを取得します
     */
    public Player[] getOnlinePlayers() {
        return players.values().toArray(new Player[0]);
    }
    
    /**
     * オンラインプレイヤー名のリストを取得します
     */
    public String[] getOnlinePlayerNames() {
        return playerNames.keySet().toArray(new String[0]);
    }
    
    /**
     * ブロードキャストメッセージを送信します
     */
    public void broadcastMessage(String message) {
        for (Player player : players.values()) {
            try {
                player.sendMessage(message);
            } catch (Exception e) {
                logger.error("プレイヤー " + player.getUsername() + " にメッセージを送信できませんでした", e);
            }
        }
    }
    
    /**
     * ブロードキャストメッセージを送信します（送信者を除く）
     */
    public void broadcastMessage(String message, Player sender) {
        for (Player player : players.values()) {
            if (player != sender) {
                try {
                    player.sendMessage(message);
                } catch (Exception e) {
                    logger.error("プレイヤー " + player.getUsername() + " にメッセージを送信できませんでした", e);
                }
            }
        }
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