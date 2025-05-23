package com.evacoffee.beautymod.love;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.HashMap;
import java.util.UUID;

public class LoveData {
    private static final HashMap<UUID, Integer> loveLevels = new HashMap<>();

    public static int getLove(ServerPlayerEntity player) {
        return loveLevels.getOrDefault(player.getUuid(), 0);
    }

    public static void addLove(ServerPlayerEntity player, int amount) {
        UUID id = player.getUuid();
        loveLevels.put(id, getLove(player) + amount);
    }

    public static void saveData(ServerPlayerEntity player, NbtCompound tag) {
        tag.putInt("LoveLevel", getLove(player));
    }

    public static void loadData(ServerPlayerEntity player, NbtCompound tag) {
        UUID id = player.getUuid();
        loveLevels.put(id, tag.getInt("LoveLevel"));
    }
}
