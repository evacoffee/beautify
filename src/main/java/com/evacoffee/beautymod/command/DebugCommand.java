package com.evacoffee.beautymod.command;

import com.evacoffee.beautymod.BeautyMod;
import com.evacoffee.beautymod.config.ModConfig;
import com.evacoffee.beautymod.util.ModLogger;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class DebugCommand implements Command<ServerCommandSource> {
    private static boolean debugMode = false;
    
    public static int toggleDebug(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        debugMode = !debugMode;
        ModLogger.setDebugMode(debugMode);
        
        context.getSource().sendFeedback(
            Text.literal("Debug mode " + (debugMode ? "enabled" : "disabled")),
            false
        );
        
        return Command.SINGLE_SUCCESS;
    }
    
    public static int getDebugInfo(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        source.sendFeedback(Text.literal("=== BeautyMod Debug Info ==="), false);
        source.sendFeedback(Text.literal("Debug Mode: " + debugMode), false);
        // Add more debug information as needed
        
        return Command.SINGLE_SUCCESS;
    }
    
    @Override
    public int run(CommandContext<ServerCommandSource> context) {
        return getDebugInfo(context);
    }
}
Update your main mod class to use these new components:
java
CopyInsert
// Add to BeautyMod.java
public class BeautyMod implements ModInitializer, EntityComponentInitializer {
    // ... existing code ...
    
    public static final Identifier MARRIAGE_EFFECTS_PACKET = new Identifier(MOD_ID, "marriage_effects");
    private static ModConfig config;
    
    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Beauty Mod");
        
        try {
            // Load config
            config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
            ModLogger.setConfig(config);
            
            // Initialize networking with security
            SecureNetworking.registerReceivers(config);
            
            // Register commands
            CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
                DebugCommand.register(dispatcher);
                // ... register other commands
            });
            
            // ... rest of initialization
        } catch (Exception e) {
            LOGGER.error("Failed to initialize Beauty Mod", e);
            throw new RuntimeException("Failed to initialize Beauty Mod", e);
        }
    }
    
    public static ModConfig getConfig() {
        return config;
    }
}
Add these dependencies to your build.gradle:
gradle
CopyInsert
dependencies {
    // ... existing dependencies
    
    // Configuration
    modImplementation "com.terraformersmc:modmenu:4.1.2"
    modApi "com.terraformersmc:modmenu:4.1.2"
    modApi "com.terraformersmc:modmenu:4.1.2" {
        exclude(group: "net.fabricmc.fabric-api")
    }
    
    // Auto Config
    modApi "com.terraformersmc:modmenu:4.1.2"
    include modApi("me.shedaniel.cloth:cloth-config-fabric:6.2.57") {
        exclude(group: "net.fabricmc.fabric-api")
    }
}