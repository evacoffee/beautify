package com.evacoffee.beautymod.marriage;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarriageMod implements ModInitializer {
    public static final String MOD_ID = "beautymod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Marriage System");
        
        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            MarriageCommand.register(dispatcher);
        });
        
        // Register events
        MarriageEvents.registerEvents();
        
        // Register server start/stop events
        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);
    }
    
    private void onServerStarting(MinecraftServer server) {
        LOGGER.info("Marriage system is ready!");
    }
    
    private void onServerStopping(MinecraftServer server) {
        LOGGER.info("Saving marriage data...");
        // Add any necessary cleanup here
    }
}