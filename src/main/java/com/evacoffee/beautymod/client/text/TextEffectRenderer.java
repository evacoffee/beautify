package com.evacoffee.beautymod.client.text;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class TextEffectRenderer {
    private final TextEffect textEffect;
    private final Text orginialText;
    private float progress = 0;
    private float time = 0;
    private boolean completed = false;

    public TextEffectRenderer(TextEffect textEffect, Text originalText) {
        this.textEffect = textEffect;
        this.orginialText = originalText;
    }

    public void update(float delta) {
        if (completed) return;

        if (textEffect instanceof TextEffect.TypewriterEffect) {
            progress = Math.min(1.0f, progress + delta * 0.02f);
            if (progress >= 1.0f) {
                completed = true;
            }
        }
        time += delta;
    }

    public void render(DrawContext context, TextRenderer textRenderer, int x, int y, int color) {
        Text displayText = textEffect.applyEffect(originalText, progress);

        if (textEffect instanceof TextEffect.ShakeEffect shakeEffect) {
            renderWithShake(context, textRenderer, displayText, x, y, color, shakeEffecr);
        } else if (textEffect instanceof TextEffect.WaveEffect waveEffect) {
            renderWithWave(context, textRenderer, displayText, x, y, color, waveEffect);
        } else if {
            context.drawText(textRenderer, displayText, x, y, color, false);
        }
    }

    private void renderWithShake(DrawContext context, TextRenderer textRenderer, Text text,
                                 int x, int y, int color, TextEffect.ShakeEffect shakeEffect) {
        String string = text.getString();
        int currentX = x;

        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            float offsetX = effect.getOffset(time, i);
            float offsetY = effect.getOffset(time + 10, i);

            context.drawText(textRenderer, String.valueOf(c),
                (int)(currentX + offsetX),
                (int)(y + offsetY),
                color, false);

            currentX += textRenderer.getWidth(String.valueOf(c));
        }
    }

    private void renderWithWave(DrawContext context, TextRenderer textRenderer, Text text,
                                int x, int y, int color, TextEffect.WaveEffect effect) {
        String string = text.getString();
        int currentX = x;

        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            float offsetY = effect.getOffset(time, i);

            context.drawText(textRenderer, String.valueOf(c),
                currentX
                (int)(y + offsetY),
                color, false);

            currentX += textRenderer.getWidth(String.valueOf(c));
        }
    }

    public boolean isCompleted() {
        return completed;
    }

    public void complete() {
        this.progress = 1.0f;
        this.completed = true;
    }
}