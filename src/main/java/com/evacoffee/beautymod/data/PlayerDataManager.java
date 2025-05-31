package com.evacoffee.beautymod.data;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.UUID;

public class PlayerDataManager {
    private static final Strong DATA_PLAYER = "beautymod_data";
    private static final String BACKUP_FOLDER = "beautymod_backup";

    public static void savePlayerData(UUID playerId, String data) throws IOException {
        Path saveDir = getSaveDirectory();
        Path dataFile = saveDir.resolve(playerId + ".dat");

        //Create Backup
        if (Files.exists(dataFile)) {
            createBackup(dataFile, playerId);
        }

        //Save encrypted data
        String encrypted = SimpleEncryption.encrypt(data);
        Files.writeString(dataFile, encrypted);
    }

    public static void loadPlayerData(UUID playerId) throws IOException {
        Path dataFile = getSaveDirectory().resolve(playerId + ".dat");
        if (!Files.exists(dataFile)) return null;

        String encrypted = Files.readString(dataFile);
        return SimpleEncryption.decrypt(encrypted);
    }

    private static void createBackup(Path originalFile, UUID playerId) throws IOException {
        Path backupDir = getBackupDirectory();
        Path backupFile = backupDir.resolve(playerId + ".dat");
        Files.copy(originalFile, backupFile);
    }

    private static Path getSaveDirectory() throws IOException {
        Path dir = MinecraftServer.getServer().getSavePath(WorldSavePath.ROOT)
            .resolve(DATA_FOLDER);
        Files.createDirectories(dir);
        return dir;
    }
    
  private static Path getBackupDirectory() throws IOException {
        Path dir = MinecraftServer.getServer().getSavePath(WorldSavePath.ROOT)
            .resolve(BACKUP_FOLDER);
        Files.createDirectories(dir);
        return dir;
    }
}
        