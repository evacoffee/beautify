package com.evacoffee.beautymod.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModEntities {
    public static EntityType<RomanceNPC> ROMANCE_NPC;

    public static void registerEntities() {
        ROMANCE_NPC = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier("beautymod", "romance_npc"),
            EntityType.Builder.create(RomanceNPC::new, SpawnGroup.CREATURE)
                .setDimensions(0.6f, 1.95f)
                .build("romance_npc")
        );
    }
}
