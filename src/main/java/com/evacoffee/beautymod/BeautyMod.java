package com.evacoffee.beautymod;

import com.evacoffee.beautymod.command.MarriageCommand;
import com.evacoffee.beautymod.event.MarriageEvents;
import com.evacoffee.beautymod.marriage.MarriageComponent;
import com.evacoffee.beautymod.network.Networking;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeautyMod implements ModInitializer, EntityComponentInitializer {
    public static final String MOD_ID = "beautymod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    // Component Registration
    public static final ComponentKey<MarriageComponent> MARRIAGE_COMPONENT = 
        ComponentRegistry.getOrCreate(
            new Identifier(MOD_ID, "marriage"), 
            MarriageComponent.class
        );

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Beauty Mod");
        
        // Initialize networking
        Networking.registerServerReceivers();
        
        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            MarriageCommand.register(dispatcher);
        });
        
        // Register events
        MarriageEvents.register();
        
        LOGGER.info("Beauty Mod initialized");
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        // Register marriage component for players
        registry.registerForPlayers(
            MARRIAGE_COMPONENT,
            player -> new MarriageComponent((PlayerEntity) player),
            RespawnCopyStrategy.ALWAYS_COPY
        );
        
        LOGGER.info("Registered entity component factories");
    }
}