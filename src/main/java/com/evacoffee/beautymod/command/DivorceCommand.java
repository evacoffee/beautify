package com.evacoffee.beautymod.command;

import com.evacoffee.beautymod.BeautyMod;
import com.evacoffee.beautymod.marriage.MarriageComponentInitializer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.literal;

public class DivorceCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("divorce")
            .requires(source -> source.hasPermissionLevel(0))
            .executes(DivorceCommand::execute));
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();
        
        // Check if player is married
        if (!MarriageComponentInitializer.getMarriage(player).isMarried()) {
            source.sendError(Text.literal("You're not married!"));
            return 0;
        }
        
        // Check if holding a diamond (divorce papers)
        ItemStack heldItem = player.getMainHandStack();
        if (heldItem.isEmpty() || heldItem.getItem() != Items.PAPER) {
            source.sendError(Text.literal("You need to hold paper to sign the divorce papers!"));
            return 0;
        }
        
        // Get spouse info before divorce
        UUID spouseUuid = MarriageComponentInitializer.getMarriage(player).getSpouseUuid();
        String spouseName = "your spouse";
        
        // Get spouse player if online
        ServerPlayerEntity spouse = player.getServer().getPlayerManager().getPlayer(spouseUuid);
        if (spouse != null) {
            spouseName = spouse.getName().getString();
        }
        
        // Perform divorce
        MarriageComponentInitializer.divorce(player);
        
        // Notify players
        player.sendMessage(Text.literal("You are now divorced from " + spouseName + "...")
            .formatted(Formatting.RED), false);
            
        if (spouse != null) {
            spouse.sendMessage(Text.literal(player.getName().getString() + " has divorced you...")
                .formatted(Formatting.RED), false);
        }
        
        // Consume the paper if not in creative mode
        if (!player.isCreative()) {
            heldItem.decrement(1);
        }
        
        // Set cooldown to prevent spam
        BeautyMod.getMarriageManager().setCooldown(player);
        
        return Command.SINGLE_SUCCESS;
    }
}
