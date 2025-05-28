package com.evacoffee.beautymod.player;

import com.evacoffee.beautymod.memory.VillagerMemory;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class PlayerComponentInitializer implements EntityComponentInitializer {
    public static final String MOD_ID = "beautymod";
    
    // Component Keys
    public static final ComponentKey<AffectionComponent> AFFECTION =
        register("affection", AffectionComponent.class);
    
    public static final ComponentKey<PlayerQuestComponent> PLAYER_QUESTS =
        register("player_quests", PlayerQuestComponent.class);
    
    public static final ComponentKey<VillagerMemory> VILLAGER_MEMORY =
        register("villager_memory", VillagerMemory.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        // Register the affection component for players
        registry.registerForPlayers(
            AFFECTION,
            AffectionComponent::new,
            RespawnCopyStrategy.ALWAYS_COPY
        );
        
        // Register the quest component for players
        registry.registerForPlayers(
            PLAYER_QUESTS,
            player -> new PlayerQuestComponent((ServerPlayerEntity) player),
            RespawnCopyStrategy.ALWAYS_COPY
        );
        
        // Register the villager memory component
        registry.registerForPlayers(
            VILLAGER_MEMORY,
            player -> new VillagerMemory(),
            RespawnCopyStrategy.ALWAYS_COPY
        );
    }
    
    // Helper method to register components
    private static <T> ComponentKey<T> register(String name, Class<T> componentClass) {
        return ComponentRegistry.getOrCreate(new Identifier(MOD_ID, name), componentClass);
    }
    
    // Helper method to get the affection component for a player
    public static AffectionComponent getAffection(PlayerEntity player) {
        return AFFECTION.get(player);
    }
    
    // Helper method to get the quest component for a player
    public static PlayerQuestComponent getQuests(ServerPlayerEntity player) {
        return PLAYER_QUESTS.get(player);
    }
    
    // Helper method to get the villager memory component
    public static VillagerMemory getVillagerMemory(ServerPlayerEntity player) {
        return VILLAGER_MEMORY.get(player);
    }
}
