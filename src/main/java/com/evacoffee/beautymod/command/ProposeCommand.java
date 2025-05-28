package com.evacoffee.beautymod.command;

import com.evacoffee.beautymod.BeautyMod;
import com.evacoffee.beautymod.marriage.MarriageComponentInitializer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ProposeCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("propose")
            .then(argument("player", EntityArgumentType.player())
                .executes(ProposeCommand::execute)));
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity proposer = source.getPlayerOrThrow();
        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
        
        // Check if already married
        if (MarriageComponentInitializer.getMarriage(proposer).isMarried()) {
            source.sendError(Text.literal("You are already married!"));
            return 0;
        }
        
        // Check if target is already married
        if (MarriageComponentInitializer.getMarriage(target).isMarried()) {
            source.sendError(Text.literal(target.getName().getString() + " is already married!"));
            return 0;
        }
        
        // Check if holding a ring
        ItemStack heldItem = proposer.getMainHandStack();
        if (heldItem.isEmpty() || heldItem.getItem() != Items.GOLDEN_APPLE) {
            source.sendError(Text.literal("You need to hold a Golden Apple to propose!"));
            return 0;
        }
        
        // Check if target is online
        if (!target.isAlive()) {
            source.sendError(Text.literal("Your beloved is not available right now"));
            return 0;
        }
        
        // Check distance
        if (proposer.squaredDistanceTo(target) > 64) {
            source.sendError(Text.literal("You're too far away to propose!"));
            return 0;
        }
        
        // Create marriage request
        boolean success = BeautyMod.getMarriageManager().createProposal(proposer, target);
        
        if (success) {
            // Notify players
            proposer.sendMessage(Text.literal("You proposed to " + target.getName().getString() + "!").formatted(Formatting.LIGHT_PURPLE), false);
            target.sendMessage(Text.literal("\u00A7d" + proposer.getName().getString() + " has proposed to you!")
                .append(Text.literal("\n\u00A7a[/accept " + proposer.getName().getString() + "]").formatted(Formatting.GREEN)
                .styled(style -> style.withClickEvent(new net.minecraft.text.ClickEvent(
                    net.minecraft.text.ClickEvent.Action.RUN_COMMAND, 
                    "/accept " + proposer.getName().getString()
                )))), false);
            
            // Consume the ring
            if (!proposer.isCreative()) {
                heldItem.decrement(1);
            }
            
            return Command.SINGLE_SUCCESS;
        } else {
            source.sendError(Text.literal("Could not create marriage proposal"));
            return 0;
        }
    }
}
