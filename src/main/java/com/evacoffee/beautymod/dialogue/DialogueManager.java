package com.evacoffee.beautymod.dialogue;

import com.evacoffee.beautymod.player.AffectionComponent;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.*;

public class DialogueManager {
    private static final Map<String, List<DialogueNode>> DIALOGUE_TREES = new HashMap<>();
    
    static {
        // Example dialogue tree for a villager named "Farmer"
        List<DialogueNode> farmerDialogue = new ArrayList<>();
        
        // Root node (always available)
        DialogueNode root = new DialogueNode("Hello there, traveler!");
        
        // First response option (always available)
        DialogueNode option1 = new DialogueNode("Who are you?");
        option1.addResponse("I'm just a simple farmer living off the land.");
        root.addOption(option1);
        
        // Second response option (requires some affection)
        DialogueNode option2 = new DialogueNode("Need any help?");
        option2.addResponse("Actually, I could use some help with my crops...");
        option2.setAffectionRequirement(100); // Requires 100 affection points
        
        // Add a follow-up option
        DialogueNode option2FollowUp = new DialogueNode("What do you need help with?");
        option2FollowUp.addResponse("I need someone to water my crops while I'm away.");
        option2FollowUp.addResponse("Could you gather some wheat for me?");
        option2.addOption(option2FollowUp);
        
        root.addOption(option2);
        
        // Add the dialogue tree for the farmer
        farmerDialogue.add(root);
        DIALOGUE_TREES.put("farmer", farmerDialogue);
    }
    
    public static void startDialogue(PlayerEntity player, VillagerEntity villager, AffectionComponent affection) {
        // Get the villager's profession (simplified)
        String profession = villager.getVillagerData().getProfession().toString().toLowerCase();
        
        if (!DIALOGUE_TREES.containsKey(profession)) {
            player.sendMessage(Text.of("I don't have much to say right now."), false);
            return;
        }
        
        // Start with the root dialogue node
        showDialogueNode(player, DIALOGUE_TREES.get(profession).get(0), villager, affection);
    }
    
    private static void showDialogueNode(PlayerEntity player, DialogueNode node, VillagerEntity villager, AffectionComponent affection) {
        // Show the NPC's message
        villager.sendMessage(Text.of(node.getMessage()));
        
        // Show available response options
        int optionNumber = 1;
        List<DialogueNode> availableOptions = new ArrayList<>();
        
        // First collect all available options
        for (DialogueNode option : node.getOptions()) {
            if (affection.getAffection(villager.getName().getString()) >= option.getAffectionRequirement()) {
                availableOptions.add(option);
            }
        }
        
        // Then display them with numbers
        for (int i = 0; i < availableOptions.size(); i++) {
            player.sendMessage(Text.of((i + 1) + ". " + availableOptions.get(i).getMessage()), false);
        }
        
        // If no options are available, add a default "Goodbye" option
        if (availableOptions.isEmpty()) {
            player.sendMessage(Text.of("1. Goodbye"), false);
        }
    }
    
    // Call this when a player selects a dialogue option
    public static void handleDialogueChoice(PlayerEntity player, int choice, VillagerEntity villager, AffectionComponent affection) {
        // This would be called when a player selects a dialogue option
        // Implementation depends on how you want to handle player input
        // For now, we'll just acknowledge the choice
        player.sendMessage(Text.of("You selected option: " + choice), false);
        
        // Add some affection when the player talks to the NPC
        affection.addAffection(villager.getName().getString(), 5);
    }
}
