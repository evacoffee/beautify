package com.evacoffee.beautymod;

import com.evacoffee.beautymod.command.DialogueCommand;
import com.evacoffee.beautymod.command.TestDialogueCommand;
import com.evacoffee.beautymod.dialogue.DialogueManager;
import com.evacoffee.beautymod.dialogue.DialogueNode;
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
    
    private static MinecraftServer serverInstance;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing BeautyMod");

        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            DialogueCommand.register(dispatcher);
            TestDialogueCommand.register(dispatcher);
        });

        // Register server lifecycle events
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            serverInstance = server;
            registerDialogues();
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            serverInstance = null;
        });

        // Register chat handler
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
            if (DialogueManager.isInDialogue(sender)) {
                DialogueManager.handleChatInput(sender, message.getContent().getString());
                return true; // Cancel the original message
            }
            return false;
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
        new DialogueNode("greeting", "Villager", 
                "Hello there, traveler! How can I help you today?")
            .addOption("I'm looking for work", "work_options", player -> {
                player.sendMessage(Text.literal("You ask about available work."));
            })
            .addOption("Tell me about this place", "village_info", player -> {
                player.sendMessage(Text.literal("You ask about the village."));
            })
            .addOption("Do you have any quests?", "quest_options", player -> {
                player.sendMessage(Text.literal("You inquire about quests."));
            })
            .addExitOption("Never mind", player -> {
                player.sendMessage(Text.literal("You decide not to talk."));
            })
            .register();

        // Work options
        new DialogueNode("work_options", "Villager", 
                "We always need help around here! What kind of work are you interested in?")
            .addOption("Farming", "farming_work", player -> {
                player.sendMessage(Text.literal("You express interest in farming."));
            })
            .addOption("Mining", "mining_work", player -> {
                player.sendMessage(Text.literal("You ask about mining opportunities."));
            })
            .addOption("Blacksmithing", "blacksmith_work", player -> {
                player.sendMessage(Text.literal("You inquire about blacksmithing."));
            })
            .addOption("Go back", "greeting", player -> {
                player.sendMessage(Text.literal("You change your mind."));
            })
            .register();

        // Quest options
        new DialogueNode("quest_options", "Villager", 
                "I have several tasks that need attention. What interests you?")
            .addOption("Monster hunting", "monster_hunt", player -> {
                player.sendMessage(Text.literal("You ask about monster hunting quests."));
            })
            .addOption("Item retrieval", "item_retrieval", player -> {
                player.sendMessage(Text.literal("You ask about retrieving lost items."));
            })
            .addOption("Escort mission", "escort_mission", player -> {
                player.sendMessage(Text.literal("You offer to escort someone."));
            })
            .addOption("Go back", "greeting", player -> {
                player.sendMessage(Text.literal("You change your mind."));
            })
            .register();

        // Monster hunt quest
        new DialogueNode("monster_hunt", "Villager", 
                "There's been reports of a dangerous creature in the nearby caves. " +
                "Can you investigate and eliminate the threat?")
            .addOption("I'll take care of it", "monster_hunt_accepted", player -> {
                player.sendMessage(Text.literal("You accept the monster hunting quest!"));
                // Add quest start logic here
            })
            .addOption("What's the reward?", "monster_hunt_reward", player -> {
                player.sendMessage(Text.literal("You ask about the reward."));
            })
            .addOption("That sounds too dangerous", "quest_options", player -> {
                player.sendMessage(Text.literal("You decline the quest."));
            })
            .register();

        // Monster hunt reward
        new DialogueNode("monster_hunt_reward", "Villager", 
                "I can offer you 10 emeralds and my eternal gratitude. " +
                "The creature has been terrorizing our village for weeks!")
            .addOption("Alright, I'll do it", "monster_hunt_accepted", player -> {
                player.sendMessage(Text.literal("You accept the quest for the reward!"));
                // Add quest start logic here
            })
            .addOption("I need more than that", "monster_hunt_negotiate", player -> {
                player.sendMessage(Text.literal("You try to negotiate a better deal."));
            })
            .addOption("No thanks", "quest_options", player -> {
                player.sendMessage(Text.literal("You decline the quest."));
            })
            .register();

        // Monster hunt negotiation
        new DialogueNode("monster_hunt_negotiate", "Villager", 
                "Very well, I can offer you 15 emeralds, but that's my final offer!")
            .addOption("Deal!", "monster_hunt_accepted", player -> {
                player.sendMessage(Text.literal("You accept the improved offer!"));
                // Add quest start logic with better reward
            })
            .addOption("I'll pass", "quest_options", player -> {
                player.sendMessage(Text.literal("You decline the quest."));
            })
            .register();

        // Village info
        new DialogueNode("village_info", "Villager", 
                "Our village has stood here for generations. " +
                "We're mostly farmers, but we have a blacksmith and a general store. " +
                "The mine to the north provides us with valuable resources.")
            .addOption("Tell me about the history", "village_history", player -> {
                player.sendMessage(Text.literal("You ask about the village's history."));
            })
            .addOption("Who's in charge here?", "village_leader", player -> {
                player.sendMessage(Text.literal("You ask about the village leader."));
            })
            .addOption("Go back", "greeting", player -> {
                player.sendMessage(Text.literal("You change the subject."));
            })
            .register();

        // Exit node
        new DialogueNode("goodbye", "Villager", 
                "Farewell, traveler! Come back if you need anything else.")
            .register();
    }

    public static MinecraftServer getServer() {
        return serverInstance;
    }
}