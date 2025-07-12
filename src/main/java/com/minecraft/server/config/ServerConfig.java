package com.minecraft.server.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.minecraft.server.util.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * サーバー設定を管理するクラス
 */
public class ServerConfig {
    
    private static final String CONFIG_FILE = "server.properties";
    private static final String JSON_CONFIG_FILE = "config/server.json";
    
    private final Logger logger = new Logger("ServerConfig");
    private final Properties properties = new Properties();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    // デフォルト設定値
    private int serverPort = 25565;
    private String serverIp = "";
    private int maxPlayers = 20;
    private boolean onlineMode = true;
    private boolean whitelist = false;
    private String motd = "A Minecraft Server";
    private String serverName = "Minecraft Emu Server";
    
    private String gamemode = "survival";
    private String difficulty = "normal";
    private boolean pvp = true;
    private int spawnProtection = 16;
    private boolean spawnNpcs = true;
    private boolean spawnAnimals = true;
    private boolean spawnMonsters = true;
    private boolean generateStructures = true;
    
    private String levelName = "world";
    private String levelSeed = "";
    private String levelType = "default";
    private String generatorSettings = "";
    private String worldType = "default";
    
    private boolean allowFlight = false;
    private boolean allowNether = true;
    private boolean enableCommandBlock = false;
    private boolean forceGamemode = false;
    private boolean hardcore = false;
    private boolean broadcastConsoleToOps = true;
    private boolean broadcastRconToOps = true;
    
    private int viewDistance = 10;
    private int simulationDistance = 8;
    private int maxTickTime = 60000;
    private int networkCompressionThreshold = 256;
    private int maxWorldSize = 29999984;
    
    private boolean enableStatus = true;
    private boolean enableQuery = false;
    private int queryPort = 25565;
    private boolean enableRcon = false;
    private int rconPort = 25575;
    private String rconPassword = "";
    private int maxBuildHeight = 256;
    private String serverResourcePack = "";
    private String serverResourcePackSha1 = "";
    private boolean requireResourcePack = false;
    private String resourcePackPrompt = "";
    private int entityBroadcastRangePercentage = 2;
    
    private boolean logIps = true;
    
    /**
     * 設定を読み込みます
     */
    public void load() throws IOException {
        Path configPath = Paths.get(CONFIG_FILE);
        
        if (Files.exists(configPath)) {
            try (InputStream input = Files.newInputStream(configPath)) {
                properties.load(input);
                loadFromProperties();
                logger.info("設定ファイルを読み込みました: " + CONFIG_FILE);
            }
        } else {
            // デフォルト設定でファイルを作成
            save();
            logger.info("デフォルト設定ファイルを作成しました: " + CONFIG_FILE);
        }
        
        // JSON設定ファイルも読み込み
        loadJsonConfig();
    }
    
    /**
     * 設定を保存します
     */
    public void save() throws IOException {
        // Propertiesファイルに保存
        saveToProperties();
        try (OutputStream output = Files.newOutputStream(Paths.get(CONFIG_FILE))) {
            properties.store(output, "Minecraft Server Properties");
        }
        
        // JSONファイルにも保存
        saveJsonConfig();
        
        logger.info("設定を保存しました");
    }
    
