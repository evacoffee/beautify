package com.evacoffee.beautymod.dating.activities;

import com.evacoffee.beautymod.dating.DateActivity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.Text;
import java.util.*;

public class PhotoActivity extends DateActivity {
    private final Set<UUID> participantsInFrame = new HashSet<>();
    private boolean photoTaken = false;
    private BlockPos photoPose;

    public PhotoActivity(UUID dateId, DateLocation location, List<UUID> participants) {
        super(dateId, location, participants, 300); // 15 seconds for photo
    }

    @Override
    public boolean canStart() {
        return allParticipantsPresent() && participants.size() >= 2;
    }

    @Override
    public void onStart() {
        // Find a good spot for the photo
        this.photoPose = findPhotoPose();
        
        participants.forEach(playerId -> {
            PlayerEntity player = location.getWorld().getPlayerByUuid(playerId);
            if (player != null) {
                // Teleport players to photo position
                player.teleport(
                    photoPose.getX() + 0.5, 
                    photoPose.getY(), 
                    photoPose.getZ() + 0.5
                );
                player.sendMessage(Text.of("Say cheese! Get ready for a photo!"), false);
            }
        });
    }

    private BlockPos findPhotoPose() {
        // Simple implementation - in a real mod, you'd want more sophisticated logic
        return location.getPosition().up(); // Just above the location
    }

    public void takePhoto(UUID photographerId) {
        if (photoTaken) return;
        
        // Check if photographer is a participant
        if (!participants.contains(photographerId)) {
            PlayerEntity photographer = location.getWorld().getPlayerByUuid(photographerId);
            if (photographer != null) {
                photographer.sendMessage(Text.of("You're not part of this date!"), false);
            }
            return;
        }

        // Check who's in the frame
        participantsInFrame.clear();
        participants.forEach(participantId -> {
            PlayerEntity participant = location.getWorld().getPlayerByUuid(participantId);
            if (participant != null && participant.getBlockPos().isWithinDistance(photoPose, 5)) {
                participantsInFrame.add(participantId);
            }
        });

        if (participantsInFrame.size() < 2) {
            PlayerEntity photographer = location.getWorld().getPlayerByUuid(photographerId);
            if (photographer != null) {
                photographer.sendMessage(Text.of("Not enough people in the frame!"), false);
            }
            return;
        }

        // Create photo item
        ItemStack photo = new ItemStack(Items.PAPER);
        NbtCompound nbt = photo.getOrCreateNbt();
        nbt.putString("Title", "Date Photo");
        nbt.put("Location", NbtHelper.fromBlockPos(photoPose));
        nbt.putLong("Timestamp", System.currentTimeMillis());
        nbt.putUuid("Photographer", photographerId);
        
        // Add participants to the photo's NBT
        NbtCompound participantsTag = new NbtCompound();
        participantsInFrame.forEach(id -> participantsTag.putBoolean(id.toString(), true));
        nbt.put("Participants", participantsTag);
        
        photo.setNbt(nbt);
        photo.setCustomName(Text.of("Photo from Date"));

        // Give photo to photographer
        PlayerEntity photographer = location.getWorld().getPlayerByUuid(photographerId);
        if (photographer != null) {
            if (!photographer.getInventory().insertStack(photo)) {
                photographer.dropItem(photo, false);
            }
            photographer.sendMessage(Text.of("Photo taken! Check your inventory."), false);
        }

        photoTaken = true;
        complete();
    }

    @Override
    public void onComplete() {
        if (photoTaken) {
            broadcastMessage(Text.of("Great photo! It