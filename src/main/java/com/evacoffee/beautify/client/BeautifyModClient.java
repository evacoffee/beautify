package com.evacoffee.beautify.client;

import com.evacoffee.beautify.BeautifyMod;
import com.evacoffee.beautify.client.gui.CustomizationScreen;
import com.evacoffee.beautify.network.CustomizationNetworkHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class BeautifyModClient implements ClientModInitializer {
    public static KeyBinding openCustomizationScreenKey;

    @Override
    public void onInitializaClient() {
        BeautifyMod.LOGGER.info("BeautifyModClient initializing...");


        CustomizationNetworkHandler.registerClientReceivers();

        openCustomizationScreenKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key." + BeautifyMod.MOD_ID + ".open_customization",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "category." + BeautifyMod.MOD_ID + ".main"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openCustomizationScreenKey.wasPressed()) {
                if (client.player != null && client.currentScreen == null) {
                    client.setScreen(new CustomizationScreen(client.player));
                }
            }
        });
        

        ClientPlayConnectionEvents.JOIN.register((handler,sender,client) -> {
            if (client.player != null) {

            }
        });
        
        BeautifyMod.LOGGER.info("BeautifyModClient initialized.");
    }
}