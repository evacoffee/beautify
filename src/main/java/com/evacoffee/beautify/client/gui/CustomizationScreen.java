package com.evacoffee.beautify.client.gui;

import com.evacoffee.beautify.customization.component.CustomizationComponent;
import com.evacoffee.beautify.customization.component.CustomizationComponents;
import com.evacoffee.beautify.customization.data.CustomizationData;
import com.evacoffee.beautify.network.CustomizationNetworkHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class CustomizationScreen extends Screen {
    private final CustomizationData originalData;
    private CustomizationData currentData;
    private PlayerEntity player;


    private ButtonWidget skinToneButton;
    private ButtonWidget hairStyleButton;
    private ButtonWidget hairColorButton;
    // Add more buttons for other options

    public CustomizationScreen(PlayerEntity player) {
        super(Text.translatable("gui.beautify.customization.title"));
        this.player = player;
        CustomizationComponent component = CustomizationComponents.get(player);

        this.originalData = CustomizationData.fromNbt(component.getData().toNbt()); 
        this.currentData = CustomizationData.fromNbt(component.getData().toNbt());
    }

    @Override
    protected void init() {
        super.init();
        int buttonWidth = 150;
        int buttonHeight = 20;
        int startX = this.width / 2 - buttonWidth / 2;
        int startY = this.height / 4;
        int yOffset = buttonHeight + 5;


        skinToneButton = ButtonWidget.builder(
                Text.translatable("gui.beautify.customization.skin_tone").append(": " + currentData.getSkinTone()),
                button -> {

                })
            .dimensions(startX, startY, buttonWidth, buttonHeight).build();
        this.addDrawableChild(skinToneButton);


        hairStyleButton = ButtonWidget.builder(
                Text.translatable("gui.beautify.customization.hair_style").append(": " + currentData.getHairStyle()),
                button -> {

                })
            .dimensions(startX, startY + yOffset, buttonWidth, buttonHeight).build();
        this.addDrawableChild(hairStyleButton);
        

        hairColorButton = ButtonWidget.builder(
                Text.translatable("gui.beautify.customization.hair_color"),
                button -> {

                })
            .dimensions(startX, startY + yOffset * 2, buttonWidth, buttonHeight).build();
        this.addDrawableChild(hairColorButton);

        // Save Button
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.beautify.save"), button -> {
                CustomizationNetworkHandler.sendUpdateToServer(this.currentData);
                // Optionally update the local component immediately for responsiveness
                CustomizationComponents.get(this.player).setData(CustomizationData.fromNbt(this.currentData.toNbt()));
                this.close();
            })
            .dimensions(this.width / 2 - 100, this.height - 40, 98, 20).build());

        // Cancel Button
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.beautify.cancel"), button -> {
                this.close();
            })
            .dimensions(this.width / 2 + 2, this.height - 40, 98, 20).build());
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices); // Default background
        drawCenteredTextWithShadow(matrices, this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
        
        // TODO: Add a preview of the character model with currentData applied
        // This is complex and would involve rendering the player model in the GUI

        super.render(matrices, mouseX, mouseY, delta); // Renders buttons
    }

    @Override
    public boolean shouldPause() {
        return false; // So the world continues to render behind the screen
    }

    @Override
    public void close() {
        super.close();
        MinecraftClient.getInstance().setScreen(null); // Ensure screen is fully closed
    }
}