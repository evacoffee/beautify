package com.evacoffee.beautymod.security;

import com.evacoffee.beautymod.BeautyMod;
import com.evacoffee.beautymod.util.ModLogger;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AntiCheat {
    private static final Map<UUID, PlayerData> playerData = new HashMap<>();
    private static final int MAX_ACTIONS_PER_SECOND = 20; // Adjust based on your needs
    
    public static void onPlayerAction(ServerPlayerEntity player, ActionType type) {
        UUID playerId = player.getUuid();
        PlayerData data = playerData.computeIfAbsent(playerId, k -> new PlayerData());
        
        long currentTime = System.currentTimeMillis();
        data.actionCounts.merge(type, 1, Integer::sum);
        
        // Reset counters every second
        if (currentTime - data.lastResetTime > 1000) {
            data.actionCounts.clear();
            data.lastResetTime = currentTime;
        }
        
        // Check for suspicious activity
        if (data.actionCounts.getOrDefault(type, 0) > MAX_ACTIONS_PER_SECOND) {
            handleSuspiciousActivity(player, type);
        }
    }
    
    private static void handleSuspiciousActivity(ServerPlayerEntity player, ActionType type) {
        ModLogger.warn("Suspicious activity detected from {}: {}", player.getName().getString(), type);
        // You can implement actions like kicking the player, sending a warning, etc.
    }
    
    public enum ActionType {
        MARRIAGE_REQUEST,
        GIFT_SENT,
        COMMAND_USED
    }
    
    private static class PlayerData {
        Map<ActionType, Integer> actionCounts = new HashMap<>();
        long lastResetTime = System.currentTimeMillis();
    }
}