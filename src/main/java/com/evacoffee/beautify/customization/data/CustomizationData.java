package com.evacoffee.beautify.customization.data;

import net.minecraft.nbt.CompoundTag;

public class CustomizationData {
    private String skinTone = "DEFAULT";
    private String hairColor = "DEFAULT";
    private int hairColor = 0x000000;
    private int eyeColor = 0x0000FF;
    private float height = 1.8f;

    private String hat = "NONE";
    private String glasses = "NONE";
    private String accessory = "NONE";
    

    public String getSkinTone() { return skinTone; }
    public void setSkinTone(String skinTone) { this.skinTone = skinTone; }
    
    public String getHairStyle() {return hairStyle; }
    public void setHairStyle(String hairStyle) { this.hairStyle = hairStyle; }

    public int getHairColor() {return hairColor; }
    public void setHairColor(int hairColor) { this.hairColor = hairColor; }
    
    public int getEyeColor() { return eyeColor; }
    public void setEyeColor(int eyeColor) { this.eyeColor = eyeColor; }

    public float getHeight() { return height; }
    public void setHeight(float height) { this.height = height; }

    public String getTop() { return top; }
    public void setTop(String top) { this.top = top; }

    public String getBottom() { return bottom; }
    public void setBottom(String bottom) { this.bottom = bottom; }

    public String getShoes() { return shoes; }
    public void setShoes(String shoes) { this.shoes = shoes; }

    public String getHat() { return hat; }
    public void setHat(String hat) { this.hat = hat; }

    public String getGlasses() { return glasses; }
    public void setGlasses(String glasses) { this.glasses = glasses; }

    public String getAccessory() { return accessory; }
    public void setAccessory(String accessory) { this.accessory = accessory; }


    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("skinTone", skinTone);
        tag.putString("hairStyle", hairStyle);
        tag.putInt("hairColor", hairColor);
        tag.putInt("eyeColor", eyeColor);
        tag.putFloat("height", height);
        tag.putString("top", top);
        tag.putString("bottom", bottom);
        tag.putString("shoes", shoes);
        tag.putString("hat", hat);
        tag.putString("glasses", glasses);
        tag.putString("accessory", accessory);
        return tag;
    }


    public static CustomizationData fromNbt(CompoundTag tag) {
        CustomizationData data = new CustomizationData();
        if (tag != null) {
            data.skinTone = tag.getString("skinTone");
            data.hairStyle = tag.getString("hairStyle");
            data.hairColor = tag.getInt("hairColor");
            data.eyeColor = tag.getInt("eyeColor");
            data.height = tag.getFloat("height");
            data.top = tag.getString("top");
            data.bottom = tag.getString("bottom");
            data.shoes = tag.getString("shoes");
            data.hat = tag.getString("hat");
            data.glasses = tag.getString("glasses");
            data.accessory = tag.getString("accessory");
        }
        return data;
    }
}
