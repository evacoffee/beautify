package com.evacoffee.beautymod.dating;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class DatingComponent implements Component {
    private final Map<UUID, DatingProgress> datingProgressMap = new HashMap<>();
    private final Set<UUID> crushes = new HashSet<>();
    private UUID currentDatePartner;
    private BlockPos dateLocation;
    private long dateStartTime;
    private int totalDates = 0;
    private int successfulDates = 0;
    
    public void addCrush(UUID playerId) {
        crushes.add(playerId);
    }
    
    public boolean hasCrushOn(UUID playerId) {
        return crushes.contains(playerId);
    }
    
    public void removeCrush(UUID playerId) {
        crushes.remove(playerId);
    }
    
    public Set<UUID> getCrushes() {
        return Collections.unmodifiableSet(crushes);
    }
    
    public void startDate(ServerPlayerEntity player, ServerPlayerEntity partner, BlockPos location) {
        this.currentDatePartner = partner.getUuid();
        this.dateLocation = location;
        this.dateStartTime = player.getWorld().getTime();
        
        // Initialize or update dating progress
        DatingProgress progress = datingProgressMap.computeIfAbsent(
            partner.getUuid(), 
            k -> new DatingProgress()
        );
        progress.incrementDatesStarted();
    }
    
    public boolean endDate(ServerPlayerEntity player, boolean successful) {
        if (currentDatePartner == null) return false;
        
        DatingProgress progress = datingProgressMap.get(currentDatePartner);
        if (progress != null && successful) {
            progress.recordDateSuccess(player.getWorld().getTime() - dateStartTime);
            successfulDates++;
        }
        
        totalDates++;
        currentDatePartner = null;
        dateLocation = null;
        return true;
    }
    
    public boolean isOnDate() {
        return currentDatePartner != null;
    }
    
    public UUID getCurrentDatePartner() {
        return currentDatePartner;
    }
    
    public BlockPos getDateLocation() {
        return dateLocation;
    }
    
    public int getTotalDates() {
        return totalDates;
    }
    
    public int getSuccessfulDates() {
        return successfulDates;
    }
    
    public Optional<DatingProgress> getDatingProgress(UUID playerId) {
        return Optional.ofNullable(datingProgressMap.get(playerId));
    }
    
    @Override
    public void readFromNbt(NbtCompound tag) {
        // Read crushes
        NbtList crushesList = tag.getList("Crushes", 11); // 11 is int array type for UUIDs
        crushes.clear();
        for (int i = 0; i < crushesList.size(); i++) {
            crushes.add(crushesList.getUuid(i));
        }
        
        // Read dating progress
        NbtCompound progressTag = tag.getCompound("DatingProgress");
        for (String key : progressTag.getKeys()) {
            try {
                UUID playerId = UUID.fromString(key);
                DatingProgress progress = new DatingProgress();
                progress.readFromNbt(progressTag.getCompound(key));
                datingProgressMap.put(playerId, progress);
            } catch (IllegalArgumentException e) {
                // Skip invalid UUIDs
            }
        }
        
        totalDates = tag.getInt("TotalDates");
        successfulDates = tag.getInt("SuccessfulDates");
    }
    
    @Override
    public void writeToNbt(NbtCompound tag) {
        // Write crushes
        NbtList crushesList = new NbtList();
        for (UUID crush : crushes) {
            crushesList.add(NbtString.of(crush.toString()));
        }
        tag.put("Crushes", crushesList);
        
        // Write dating progress
        NbtCompound progressTag = new NbtCompound();
        for (Map.Entry<UUID, DatingProgress> entry : datingProgressMap.entrySet()) {
            NbtCompound progressNbt = new NbtCompound();
            entry.getValue().writeToNbt(progressNbt);
            progressTag.put(entry.getKey().toString(), progressNbt);
        }
        tag.put("DatingProgress", progressTag);
        
        tag.putInt("TotalDates", totalDates);
        tag.putInt("SuccessfulDates", successfulDates);
    }
    
    public static class DatingProgress {
        private int datesStarted = 0;
        private int datesCompleted = 0;
        private long totalDateDuration = 0;
        private long lastDateTime = 0;
        private int streak = 0;
        private int bestStreak = 0;
        
        public void incrementDatesStarted() {
            datesStarted++;
        }
        
        public void recordDateSuccess(long duration) {
            datesCompleted++;
            totalDateDuration += duration;
            
            // Update streak
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastDateTime < 3 * 24 * 60 * 60 * 1000L) { // Within 3 days
                streak++;
                if (streak > bestStreak) {
                    bestStreak = streak;
                }
            } else {
                streak = 1;
            }
            lastDateTime = currentTime;
        }
        
        public double getAverageDateDuration() {
            return datesCompleted > 0 ? (double) totalDateDuration / datesCompleted : 0;
        }
        
        public int getDatesStarted() {
            return datesStarted;
        }
        
        public int getDatesCompleted() {
            return datesCompleted;
        }
        
        public int getCurrentStreak() {
            return streak;
        }
        
        public int getBestStreak() {
            return bestStreak;
        }
        
        public void readFromNbt(NbtCompound tag) {
            datesStarted = tag.getInt("DatesStarted");
            datesCompleted = tag.getInt("DatesCompleted");
            totalDateDuration = tag.getLong("TotalDateDuration");
            lastDateTime = tag.getLong("LastDateTime");
            streak = tag.getInt("Streak");
            bestStreak = tag.getInt("BestStreak");
        }
        
        public void writeToNbt(NbtCompound tag) {
            tag.putInt("DatesStarted", datesStarted);
            tag.putInt("DatesCompleted", datesCompleted);
            tag.putLong("TotalDateDuration", totalDateDuration);
            tag.putLong("LastDateTime", lastDateTime);
            tag.putInt("Streak", streak);
            tag.putInt("BestStreak", bestStreak);
        }
    }
}
