package com.evacoffee.beautymod.init;

import com.evacoffee.beautymod.BeautyMod;
import com.evacoffee.beautymod.dialogue.DialogueManager;
import com.evacoffee.beautymod.dialogue.DialogueNode;
import com.evacoffee.beautymod.dialogue.DialogueOption;
import com.evacoffee.beautymod.dialogue.conditions.DialogueConditions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModDialogues {
    public static final String MOD_ID = BeautyMod.MOD_ID;
    
    public static void register() {
        // Farmer John's dialogue
        DialogueNode farmerGreeting = new DialogueNode.Builder("farmer_john_greeting")
            .npcName("Farmer John")
            .text("Howdy there, partner! Lovely day for farmin', ain't it?")
            .addOption(new DialogueOption.Builder(new LiteralText("Do you need any help?"))
                .nextNode("farmer_quest")
                .onSelect(player -> player.sendMessage(new LiteralText("John's eyes light up with hope."), false))
                .build())
            .addOption(new DialogueOption.Builder(new LiteralText("Nice weather we're having."))
                .nextNode("weather_chat")
                .onSelect(player -> player.sendMessage(new LiteralText("John looks up at the sky and nods."), false))
                .build())
            .build();

        // Farmer's quest dialogue
        DialogueNode farmerQuest = new DialogueNode.Builder("farmer_quest")
            .npcName("Farmer John")
            .text("Actually, I could use some help. My wheat field needs harvesting. Could you gather 10 wheat for me?")
            .addOption(new DialogueOption.Builder(new LiteralText("I'll help you!"))
                .nextNode("accept_quest")
                .onSelect(player -> {
                    player.sendMessage(new LiteralText("John smiles gratefully."), false);
                    // Start quest logic here
                })
                .build())
            .addOption(new DialogueOption.Builder(new LiteralText("Maybe later."))
                .nextNode("farmer_greeting")
                .onSelect(player -> player.sendMessage(new LiteralText("John looks disappointed but nods."), false))
                .build())
            .build();

        // Register all dialogue nodes
        DialogueManager.registerNode(farmerGreeting);
        DialogueManager.registerNode(farmerQuest);
    }
}
