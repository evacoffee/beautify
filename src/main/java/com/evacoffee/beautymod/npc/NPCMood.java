package com.evacoffee.beautymod.npc;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NPCMood {
    private static final float MIN_MOOD = 0f;
    private static final float MAX_MOOD = 1f;
    
    private float mood = 0.5f; // Start neutral
    private final Map<UUID, Float> playerAffinity = new HashMap<>();
    
    public float getMood(PlayerEntity player) {
        float baseMood = mood;
        if (player != null) {
            baseMood += playerAffinity.getOrDefault(player.getUuid(), 0f);
        }
        return MathHelper.clamp(baseMood, MIN_MOOD, MAX_MOOD);
    }
    
    public void onGift(PlayerEntity player) {
        adjustMood(0.2f, player);
    }
    
    public void onCompliment(PlayerEntity player) {
        adjustMood(0.1f, player);
    }
    
    public void onInsult(PlayerEntity player) {
        adjustMood(-0.2f, player);
    }
    
    private void adjustMood(float amount, PlayerEntity player) {
        mood = MathHelper.clamp(mood + amount, MIN_MOOD, MAX_MOOD);
        if (player != null) {
            UUID playerId = player.getUuid();
            float newAffinity = playerAffinity.getOrDefault(playerId, 0f) + (amount * 0.5f);
            playerAffinity.put(playerId, MathHelper.clamp(newAffinity, -0.5f, 0.5f));
        }
    }
    
    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putFloat("mood", mood);
        
        NbtCompound affinities = new NbtCompound();
        playerAffinity.forEach((id, value) -> 
            affinities.putFloat(id.toString(), value));
        nbt.put("affinities", affinities);
        
        return nbt;
    }
    
    public void fromNbt(NbtCompound nbt) {
        mood = nbt.getFloat("mood");
        
        NbtCompound affinities = nbt.getCompound("affinities");
        for (String key : affinities.getKeys()) {
            playerAffinity.put(UUID.fromString(key), affinities.getFloat(key));
        }
    }
}