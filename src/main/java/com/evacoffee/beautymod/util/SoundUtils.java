package com.evacoffee.beautymod.util;

import com.evacoffee.beautymod.sound.ModSounds;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public class SoundUtils {
    public static void playSoundForPlayer(PlayerEntity player, SoundEvent sound, float volume, float pitch) {
        if (!player.getWorld().isClient) {
            ((ServerWorld) player.getWorld()).playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                sound,
                SoundCategory.PLAYERS,
                volume,
                pitch
            );
        }
    }

    public static void playHeartbeat(PlayerEntity player) {
        playSoundForPlayer(player, ModSounds.HEARTBEAT, 0.8f, 1.0f);
    }

    public static void playKiss(PlayerEntity player) {
        playSoundForPlayer(player, ModSounds.KISS, 1.0f, 1.0f);
    }

    public static void playWeddingBells(PlayerEntity player) {
        playSoundForPlayer(player, ModSounds.WEDDING_BELLS, 1.0f, 1.0f);
    }
}