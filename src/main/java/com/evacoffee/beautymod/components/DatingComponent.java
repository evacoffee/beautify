package com.evacoffee.beautymod.components;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.nbt.NbtCompound;

public class DatingComponent implements Component {
    private long lastDateTime = 0;
    private boolean isOnDate = false;
    private String datePartnerId = "";

    // Getters and setters
    public long getLastDateTime() { return lastDateTime; }
    public void setLastDateTime(long time) { this.lastDateTime = time; }
    
    public boolean isOnDate() { return isOnDate; }
    public void setOnDate(boolean onDate) { this.isOnDate = onDate; }
    
    public String getDatePartnerId() { return datePartnerId; }
    public void setDatePartnerId(String id) { this.datePartnerId = id; }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.lastDateTime = tag.getLong("LastDateTime");
        this.isOnDate = tag.getBoolean("IsOnDate");
        this.datePartnerId = tag.getString("DatePartnerId");
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putLong("LastDateTime", this.lastDateTime);
        tag.putBoolean("IsOnDate", this.isOnDate);
        tag.putString("DatePartnerId", this.datePartnerId);
    }
}