    /**
     * Propertiesから設定値を読み込みます
     */
    private void loadFromProperties() {
        // 環境変数から設定を読み込み（Render用）
        serverPort = getIntFromEnv("SERVER_PORT", getIntProperty("server-port", serverPort));
        serverIp = getStringFromEnv("SERVER_IP", getStringProperty("server-ip", serverIp));
        maxPlayers = getIntFromEnv("MAX_PLAYERS", getIntProperty("max-players", maxPlayers));
        onlineMode = getBooleanFromEnv("ONLINE_MODE", getBooleanProperty("online-mode", onlineMode));
        whitelist = getBooleanProperty("whitelist", whitelist);
        motd = getStringFromEnv("MOTD", getStringProperty("motd", motd));
        serverName = getStringProperty("server-name", serverName);
        
        gamemode = getStringProperty("gamemode", gamemode);
        difficulty = getStringProperty("difficulty", difficulty);
        pvp = getBooleanProperty("pvp", pvp);
        spawnProtection = getIntProperty("spawn-protection", spawnProtection);
        spawnNpcs = getBooleanProperty("spawn-npcs", spawnNpcs);
        spawnAnimals = getBooleanProperty("spawn-animals", spawnAnimals);
        spawnMonsters = getBooleanProperty("spawn-monsters", spawnMonsters);
        generateStructures = getBooleanProperty("generate-structures", generateStructures);
        
        levelName = getStringProperty("level-name", levelName);
        levelSeed = getStringProperty("level-seed", levelSeed);
        levelType = getStringProperty("level-type", levelType);
        generatorSettings = getStringProperty("generator-settings", generatorSettings);
        worldType = getStringProperty("world-type", worldType);
        
        allowFlight = getBooleanProperty("allow-flight", allowFlight);
        allowNether = getBooleanProperty("allow-nether", allowNether);
        enableCommandBlock = getBooleanProperty("enable-command-block", enableCommandBlock);
        forceGamemode = getBooleanProperty("force-gamemode", forceGamemode);
        hardcore = getBooleanProperty("hardcore", hardcore);
        broadcastConsoleToOps = getBooleanProperty("broadcast-console-to-ops", broadcastConsoleToOps);
        broadcastRconToOps = getBooleanProperty("broadcast-rcon-to-ops", broadcastRconToOps);
        
        viewDistance = getIntProperty("view-distance", viewDistance);
        simulationDistance = getIntProperty("simulation-distance", simulationDistance);
        maxTickTime = getIntProperty("max-tick-time", maxTickTime);
        networkCompressionThreshold = getIntProperty("network-compression-threshold", networkCompressionThreshold);
        maxWorldSize = getIntProperty("max-world-size", maxWorldSize);
        
        enableStatus = getBooleanProperty("enable-status", enableStatus);
        enableQuery = getBooleanProperty("enable-query", enableQuery);
        queryPort = getIntProperty("query.port", queryPort);
        enableRcon = getBooleanProperty("enable-rcon", enableRcon);
        rconPort = getIntProperty("rcon.port", rconPort);
        rconPassword = getStringProperty("rcon.password", rconPassword);
        maxBuildHeight = getIntProperty("max-build-height", maxBuildHeight);
        serverResourcePack = getStringProperty("server-resource-pack", serverResourcePack);
        serverResourcePackSha1 = getStringProperty("server-resource-pack-sha1", serverResourcePackSha1);
        requireResourcePack = getBooleanProperty("require-resource-pack", requireResourcePack);
        resourcePackPrompt = getStringProperty("resource-pack-prompt", resourcePackPrompt);
        entityBroadcastRangePercentage = getIntProperty("entity-broadcast-range-percentage", entityBroadcastRangePercentage);
        
        logIps = getBooleanProperty("log-ips", logIps);
    }
    
