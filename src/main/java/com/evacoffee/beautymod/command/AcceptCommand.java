package com.evacoffee.beautymod.command;

import com.evacoffee.beautymod.BeautyMod;
import com.evacoffee.beautymod.marriage.MarriageComponentInitializer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class AcceptCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("accept")
            .then(argument("player", EntityArgumentType.player())
                .executes(AcceptCommand::execute)));
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity accepter = source.getPlayerOrThrow();
        ServerPlayerEntity proposer = EntityArgumentType.getPlayer(context, "player");
        
        // Check if there's a pending proposal
        if (!BeautyMod.getMarriageManager().hasPendingProposal(proposer, accepter)) {
            source.sendError(Text.literal("No pending proposal from " + proposer.getName().getString()));
            return 0;
        }
        
        // Get current day (or use world time)
        long weddingDay = accepter.getWorld().getTimeOfDay() / 24000L;
        
        // Perform marriage
        boolean success = MarriageComponentInitializer.marry(proposer, accepter, weddingDay);
        
        if (success) {
            // Clear any pending proposals
            BeautyMod.getMarriageManager().clearProposal(proposer);
            
            // Announce marriage
            String message = String.format("§d§l❤ %s and %s are now married! ❤", 
                proposer.getName().getString(), 
                accepter.getName().getString());
            
            // Send message to all players
            for (ServerPlayerEntity player : accepter.getServer().getPlayerManager().getPlayerList()) {
                player.sendMessage(Text.literal(message), false);
            }
            
            // Give wedding rings
            giveWeddingRing(proposer, accepter.getName().getString());
            giveWeddingRing(accepter, proposer.getName().getString());
            
            return Command.SINGLE_SUCCESS;
        } else {
            source.sendError(Text.literal("Could not complete the marriage. One of you might already be married."));
            return 0;
        }
    }
    
    private static void giveWeddingRing(ServerPlayerEntity player, String spouseName) {
        ItemStack ring = new ItemStack(Items.GOLD_NUGGET);
        
        // Create custom name and lore
        ring.setCustomName(Text.literal("Wedding Ring")
            .formatted(Formatting.GOLD)
            .formatted(Formatting.BOLD));
        
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal("A symbol of eternal love").formatted(Formatting.GRAY));
        lore.add(Text.literal("Spouse: " + spouseName + "").formatted(Formatting.LIGHT_PURPLE));
        
        NbtCompound nbt = ring.getOrCreateNbt();
        NbtList loreList = new NbtList();
        for (Text line : lore) {
            loreList.add(NbtString.of(Text.Serializer.toJson(line)));
        }
        
        NbtCompound display = new NbtCompound();
        display.put("Lore", loreList);
        nbt.put("display", display);
        nbt.putBoolean("Unbreakable", true);
        
        // Add to inventory or drop if full
        if (!player.getInventory().insertStack(ring)) {
            player.dropItem(ring, false);
        }
    }
}
