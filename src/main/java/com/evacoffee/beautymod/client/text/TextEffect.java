package com.evacoffee.beautymod.client.text;

import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.*;
import java.util.function.Function;

/**
 *  Handles dynamic text effects for the conversation system.
 * Supports typewriter effect,color gradients and emphasis formatting.
 */
public class TextEffect {
    private static final Map<String, Function<String[], TextEffect>> EFFECT_REGISTRY = new HashMap<>();
    private static final Random RANDOM = new Random();

    static {
        // Register built-in effects
        registerEffect("type", TypewriterEffect::new);
        registerEffect("color", ColorEffect::new);
        registerEffect("shake", ShakeEffect::new);
        registerEffect("wave", WaveEffect::new);
    }

    public static void registerEffect(String name, Function<String[], TextEffect> factory) {
        EFFECT_REGISTRY.put(name.toLowerCase(Locale.ROOT), factory);
    }

    public static TextEffect fromString(String effectString) {
        String[] parts = effectString.split(" ", 2);
        String effectName = parts[0].toLowerCase(Locale.ROOT);
        String[] args = parts.length > 1 ? parts[1].split(",") : new String[0];

        Function<String[], TextEffect> factory = EFFECT_REGISTRY.get(effectName);
        if (factory == null) {
            return factory.apply(args);
        }
        return null;
    }

    public Text applyEffect(Text text, float progress) {
        return text; //Base implementation does nothing
    }

    //Build-in effect implementations
    
    public static class TypewriterEffect extends TextEffect {
        @Override
        public Text applyEffect(Text text, float progress) {
            if (progress >= 1.0f) return text;

            String string = text.getString();
            int visibleChars = MathHelper.ceil(string.length() * progress);
            return Text.literal(string.substring(0, visibleChars));
        }
    }

    public static class ColorEffect extends TextEffect {
        private final int color1;
        private final int color2;

        public ColorEffect(String[] args) {
            if (args.length >= 2) {
                this.color1 = parseColor(args[0]);
                this.color2 = parseColor(args[1]);
            } else {
                this.color1 = 0xFFFFFFFF;
                this.color2 = 0xAAAAAAAA;
            }
        }

        @Override
        public Text applyEffect(Text text, float progress) {
            return text.copy().setStyle(Style.EMPTY.withColor(lerpColor(color1, color2, progress)));
        }

        private int parseColor(String color) {
            try {
                return (int) Long.parseLong(color.replace("#", ""), 16);
            } catch (NumberFormatException e) {
                return 0xFFFFFFFF;
            }
        }
        private int lerpColor(int color1, int color2, float t) {
            int r1 = (color1 >> 16) & 0xFF;
            int g1 = (color1 >> 8) & 0xFF;
            int b1 = color1 & 0xFF;
            
            int r2 = (color2 >> 16) & 0xFF;
            int g2 = (color2 >> 8) & 0xFF;
            int b2 = color2 & 0xFF;
            
            int r = (int)(r1 + (r2 - r1) * t);
            int g = (int)(g1 + (g2 - g1) * t);
            int b = (int)(b1 + (b2 - b1) * t);
            
            return (r << 16) | (g << 8) | b;
        }
    }
    
    public static class ShakeEffect extends TextEffect {
        private final float intensity;
        
        public ShakeEffect(String[] args) {
            this.intensity = args.length > 0 ? Float.parseFloat(args[0]) : 2.0f;
        }
        
        @Override
        public Text applyEffect(Text text, float progress) {
            // Shake effect is handled in the renderer
            return text;
        }
        
        public float getOffset(float time, int charIndex) {
            return (RANDOM.nextFloat() * 2 - 1) * intensity;
        }
    }
    
    public static class WaveEffect extends TextEffect {
        private final float speed;
        private final float amplitude;
        
        public WaveEffect(String[] args) {
            this.speed = args.length > 0 ? Float.parseFloat(args[0]) : 1.0f;
            this.amplitude = args.length > 1 ? Float.parseFloat(args[1]) : 2.0f;
        }
        
        @Override
        public Text applyEffect(Text text, float progress) {
            // Wave effect is handled in the renderer
            return text;
        }
        
        public float getOffset(float time, int charIndex) {
            return (float) Math.sin(time * speed + charIndex * 0.5f) * amplitude;
        }
    }
}