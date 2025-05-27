package com.evacoffee.beautymod.player;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.nbt.NbtCompound;

import java.util.HashMap;
import java.util.Map;

public class AffectionComponent implements Component {

    // Stores affection values per NPC
    private final Map<String, Integer> affectionMap = new HashMap<>();

    // Sets affection for a specific NPC
    public void setAffection(String npcName, int value) {
        affectionMap.put(npcName.toLowerCase(), value);
    }

    // Gets affection for a specific NPC
    public int getAffection(String npcName) {
        return affectionMap.getOrDefault(npcName.toLowerCase(), 0);
    }

    // Adds affection points for a specific NPC
    public void addAffection(String npcName, int amount) {
        String key = npcName.toLowerCase();
        int current = affectionMap.getOrDefault(key, 0);
        affectionMap.put(key, current + amount);
    }

    // Read data from NBT to keep it persistent
    @Override
    public void readFromNbt(NbtCompound nbt) {
        affectionMap.clear();
        for (String key : nbt.getKeys()) {
            affectionMap.put(key, nbt.getInt(key));
        }
    }

    // Write affection data to NBT to save it
    @Override
    public void writeToNbt(NbtCompound nbt) {
        for (Map.Entry<String, Integer> entry : affectionMap.entrySet()) {
            nbt.putInt(entry.getKey(), entry.getValue());
        }
    }
}
