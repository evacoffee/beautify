package com.evacoffee.beautymod.marriage;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MarriageManager {
    private static final Map<UUID, UUID> MARRIAGES = new HashMap<>();
    private static final Map<UUID, BlockPos> MARRIAGE_HOMES = new HashMap<>();
    private static final Map<UUID, World> MARRIAGE_WORLDS = new HashMap<>();
    private static final Map<UUID, UUID> MARRIAGE_PROPOSALS = new HashMap<>();
    private static final Map<UUID, Long> LAST_TELEPORT_REQUESTS = new HashMap<>();

    public static boolean marry(ServerPlayerEntity player1, ServerPlayerEntity player2) {
        UUID uuid1 = player1.getUuid();
        UUID uuid2 = player2.getUuid();
        
        if (isMarried(uuid1) || isMarried(uuid2)) {
            return false;
        }

        MARRIAGES.put(uuid1, uuid2);
        MARRIAGES.put(uuid2, uuid1);
        return true;
    }

    public static boolean divorce(UUID playerUuid) {
        UUID spouseUuid = MARRIAGES.get(playerUuid);
        if (spouseUuid != null) {
            MARRIAGES.remove(playerUuid);
            MARRIAGES.remove(spouseUuid);
            MARRIAGE_HOMES.remove(playerUuid);
            MARRIAGE_HOMES.remove(spouseUuid);
            return true;
        }
        return false;
    }

    public static boolean isMarried(UUID playerUuid) {
        return MARRIAGES.containsKey(playerUuid);
    }

    public static UUID getSpouse(UUID playerUuid) {
        return MARRIAGES.get(playerUuid);
    }

    public static void setHome(ServerPlayerEntity player) {
        MARRIAGE_HOMES.put(player.getUuid(), player.getBlockPos());
        MARRIAGE_WORLDS.put(player.getUuid(), player.getWorld());
    }

    public static BlockPos getHomePosition(UUID playerUuid) {
        return MARRIAGE_HOMES.get(playerUuid);
    }

    public static World getHomeWorld(UUID playerUuid) {
        return MARRIAGE_WORLDS.get(playerUuid);
    }

    public static void propose(ServerPlayerEntity proposer, ServerPlayerEntity target) {
        MARRIAGE_PROPOSALS.put(target.getUuid(), proposer.getUuid());
    }

    public static boolean hasPendingProposal(ServerPlayerEntity player) {
        return MARRIAGE_PROPOSALS.containsKey(player.getUuid());
    }

    public static UUID getProposer(ServerPlayerEntity player) {
        return MARRIAGE_PROPOSALS.get(player.getUuid());
    }

    public static void clearProposal(ServerPlayerEntity player) {
        MARRIAGE_PROPOSALS.remove(player.getUuid());
    }

    public static boolean canRequestTeleport(ServerPlayerEntity player) {
        Long lastRequest = LAST_TELEPORT_REQUESTS.get(player.getUuid());
        return lastRequest == null || (System.currentTimeMillis() - lastRequest) > 60000; // 1 minute cooldown
    }

    public static void setLastTeleportRequest(ServerPlayerEntity player) {
        LAST_TELEPORT_REQUESTS.put(player.getUuid(), System.currentTimeMillis());
    }
}