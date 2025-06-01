package com.evacoffee.beautymod;

import com.evacoffee.beautymod.command.*;
import com.evacoffee.beautymod.init.ModDialogues;
import com.evacoffee.beautymod.config.ModConfig;
import com.evacoffee.beautymod.config.ModMenuIntegration;
import com.evacoffee.beautymod.data.PlayerDataManager;
import com.evacoffee.beautymod.dialogue.DialogueManager;
import com.evacoffee.beautymod.dialogue.TestDialogueCommand;
import com.evacoffee.beautymod.event.ModEvents;
import com.evacoffee.beautymod.event.PartnerProximityHandler;
import com.evacoffee.beautymod.integration.ModIntegration;
import com.evacoffee.beautymod.network.ModPackets;
import com.evacoffee.beautymod.security.AntiHarassmentManager;
import com.evacoffee.beautymod.security.PermissionManager;
import com.evacoffee.beautymod.sound.ModSounds;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.UUID;

public class BeautyMod implements ModInitializer {
    public static final String MOD_ID = "beautymod";
    public static final String MOD_NAME = "Beauty Mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
    
    private static BeautyMod instance;
    private static MinecraftServer serverInstance;
    public static ModConfig CONFIG;

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

        // Initialize configuation
        AuthoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        // Register commands
        registerCommands();

        // Register event handlers
        registerEvents();

        // Initialize systems
        initializeSystems();

        // Register sounds
        ModSounds.registerSounds();

        LOGGER.info("{} has been successfully initialized!", MOD_NAME);
    }

    private void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // Core commands
            new DatingCommand().register(dispatcher);
            new ProposeCommand().register(dispatcher);
            new DivorceCommand().register(dispatcher);
            new RelationshipCommand().register(dispatcher);
            
            // Admin commands
            new AdminRelationshipCommand().register(dispatcher);
            new PermissionCommand().register(dispatcher);
            
            // Register test dialogue command (debug only)
            if (CONFIG.enableDebugCommands) {
                new DebugRelationshipCommand().register(dispatcher);
                TestDialogueCommand.register(dispatcher);
            }
        });
    }
    
    private void registerEventHandlers() {
        // Server lifecycle events
        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
        ServerLifecycleEvents.SERVER_STOPPED.register(this::onServerStopped);
        
        // Player events
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            onPlayerJoin(handler.getPlayer());
        });
        
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            onPlayerLeave(handler.getPlayer());
        });
        
        // Message events
        ServerMessageEvents.CHAT_MESSAGE.register((message, player, params) -> {
            // Handle chat messages if needed
        });
        
        // Interaction events
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity instanceof VillagerEntity) {
                return onVillagerInteract((ServerPlayerEntity) player, (VillagerEntity) entity, hand);
            }
            return ActionResult.PASS;
        });
        
        // Custom events
        ModEvents.RELATIONSHIP_CHANGE.register(this::onRelationshipChange);
        ModEvents.DATE_START.register(this::onDateStart);
        ModEvents.PROPOSAL.register(this::onProposal);
    }
    
    private void initializeSystems() {
        // Initialize data management
        PlayerDataManager.initialize();
        
        // Initialize security systems
        AntiHarassmentManager.initialize();
        PermissionManager.initialize();
        
        // Initialize networking
        ModPackets.registerServerPackets();
        
        // Initialize mod integrations
        ModIntegration.initialize();
        
        // Initialize proximity handler
        PartnerProximityHandler.initialize();
        
        // Initialize dialogue system
        ModDialogues.register();
    }
    
    // Event handlers
    private void onServerStarting(MinecraftServer server) {
        serverInstance = server;
        LOGGER.info("{} is initializing with server...", MOD_NAME);
        
        // Load data
        PlayerDataManager.loadAllData();
        PermissionManager.loadPermissions();
    }
    
    private void onServerStopped(MinecraftServer server) {
        LOGGER.info("{} is shutting down...", MOD_NAME);
        
        // Save data
        PlayerDataManager.saveAllData();
        PermissionManager.savePermissions();
        
        serverInstance = null;
    }
    
    private void onPlayerJoin(ServerPlayerEntity player) {
        if (player == null) return;
        
        UUID playerId = player.getUuid();
        
        // Load player data
        PlayerDataManager.loadPlayerData(playerId);
        
        // Send welcome message if enabled
        if (CONFIG.sendWelcomeMessage) {
            player.sendMessage(Text.literal("Welcome to the server! Type /dating help for relationship commands."));
        }
        
        // Update presence
        ModIntegration.updatePlayerPresence(player, true);
    }
    
    private void onPlayerLeave(ServerPlayerEntity player) {
        if (player == null) return;
        
        UUID playerId = player.getUuid();
        
        // Save player data
        PlayerDataManager.savePlayerData(playerId);
        
        // Update presence
        ModIntegration.updatePlayerPresence(player, false);
    }
    
    private ActionResult onVillagerInteract(ServerPlayerEntity player, VillagerEntity villager, net.minecraft.util.Hand hand) {
        // Check if the villager is a farmer (example condition)
        if (villager.getVillagerData().getProfession().toString().contains("farmer")) {
            // Start dialogue with the farmer
            if (DialogueManager.startDialogue(player, "farmer_john_greeting")) {
                return ActionResult.SUCCESS;
            }
        }
        // Check for dating interactions
        if (player.getStackInHand(hand).isEmpty()) {
            if (ModEvents.DATE_START.invoker().onDateStart(player, villager)) {
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }
    
    // Event handler stubs
    private void onRelationshipChange(UUID player1, UUID player2, int oldStatus, int newStatus) {
        // Handle relationship change
    }
    
    private boolean onDateStart(ServerPlayerEntity player, VillagerEntity villager) {
        // Handle date start
        return false;
    }
    
    private boolean onProposal(ServerPlayerEntity proposer, ServerPlayerEntity target) {
        // Handle marriage proposal
        return false;
    }
    
    public static String getModVersion() {
        return FabricLoader.getInstance()
                .getModContainer(MOD_ID)
                .orElseThrow()
                .getMetadata()
                .getVersion()
                .getFriendlyString();
    }
}
