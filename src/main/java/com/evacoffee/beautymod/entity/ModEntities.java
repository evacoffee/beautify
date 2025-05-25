package com.evacoffee.beautymod.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModEntities {

    public static final EntityType<RomanceNPC> ROMANCE_NPC = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier("beautymod", "romance_npc"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, RomanceNPC::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 1.8f)) // Size of a player
                    .build()
    );

    // Optional helper method to spawn NPC with personality
    public static RomanceNPC createCustomNPC(net.minecraft.world.World world, String personality) {
        RomanceNPC npc = new RomanceNPC(ROMANCE_NPC, world);
        npc.setPersonality(personality);
        return npc;
    }

    public static void registerModEntities() {
        System.out.println("Registering Mod Entities for Beauty Mod");
    }
}
