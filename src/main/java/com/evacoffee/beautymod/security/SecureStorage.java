package com.evacoffee.beautymod.security;

import com.evacoffee.beautymod.BeautyMod;
import com.evacoffee.beautymod.util.ModLogger;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class SecureStorage extends PersistentState {
    private static final String DATA_NAME = BeautyMod.MOD_ID + "_secure_data";
    private final Map<String, String> secureData = new HashMap<>();
    
    public static SecureStorage getServerState(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(
            SecureStorage::fromNbt,
            SecureStorage::new,
            DATA_NAME
        );
    }
    
    public String getSecureData(String key) {
        return secureData.get(key);
    }
    
    public void setSecureData(String key, String value) {
        secureData.put(key, value);
        markDirty();
    }
    
    public static SecureStorage fromNbt(NbtCompound nbt) {
        SecureStorage state = new SecureStorage();
        // Load data from NBT
        nbt.getKeys().forEach(key -> 
            state.secureData.put(key, nbt.getString(key))
        );
        return state;
    }
    
    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        secureData.forEach(nbt::putString);
        return nbt;
    }
    
    public static String hashData(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            ModLogger.error("Failed to hash data", e);
            throw new RuntimeException("Failed to hash data", e);
        }
    }
}