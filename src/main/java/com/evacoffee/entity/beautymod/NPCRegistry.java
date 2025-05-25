package com.evacoffee.beautymod.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class NPCRegistry {

    public static EntityType<RomanceNPC> ROMANCE_NPC;

    public static void registerNPCs() {
        ROMANCE_NPC = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier("beautymod", "romance_npc"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, RomanceNPC::new)
                .dimensions(EntityDimensions.fixed(0.6f, 1.95f))
                .build()
        );

        FabricDefaultAttributeRegistry.register(ROMANCE_NPC, RomanceNPC.createMobAttributes());
    }

    public static DefaultAttributeContainer.Builder createMobAttributes() {
        return PathAwareEntity.createMobAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3);
    }
}
