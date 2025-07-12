package com.minecraft.server.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * サーバー用のログユーティリティクラス
 */
public class Logger {
    
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private final String name;
    private final Path logFile;
    private final ConcurrentLinkedQueue<String> logQueue = new ConcurrentLinkedQueue<>();
    private final Thread logWriter;
    private volatile boolean running = true;
    
    public Logger(String name) {
        this.name = name;
        this.logFile = Paths.get("logs", "server-" + DATE_FORMAT.format(LocalDateTime.now()) + ".log");
        
        // ログディレクトリを作成
        try {
            Files.createDirectories(logFile.getParent());
        } catch (IOException e) {
            System.err.println("ログディレクトリの作成に失敗しました: " + e.getMessage());
        }
        
        // ログ書き込みスレッドを開始
        this.logWriter = new Thread(this::writeLogs, "LogWriter-" + name);
        this.logWriter.setDaemon(true);
        this.logWriter.start();
    }
    
    /**
     * 情報ログを出力します
     */
    public void info(String message) {
        log("INFO", message, null);
    }
    
    /**
     * 警告ログを出力します
     */
    public void warn(String message) {
        log("WARN", message, null);
    }
    
    /**
     * エラーログを出力します
     */
    public void error(String message) {
        log("ERROR", message, null);
    }
    
    /**
     * エラーログを出力します（例外付き）
     */
    public void error(String message, Throwable throwable) {
        log("ERROR", message, throwable);
    }
    
    /**
     * デバッグログを出力します
     */
    public void debug(String message) {
        log("DEBUG", message, null);
    }
    
    /**
     * ログを出力します
     */
    private void log(String level, String message, Throwable throwable) {
        String timestamp = TIMESTAMP_FORMAT.format(LocalDateTime.now());
        String logEntry = String.format("[%s] [%s] [%s] %s", timestamp, level, name, message);
        
        // コンソールに出力
        if (level.equals("ERROR")) {
            System.err.println(logEntry);
        } else {
            System.out.println(logEntry);
        }
        
        // 例外のスタックトレースを出力
        if (throwable != null) {
            System.err.println("Exception: " + throwable.getMessage());
            throwable.printStackTrace();
        }
        
        // ログキューに追加
        logQueue.offer(logEntry);
        
        // 例外のスタックトレースもキューに追加
        if (throwable != null) {
            StringBuilder stackTrace = new StringBuilder();
            stackTrace.append("Exception: ").append(throwable.getMessage()).append("\n");
            for (StackTraceElement element : throwable.getStackTrace()) {
                stackTrace.append("\tat ").append(element.toString()).append("\n");
            }
            logQueue.offer(stackTrace.toString());
        }
    }
    
    /**
     * ログファイルに書き込みます
     */
    private void writeLogs() {
        while (running) {
            try {
                String logEntry = logQueue.poll();
                if (logEntry != null) {
                    Files.write(logFile, (logEntry + "\n").getBytes(), 
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                } else {
                    Thread.sleep(100); // 100ms待機
                }
            } catch (IOException e) {
                System.err.println("ログファイルの書き込みに失敗しました: " + e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    /**
     * ロガーを停止します
     */
    public void shutdown() {
        running = false;
        if (logWriter != null) {
            logWriter.interrupt();
        }
        
        // 残りのログを書き込み
        String logEntry;
        while ((logEntry = logQueue.poll()) != null) {
            try {
                Files.write(logFile, (logEntry + "\n").getBytes(), 
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                System.err.println("ログファイルの書き込みに失敗しました: " + e.getMessage());
            }
        }
    }
    
    /**
     * ログファイルのパスを取得します
     */
    public Path getLogFile() {
        return logFile;
    }
    
    /**
     * ロガー名を取得します
     */
    public String getName() {
        return name;
    }
} 