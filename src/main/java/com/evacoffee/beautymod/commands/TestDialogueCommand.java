package com.evacoffee.beautymod.commands;

import com.evacoffee.beautymod.dialogue.DialogueManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import static net.minecraft.server.command.CommandManager.literal;

public class TestDialogueCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("testdialogue")
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        // Start the dialogue
                        if (DialogueManager.startDialogue(player, "farmer_john_greeting")) {
                            context.getSource().sendFeedback(new LiteralText("Started dialogue with Farmer John"), false);
                            return 1;
                        } else {
                            context.getSource().sendError(new LiteralText("Failed to start dialogue"));
                            return 0;
                        }
                    }
                    return 0;
                })
        );
    }
}
