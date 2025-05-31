package com.evacoffee.beautymod.client.gui; 

import net.fabric.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class RelationshipHud {
    private static final Identifier HEART_TEXTURE = new Identifier("textures/gui/icons.png:);
    private final MinecraftClient client;
    
    public RelationshipHud(MinecraftClient client) {
        this.client = client;
    }
    
    public void render(DrawContext context, float tickDelta) {
        if (client.player ==null) return;
        
        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();
        
        // Draw relationship status
        context.drawText(client.textRenderer, 
                        Text.literal("❤ Relationship Status: Single"), 
                        width - 150, 10, 0xFFFFFF, true);
        
        // Draw heart icons for relationship level
        int hearts = 5; // Get from player data
        for (int i = 0; i < hearts; i++) {
            context.drawTexture(HEART_TEXTURE, 
                              width - 150 + (i * 10), 20, 
                              16, 0, 9, 9, 256, 256);
        }
    }
}
