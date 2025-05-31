package com.evacoffee.beautymod.command;

import com.evacoffee.beautymod.dialogue.DialogueManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public class TestDialogueCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("testdialogue")
            .executes(context -> {
                ServerPlayerEntity player = context.getSource().getPlayer();
                if (player != null) {
                    if (DialogueManager.isInDialogue(player)) {
                        player.sendMessage(Text.literal("§cYou're already in a dialogue!"), false);
                    } else {
                        DialogueManager.startDialogue(player, "greeting");
                        player.sendMessage(Text.literal("§aStarted test dialogue! Type numbers to select options."), false);
                    }
                    return Command.SINGLE_SUCCESS;
                }
                return 0;
            }));
    }
}