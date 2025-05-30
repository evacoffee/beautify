package com.evacoffee.beautymod.dating;

public enum DateType {
    PANCAKE_DATE("Pancake Date", "pancake", 0.8f),
    SPORT_DATE("Sport Date", "sport", 1.0f),
    MOVIE_DATE("Movie Date", "movie", 0.9f),
    ROMANTIC_DINNER("Romantic Dinner", "dinner", 0.95f);

    private final String displayName;
    private final String id;
    private final float successRate;

    DateType(String displayName, String id, float successRate) {
        this.displayName = displayName;
        this.id = id;
        this.successRate = successRate;
    }

    public String getDisplayName() { return displayName; }
    public String getId() { return id; }
    public float getSuccessRate() { return successRate; }

    public static DateType byId(String id) {
        for (DateType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return null;
    }
}