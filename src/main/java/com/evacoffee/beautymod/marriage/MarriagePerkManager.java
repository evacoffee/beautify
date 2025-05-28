package com.evacoffee.beautymod.marriage;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.EnumMap;
import java.util.Map;

public class MarriagePerkManager {
    private static final Map<MarriagePerk, Integer> PERK_LEVELS = new EnumMap<>(MarriagePerk.class);
    
    static {
        // Initialize perk levels
        PERK_LEVELS.put(MarriagePerk.SHARED_INVENTORY, 1);
        PERK_LEVELS.put(MarriagePerk.HOME_TELEPORT, 5);
        PERK_LEVELS.put(MarriagePerk.SPOUSE_TELEPORT, 10);
        PERK_LEVELS.put(MarriagePerk.SHARED_XP, 15);
        PERK_LEVELS.put(MarriagePerk.LUCKY_CHARM, 20);
        PERK_LEVELS.put(MarriagePerk.SOUL_BOND, 25);
        PERK_LEVELS.put(MarriagePerk.TELEPATHY, 30);
        PERK_LEVELS.put(MarriagePerk.COMBAT_LINK, 35);
        PERK_LEVELS.put(MarriagePerk.ETERNAL_BOND, 50);
    }

    public static boolean canUnlock(MarriagePerk perk, int marriageLevel) {
        return marriageLevel >= PERK_LEVELS.getOrDefault(perk, Integer.MAX_VALUE);
    }

    public static void applyPerkEffects(ServerPlayerEntity player, MarriagePerk perk) {
        switch (perk) {
            case LUCKY_CHARM:
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.LUCK, 6000, 1, true, false));
                break;
            case SOUL_BOND:
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.REGENERATION, 100, 0, true, false));
                break;
            case COMBAT_LINK:
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.RESISTANCE, 100, 0, true, false));
                break;
            case ETERNAL_BOND:
                player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.ABSORPTION, 100, 0, true, false));
                break;
        }
    }

    public static int getRequiredLevel(MarriagePerk perk) {
        return PERK_LEVELS.getOrDefault(perk, Integer.MAX_VALUE);
    }

    public static String getPerkInfo(MarriagePerk perk) {
        return String.format("%s (Level %d): %s", 
            perk.getDisplayName(), 
            getRequiredLevel(perk), 
            perk.getDescription());
    }
}