package com.evacoffee.beautymod.marriage;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class MarriageComponentInitializer implements EntityComponentInitializer {
    public static final ComponentKey<MarriageComponent> MARRIAGE = 
        ComponentRegistryV3.INSTANCE.getOrCreate(
            new Identifier("beautymod", "marriage"), 
            MarriageComponent.class
        );

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        // Register the component for PlayerEntity
        registry.registerForPlayers(
            MARRIAGE,
            player -> new MarriageComponent((PlayerEntity) player),
            RespawnCopyStrategy.ALWAYS_COPY
        );
    }

    public static MarriageComponent get(Entity entity) {
        return MARRIAGE.get(entity);
    }
}