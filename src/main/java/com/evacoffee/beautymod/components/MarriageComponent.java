package com.evacoffee.beautymod.components;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.nbt.NbtCompound;

public class MarriageComponent implements Component {
    private boolean isMarried = false;
    private String spouseId = "";
    private long marriageDate = 0;
    private String marriageLocation = "";

    // Getters and setters
    public boolean isMarried() { return isMarried; }
    public void setMarried(boolean married) { this.isMarried = married; }
    
    public String getSpouseId() { return spouseId; }
    public void setSpouseId(String id) { this.spouseId = id; }
    
    public long getMarriageDate() { return marriageDate; }
    public void setMarriageDate(long date) { this.marriageDate = date; }
    
    public String getMarriageLocation() { return marriageLocation; }
    public void setMarriageLocation(String location) { this.marriageLocation = location; }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.isMarried = tag.getBoolean("IsMarried");
        this.spouseId = tag.getString("SpouseId");
        this.marriageDate = tag.getLong("MarriageDate");
        this.marriageLocation = tag.getString("MarriageLocation");
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putBoolean("IsMarried", this.isMarried);
        tag.putString("SpouseId", this.spouseId);
        tag.putLong("MarriageDate", this.marriageDate);
        tag.putString("MarriageLocation", this.marriageLocation);
    }
}
