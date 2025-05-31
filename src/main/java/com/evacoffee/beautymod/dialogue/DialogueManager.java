package com.evacoffee.beautymod.dialogue;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;

public class DialogueManager {
    private static final Map<String, DialogueNode> DIALOGUE_NODES = new HashMap<>();
    private static final Map<UUID, ActiveDialogue> ACTIVE_DIALOGUES = new HashMap<>();
    private static final Map<String, List<DialogueOption>> GLOBAL_OPTIONS = new HashMap<>();

    public static class ActiveDialogue {
        private final String startNodeId;
        private DialogueNode currentNode;
        private final Map<String, Object> variables = new HashMap<>();

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

        public void setVariable(String key, Object value) {
            variables.put(key, value);
        }

        @SuppressWarnings("unchecked")
        public <T> T getVariable(String key) {
            return (T) variables.get(key);
        }
    }

    public static void registerNode(DialogueNode node) {
        if (DIALOGUE_NODES.containsKey(node.getId())) {
            throw new IllegalArgumentException("Duplicate dialogue node ID: " + node.getId());
        }
        DIALOGUE_NODES.put(node.getId(), node);
    }

    public static void registerGlobalOption(String nodeId, DialogueOption option) {
        GLOBAL_OPTIONS.computeIfAbsent(nodeId, k -> new ArrayList<>()).add(option);
    }

    public static boolean startDialogue(ServerPlayerEntity player, String nodeId) {
        if (isInDialogue(player)) {
            return false;
        }

        DialogueNode node = DIALOGUE_NODES.get(nodeId);
        if (node == null) {
            return false;
        }

        ActiveDialogue dialogue = new ActiveDialogue(nodeId, node);
        ACTIVE_DIALOGUES.put(player.getUuid(), dialogue);
        sendDialogueScreen(player, node);
        return true;
    }

    public static void handleResponse(ServerPlayerEntity player, int optionIndex) {
        ActiveDialogue activeDialogue = ACTIVE_DIALOGUES.get(player.getUuid());
        if (activeDialogue == null) {
            return;
        }

        DialogueNode currentNode = activeDialogue.getCurrentNode();
        List<DialogueOption> availableOptions = getAvailableOptions(player, currentNode);
        
        if (optionIndex < 0 || optionIndex >= availableOptions.size()) {
            return;
        }

        DialogueOption chosenOption = availableOptions.get(optionIndex);
        chosenOption.getOnSelect().accept(player);

        if (chosenOption.getNextNodeId() == null || chosenOption.getNextNodeId().isEmpty()) {
            endDialogue(player);
            return;
        }

        DialogueNode nextNode = DIALOGUE_NODES.get(chosenOption.getNextNodeId());
        if (nextNode == null) {
            endDialogue(player);
            return;
        }

        activeDialogue.setCurrentNode(nextNode);
        if (nextNode.getOnStart() != null) {
            nextNode.getOnStart().accept(player);
        }
        sendDialogueScreen(player, nextNode);
    }

    public static void handleChatInput(ServerPlayerEntity player, String message) {
        if (!isInDialogue(player)) return;
        
        try {
            int choice = Integer.parseInt(message.trim()) - 1;
            handleResponse(player, choice);
        } catch (NumberFormatException e) {
            player.sendMessage(Text.literal("§cPlease enter a number to select an option."), false);
        }
    }

    public static void endDialogue(ServerPlayerEntity player) {
        ACTIVE_DIALOGUES.remove(player.getUuid());
        player.sendMessage(Text.literal("§7[Conversation ended]"), false);
    }

    public static boolean isInDialogue(ServerPlayerEntity player) {
        return ACTIVE_DIALOGUES.containsKey(player.getUuid());
    }

    private static List<DialogueOption> getAvailableOptions(ServerPlayerEntity player, DialogueNode node) {
        List<DialogueOption> available = new ArrayList<>();
        
        // Add regular node options
        for (DialogueOption option : node.getOptions()) {
            if (option.isAvailable(player)) {
                available.add(option);
            }
        }
        
        // Add global options
        List<DialogueOption> globalOptions = GLOBAL_OPTIONS.getOrDefault(node.getId(), Collections.emptyList());
        for (DialogueOption option : globalOptions) {
            if (option.isAvailable(player)) {
                available.add(option);
            }
        }
        
        return available;
    }

    private static void sendDialogueScreen(ServerPlayerEntity player, DialogueNode node) {
        // Clear previous messages
        for (int i = 0; i < 10; i++) {
            player.sendMessage(Text.literal(""), false);
        }
        
        // Send NPC name and text
        player.sendMessage(Text.literal("§e" + node.getNpcName() + ": §f" + node.getText()), false);
        player.sendMessage(Text.literal(""), false);
        
        // Send available options
        List<DialogueOption> availableOptions = getAvailableOptions(player, node);
        for (int i = 0; i < availableOptions.size(); i++) {
            player.sendMessage(Text.literal("§a[" + (i + 1) + "] §7" + 
                availableOptions.get(i).getText()), false);
        }
    }

    // Helper method to get the current dialogue node for a player
    public static DialogueNode getCurrentNode(ServerPlayerEntity player) {
        ActiveDialogue dialogue = ACTIVE_DIALOGUES.get(player.getUuid());
        return dialogue != null ? dialogue.getCurrentNode() : null;
    }

    // Helper method to get the active dialogue for a player
    public static ActiveDialogue getActiveDialogue(ServerPlayerEntity player) {
        return ACTIVE_DIALOGUES.get(player.getUuid());
    }
}