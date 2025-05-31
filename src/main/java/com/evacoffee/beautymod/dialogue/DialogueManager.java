package com.evacoffee.beautymod.dialogue;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class DialogueManager {
    private static final Map<String, DialogueNode> DIALOGUE_NODES = new HashMap<>();
    private static final Map<UUID, ActiveDialogue> ACTIVE_DIALOGUES = new HashMap<>();

    public static void registerNode(DialogueNode node) {
        if (DIALOGUE_NODES.containsKey(node.getId())) {
            throw new IllegalArgumentException("Duplicate dialogue node ID: " + node.getId());
        }
        DIALOGUE_NODES.put(node.getId(), node);
    }

    public static boolean startDialogue(ServerPlayerEntity player, String nodeId) {
        if (isInDialogue(player)) {
            return false;
        }

        DialogueNode node = DIALOGUE_NODES.get(nodeId);
        if (node == null) {
            return false;
        }

        ACTIVE_DIALOGUES.put(player.getUuid(), new ActiveDialogue(nodeId, node));
        sendDialogueScreen(player, node);
        return true;
    }

    public static void handleResponse(ServerPlayerEntity player, int optionIndex) {
        ActiveDialogue activeDialogue = ACTIVE_DIALOGUES.get(player.getUuid());
        if (activeDialogue == null) {
            return;
        }

        DialogueNode currentNode = activeDialogue.getCurrentNode();
        if (optionIndex < 0 || optionIndex >= currentNode.getOptions().size()) {
            return;
        }

        DialogueOption chosenOption = currentNode.getOptions().get(optionIndex);
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

    public static void handleChatInput(ServerPlayerEntity player, String message) {
        if (!isInDialogue(player)) return;
        
        try {
            int choice = Integer.parseInt(message.trim()) - 1; // Convert to 0-based index
            handleResponse(player, choice);
        } catch (NumberFormatException e) {
            player.sendMessage(Text.literal("§cPlease enter a number to select an option."), false);
        }
    }

    public static void endDialogue(PlayerEntity player) {
        ACTIVE_DIALOGUES.remove(player.getUuid());
        player.sendMessage(Text.literal("§7[Conversation ended]"), false);
    }

    public static boolean isInDialogue(PlayerEntity player) {
        return ACTIVE_DIALOGUES.containsKey(player.getUuid());
    }

    private static void sendDialogueScreen(ServerPlayerEntity player, DialogueNode node) {
        // Clear previous messages
        for (int i = 0; i < 10; i++) {
            player.sendMessage(Text.literal(""), false);
        }
        
        // Send NPC name and text
        player.sendMessage(Text.literal("§e" + node.getNpcName() + ": §f" + node.getText()), false);
        player.sendMessage(Text.literal(""), false);
        
        // Send options
        for (int i = 0; i < node.getOptions().size(); i++) {
            player.sendMessage(Text.literal("§a[" + (i + 1) + "] §7" + 
                node.getOptions().get(i).getText()), false);
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