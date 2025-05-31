package com.evacoffee.beautymod;

import com.evacoffee.beautymod.command.*;
import com.evacoffee.beautymod.config.ModConfig;
import com.evacoffee.beautymod.config.ModMenuIntegration;
import com.evacoffee.beautymod.dialogue.DialogueManager;
import com.evacoffee.beautymod.dialogue.DialogueNode;
import com.evacoffee.beautymod.event.ModEvents;
import com.evacoffee.beautymod.integration.ModIntegration;
import com.evacoffee.beautymod.network.ModPackets;
import com.evacoffee.beautymod.sound.ModSounds;
import com.evacoffee.beautymod.event.PartnerProximityHandler;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeautyMod implements ModInitializer {
    public static final String MOD_ID = "beautymod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static ModConfig CONFIG;
    
    private static MinecraftServer serverInstance;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing BeautyMod");
        
        // Initialize configuration
        AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        
        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            DialogueCommand.register(dispatcher);
            TestDialogueCommand.register(dispatcher);
            DatingCommand.register(dispatcher);
            ProposeCommand.register(dispatcher);
            DivorceCommand.register(dispatcher);
            LoveCommand.register(dispatcher);
            MemoriesCommand.register(dispatcher);
        });

        // Register server lifecycle events
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            serverInstance = server;
            registerDialogues();
            ModIntegration.init();
            ModPackets.registerServerReceivers();
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            serverInstance = null;
        });

        // Register events
        ModEvents.DATE_START.register((player1, player2) -> {
            player1.sendMessage(Text.literal("You asked " + player2.getName().getString() + " on a date!"));
            return ActionResult.SUCCESS;
        });

        // Initialize mod menu integration
        ModMenuIntegration.registerModsPage();
        
        // Register sounds and proximity handlers
        ModSounds.registerSounds();
        PartnerProximityHandler.register();
        LOGGER.info("BeautyMod sounds and proximity handlers registered successfully!");
    }

    private static void registerDialogues() {
        // Greeting dialogue
        new DialogueNode("greeting", "Villager", 
                "Hello there, traveler! How can I help you today?")
            .addOption("I'm looking for work", "work_options", player -> {
                player.sendMessage(Text.literal("You ask about available work."));
            })
            .addOption("Tell me about this place", "village_info", player -> {
                player.sendMessage(Text.literal("You ask about the village."));
            })
            .addOption("Would you like to go on a date?", "date_invite", player -> {
                player.sendMessage(Text.literal("You ask them out on a date."));
            })
            .addExitOption("Never mind", player -> {
                player.sendMessage(Text.literal("You decide not to talk."));
            })
            .register();

        // Date invitation
        new DialogueNode("date_invite", "Villager", 
                "Oh! I'd love to go on a date with you! Where should we go?")
            .addOption("Let's go to the village square", "date_location_square", player -> {
                player.sendMessage(Text.literal("You suggest going to the village square."));
            })
            .addOption("How about a walk in the forest?", "date_location_forest", player -> {
                player.sendMessage(Text.literal("You suggest a walk in the forest."));
            })
            .addExitOption("On second thought...", player -> {
                player.sendMessage(Text.literal("You change your mind."));
            })
            .register();
    }

    public static MinecraftServer getServer() {
        return serverInstance;
    }
}