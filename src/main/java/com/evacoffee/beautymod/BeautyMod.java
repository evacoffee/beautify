package com.evacoffee.beautymod;

import com.evacoffee.beautymod.item.ModItems;
import com.evacoffee.beautymod.command.LoveCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import com.yourname.beautymod.events.GiftHandler;
import com.yourname.beautymod.particles.ParticleHandler;

// inside onInitialize(import com.yourname.beautymod.entity.ModEntities;ModEntities.registerEntities();)
GiftHandler.register()
ParticleHandler.register();


public class BeautyMod implements ModInitializer {
    @Override
    public void onInitialize() {
        ModItems.registerModItems();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LoveCommand.register(dispatcher);
        });
        System.out.println("Beauty Mod initialized.");
    }
}
