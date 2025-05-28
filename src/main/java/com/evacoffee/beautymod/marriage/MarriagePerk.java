package com.evacoffee.beautymod.marriage;

import net.minecraft.text.Text;

public enum MarriagePerk {
    SHARED_INVENTORY("Shared Inventory", 1, "Allows sharing items with your spouse"),
    HOME_TELEPORT("Home Teleport", 5, "Teleport to your shared home"),
    SPOUSE_TELEPORT("Spouse Teleport", 10, "Teleport to your spouse"),
    SHARED_XP("Shared XP", 15, "Earn bonus XP when near your spouse"),
    LUCKY_CHARM("Lucky Charm", 20, "Increased luck when together"),
    SOUL_BOND("Soul Bond", 25, "Share a portion of your health with your spouse"),
    TELEPATHY("Telepathy", 30, "Send private messages to your spouse from anywhere"),
    COMBAT_LINK("Combat Link", 35, "Share damage with your spouse when nearby"),
    ETERNAL_BOND("Eternal Bond", 50, "Permanent effects when both spouses are online");

    private final String displayName;
    private final int levelRequired;
    private final String description;

    MarriagePerk(String displayName, int levelRequired, String description) {
        this.displayName = displayName;
        this.levelRequired = levelRequired;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Text getDisplayText() {
        return Text.translatable("perk.beautymod.marriage." + name().toLowerCase());
    }

    public int getLevelRequired() {
        return levelRequired;
    }

    public String getDescription() {
        return description;
    }

    public Text getDescriptionText() {
        return Text.translatable("perk.beautymod.marriage." + name().toLowerCase() + ".desc");
    }

    public static MarriagePerk fromString(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}