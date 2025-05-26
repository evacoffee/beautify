package com.evacoffee.beautymod.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;

public class NPCRegistry {

    public static final Map<String, EntityType<RomanceNPC>> REGISTERED_NPCS = new HashMap<>();

    public static void registerNPCs() {
        for (Map.Entry<String, RomanceNPC.NPCInfo> entry : RomanceNPC.NPC_DATA.entrySet()) {
            String id = entry.getKey();
            RomanceNPC.NPCInfo info = entry.getValue();

            // Register a unique EntityType for each NPC
            EntityType<RomanceNPC> npcType = Registry.register(
                    Registry.ENTITY_TYPE,
                    new Identifier("beautymod", id),
                    FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, RomanceNPC::new)
                            .dimensions(EntityDimensions.fixed(0.6F, 1.8F))
                            .trackRangeBlocks(80)
                            .build()
            );

            REGISTERED_NPCS.put(id, npcType);
        }

        System.out.println("All romance NPCs have been registered!");
    }
}
