package com.evacoffee.beautymod.dialogue;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages all dialogue interactions in the game.
 */
public class DialogueManager {
    private static final Map<String, DialogueNode> DIALOGUE_NODES = new HashMap<>();
    private static final Map<UUID, ActiveDialogue> ACTIVE_DIALOGUES = new HashMap<>();

    /**
     * Registers a dialogue node with the manager.
     * @param node The node to register
     * @throws IllegalArgumentException if a node with the same ID is already registered
     */
    public static void registerNode(DialogueNode node) {
        if (DIALOGUE_NODES.containsKey(node.getId())) {
            throw new IllegalArgumentException("Dialogue node with ID " + node.getId() + " is already registered");
        }
        DIALOGUE_NODES.put(node.getId(), node);
    }

    /**
     * Starts a dialogue for a player.
     * @param player The player to start the dialogue for
     * @param startNodeId The ID of the node to start with
     * @return true if the dialogue was started successfully, false otherwise
     */
    public static boolean startDialogue(ServerPlayerEntity player, String startNodeId) {
        if (isInDialogue(player)) {
            return false;
        }

        DialogueNode startNode = DIALOGUE_NODES.get(startNodeId);
        if (startNode == null) {
            return false;
        }

        ACTIVE_DIALOGUES.put(player.getUuid(), new ActiveDialogue(startNodeId, startNode));
        sendDialogueScreen(player, startNode);
        return true;
    }

    /**
     * Handles a player's response to a dialogue.
     * @param player The player responding
     * @param optionIndex The index of the option chosen
     */
    public static void handleResponse(ServerPlayerEntity player, int optionIndex) {
        ActiveDialogue activeDialogue = ACTIVE_DIALOGUES.get(player.getUuid());
        if (activeDialogue == null) {
            return;
        }

        DialogueNode currentNode = activeDialogue.getCurrentNode();
        if (optionIndex < 0 || optionIndex >= currentNode.getOptions().size()) {
            return;
        }

        DialogueNode.DialogueOption chosenOption = currentNode.getOptions().get(optionIndex);
        chosenOption.getOnSelect().accept(player);

        if (chosenOption.getNextNodeId() == null) {
            endDialogue(player);
            return;
        }

        DialogueNode nextNode = DIALOGUE_NODES.get(chosenOption.getNextNodeId());
        if (nextNode == null) {
            endDialogue(player);
            return;
        }

        activeDialogue.setCurrentNode(nextNode);
        sendDialogueScreen(player, nextNode);
    }

    /**
     * Ends a player's current dialogue.
     * @param player The player to end the dialogue for
     */
    public static void endDialogue(PlayerEntity player) {
        ActiveDialogue dialogue = ACTIVE_DIALOGUES.remove(player.getUuid());
        if (dialogue != null) {
            player.sendMessage(Text.literal("§7[Conversation ended]"), false);
        }
    }

    /**
     * Checks if a player is currently in a dialogue.
     * @param player The player to check
     * @return true if the player is in a dialogue, false otherwise
     */
    public static boolean isInDialogue(PlayerEntity player) {
        return ACTIVE_DIALOGUES.containsKey(player.getUuid());
    }

    private static void sendDialogueScreen(ServerPlayerEntity player, DialogueNode node) {
        // Clear previous messages
        player.sendMessage(Text.literal("§7--------------------------------"), false);
        
        // Send NPC name and text
        player.sendMessage(Text.literal("§e" + node.getNpcName() + ": §f" + node.getText().getString()), false);
        
        // Send options
        List<DialogueNode.DialogueOption> options = node.getOptions();
        for (int i = 0; i < options.size(); i++) {
            player.sendMessage(Text.literal("§a[" + (i + 1) + "] " + options.get(i).getText().getString()), false);
        }
    }

    private static class ActiveDialogue {
        private final String startNodeId;
        private DialogueNode currentNode;

        public ActiveDialogue(String startNodeId, DialogueNode startNode) {
            this.startNodeId = startNodeId;
            this.currentNode = startNode;
        }

        public String getStartNodeId() {
            return startNodeId;
        }

        public DialogueNode getCurrentNode() {
            return currentNode;
        }

        public void setCurrentNode(DialogueNode node) {
            this.currentNode = node;
        }
    }
}