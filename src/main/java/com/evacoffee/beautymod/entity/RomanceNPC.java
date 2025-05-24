package com.evacoffee.beautymod.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class RomanceNPC extends VillagerEntity {

    private String name = "Unknown";
    private String personality = "Neutral";
    private String race = "Human";
    private int lovePoints = 0;

    public RomanceNPC(EntityType<? extends VillagerEntity> entityType, World world) {
        super(entityType, world);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPersonality(String personality) {
        this.personality = personality;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public void increaseLove(PlayerEntity player, int amount) {
        this.lovePoints += amount;
        player.sendMessage(Text.of(getLoveMessage()), true);
    }

    public String getPersonality() {
        return this.personality;
    }

    public String getNPCName() {
        return this.name;
    }

    public int getLovePoints() {
        return this.lovePoints;
    }

    public String getRace() {
        return this.race;
    }

    private String getLoveMessage() {
        boolean highLove = lovePoints >= 50;

        switch (personality.toLowerCase()) {
            case "shy":
                return highLove ? name + ": I’m really glad you’re here... ❤️" : name + ": ...Hi.";
            case "flirty":
                return highLove ? name + ": You’ve totally got my heart, babe 💋" : name + ": Hey cutie 😉";
            case "artistic":
                return highLove ? name + ": You are my muse. 🎨" : name + ": Have you ever seen a sunset in watercolor?";
            case "kind":
                return highLove ? name + ": You’re wonderful, and I’m lucky to know you." : name + ": How are you feeling today?";
            case "cool":
                return highLove ? name + ": Wanna hang out sometime? 😎" : name + ": Damn. You look beautiful.";
            case "charismatic":
                return highLove ? name + ": I like you." : name + ": Did you know love boosts serotonin? I'm feeling really happy right now.";
            case "nerdy":
                return highLove ? name + ": I like spending time with you..." : name + ": Nice to meet you. You are beautiful.";
            case "funny":
                return highLove ? name + ": You laugh at my jokes. You must love me." : name + ": Why don’t we skip to the kiss scene?";
            default:
                return name + ": Hi!";
        }
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (!player.getWorld().isClient) {
            increaseLove(player, 10); // Every interaction increases love
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putString("npcName", this.name);
        nbt.putString("personality", this.personality);
        nbt.putString("race", this.race);
        nbt.putInt("lovePoints", this.lovePoints);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.name = nbt.getString("npcName");
        this.personality = nbt.getString("personality");
        this.race = nbt.getString("race");
        this.lovePoints = nbt.getInt("lovePoints");
    }
}
