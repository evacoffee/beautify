package com.evacoffee.beautymod.quest;

import com.evacoffee.beautymod.BeautyMod;
import com.evacoffee.beautymod.player.AffectionComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;

public class QuestManager {
    private final Map<Identifier, Quest> quests = new HashMap<>();
    private final Map<Identifier, QuestStatus> playerQuestStatus = new HashMap<>();
    private final PlayerEntity player;
    
    public QuestManager(PlayerEntity player) {
        this.player = player;
        registerDefaultQuests();
    }
    
    private void registerDefaultQuests() {
        // Farmer quests
        registerQuest(new FetchQuest(
            new Identifier(BeautyMod.MOD_ID, "gather_wheat"),
            "Wheat for the Baker",
            "Bring me 10 wheat for my bakery.",
            20,  // Requires 20 affection
            Items.WHEAT,
            10
        ));
        
        // Butcher quests
        registerQuest(new FetchQuest(
            new Identifier(BeautyMod.MOD_ID, "gather_meat"),
            "Meat for the Butcher",
            "Collect 5 pieces of cooked meat for the butcher.",
            30,
            Items.COOKED_BEEF,
            5
        ));
        
        // Fisherman quests
        registerQuest(new FetchQuest(
            new Identifier(BeautyMod.MOD_ID, "catch_fish"),
            "Fresh Catch",
            "Catch 3 fish for the fisherman.",
            25,
            Items.COD,
            3
        ));
        
        // Gift quests
        registerQuest(new GiftQuest(
            new Identifier(BeautyMod.MOD_ID, "gift_flowers"),
            "A Bouquet for Me?",
            "Bring me some beautiful flowers.",
            40,
            Items.POPPY,
            3,
            "Farmer"
        ));
        
        // Dating quests
        registerQuest(new DatingQuest(
            new Identifier(BeautyMod.MOD_ID, "romantic_walk"),
            "A Walk to Remember",
            "Take me on a romantic walk around the village.",
            60,
            100  // Walk 100 blocks
        ));
        
        // Dining quests
        registerQuest(new DiningQuest(
            new Identifier(BeautyMod.MOD_ID, "dinner_date"),
            "Dinner for Two",
            "Take me out for a nice dinner.",
            50,
            Items.COOKED_BEEF,
            "The Rusty Spoon Tavern"
        ));
    }
    
    public void registerQuest(Quest quest) {
        quests.put(quest.getId(), quest);
    }
    
    public void updateQuests() {
        if (player == null || player.getWorld().isClient) return;
        
        for (Quest quest : quests.values()) {
            if (quest.getStatus() == Quest.QuestStatus.IN_PROGRESS && quest.checkCompletion(player)) {
                quest.onComplete(player);
                // Reward the player
                AffectionComponent affection = AffectionComponent.get(player);
                affection.addAffection("farmer", 25); // Add affection when completing a quest
                player.sendMessage(Text.of("§6+25 Affection with Farmer"), false);
            }
        }
    }
    
    public void startQuest(Identifier questId) {
        Quest quest = quests.get(questId);
        if (quest != null && quest.getStatus() == Quest.QuestStatus.NOT_STARTED) {
            // Check affection requirement
            AffectionComponent affection = AffectionComponent.get(player);
            if (affection.getAffection("farmer") >= quest.getRequiredAffection()) {
                quest.start(player);
            } else {
                player.sendMessage(Text.of("§cYou need more affection with this villager to start this quest!"), false);
            }
        }
    }
    
    public List<Quest> getActiveQuests() {
        List<Quest> activeQuests = new ArrayList<>();
        for (Quest quest : quests.values()) {
            if (quest.getStatus() == Quest.QuestStatus.IN_PROGRESS) {
                activeQuests.add(quest);
            }
        }
        return activeQuests;
    }
    
    public void readFromNbt(NbtCompound nbt) {
        NbtList questsList = nbt.getList("Quests", NbtElement.COMPOUND_TYPE);
        for (NbtElement element : questsList) {
            NbtCompound questTag = (NbtCompound) element;
            Identifier id = new Identifier(questTag.getString("Id"));
            Quest.QuestStatus status = Quest.QuestStatus.valueOf(questTag.getString("Status"));
            
            Quest quest = quests.get(id);
            if (quest != null) {
                // We can't directly set the status, but we can recreate the quest with the correct status
                if (status == Quest.QuestStatus.IN_PROGRESS) {
                    quest.start(player);
                } else if (status == Quest.QuestStatus.COMPLETED) {
                    // For completed quests, we just mark them as completed
                    quests.put(id, new CompletedQuestDecorator(quest));
                }
            }
        }
    }
    
    public void writeToNbt(NbtCompound nbt) {
        NbtList questsList = new NbtList();
        for (Map.Entry<Identifier, Quest> entry : quests.entrySet()) {
            if (entry.getValue().getStatus() != Quest.QuestStatus.NOT_STARTED) {
                NbtCompound questTag = new NbtCompound();
                questTag.putString("Id", entry.getKey().toString());
                questTag.putString("Status", entry.getValue().getStatus().name());
                questsList.add(questTag);
            }
        }
        nbt.put("Quests", questsList);
    }
    
    // Helper class to mark quests as completed in the quest list
    private static class CompletedQuestDecorator extends Quest {
        private final Quest wrapped;
        
        public CompletedQuestDecorator(Quest quest) {
            super(quest.id, quest.title, quest.description, quest.requiredAffection);
            this.wrapped = quest;
            this.status = QuestStatus.COMPLETED;
        }
        
        @Override
        public boolean checkCompletion(PlayerEntity player) {
            return true;
        }
        
        @Override
        public String getDescription() {
            return "§m" + wrapped.getDescription() + "§r\n§aCOMPLETED";
        }
    }
}
