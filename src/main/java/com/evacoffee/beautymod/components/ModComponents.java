package com.evacoffee.beautymod.components;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.util.Identifier;
import net.minecraft.entity.player.PlayerEntity;

public final class ModComponents implements EntityComponentInitializer {
    // Component Keys
    public static final ComponentKey<PlayerDataComponent> PLAYER_DATA = 
        ComponentRegistry.getOrCreate(new Identifier("beautymod", "player_data"), PlayerDataComponent.class);
    public static final ComponentKey<DatingComponent> DATING = 
        ComponentRegistry.getOrCreate(new Identifier("beautymod", "dating"), DatingComponent.class);
    public static final ComponentKey<MarriageComponent> MARRIAGE = 
        ComponentRegistry.getOrCreate(new Identifier("beautymod", "marriage"), MarriageComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        // Register components for players
        registry.registerForPlayers(PLAYER_DATA, PlayerDataComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
        registry.registerForPlayers(DATING, DatingComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
        registry.registerForPlayers(MARRIAGE, MarriageComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
    }
}