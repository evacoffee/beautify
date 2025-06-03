package com.evacoffee.beautymod.relationship;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;
import java.util.Comparator;

public enum RelationshipLevel {
    // Negative relationship levels
    HATED("hated", -1000, -500, Formatting.DARK_RED, "Hated", 0xFF0000),
    DISLIKED("disliked", -499, -100, Formatting.RED, "Disliked", 0xFF5555),
    
    // Neutral
    STRANGER("stranger", -99, 99, Formatting.GRAY, "Stranger", 0xAAAAAA),
    ACQUAINTANCE("acquaintance", 100, 249, Formatting.WHITE, "Acquaintance", 0xFFFFFF),
    
    // Positive relationship levels
    FRIEND("friend", 250, 499, Formatting.BLUE, "Friend", 0x5555FF),
    CLOSE_FRIEND("close_friend", 500, 749, Formatting.DARK_BLUE, "Close Friend", 0x0000FF),
    CRUSH("crush", 750, 899, Formatting.LIGHT_PURPLE, "Crush", 0xFF55FF),
    LOVE_INTEREST("love_interest", 900, 999, Formatting.RED, "Love Interest", 0xFF0000),
    PARTNER("partner", 1000, Integer.MAX_VALUE, Formatting.GOLD, "Partner", 0xFFD700);

    private static final RelationshipLevel[] VALUES = values();
    private static final int MIN_VALUE = -1000;
    private static final int MAX_VALUE = 1000;
    
    private final String id;
    private final int minPoints;
    private final int maxPoints;
    private final Formatting formatting;
    private final String displayName;
    private final int color;

    static {
        // Verify that levels are in the correct order
        Arrays.sort(VALUES, Comparator.comparingInt(RelationshipLevel::getMinPoints));
    }

    RelationshipLevel(String id, int minPoints, int maxPoints, Formatting formatting, String displayName, int color) {
        this.id = id;
        this.minPoints = minPoints;
        this.maxPoints = maxPoints;
        this.formatting = formatting;
        this.displayName = displayName;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public int getMinPoints() {
        return minPoints;
    }

    public int getMaxPoints() {
        return maxPoints;
    }

    public Formatting getFormatting() {
        return formatting;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColor() {
        return color;
    }

    public MutableText getDisplayText() {
        return Text.literal(displayName).formatted(formatting);
    }

    public MutableText getDisplayTextWithPoints(int points) {
        return Text.literal(String.format("%s (%d)", displayName, points)).formatted(formatting);
    }

    public static RelationshipLevel fromPoints(int points) {
        points = MathHelper.clamp(points, MIN_VALUE, MAX_VALUE);
        for (RelationshipLevel level : VALUES) {
            if (points >= level.minPoints && points <= level.maxPoints) {
                return level;
            }
        }
        return STRANGER; // Default fallback
    }

    public static int clampPoints(int points) {
        return MathHelper.clamp(points, MIN_VALUE, MAX_VALUE);
    }

    public boolean isAtLeast(RelationshipLevel other) {
        return this.minPoints >= other.minPoints;
    }

    public boolean isAtMost(RelationshipLevel other) {
        return this.maxPoints <= other.maxPoints;
    }

    public boolean isNegative() {
        return this == HATED || this == DISLIKED;
    }

    public boolean isPositive() {
        return !isNegative() && this != STRANGER;
    }

    public boolean isRomantic() {
        return this == CRUSH || this == LOVE_INTEREST || this == PARTNER;
    }

    public static int getPointsForLevelChange(RelationshipLevel from, RelationshipLevel to) {
        if (from == to) return 0;
        return to.minPoints - from.minPoints;
    }

    public static int getProgressToNextLevel(int currentPoints) {
        RelationshipLevel current = fromPoints(currentPoints);
        if (current == PARTNER) return 0; // Max level
        
        int nextMin = current.maxPoints + 1;
        RelationshipLevel next = fromPoints(nextMin);
        int range = next.maxPoints - next.minPoints + 1;
        int progress = currentPoints - current.minPoints;
        
        return (progress * 100) / (current.maxPoints - current.minPoints + 1);
    }

    public static RelationshipLevel getNextLevel(RelationshipLevel current) {
        int nextOrdinal = current.ordinal() + 1;
        return nextOrdinal < VALUES.length ? VALUES[nextOrdinal] : null;
    }

    public static RelationshipLevel getPreviousLevel(RelationshipLevel current) {
        int prevOrdinal = current.ordinal() - 1;
        return prevOrdinal >= 0 ? VALUES[prevOrdinal] : null;
    }

    public static int getPointsToNextLevel(int currentPoints) {
        RelationshipLevel current = fromPoints(currentPoints);
        if (current == PARTNER) return 0; // Already at max level
        
        return (current.maxPoints - currentPoints) + 1;
    }

    public static int getPointsFromLastLevel(int currentPoints) {
        RelationshipLevel current = fromPoints(currentPoints);
        return currentPoints - current.minPoints;
    }

    public static int getPointsBetweenLevels(RelationshipLevel from, RelationshipLevel to) {
        return Math.abs(from.minPoints - to.minPoints);
    }
}