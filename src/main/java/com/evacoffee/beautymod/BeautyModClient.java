package com.evacoffee.beautymod;

import com.evacoffee.beautymod.client.gui.RelationshipHud;
import com.evacoffee.beautymod.config.ModConfig;
import com.evacoffee.beautymod.config.ModMenuIntegration;
import com.evacoffee.beautymod.network.ModPackets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class BeautyModClient implements ClientModInitializer {
    public static final RelationshipHud RELATIONSHIP_HUD = new RelationshipHud();
    private static KeyBinding openRelationshipScreen;
    
    @Override
    public void onInitializeClient() {
        // Register HUD renderer
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (MinecraftClient.getInstance().options.hudHidden) return;
            if (ModConfig.CONFIG.enableRelationshipHud) {
                RELATIONSHIP_HUD.render(drawContext, tickDelta);
            }
        });
        
        // Register client packet receivers
        ModPackets.registerClientReceivers();
        
        // Register key bindings
        openRelationshipScreen = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.beautymod.open_relationships",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "category.beautymod.general"
        ));
        
        // Register client tick event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openRelationshipScreen.wasPressed()) {
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("Opening relationships screen..."));
                    // TODO: Open relationships screen
                }
            }
        });
        
        // Register mod menu integration
        ModMenuIntegration.registerClient();
    }
}