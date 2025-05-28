package com.evacoffee.beautymod.command;

import com.evacoffee.beautymod.BeautyMod;
import com.evacoffee.beautymod.memory.VillagerMemory;
import com.evacoffee.beautymod.player.PlayerComponentInitializer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class MemoriesCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("memories")
            .then(argument("villager", EntityArgumentType.entity())
                .executes(MemoriesCommand::viewMemories)));
                
        dispatcher.register(literal("memories")
            .then(literal("list")
                .executes(MemoriesCommand::listVillagers)));
    }
    
    private static int viewMemories(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();
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
            
            // Sort by most recent first and limit to 10
            memories.stream()
                .sorted(Comparator.comparingLong(VillagerMemory.Memory::getTimestamp).reversed())
                .limit(10)
                .forEach(m -> player.sendMessage(m.toText().copy()
                    .formatted(Formatting.GRAY), false));
        }
        
        // Display gift preferences
        var giftPreferences = memory.getGiftPreferences(villagerName);
        if (!giftPreferences.isEmpty()) {
            player.sendMessage(Text.literal("\n" + villagerName + " likes receiving:")
                .formatted(Formatting.GOLD), false);
                
            giftPreferences.stream()
                .map(ItemStack::new)
                .forEach(gift -> player.sendMessage(
                    Text.literal("- ").append(gift.getName())
                        .formatted(Formatting.GRAY), false));
        }
        
        return Command.SINGLE_SUCCESS;
    }
    
    private static int listVillagers(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();
        
        // Get all villagers with memories
        VillagerMemory memory = PlayerComponentInitializer.getVillagerMemory(player);
        
        if (memory.getMemories().isEmpty()) {
            player.sendMessage(Text.literal("You haven't met any villagers yet!")
                .formatted(Formatting.ITALIC, Formatting.GRAY), false);
            return 0;
        }
        
        player.sendMessage(Text.literal("=== Your Villager Friends ===")
            .formatted(Formatting.GOLD, Formatting.BOLD), false);
            
        memory.getMemories().entrySet().stream()
            .sorted((e1, e2) -> {
                // Sort by most recent memory
                long time1 = e1.getValue().stream()
                    .mapToLong(VillagerMemory.Memory::getTimestamp)
                    .max().orElse(0);
                long time2 = e2.getValue().stream()
                    .mapToLong(VillagerMemory.Memory::getTimestamp)
                    .max().orElse(0);
                return Long.compare(time2, time1);
            })
            .forEach(entry -> {
                String name = entry.getKey();
                int memoryCount = entry.getValue().size();
                String lastInteraction = "";
                
                if (!entry.getValue().isEmpty()) {
                    long lastTime = entry.getValue().stream()
                        .mapToLong(VillagerMemory.Memory::getTimestamp)
                        .max()
                        .orElse(0);
                    lastInteraction = " (last seen: " + formatTimeAgo(System.currentTimeMillis() - lastTime) + " ago)";
                }
                
                player.sendMessage(Text.literal("- " + name + ": " + memoryCount + " memory" + 
                    (memoryCount != 1 ? "s" : "") + lastInteraction)
                    .formatted(Formatting.WHITE), false);
            });
            
        return Command.SINGLE_SUCCESS;
    }
    
    private static String formatTimeAgo(long millis) {
        long seconds = millis / 1000;
        if (seconds < 60) return seconds + " second" + (seconds != 1 ? "s" : "");
        
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + " minute" + (minutes != 1 ? "s" : "");
        
        long hours = minutes / 60;
        if (hours < 24) return hours + " hour" + (hours != 1 ? "s" : "");
        
        long days = hours / 24;
        return days + " day" + (days != 1 ? "s" : "");
    }
}
