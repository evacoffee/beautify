package com.evacoffee.beautymod;

import com.evacoffee.beautymod.entity.NPCRegistry;
import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class BeautyMod implements ModInitializer {

    // Register a beauty item: lipstick
    public static final Item LIPSTICK = Registry.register(
        Registries.ITEM,
        new Identifier("beautymod", "lipstick"),
        new Item(new Item.Settings().maxCount(1))
    );

    @Override
    public void onInitialize() {
        // Register custom romance NPCs
        NPCRegistry.registerNPCs();

        // Confirm mod and item loaded
        System.out.println("Beauty Mod has been initialized!");
        System.out.println("Lipstick item registered!");
    }
}
