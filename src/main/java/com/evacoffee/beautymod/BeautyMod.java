package com.evacoffee.beautymod;

import com.evacoffee.beautymod.command.MarriageCommand;
import com.evacoffee.beautymod.dating.DateCommand;
import com.evacoffee.beautymod.dating.DatingEvents;  // For future event handling
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
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeautyMod implements ModInitializer, EntityComponentInitializer {
    public static final String MOD_ID = "beautymod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static MinecraftServer serverInstance;
    
    // Component Registration
    public static final ComponentKey<MarriageComponent> MARRIAGE_COMPONENT = 
        ComponentRegistry.getOrCreate(
            new Identifier(MOD_ID, "marriage"), 
            MarriageComponent.class
        );

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Beauty Mod");
        
        try {
            // Initialize networking
            Networking.registerServerReceivers();
            
            // Register server start/stop events
            ServerLifecycleEvents.SERVER_STARTING.register(server -> serverInstance = server);
            ServerLifecycleEvents.SERVER_STOPPED.register(server -> serverInstance = null);
            
            // Register commands
            registerCommands();
            
            // Register events
            MarriageEvents.register();
            DatingEvents.register();  // Register dating events
            
            LOGGER.info("Successfully initialized Beauty Mod");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize Beauty Mod", e);
            throw new RuntimeException("Failed to initialize Beauty Mod", e);
        }
    }

    private void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            MarriageCommand.register(dispatcher);
            DateCommand.register(dispatcher);  // Pass dispatcher to DateCommand
        });
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        try {
            // Register marriage component for players
            registry.registerForPlayers(
                MARRIAGE_COMPONENT,
                player -> new MarriageComponent(),
                RespawnCopyStrategy.ALWAYS_COPY
            );
            
            // Register dating components if needed
            // registry.registerForPlayers(...);
            
            LOGGER.debug("Registered entity component factories");
        } catch (Exception e) {
            LOGGER.error("Failed to register entity components", e);
            throw new RuntimeException("Failed to register entity components", e);
        }
    }

    public static MinecraftServer getServer() {
        return serverInstance;
    }
}