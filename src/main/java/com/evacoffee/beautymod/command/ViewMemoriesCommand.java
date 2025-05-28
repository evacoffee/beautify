package com.evacoffee.beautymod.command;

import com.evacoffee.beautymod.memory.VillagerMemory;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.stream.Collectors;

public class ViewMemoriesCommand implements Command<ServerCommandSource> {
    
    public static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();
        
        // Get the villager the player is looking at
        VillagerEntity villager = (VillagerEntity) EntityArgumentType.getEntity(context, "villager");
        String villagerName = villager.getName().getString();
        
        // Get the memory component
        VillagerMemory memory = PlayerComponentInitializer.getVillagerMemory(player);
        
        // Display memories
        List<VillagerMemory.Memory> memories = memory.getMemories(villagerName);
        if (memories.isEmpty()) {
            player.sendMessage(Text.literal("You don't have any memories with " + villagerName + " yet.")
                .formatted(Formatting.ITALIC, Formatting.GRAY), false);
        } else {
            player.sendMessage(Text.literal("=== Memories with " + villagerName + " ===")
                .formatted(Formatting.GOLD, Formatting.BOLD), false);
            
            // Sort by most recent first
            memories.stream()
                .sorted((m1, m2) -> Long.compare(m2.getTimestamp(), m1.getTimestamp()))
                .limit(10) // Show only the 10 most recent memories
                .forEach(m -> player.sendMessage(m.toText().copy()
                    .formatted(Formatting.GRAY), false));
        }
        
        // Display gift preferences
        Set<ItemStack> preferredGifts = memory.getGiftPreferences(villagerName).stream()
            .map(ItemStack::new)
            .collect(Collectors.toSet());
            
        if (!preferredGifts.isEmpty()) {
            player.sendMessage(Text.literal("\n" + villagerName + " likes receiving:")
                .formatted(Formatting.GOLD), false);
                
            preferredGifts.forEach(gift -> 
                player.sendMessage(Text.literal("- ").append(gift.getName())
                    .formatted(Formatting.GRAY), false));
        }
        
        return Command.SINGLE_SUCCESS;
    }
}
