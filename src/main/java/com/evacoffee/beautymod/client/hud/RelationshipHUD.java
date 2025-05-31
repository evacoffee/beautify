package com.evacoffee.beautymod.client.hud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class RelationshipHUD implements HudRenderCallback {
    private static final Identifier HEART_TEXTURE = new Identifier("beautymod", "textures/gui/heart.png");
    
    @Override
    public void onHudRender(DrawContext drawContext, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;
        
        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        
        // Draw relationship status
        String status = "Single"; // Get from your relationship system
        int textWidth = client.textRenderer.getWidth(status);
        drawContext.drawText(client.textRenderer, status, width - textWidth - 10, 10, 0xFFFFFF, true);
        
        // Draw hearts for relationship level
        int hearts = 3; // Get from your relationship system
        for (int i = 0; i < hearts; i++) {
            drawContext.drawTexture(HEART_TEXTURE, 
                width - 30 - (i * 12), 25, 
                0, 0, 10, 10, 10, 10);
        }
    }
}