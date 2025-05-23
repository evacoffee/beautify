package com.evacoffee.beautymod;

import com.evacoffee.beautymod.item.ModItems;
import com.evacoffee.beautymod.command.LoveCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import com.yourname.beautymod.events.GiftHandler;

// inside onInitialize()
GiftHandler.register();

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
