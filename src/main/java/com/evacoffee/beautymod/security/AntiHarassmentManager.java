package com.evacoffee.beautymod.security;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.*;

public class AntiHarassmentManager {
    private static final Map<UUID, Set<UUID>> blockLists = new HashMap<>();
    private static final Map<UUID, Long> lastInteraction = new HashMap<>();
    private static final long DEFAULT_COOLDOWN_MS = 30000; // 30 seconds

    public static boolean canInteract(ServerPlayerEntity source, ServerPlayerEntity target) {
        if (source.hasPermissionLevel(2)) return true;
        
        long now = System.currentTimeMillis();
        UUID sourceId = source.getUuid();
        UUID targetId = target.getUuid();
        
        // Check if target has blocked the source
        if (isBlocked(targetId, sourceId)) {
            source.sendMessage(Text.literal("This player is not accepting interactions.").formatted(Formatting.RED));
            return false;
        }
        
        // Check cooldown
        if (now - lastInteraction.getOrDefault(sourceId, 0L) < DEFAULT_COOLDOWN_MS) {
            source.sendMessage(Text.literal("Please wait before interacting again.").formatted(Formatting.RED));
            return false;
        }
        
        lastInteraction.put(sourceId, now);
        return true;
    }
    
    public static void blockPlayer(UUID player, UUID toBlock) {
        blockLists.computeIfAbsent(player, k -> new HashSet<>()).add(toBlock);
    }
    
    public static void unblockPlayer(UUID player, UUID toUnblock) {
        if (blockLists.containsKey(player)) {
            blockLists.get(player).remove(toUnblock);
        }
    }
    
    public static boolean isBlocked(UUID player, UUID potentialBlocker) {
        return blockLists.getOrDefault(potentialBlocker, Collections.emptySet()).contains(player);
    }
}   