package com.evacoffee.beautymod.memory;

import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;

public class VillagerMemory {
    private final Map<String, List<Memory>> memories = new HashMap<>();
    private final Map<String, Set<Identifier>> completedQuests = new HashMap<>();
    private final Map<String, Set<Item>> giftPreferences = new HashMap<>();
    private final Map<String, Set<String>> dateHistory = new HashMap<>();
    
    public void addMemory(String villagerName, Memory memory) {
        memories.computeIfAbsent(villagerName, k -> new ArrayList<>()).add(memory);
    }
    
    public List<Memory> getMemories(String villagerName) {
        return memories.getOrDefault(villagerName, Collections.emptyList());
    }
    
    public void addCompletedQuest(String villagerName, Identifier questId) {
        completedQuests.computeIfAbsent(villagerName, k -> new HashSet<>()).add(questId);
        addMemory(villagerName, new Memory(MemoryType.QUEST, "Completed quest: " + questId.getPath()));
    }
    
    public boolean hasCompletedQuest(String villagerName, Identifier questId) {
        return completedQuests.getOrDefault(villagerName, Collections.emptySet()).contains(questId);
    }
    
    public void addGiftPreference(String villagerName, Item item) {
        giftPreferences.computeIfAbsent(villagerName, k -> new HashSet<>()).add(item);
        addMemory(villagerName, new Memory(MemoryType.GIFT, "Likes receiving " + 
            Registries.ITEM.getId(item).getPath()));
    }
    
    public Set<Item> getGiftPreferences(String villagerName) {
        return giftPreferences.getOrDefault(villagerName, Collections.emptySet());
    }
    
    public void addDate(String villagerName, String dateActivity) {
        dateHistory.computeIfAbsent(villagerName, k -> new HashSet<>()).add(dateActivity);
        addMemory(villagerName, new Memory(MemoryType.DATE, "Went on a date: " + dateActivity));
    }
    
    public Set<String> getDateHistory(String villagerName) {
        return dateHistory.getOrDefault(villagerName, Collections.emptySet());
    }
    
    public void readFromNbt(NbtCompound nbt) {
        // Read memories
        NbtCompound memoriesNbt = nbt.getCompound("memories");
        for (String villagerName : memoriesNbt.getKeys()) {
            NbtList memoryList = memoriesNbt.getList(villagerName, NbtElement.STRING_TYPE);
            List<Memory> memoryObjects = new ArrayList<>();
            for (NbtElement element : memoryList) {
                String[] parts = element.asString().split("\|", 3);
                if (parts.length == 3) {
                    try {
                        MemoryType type = MemoryType.valueOf(parts[0]);
                        long timestamp = Long.parseLong(parts[1]);
                        String text = parts[2];
                        memoryObjects.add(new Memory(type, text, timestamp));
                    } catch (IllegalArgumentException e) {
                        // Skip invalid entries
                    }
                }
            }
            memories.put(villagerName, memoryObjects);
        }
        
        // Read gift preferences
        NbtCompound giftsNbt = nbt.getCompound("giftPreferences");
        for (String villagerName : giftsNbt.getKeys()) {
            NbtList itemList = giftsNbt.getList(villagerName, NbtElement.STRING_TYPE);
            Set<Item> items = new HashSet<>();
            for (NbtElement element : itemList) {
                Item item = Registries.ITEM.get(new Identifier(element.asString()));
                if (item != Items.AIR) {
                    items.add(item);
                }
            }
            if (!items.isEmpty()) {
                giftPreferences.put(villagerName, items);
            }
        }
    }
    
    public void writeToNbt(NbtCompound nbt) {
        // Write memories
        NbtCompound memoriesNbt = new NbtCompound();
        for (Map.Entry<String, List<Memory>> entry : memories.entrySet()) {
            NbtList memoryList = new NbtList();
            for (Memory memory : entry.getValue()) {
                memoryList.add(NbtString.of(String.format("%s|%d|%s", 
                    memory.getType(), 
                    memory.getTimestamp(), 
                    memory.getMemoryText())));
            }
            memoriesNbt.put(entry.getKey(), memoryList);
        }
        nbt.put("memories", memoriesNbt);
        
        // Write gift preferences
        NbtCompound giftsNbt = new NbtCompound();
        for (Map.Entry<String, Set<Item>> entry : giftPreferences.entrySet()) {
            NbtList itemList = new NbtList();
            for (Item item : entry.getValue()) {
                itemList.add(NbtString.of(Registries.ITEM.getId(item).toString()));
            }
            giftsNbt.put(entry.getKey(), itemList);
        }
        nbt.put("giftPreferences", giftsNbt);
    }
    
    public static class Memory {
        private final long timestamp;
        private final String memoryText;
        private final MemoryType type;
        
        public Memory(MemoryType type, String memoryText) {
            this(type, memoryText, System.currentTimeMillis());
        }
        
        public Memory(MemoryType type, String memoryText, long timestamp) {
            this.timestamp = timestamp;
            this.type = type;
            this.memoryText = memoryText;
        }
        
        public Text toText() {
            return Text.of(String.format("[%tF %<tR] %s: %s", 
                new Date(timestamp),
                type.getDisplayName(),
                memoryText));
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public String getMemoryText() {
            return memoryText;
        }
        
        public MemoryType getType() {
            return type;
        }
    }
    
    public enum MemoryType {
        GIFT("Gift"),
        QUEST("Quest"),
        DATE("Date"),
        SPECIAL_MOMENT("Special Moment");
        
        private final String displayName;
        
        MemoryType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
