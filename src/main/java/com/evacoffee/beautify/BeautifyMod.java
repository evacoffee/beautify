package com.evacoffee.beautify;

import com.evacoffee.beautify.network.CustomizationNetworkHandler;
import com.evacoffee.beautymod.data.PlayerDataManager;
import com.evacoffee.beautymod.network.SecurePacketHandler;
import com.evacoffee.beautymod.security.DataEncryption;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BeautifyMod implements ModInitializer {
    public static final String MOD_ID = "beautify";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    private static final String KEYSTORE_FILE = "beautify-keystore.bks";
    private static final String KEY_ALIAS = "beautify_key";
    private static final String KEYSTORE_PASSWORD = System.getenv("BEAUTIFY_KEYSTORE_PASSWORD");
    
    private static RateLimiter<UUID> rateLimiter;
    
    @Override
    public void onInitialize() {
        LOGGER.info("BeautifyMod initializing...");
        
        try {
            // Initialize encryption
            SecretKey encryptionKey = initializeEncryption();
            
            // Initialize player data manager
            PlayerDataManager.initialize(encryptionKey);
            
            // Register server lifecycle events
            ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
            ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);
            
            // Initialize rate limiter
            rateLimiter = new RateLimiter<>(100, 1, TimeUnit.SECONDS);
            
            // Initialize network
            initializeNetwork();
            
            // Original initialization
            CustomizationNetworkHandler.registerServerReceivers();
            
            LOGGER.info("BeautifyMod initialized with enhanced security features.");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize BeautifyMod", e);
            throw new RuntimeException("Failed to initialize BeautifyMod", e);
        }
    }
    
    private SecretKey initializeEncryption() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("BKS");
        File keyStoreFile = new File(KEYSTORE_FILE);
        
        if (keyStoreFile.exists()) {
            // Load existing keystore
            try (FileInputStream fis = new FileInputStream(keyStoreFile)) {
                keyStore.load(fis, KEYSTORE_PASSWORD.toCharArray());
                return (SecretKey) keyStore.getKey(KEY_ALIAS, KEYSTORE_PASSWORD.toCharArray());
            }
        } else {
            // Create new keystore and key
            if (KEYSTORE_PASSWORD == null || KEYSTORE_PASSWORD.length() < 16) {
                throw new IllegalStateException(
                    "BEAUTIFY_KEYSTORE_PASSWORD environment variable must be set with at least 16 characters");
            }
            
            // Generate new key
            SecretKey secretKey = DataEncryption.generateKey();
            
            // Save to keystore
            keyStore.load(null, null);
            keyStore.setKeyEntry(KEY_ALIAS, secretKey, 
                KEYSTORE_PASSWORD.toCharArray(), null);
            
            try (FileOutputStream fos = new FileOutputStream(keyStoreFile)) {
                keyStore.store(fos, KEYSTORE_PASSWORD.toCharArray());
            }
            
            // Set restrictive permissions on the keystore file
            keyStoreFile.setReadable(false, false);
            keyStoreFile.setReadable(true, true);
            keyStoreFile.setWritable(false, false);
            keyStoreFile.setWritable(true, true);
            keyStoreFile.setExecutable(false, false);
            
            return secretKey;
        }
    }
    
    private void initializeNetwork() {
        SecurePacketHandler.registerServerReceiver(
            new Identifier(MOD_ID, "customization_update"),
            (server, player, handler, buf, responseSender) -> {
                // Handle customization update securely
                CustomizationNetworkHandler.handleCustomizationUpdate(server, player, buf);
            }
        );
    }
    
    private void onServerStarting(MinecraftServer server) {
        LOGGER.info("BeautifyMod server starting...");
        // Additional server startup logic
    }
    
    private void onServerStopping(MinecraftServer server) {
        LOGGER.info("BeautifyMod server stopping...");
        // Cleanup resources
        if (rateLimiter != null) {
            rateLimiter.cleanup();
        }
        PlayerDataManager.cleanup();
    }
}