    /**
     * Propertiesに設定値を保存します
     */
    private void saveToProperties() {
        properties.setProperty("server-port", String.valueOf(serverPort));
        properties.setProperty("server-ip", serverIp);
        properties.setProperty("max-players", String.valueOf(maxPlayers));
        properties.setProperty("online-mode", String.valueOf(onlineMode));
        properties.setProperty("whitelist", String.valueOf(whitelist));
        properties.setProperty("motd", motd);
        properties.setProperty("server-name", serverName);
        
        properties.setProperty("gamemode", gamemode);
        properties.setProperty("difficulty", difficulty);
        properties.setProperty("pvp", String.valueOf(pvp));
        properties.setProperty("spawn-protection", String.valueOf(spawnProtection));
        properties.setProperty("spawn-npcs", String.valueOf(spawnNpcs));
        properties.setProperty("spawn-animals", String.valueOf(spawnAnimals));
        properties.setProperty("spawn-monsters", String.valueOf(spawnMonsters));
        properties.setProperty("generate-structures", String.valueOf(generateStructures));
        
        properties.setProperty("level-name", levelName);
        properties.setProperty("level-seed", levelSeed);
        properties.setProperty("level-type", levelType);
        properties.setProperty("generator-settings", generatorSettings);
        properties.setProperty("world-type", worldType);
        
        properties.setProperty("allow-flight", String.valueOf(allowFlight));
        properties.setProperty("allow-nether", String.valueOf(allowNether));
        properties.setProperty("enable-command-block", String.valueOf(enableCommandBlock));
        properties.setProperty("force-gamemode", String.valueOf(forceGamemode));
        properties.setProperty("hardcore", String.valueOf(hardcore));
        properties.setProperty("broadcast-console-to-ops", String.valueOf(broadcastConsoleToOps));
        properties.setProperty("broadcast-rcon-to-ops", String.valueOf(broadcastRconToOps));
        
        properties.setProperty("view-distance", String.valueOf(viewDistance));
        properties.setProperty("simulation-distance", String.valueOf(simulationDistance));
        properties.setProperty("max-tick-time", String.valueOf(maxTickTime));
        properties.setProperty("network-compression-threshold", String.valueOf(networkCompressionThreshold));
        properties.setProperty("max-world-size", String.valueOf(maxWorldSize));
        
        properties.setProperty("enable-status", String.valueOf(enableStatus));
        properties.setProperty("enable-query", String.valueOf(enableQuery));
        properties.setProperty("query.port", String.valueOf(queryPort));
        properties.setProperty("enable-rcon", String.valueOf(enableRcon));
        properties.setProperty("rcon.port", String.valueOf(rconPort));
        properties.setProperty("rcon.password", rconPassword);
        properties.setProperty("max-build-height", String.valueOf(maxBuildHeight));
        properties.setProperty("server-resource-pack", serverResourcePack);
        properties.setProperty("server-resource-pack-sha1", serverResourcePackSha1);
        properties.setProperty("require-resource-pack", String.valueOf(requireResourcePack));
        properties.setProperty("resource-pack-prompt", resourcePackPrompt);
        properties.setProperty("entity-broadcast-range-percentage", String.valueOf(entityBroadcastRangePercentage));
        
        properties.setProperty("log-ips", String.valueOf(logIps));
    }
    
    /**
     * JSON設定ファイルを読み込みます
     */
    private void loadJsonConfig() {
        Path jsonPath = Paths.get(JSON_CONFIG_FILE);
        if (Files.exists(jsonPath)) {
            try {
                String json = Files.readString(jsonPath);
                JsonConfig jsonConfig = gson.fromJson(json, JsonConfig.class);
                if (jsonConfig != null) {
                    // JSON設定で上書き
                    applyJsonConfig(jsonConfig);
                }
                logger.info("JSON設定ファイルを読み込みました: " + JSON_CONFIG_FILE);
            } catch (Exception e) {
                logger.warn("JSON設定ファイルの読み込みに失敗しました", e);
            }
        }
    }
    
    /**
     * JSON設定ファイルを保存します
     */
    private void saveJsonConfig() throws IOException {
        JsonConfig jsonConfig = new JsonConfig();
        jsonConfig.serverPort = serverPort;
        jsonConfig.maxPlayers = maxPlayers;
        jsonConfig.motd = motd;
        jsonConfig.gamemode = gamemode;
        jsonConfig.difficulty = difficulty;
        jsonConfig.levelName = levelName;
        jsonConfig.viewDistance = viewDistance;
        
        Path jsonPath = Paths.get(JSON_CONFIG_FILE);
        Files.createDirectories(jsonPath.getParent());
        Files.writeString(jsonPath, gson.toJson(jsonConfig));
    }
    
