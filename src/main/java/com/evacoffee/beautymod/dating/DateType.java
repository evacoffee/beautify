package com.evacoffee.beautymod.dating;

import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public enum DateType {
    //Casual Dates
    PANCAKE_DATE("Panckae Date", "pancake", 0.8f, 1.0f, 0.5f, false, null, 0),
    CAFE_VISIT("Cafe Visit", "cafe", 0.85f, 1.2f, 0.6f, false, null, 0),
    PICNIC("Picnic in the Park", "picnic", 0.9f, 1.5f, 0.7f, false, null, 1),

    // Active Dates
    SPORT_DATE("Sport Date", "sport", 1.0f, 0.8f, 0.4f, false, null, 0),
    HIKING("Hiking Adventure", "hiking", 0.95f, 1.3f, 0.6f, false, null, 1),
    DANCE_CLASS("Dance Class", "dance", 1.4f, 0.8f, false, null, 1),
    
    // Cultural Dates
    MUSEUM_TOUR("Museum Tour", "museum", 0.85f, 1.1f, 0.7f, false, null, 0),
    CONCERT("Concert Night", "concert", 0.9f, 1.6f, 0.9f, false, null, 2),
    THEATER("Theater Night", "theater", 0.88f, 1.4f, 0.8f, false, null, 1),
    
    // Romantic Dates
    ROMANTIC_DINNER("Romantic Dinner", "dinner", 0.95f, 1.8f, 1.0f, false, null, 2),
    SUNSET_CRUISE("Sunset Cruise", "cruise", 0.92f, 2.0f, 1.2f, false, null, 3),
    STARGAZING("Stargazing", "stargazing", 0.9f, 2.2f, 1.4f, false, null, 2),
    
    // Seasonal Dates
    BEACH_DAY("Beach Day", "beach", 0.88f, 1.3f, 0.7f, true, "summer", 1),
    ICE_SKATING("Ice Skating", "skating", 0.9f, 1.4f, 0.8f, true, "winter", 1),
    PUMPKIN_PATCH("Pumpkin Patch Visit", "pumpkin", 0.85f, 1.2f, 0.7f, true, "autumn", 0),
    FLOWER_FESTIVAL("Flower Festival", "flower_fest", 0.9f, 1.5f, 0.8f, true, "spring", 0);

    private final String displayName;
    private final String id;
    private final float baseSuccessRate;
    private final float romanceMultiplier;
    private final float friendshipMultiplier;
    private final boolean isSeasonal;
    private final String season;
    private final int minRelationshipLevel;

    DateType(String displayName, String id, float baseSuccessRate, float romanceMultiplier, 
            float friendshipMultiplier, boolean isSeasonal, String season, int minRelationshipLevel) {
        this.displayName = displayName;
        this.id = id;
        this.baseSuccessRate = baseSuccessRate;
        this.romanceMultiplier = romanceMultiplier;
        this.friendshipMultiplier = friendshipMultiplier;
        this.isSeasonal = isSeasonal;
        this.season = season;
        this.minRelationshipLevel = minRelationshipLevel;
    }

    // Getters
    public String getDisplayName() { return displayName; }
    public String getId() { return id; }
    public float getBaseSuccessRate() { return baseSuccessRate; }
    public float getRomanceMultiplier() { return romanceMultiplier; }
    public float getFriendshipMultiplier() { return friendshipMultiplier; }
    public boolean isSeasonal() { return isSeasonal; }
    public String getSeason() { return season; }
    public int getMinRelationshipLevel() { return minRelationshipLevel; }

    public static DateType byId(String id) {
        for (DateType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return null;
    }

    public boolean isAvailable(World world) {
        if (!isSeasonal) return true;
        // Check if current in-game season matches
        return world.getSeasonManager().getCurrentSeason().equalsIgnoreCase(season);
    }
}