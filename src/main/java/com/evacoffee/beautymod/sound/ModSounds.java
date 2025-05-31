package com.evacoffee.beautymod.sound;

import com.evacoffee.beautymod.BeautyMod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {
    public static final SoundEvent HEARTBEAT = registerSoundEvent("heartbeat");
    public static final SoundEvent KISS = registerSoundEvent("kiss");
    public static final SoundEvent WEDDING_BELLS = registerSoundEvent("wedding_bells");

    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = new Identifier(BeautyMod.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void registerSounds() {
        BeautyMod.LOGGER.info("Registering Mod Sounds for " + BeautyMod.MOD_ID);
    }
}