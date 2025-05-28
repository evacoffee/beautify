package com.evacoffee.beautymod.marriage.quest;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.UUID;

public class MarriageQuest {
    public enum QuestType {
        SPEND_TIME_TOGETHER("Spend Time Together", "Spend 1 hour within 50 blocks of your spouse"),
        KILL_MOBS_TOGETHER("Slay Monsters", "Kill 50 mobs while near your spouse"),
        GATHER_RESOURCES("Gather Resources", "Collect 64 of any resource while married"),
        EXPLORE_TOGETHER("Explore Together", "Discover 5 new biomes with your spouse");

        private final String name;
        private final String description;

        QuestType(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public Text getDisplayName() {
            return Text.literal(name);
        }

        public Text getDescription() {
            return Text.literal(description);
        }
    }

    private final String id;
    private final String title;
    private final String description;
    private final QuestType type;
    private final int targetAmount;
    private int progress;
    private final ItemStack reward;
    private final long expiryTime;
    private final UUID player1;
    private final UUID player2;
    private boolean completed;

    public MarriageQuest(String id, String title, String description, QuestType type, 
                        int targetAmount, ItemStack reward, int expiryHours, 
                        ServerPlayerEntity player1, ServerPlayerEntity player2) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.targetAmount = targetAmount;
        this.reward = reward;
        this.expiryTime = System.currentTimeMillis() + (expiryHours * 3600 * 1000L);
        this.player1 = player1.getUuid();
        this.player2 = player2.getUuid();
        this.progress = 0;
        this.completed = false;
    }

    public void updateProgress(int amount, ServerPlayerEntity player, ServerPlayerEntity spouse) {
        if (completed) return;
        
        progress += amount;
        if (progress >= targetAmount) {
            complete(player, spouse);
        }
    }

    private void complete(ServerPlayerEntity player, ServerPlayerEntity spouse) {
        completed = true;
        int xpReward = 100; // Base XP reward
        
        // Notify players
        player.sendMessage(Text.literal("§aQuest Complete: " + type.name + "!"), false);
        spouse.sendMessage(Text.literal("§aQuest Complete: " + type.name + "!"), false);
        
        // Reward XP to marriage
        // This assumes you have a way to access the marriage component
        // You'll need to implement this part based on your component system
        // marriageComponent.addMarriageXP(xpReward);
        
        // Give item rewards
        if (!reward.isEmpty()) {
            player.giveItemStack(reward.copy());
            spouse.giveItemStack(reward.copy());
        }
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }

    public Text getProgressText() {
        return Text.literal(type.name + ": " + progress + "/" + targetAmount + 
               (completed ? " §aCOMPLETED" : ""));
    }

    public String getId() {
        return id;
    }

    public QuestType getType() {
        return type;
    }

    public int getProgress() {
        return progress;
    }

    public int getTargetAmount() {
        return targetAmount;
    }
}