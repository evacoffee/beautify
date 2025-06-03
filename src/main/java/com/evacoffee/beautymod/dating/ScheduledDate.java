package com.evacoffee.beautymod.dating;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.UUID;

public class ScheduledDate {
    private final UUID id;
    private final UUID player1;
    private final UUID player2;
    private final long scheduledTime;
    private final String locationName;
    private final BlockPos location;
    private final UUID dimensionId;
    private boolean completed;
    private boolean started;
    private long completedTime;
    private int duration; // in ticks (20 ticks = 1 second)

    public ScheduledDate(UUID player1, UUID player2, long scheduledTime, String locationName, 
                        BlockPos location, World world, int durationMinutes) {
        this.id = UUID.randomUUID();
        this.player1 = player1;
        this.player2 = player2;
        this.scheduledTime = scheduledTime;
        this.locationName = locationName;
        this.location = location;
        this.dimensionId = world.getRegistryKey().getValue();
        this.completed = false;
        this.started = false;
        this.duration = durationMinutes * 60 * 20; // Convert minutes to ticks
    }

    public boolean isTimeForDate(long currentTime) {
        return !started && !completed && currentTime >= scheduledTime;
    }

    public boolean isDateInProgress(long currentTime) {
        return started && !completed && currentTime < (scheduledTime + duration);
    }

    public boolean isDateCompleted() {
        return completed;
    }

    public void startDate(ServerWorld world) {
        if (!started && !completed) {
            started = true;
            // Notify players
            notifyPlayers(world, "Date has started! Meet at " + locationName);
        }
    }

    public void completeDate(ServerWorld world, boolean success) {
        if (started && !completed) {
            completed = true;
            completedTime = world.getTime();
            String message = success ? 
                "Date completed successfully!" : 
                "Date ended unsuccessfully.";
            notifyPlayers(world, message);
        }
    }

    private void notifyPlayers(ServerWorld world, String message) {
        ServerPlayerEntity p1 = world.getServer().getPlayerManager().getPlayer(player1);
        ServerPlayerEntity p2 = world.getServer().getPlayerManager().getPlayer(player2);
        
        Text text = Text.literal("[Date] " + message);
        if (p1 != null) p1.sendMessage(text, false);
        if (p2 != null) p2.sendMessage(text, false);
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putUuid("id", id);
        nbt.putUuid("player1", player1);
        nbt.putUuid("player2", player2);
        nbt.putLong("scheduledTime", scheduledTime);
        nbt.putString("locationName", locationName);
        nbt.putLong("location", location.asLong());
        nbt.putUuid("dimensionId", dimensionId);
        nbt.putBoolean("completed", completed);
        nbt.putBoolean("started", started);
        nbt.putLong("completedTime", completedTime);
        nbt.putInt("duration", duration);
        return nbt;
    }

    public static ScheduledDate fromNbt(NbtCompound nbt, ServerWorld world) {
        UUID player1 = nbt.getUuid("player1");
        UUID player2 = nbt.getUuid("player2");
        long scheduledTime = nbt.getLong("scheduledTime");
        String locationName = nbt.getString("locationName");
        BlockPos location = BlockPos.fromLong(nbt.getLong("location"));
        UUID dimensionId = nbt.getUuid("dimensionId");
        int durationTicks = nbt.getInt("duration");
        
        ScheduledDate date = new ScheduledDate(
            player1, player2, scheduledTime, locationName, 
            location, world, durationTicks / (60 * 20)
        );
        
        date.id = nbt.getUuid("id");
        date.completed = nbt.getBoolean("completed");
        date.started = nbt.getBoolean("started");
        date.completedTime = nbt.getLong("completedTime");
        
        return date;
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getPlayer1() { return player1; }
    public UUID getPlayer2() { return player2; }
    public long getScheduledTime() { return scheduledTime; }
    public String getLocationName() { return locationName; }
    public BlockPos getLocation() { return location; }
    public UUID getDimensionId() { return dimensionId; }
    public boolean isCompleted() { return completed; }
    public boolean isStarted() { return started; }
    public long getCompletedTime() { return completedTime; }
    public int getDuration() { return duration; }
}