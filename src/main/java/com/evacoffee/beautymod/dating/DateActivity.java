package com.evacoffee.beautymod.dating;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import java.util.List;
import java.util.UUID;
import java.util.Objects;

public abstract class DateActivity {
    protected final UUID dateId;
    protected final DateLocation location;
    protected final List<UUID> participants;
    protected boolean isCompleted;
    protected int duration;
    protected int progress;

    public DateActivity(UUID dateId, DateLocation location, List<UUID> participant, int duration) {
        this.dateId = dateId;
        this.location = location;
        this.participants = participant;
        this.isCompleted = false;
        this.duration = duration;
        this.progress = 0;
        this.isCompleted = false;
    }

    public abstract boolean canStart();
    public abstract void onStart();
    public abstract void tick();
    public abstract void onComplete();
    public abstract void onCancel();

    public void updateProgress() {
        if (!isCompleted) {
            progress++;
            if (progress >= duration) {
                isCompleted = true;
                onComplete();
            }
        }
    }

    public float getProgress() {
        return (float) progress / duration;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void cancel() {
        if (!isCompleted) {
            onCancel();
            isCompleted = true;
        }
    }

    protected boolean allParticipantsPresent() {
        //Check if all participants are within the location
        return participants.stream().allMatch(this::isParticipantPresent);
    }

    protected boolean isParticipantPresent(UUID playerID) {
        PlayerEntity player = location.getWorld().getPlayerByUuid(playerId);
        return player != null && location.isAtLocation(player.getBlockPos(), player.world);
    }

    protected void giveRewards() {
        participants.forEach(playerId -> {
            PlayerEntity player = location.getWorld().getPlayerByUuid(playerId);
            if (player != null) {
                // Add relationship points, etc
            }
        });
    }
    
    public boolean isActive() {
        return isActive && !isCompleted;
    }
    
    public Text getStatus() {
        return currentStatus;
    }
    
    protected void setStatus(Text status) {
        this.currentStatus = status;
    }
    
    public List<UUID> getParticipants() {
        return List.copyOf(participants);
    }
    
    public DateLocation getLocation() {
        return location;
    }