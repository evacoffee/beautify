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
        dispatcher.register(
            literal("testdialogue")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        // Example dialogue - replace with your actual dialogue logic
                        player.sendMessage(Text.literal("Starting test dialogue..."), false);
                        // Add your dialogue start logic here
                        return 1;
                    }
                    return 0;
                })
        );
    