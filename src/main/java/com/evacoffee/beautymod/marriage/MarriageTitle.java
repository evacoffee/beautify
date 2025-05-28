// MarriageTitle.java
package com.evacoffee.beautymod.marriage;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class MarriageTitle {
    private final String title;
    private final int levelRequirement;
    private final Formatting color;

    public MarriageTitle(String title, int levelRequirement, Formatting color) {
        this.title = title;
        this.levelRequirement = levelRequirement;
        this.color = color;
    }

    public String getTitle() {
        return title;
    }

    public int getLevelRequirement() {
        return levelRequirement;
    }

    public Formatting getColor() {
        return color;
    }

    public Text getFormattedTitle() {
        return Text.literal(title).formatted(color);
    }

    public static MarriageTitle getTitleForLevel(int level) {
        if (level >= 50) return new MarriageTitle("Eternal Soulmates", 50, Formatting.LIGHT_PURPLE);
        if (level >= 40) return new MarriageTitle("Legendary Lovers", 40, Formatting.GOLD);
        if (level >= 30) return new MarriageTitle("Beloved Partners", 30, Formatting.YELLOW);
        if (level >= 20) return new MarriageTitle("Devoted Couple", 20, Formatting.GREEN);
        if (level >= 10) return new MarriageTitle("Happily Married", 10, Formatting.BLUE);
        return new MarriageTitle("Newlyweds", 0, Formatting.WHITE);
    }
}