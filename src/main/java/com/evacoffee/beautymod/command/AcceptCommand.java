package com.evacoffee.beautymod.command;

import com.evacoffee.beautymod.BeautyMod;
import com.evacoffee.beautymod.quest.QuestManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.literal;

public class AcceptCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("accept")
            .executes(AcceptCommand::execute));
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();
        
        // Check for pending quests
        QuestManager questManager = BeautyMod.getQuestManager();
        if (questManager.hasPendingQuest(player.getUuid())) {
            boolean accepted = questManager.acceptPendingQuest(player);
            if (accepted) {
                player.sendMessage(Text.literal("Quest accepted! Check your quest log with /quests")
                    .formatted(Formatting.GREEN), false);
                return Command.SINGLE_SUCCESS;
            }
        }
        
        // Check for pending proposals
        if (BeautyMod.hasPendingProposal(player)) {
            boolean accepted = BeautyMod.acceptProposal(player);
            if (accepted) {
                player.sendMessage(Text.literal("Proposal accepted! You are now in a relationship.")
                    .formatted(Formatting.LIGHT_PURPLE), false);
                return Command.SINGLE_SUCCESS;
            }
        }
        
        player.sendMessage(Text.literal("You don't have anything to accept right now.")
            .formatted(Formatting.RED), false);
        return 0;
    }
}