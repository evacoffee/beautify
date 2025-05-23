package com.evacoffee.beautymod;

import com.yourname.beautymod.item.ModItems;
import net.fabricmc.api.ModInitializer;

public class BeautyMod implements ModInitializer {
    @Override
    public void onInitialize() {
        ModItems.registerModItems();
        System.out.println("Hello from Beauty Mod!");
    }
}
