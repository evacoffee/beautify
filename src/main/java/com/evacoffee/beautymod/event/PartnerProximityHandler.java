package com.evacoffee.beautymod.event;

import com.evacoffee.beautymod.BeautyMod;
import com.evacoffee.beautymod.util.SoundUtils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles automatic sound effects when players are near their partners.
 */

public class PartnerProximityHandler {
    private static final int CHECK_INTERVAL = 20; // Check every second (20 ticks)
    private static final double HEARTBEAT_DISTANCE = 8.0; // Blocks
    private static final int HEARTBEAT_COOLDOWN = 100; // 5 seconds cooldown (in ticks)
    
    private static final Map<UUID, Integer> cooldowns = new HashMap<>();

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world.getServer().getTicks() % CHECK_INTERVAL != 0) return;
            
            for (ServerPlayerEntity player : world.getPlayers()) {
                // Get partner UUID from your relationship system
                UUID partnerUuid = getPartnerUuid(player.getUuid());
                if (partnerUuid == null) continue;
                
                ServerPlayerEntity partner = world.getServer().getPlayerManager().getPlayer(partnerUuid);
                if (partner == null || !partner.isAlive()) continue;
                
                // Check distance
                double distance = player.getPos().distanceTo(partner.getPos());
                if (distance <= HEARTBEAT_DISTANCE) {
                    // Check cooldown
                    int cooldown = cooldowns.getOrDefault(player.getUuid(), 0);
                    if (cooldown <= 0) {
                        // Play heartbeat sound for both players
                        SoundUtils.playHeartbeat(player);
                        SoundUtils.playHeartbeat(partner);
                        
                        // Set cooldown
                        cooldowns.put(player.getUuid(), HEARTBEAT_COOLDOWN);
                        cooldowns.put(partnerUuid, HEARTBEAT_COOLDOWN);
                    }
                }
            }
            
            // Update cooldowns
            cooldowns.replaceAll((uuid, cd) -> Math.max(0, cd - CHECK_INTERVAL));
        });
    }
    
    // Replace this with your actual method to get partner UUID
    private static UUID getPartnerUuid(UUID playerUuid) {
        // Example: return RelationshipManager.getPartnerUuid(playerUuid);
        return null; // Implement this based on your relationship system
    }
}