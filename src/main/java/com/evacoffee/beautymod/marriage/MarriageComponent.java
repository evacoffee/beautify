package com.evacoffee.beautymod.marriage;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class MarriageComponent implements AutoSyncedComponent {
    private UUID spouseUuid;
    private String spouseName;
    private boolean isMarried = false;
    private long weddingDay;
    private BlockPos homePos;
    private String homeWorld;
    private final Set<MarriagePerk> unlockedPerks = new HashSet<>();
    private final Map<String, Integer> questProgress = new HashMap<>();
    private final PlayerEntity provider;

    public MarriageComponent(PlayerEntity provider) {
        this.provider = provider;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        isMarried = tag.getBoolean("isMarried");
        if (tag.contains("spouseUuid")) {
            spouseUuid = tag.getUuid("spouseUuid");
        }
        spouseName = tag.getString("spouseName");
        weddingDay = tag.getLong("weddingDay");
        
        if (tag.contains("homeX")) {
            homePos = new BlockPos(
                tag.getInt("homeX"),
                tag.getInt("homeY"),
                tag.getInt("homeZ")
            );
            homeWorld = tag.getString("homeWorld");
        }
        
        // Load unlocked perks
        unlockedPerks.clear();
        int[] perkIds = tag.getIntArray("unlockedPerks");
        for (int id : perkIds) {
            if (id >= 0 && id < MarriagePerk.values().length) {
                unlockedPerks.add(MarriagePerk.values()[id]);
            }
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putBoolean("isMarried", isMarried);
        if (spouseUuid != null) {
            tag.putUuid("spouseUuid", spouseUuid);
        }
        tag.putString("spouseName", spouseName != null ? spouseName : "");
        tag.putLong("weddingDay", weddingDay);
        
        if (homePos != null && homeWorld != null) {
            tag.putInt("homeX", homePos.getX());
            tag.putInt("homeY", homePos.getY());
            tag.putInt("homeZ", homePos.getZ());
            tag.putString("homeWorld", homeWorld);
        }
        
        // Save unlocked perks
        int[] perkIds = unlockedPerks.stream()
            .mapToInt(Enum::ordinal)
            .toArray();
        tag.putIntArray("unlockedPerks", perkIds);
    }

    public boolean isMarried() {
        return isMarried;
    }

    public void marry(ServerPlayerEntity spouse, long currentTime) {
        this.isMarried = true;
        this.spouseUuid = spouse.getUuid();
        this.spouseName = spouse.getEntityName();
        this.weddingDay = currentTime;
        sync();
    }

    public void divorce() {
        this.isMarried = false;
        this.spouseUuid = null;
        this.spouseName = null;
        this.homePos = null;
        this.homeWorld = null;
        this.unlockedPerks.clear();
        sync();
    }

    public UUID getSpouseUuid() {
        return spouseUuid;
    }

    public String getSpouseName() {
        return spouseName != null ? spouseName : "Unknown";
    }

    public long getWeddingDay() {
        return weddingDay;
    }

    public void setHome(BlockPos pos, String world) {
        this.homePos = pos;
        this.homeWorld = world;
        sync();
    }

    public BlockPos getHomePos() {
        return homePos;
    }

    public String getHomeWorld() {
        return homeWorld;
    }

    public boolean hasPerk(MarriagePerk perk) {
        return unlockedPerks.contains(perk);
    }

    public boolean unlockPerk(MarriagePerk perk, int marriageLevel) {
        if (marriageLevel >= perk.getRequiredLevel() && !hasPerk(perk)) {
            unlockedPerks.add(perk);
            sync();
            return true;
        }
        return false;
    }

    public Set<MarriagePerk> getUnlockedPerks() {
        return Collections.unmodifiableSet(unlockedPerks);
    }

    public int getQuestProgress(String questId) {
        return questProgress.getOrDefault(questId, 0);
    }

    public void updateQuestProgress(String questId, int amount) {
        questProgress.put(questId, getQuestProgress(questId) + amount);
        sync();
    }

    private void sync() {
        MarriageComponentInitializer.MARRIAGE.sync(provider);
    }
}