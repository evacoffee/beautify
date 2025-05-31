package com.evacoffee.beautymod.reputation;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import java.util.HashMap;
import java.util.Map;

public class ReputationComponent implements Component {
    private final Map<Identifier, Integer> reputationMap = new HashMap<>();
    private final Map<Identifier, Integer> affinityMap = new HashMap<>();
    
    public int getReputation(Identifier npcId) {
        return reputationMap.getOrDefault(npcId, 0);
    }
    
    public void addReputation(Identifier npcId, int amount) {
        int current = getReputation(npcId);
        reputationMap.put(npcId, Math.max(-100, Math.min(100, current + amount)));
    }
    
    public int getAffinity(Identifier npcId) {
        return affinityMap.getOrDefault(npcId, 0);
    }
    
    public void addAffinity(Identifier npcId, int amount) {
        int current = getAffinity(npcId);
        affinityMap.put(npcId, Math.max(0, Math.min(100, current + amount)));
    }
    
    public String getReputationLevel(Identifier npcId) {
        int rep = getReputation(npcId);
        if (rep <= -75) return "hated";
        if (rep <= -25) return "disliked";
        if (rep <= 25) return "neutral";
        if (rep <= 75) return "liked";
        return "loved";
    }
    
    @Override
    public void readFromNbt(NbtCompound tag) {
        // Implementation for reading from NBT
    }
    
    @Override
    public void writeToNbt(NbtCompound tag) {
        // Implementation for writing to NBT
    }
}