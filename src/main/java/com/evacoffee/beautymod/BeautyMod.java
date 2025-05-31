package com.evacoffee.beautymod;

import com.evacoffee.beautymod.command.DialogueCommand;
import com.evacoffee.beautymod.dialogue.DialogueManager;
import com.evacoffee.beautymod.dialogue.DialogueNode;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main mod class for the BeautyMod.
 */
public class BeautyMod implements ModInitializer {
    public static final String MOD_ID = "beautymod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    private static MinecraftServer serverInstance;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing BeautyMod");

        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            DialogueCommand.register(dispatcher);
        });

        // Register server lifecycle events
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            serverInstance = server;
            registerDialogues();
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            serverInstance = null;
        });

        // Register entity interaction handler
        UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient) {
                return ActionResult.PASS;
            }

            if (entity instanceof VillagerEntity) {
                // Start dialogue when right-clicking a villager
                if (!DialogueManager.isInDialogue(player)) {
                    DialogueManager.startDialogue((ServerPlayerEntity) player, "greeting");
                }
                return ActionResult.SUCCESS;
            }

            return ActionResult.PASS;
        });
    }

    /**
     * Registers all dialogue nodes.
     */
    private static void registerDialogues() {
        // Greeting dialogue
        new DialogueNode("greeting", "Villager", "Hello there, traveler! How can I help you today?")
            .addOption("I'm looking for a quest", "quest_offer", player -> {
                player.sendMessage(Text.literal("You show interest in a quest."));
            })
            .addOption("Just passing through", "farewell", player -> {
                player.sendMessage(Text.literal("You nod politely."));
            })
            .addExitOption("Never mind", player -> {
                player.sendMessage(Text.literal("You decide not to talk."));
            })
            .register();

        // Quest offer dialogue
        new DialogueNode("quest_offer", "Villager", 
                "I have a task that needs doing. Will you help?")
            .addOption("Yes, I'll help!", "quest_accepted", player -> {
                player.sendMessage(Text.literal("You accepted the quest!"));
                // Add quest logic here
            })
            .addOption("What's involved?", "quest_details", player -> {
                player.sendMessage(Text.literal("You ask for more details."));
            })
            .addExitOption("Not right now", player -> {
                player.sendMessage(Text.literal("Maybe next time."));
            })
            .register();

        // More dialogue nodes...
        new DialogueNode("quest_accepted", "Villager", 
                "Thank you! Please bring me 10 wheat.")
            .addExitOption("I'll be back soon!", player -> {
                player.sendMessage(Text.literal("Quest started!"));
            })
            .register();

        new DialogueNode("quest_details", "Villager", 
                "I need someone to gather 10 wheat from the fields. " +
                "The crows have been stealing my crops!")
            .addOption("I'll help with that", "quest_accepted", player -> {
                player.sendMessage(Text.literal("You agree to help with the wheat."));
            })
            .addExitOption("That sounds dangerous", player -> {
                player.sendMessage(Text.literal("You decide against helping."));
            })
            .register();

        new DialogueNode("farewell", "Villager", 
                "Safe travels, friend. Come back if you change your mind!")
            .register();
    }

    /**
     * Gets the current Minecraft server instance.
     * @return The server instance, or null if not in a server context
     */
    @Nullable
    public static MinecraftServer getServer() {
        return serverInstance;
    }
}