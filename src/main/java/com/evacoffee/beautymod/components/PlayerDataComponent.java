package com.evacoffee.beautymod.components;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.nbt.NbtCompound;

public class PlayerDataComponent implements Component {
    private int relationshipPoints = 0;
    private String partnerId = "";

    // Getters and setters
    public int getRelationshipPoints() { return relationshipPoints; }
    public void addRelationshipPoints(int points) { this.relationshipPoints += points; }
    public String getPartnerId() { return partnerId; }
    public void setPartnerId(String id) { this.partnerId = id; }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.relationshipPoints = tag.getInt("RelationshipPoints");
        this.partnerId = tag.getString("PartnerId");
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putInt("RelationshipPoints", this.relationshipPoints);
        tag.putString("PartnerId", this.partnerId);
    }
}