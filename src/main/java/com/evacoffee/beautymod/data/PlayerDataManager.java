package com.evacoffee.beautymod.data;

import com.evacoffee.beautymod.security.DataEncryption;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PlayerDataManager {
    private static final Logger LOGGER = LogManager.getLogger("Beautify/PlayerDataManager");
    private static final String DATA_FOLDER = "beautymod_data";
    private static final String BACKUP_FOLDER = "beautymod_backups";
    private static final int MAX_BACKUPS = 5;
    private static final Executor IO_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "Beautify-Data-IO");
        t.setDaemon(true);
        return t;
    });

    private static DataEncryption encryption;
    private static Path saveDir;
    private static Path backupDir;

    public static void initialize(SecretKey encryptionKey) {
        encryption = new DataEncryption(encryptionKey);
        try {
            saveDir = getOrCreateDirectory(DATA_FOLDER);
            backupDir = getOrCreateDirectory(BACKUP_FOLDER);
            LOGGER.info("PlayerDataManager initialized");
        } catch (IOException e) {
            LOGGER.error("Failed to initialize PlayerDataManager", e);
            throw new RuntimeException("Failed to initialize PlayerDataManager", e);
        }
    }

    public static CompletableFuture<Void> savePlayerDataAsync(UUID playerId, String data) {
        return CompletableFuture.runAsync(() -> {
            try {
                savePlayerData(playerId, data);
            } catch (Exception e) {
                LOGGER.error("Failed to save data for player: " + playerId, e);
                throw new RuntimeException("Failed to save player data", e);
            }
        }, IO_EXECUTOR);
    }

    public static CompletableFuture<String> loadPlayerDataAsync(UUID playerId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return loadPlayerData(playerId);
            } catch (Exception e) {
                LOGGER.error("Failed to load data for player: " + playerId, e);
                throw new RuntimeException("Failed to load player data", e);
            }
        }, IO_EXECUTOR);
    }

    private static synchronized void savePlayerData(UUID playerId, String data) throws Exception {
        Path dataFile = saveDir.resolve(playerId + ".dat");
        Path tempFile = saveDir.resolve(playerId + ".tmp");

        try {
            if (Files.exists(dataFile)) {
                createBackup(dataFile, playerId);
            }

            String encrypted = encryption.encrypt(data);
            Files.writeString(tempFile, encrypted, 
                StandardOpenOption.CREATE, 
                StandardOpenOption.TRUNCATE_EXISTING, 
                StandardOpenOption.WRITE, 
                StandardOpenOption.SYNC
            );

            Files.move(tempFile, dataFile, 
                StandardCopyOption.REPLACE_EXISTING, 
                StandardCopyOption.ATOMIC_MOVE
            );
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private static synchronized String loadPlayerData(UUID playerId) throws Exception {
        Path dataFile = saveDir.resolve(playerId + ".dat");
        if (!Files.exists(dataFile)) {
            return null;
        }
        String encrypted = Files.readString(dataFile);
        return encryption.decrypt(encrypted);
    }

    private static void createBackup(Path dataFile, UUID playerId) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path backupFile = backupDir.resolve(String.format("%s_%s.dat", playerId, timestamp));
        
        Path tempBackup = Files.createTempFile(backupDir, "backup_", ".tmp");
        try {
            Files.copy(dataFile, tempBackup, StandardCopyOption.REPLACE_EXISTING);
            Files.move(tempBackup, backupFile, StandardCopyOption.ATOMIC_MOVE);
            cleanupOldBackups(playerId);
        } finally {
            Files.deleteIfExists(tempBackup);
        }
    }

    private static void cleanupOldBackups(UUID playerId) throws IOException {
        try (var dirStream = Files.newDirectoryStream(backupDir, 
                path -> path.getFileName().toString().startsWith(playerId.toString()))) {
            
            var backups = dirStream.sorted((p1, p2) -> {
                try {
                    return -Files.getLastModifiedTime(p1).compareTo(Files.getLastModifiedTime(p2));
                } catch (IOException e) {
                    return 0;
                }
            }).toList();

            for (int i = MAX_BACKUPS; i < backups.size(); i++) {
                Files.deleteIfExists(backups.get(i));
            }
        }
    }

    private static Path getOrCreateDirectory(String folderName) throws IOException {
        Path dir = MinecraftServer.getServer()
            .getSavePath(WorldSavePath.ROOT)
            .resolve(folderName);
        
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
            try {
                Files.setPosixFilePermissions(dir, PosixFilePermissions.fromString("rwx------"));
            } catch (UnsupportedOperationException e) {
                LOGGER.warn("Could not set POSIX file permissions on directory: " + dir);
            }
        }
        
        return dir.toAbsolutePath();
    }

    public static void cleanup() {
        // Cleanup any resources if needed
    }
}