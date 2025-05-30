package com.evacoffee.beautymod.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class ModItems {
    // Beauty Tools
    public static final Item MIRROR = registerItem("mirror", 
        new Item(new FabricItemSettings().maxCount(1).rarity(Rarity.UNCOMMON)));
    
    public static final Item MAKEUP_BRUSH = registerItem("makeup_brush",
        new Item(new FabricItemSettings().maxCount(1).maxDamage(128)));
    
    public static final Item COMB = registerItem("comb",
        new Item(new FabricItemSettings().maxDamage(32)));

    // Cosmetics
    public static final Item LIPSTICK = registerItem("lipstick",
        new DyeableItem(new FabricItemSettings().maxCount(1).maxDamage(16)) {
            @Override
            public int getColor(ItemStack stack) {
                return 0xFF69B4; // Hot pink by default
            }
        });
    
    public static final Item EYELINER = registerItem("eyeliner",
        new DyeableItem(new FabricItemSettings().maxCount(1).maxDamage(32)) {
            @Override
            public int getColor(ItemStack stack) {
                return 0x000000; // Black by default
            }
        });

    // Special Items
    public static final Item PERFUME = registerItem("perfume",
        new Item(new FabricItemSettings().maxCount(1).rarity(Rarity.UNCOMMON)) {
            @Override
            public void appendTooltip(ItemStack stack, net.minecraft.world.World world, 
                java.util.List<net.minecraft.text.Text> tooltip, net.minecraft.client.item.TooltipContext context) {
                tooltip.add(net.minecraft.text.Text.translatable("item.beautymod.perfume.tooltip"));
            }
        });

    public static final Item LOVE_POTION = registerItem("love_potion",
        new Item(new FabricItemSettings().maxCount(16).rarity(Rarity.RARE)) {
            @Override
            public UseAction getUseAction(ItemStack stack) {
                return UseAction.DRINK;
            }

            @Override
            public TypedActionResult<ItemStack> use(net.minecraft.world.World world, net.minecraft.entity.player.PlayerEntity user, 
                net.minecraft.util.Hand hand) {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 1));
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 400, 0));
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 1200, 0));
                return TypedActionResult.success(user.getStackInHand(hand).decrement(1));
            }
        });

    // Jewelry
    public static final Item ENGAGEMENT_RING = registerItem("engagement_ring",
        new Item(new FabricItemSettings().maxCount(1).rarity(Rarity.EPIC)));

    public static final Item PEARL_NECKLACE = registerItem("pearl_necklace",
        new Item(new FabricItemSettings().maxCount(1).rarity(Rarity.RARE)));

    // Gift Items
    public static final Item BOUQUET = registerItem("bouquet",
        new Item(new FabricItemSettings().maxCount(1)));

    public static final Item CHOCOLATE_BOX = registerItem("chocolate_box",
        new Item(new FabricItemSettings().food(new FoodComponent.Builder()
            .hunger(8)
            .saturationModifier(1.2f)
            .statusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 0), 1.0f)
            .alwaysEdible()
            .build())));

    // Helper Methods
    private static Item registerItem(String name, Item item) {
        Item registeredItem = Registry.register(Registries.ITEM, new Identifier("beautymod", name), item);
        addToItemGroup(registeredItem);
        return registeredItem;
    }

    private static void addToItemGroup(Item item) {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(item));
    }

    public static void registerModItems() {
        System.out.println("Registering Beauty Mod Items for " + "beautymod");
    }
}