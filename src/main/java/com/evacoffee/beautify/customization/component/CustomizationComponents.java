package com.evacoffee.beautify.customization.component;

import com.evacoffee.beautify.BeautifyMod;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class CustomizationComponents implements EntityComponentInitializer {
    public static final ComponentKey<CustomizationComponent> CUSTOMIZATION =
        ComponentRegistry.getOrCreate(
            new Identifier(BeautifyMod.MOD_ID, "customization"),
            CustomizationComponent.class
        );

    public static CustomizationComponent get(PlayerEntity player) {
        return CUSTOMIZATION.get(player);
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(
            CUSTOMIZATION,
            player -> new CustomizationComponent(),
            RespawnCopyStrategy.ALWAYS_COPY
        );
    }
}
