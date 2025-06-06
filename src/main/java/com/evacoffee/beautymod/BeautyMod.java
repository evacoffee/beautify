package com.evacoffee.beautymod;

import com.evacoffee.beautify.network.CustomizationNetworkHandler;
import com.evacoffee.beautify.util.RateLimiter;
import com.evacoffee.beautymod.command.*;
import com.evacoffee.beautymod.components.*;
import com.evacoffee.beautymod.config.ModConfig;
import com.evacoffee.beautymod.config.ModMenuIntegration;
import com.evacoffee.beautymod.data.PlayerDataManager;
import com.evacoffee.beautymod.dating.DateLocationManager;
import com.evacoffee.beautymod.dialogue.DialogueManager;
import com.evacoffee.beautymod.dialogue.DialogueRegistry;
import com.evacoffee.beautymod.entity.ModEntities;
import com.evacoffee.beautymod.entity.NPCRegistry;
import com.evacoffee.beautymod.network.ModPackets;
import com.evacoffee.beautymod.network.SecurePacketHandler;
import com.evacoffee.beautymod.security.AntiHarassmentManager;
import com.evacoffee.beautymod.security.DataEncryption;
import com.evacoffee.beautymod.security.PermissionManager;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BeautyMod implements ModInitializer {
    public static final String MOD_ID = "beautymod";
    public static final String MOD_NAME = "Beauty Mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
    
    private static BeautyMod instance;
    private static MinecraftServer serverInstance;
    public static ModConfig CONFIG;

    // Rate limiter for commands and interactions
    private static RateLimiter<UUID> rateLimiter;

    public BeautyMod() {
        instance = this;
    }

    public static BeautyMod getInstance() {
        return instance;
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing {} v{}", MOD_NAME, getModVersion());

        // Create config directory if it doesn't exist
        if (!CONFIG_DIR.toFile().exists()) {
            CONFIG_DIR.toFile().mkdirs();
        }

        // Initialize configuration
        AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        try {
            // Initialize encryption
            SecretKey encryptionKey = DataEncryption.generateKey();
            
            // Initialize core systems
            initializeComponents();
            
            // Initialize player data manager with encryption
            PlayerDataManager.initialize(encryptionKey);
            
            // Register commands
            registerCommands();
            
            // Register event handlers
            registerEventHandlers();
            
            // Initialize networking
            ModPackets.registerCommonPackets();
            
            // Initialize security systems
            AntiHarassmentManager.initialize();
            PermissionManager.initialize();
            
            // Initialize dating system
            DateLocationManager.initialize();
            
            // Initialize NPC registry
            NPCRegistry.initialize();
            
            // Initialize dialogue system
            DialogueRegistry.initialize();
            DialogueManager.initialize();
            
            // Initialize rate limiter (100 requests per second per UUID)
            rateLimiter = new RateLimiter<>(100, 1, TimeUnit.SECONDS);
            
            // Register ModMenu integration
            ModMenuIntegration.register();
            
            LOGGER.info("{} has been successfully initialized!", MOD_NAME);
        } catch (Exception e) {
            LOGGER.error("Failed to initialize " + MOD_NAME, e);
            throw new RuntimeException("Failed to initialize " + MOD_NAME, e);
        }
    }

    private void initializeComponents() {
        // Register all component types
        ModComponents.registerComponentTypes();
    }

    private void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // Core commands
            new DatingCommand().register(dispatcher);
            new ProposeCommand().register(dispatcher);
            new DivorceCommand().register(dispatcher);
            new HomeCommand().register(dispatcher);
            new LoveCommand().register(dispatcher);
            new MemoriesCommand().register(dispatcher);
            new ViewMemoriesCommand().register(dispatcher);
            
            // Debug commands (only if enabled in config)
            if (CONFIG.enableDebugCommands) {
                new DebugCommand().register(dispatcher);
                new TalkToNpcCommand().register(dispatcher);
            }
            
            // Security commands (admin only)
            new SecurityCommands().register(dispatcher);
        });
    }
    
    private void registerEventHandlers() {
        // Server lifecycle events
        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
        ServerLifecycleEvents.SERVER_STOPPED.register(this::onServerStopped);
        
        // Entity interaction
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity instanceof VillagerEntity) {
                return onVillagerInteract((ServerPlayerEntity) player, (VillagerEntity) entity, hand);
            }
            return ActionResult.PASS;
        });
    }
    
    // Server event handlers
    private void onServerStarting(MinecraftServer server) {
        serverInstance = server;
        LOGGER.info("{} is initializing with server...", MOD_NAME);
        
        // Load data
        try {
            PlayerDataManager.loadAllData();
            PermissionManager.loadPermissions();
            DateLocationManager.loadLocations();
            DialogueManager.loadDialogues();
        } catch (Exception e) {
            LOGGER.error("Failed to load data", e);
        }
    }
    
    private void onServerStopped(MinecraftServer server) {
        LOGGER.info("{} is shutting down...", MOD_NAME);
        
        try {
            // Save data
            PlayerDataManager.saveAllData();
            PermissionManager.savePermissions();
            DateLocationManager.saveLocations();
            DialogueManager.saveDialogues();
        } catch (Exception e) {
            LOGGER.error("Failed to save data", e);
        }
        
        serverInstance = null;
    }
    
    // Interaction handlers
    private ActionResult onVillagerInteract(ServerPlayerEntity player, VillagerEntity villager, net.minecraft.util.Hand hand) {
        // Check if the villager is a romanceable NPC
        if (NPCRegistry.isRomanceNPC(villager)) {
            // Handle romance interaction
            return NPCRegistry.handleInteraction(player, villager, hand);
        }
        return ActionResult.PASS;
    }
    
    // Utility methods
    public static String getModVersion() {
        return FabricLoader.getInstance()
                .getModContainer(MOD_ID)
                .orElseThrow()
                .getMetadata()
                .getVersion()
                .getFriendlyString();
    }
    
    public static MinecraftServer getServer() {
        return serverInstance;
    }
    
    public static RateLimiter<UUID> getRateLimiter() {
        return rateLimiter;
    }
}