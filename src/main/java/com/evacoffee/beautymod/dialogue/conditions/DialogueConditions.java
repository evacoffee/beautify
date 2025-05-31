package com.evacoffee.beautymod.dialogue.conditions;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import java.util.function.Predicate;

public class DialogueConditions {
    // Item conditions
    public static Predicate<PlayerEntity> hasItem(Item item, int count) {
        return player -> {
            if (player == null || item == null) return false;
            return player.getInventory().count(item) >= count;
        };
    }

    public static Predicate<PlayerEntity> hasItem(Identifier itemId, int count) {
        return player -> {
            if (player == null || itemId == null) return false;
            Item item = Registry.ITEM.get(itemId);
            return item != null && player.getInventory().count(item) >= count;
        };
    }

    // Level conditions
    public static Predicate<PlayerEntity> hasExperienceLevel(int minLevel) {
        return player -> player != null && player.experienceLevel >= minLevel;
    }

    // Reputation conditions (requires your reputation system)
    public static Predicate<PlayerEntity> hasReputation(Identifier npcId, int minReputation) {
        return player -> {
            if (!(player instanceof ServerPlayerEntity)) return false;
            // Implement your reputation check here
            // Example: return ReputationSystem.getReputation((ServerPlayerEntity)player, npcId) >= minReputation;
            return true;
        };
    }

    // Quest conditions
    public static Predicate<PlayerEntity> hasCompletedQuest(String questId) {
        return player -> {
            if (!(player instanceof ServerPlayerEntity)) return false;
            // Implement quest completion check
            // Example: return QuestSystem.hasCompleted((ServerPlayerEntity)player, questId);
            return false;
        };
    }

    // Time-based conditions
    public static Predicate<PlayerEntity> isDayTime() {
        return player -> player != null && player.world.isDay();
    }

    public static Predicate<PlayerEntity> isNightTime() {
        return player -> player != null && player.world.isNight();
    }

    // Location conditions
    public static Predicate<PlayerEntity> isInDimension(Identifier dimensionId) {
        return player -> player != null && player.world.getRegistryKey().getValue().equals(dimensionId);
    }

    // Combined conditions
    @SafeVarargs
    public static Predicate<PlayerEntity> all(Predicate<PlayerEntity>... conditions) {
        return player -> {
            if (player == null) return false;
            for (Predicate<PlayerEntity> condition : conditions) {
                if (!condition.test(player)) return false;
            }
            return true;
        };
    }

    @SafeVarargs
    public static Predicate<PlayerEntity> any(Predicate<PlayerEntity>... conditions) {
        return player -> {
            if (player == null) return false;
            for (Predicate<PlayerEntity> condition : conditions) {
                if (condition.test(player)) return true;
            }
            return false;
        };
    }

    public static Predicate<PlayerEntity> not(Predicate<PlayerEntity> condition) {
        return player -> player != null && !condition.test(player);
    }
}