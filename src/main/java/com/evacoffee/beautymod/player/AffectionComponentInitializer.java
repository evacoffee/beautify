package com.evacoffee.beautymod.player;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class AffectionComponentInitializer implements EntityComponentInitializer {
    public static final ComponentKey<AffectionComponent> AFFECTION =
        ComponentRegistry.getOrCreate(new Identifier("beautymod", "affection"), AffectionComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        // Register the component for PlayerEntity
        registry.registerForPlayers(
            AFFECTION,
            player -> new AffectionComponent(),
            RespawnCopyStrategy.ALWAYS_COPY
        );
    }
    
    // Helper method to get the component for a player
    public static AffectionComponent get(PlayerEntity player) {
        return AFFECTION.get(player);
    }
}