    /**
     * JSON設定を適用します
     */
    private void applyJsonConfig(JsonConfig jsonConfig) {
        if (jsonConfig.serverPort > 0) serverPort = jsonConfig.serverPort;
        if (jsonConfig.maxPlayers > 0) maxPlayers = jsonConfig.maxPlayers;
        if (jsonConfig.motd != null) motd = jsonConfig.motd;
        if (jsonConfig.gamemode != null) gamemode = jsonConfig.gamemode;
        if (jsonConfig.difficulty != null) difficulty = jsonConfig.difficulty;
        if (jsonConfig.levelName != null) levelName = jsonConfig.levelName;
        if (jsonConfig.viewDistance > 0) viewDistance = jsonConfig.viewDistance;
    }
    
    // ヘルパーメソッド
    private String getStringProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    private int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.warn("無効な数値設定: " + key + " = " + value);
            }
        }
        return defaultValue;
    }
    
    private boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }
    
    // 環境変数から値を読み込むヘルパーメソッド
    private String getStringFromEnv(String envKey, String defaultValue) {
        String value = System.getenv(envKey);
        return value != null ? value : defaultValue;
    }
    
    private int getIntFromEnv(String envKey, int defaultValue) {
        String value = System.getenv(envKey);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.warn("無効な環境変数値: " + envKey + " = " + value);
            }
        }
        return defaultValue;
    }
    
    private boolean getBooleanFromEnv(String envKey, boolean defaultValue) {
        String value = System.getenv(envKey);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }
    
    // Getter methods
    public int getServerPort() { return serverPort; }
    public String getServerIp() { return serverIp; }
    public int getMaxPlayers() { return maxPlayers; }
    public boolean isOnlineMode() { return onlineMode; }
    public boolean isWhitelist() { return whitelist; }
    public String getMotd() { return motd; }
    public String getServerName() { return serverName; }
    public String getGamemode() { return gamemode; }
    public String getDifficulty() { return difficulty; }
    public boolean isPvp() { return pvp; }
    public int getSpawnProtection() { return spawnProtection; }
    public boolean isSpawnNpcs() { return spawnNpcs; }
    public boolean isSpawnAnimals() { return spawnAnimals; }
    public boolean isSpawnMonsters() { return spawnMonsters; }
    public boolean isGenerateStructures() { return generateStructures; }
    public String getLevelName() { return levelName; }
    public String getLevelSeed() { return levelSeed; }
    public String getLevelType() { return levelType; }
    public String getGeneratorSettings() { return generatorSettings; }
    public String getWorldType() { return worldType; }
    public boolean isAllowFlight() { return allowFlight; }
    public boolean isAllowNether() { return allowNether; }
    public boolean isEnableCommandBlock() { return enableCommandBlock; }
    public boolean isForceGamemode() { return forceGamemode; }
    public boolean isHardcore() { return hardcore; }
    public boolean isBroadcastConsoleToOps() { return broadcastConsoleToOps; }
    public boolean isBroadcastRconToOps() { return broadcastRconToOps; }
    public int getViewDistance() { return viewDistance; }
    public int getSimulationDistance() { return simulationDistance; }
    public int getMaxTickTime() { return maxTickTime; }
    public int getNetworkCompressionThreshold() { return networkCompressionThreshold; }
    public int getMaxWorldSize() { return maxWorldSize; }
    public boolean isEnableStatus() { return enableStatus; }
    public boolean isEnableQuery() { return enableQuery; }
    public int getQueryPort() { return queryPort; }
    public boolean isEnableRcon() { return enableRcon; }
    public int getRconPort() { return rconPort; }
    public String getRconPassword() { return rconPassword; }
    public int getMaxBuildHeight() { return maxBuildHeight; }
    public String getServerResourcePack() { return serverResourcePack; }
    public String getServerResourcePackSha1() { return serverResourcePackSha1; }
    public boolean isRequireResourcePack() { return requireResourcePack; }
    public String getResourcePackPrompt() { return resourcePackPrompt; }
    public int getEntityBroadcastRangePercentage() { return entityBroadcastRangePercentage; }
    public boolean isLogIps() { return logIps; }
    
    /**
     * JSON設定用の内部クラス
     */
    private static class JsonConfig {
        public int serverPort;
        public int maxPlayers;
        public String motd;
        public String gamemode;
        public String difficulty;
        public String levelName;
        public int viewDistance;
    }
} 