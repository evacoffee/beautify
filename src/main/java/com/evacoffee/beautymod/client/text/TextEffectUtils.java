package com.evacoffee.beautymod.client.text;

import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextEffectUtils {
    private static final Pattern EFFECT_PATTERN = Pattern.compile("\\{([^}]+)\\}([^{]*)");
    private static final Map<String, TextEffect> effectCache = new HashMap<>();

    public static Text parseFormattedText(String input) {
        Text result = Text.empty();
        Matcher matcher = EFFECT_PATTERN.matcher(input);
        int lastEnd = 0;

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                result.append(Text.literal(input.substring(lastEnd, matcher.start())));
            }

            String effectStr = matcher.group(1);
            String content = matcher.group(2);

            TextEffect effect = effectCache.computeIfAbsent(effectStr, TextEffect::fromString);
            if (effect != null) {
                TextEffectRenderer renderer = new TextEffectRenderer(effect, Text.literal(content));
                result.append(Text.literal(content).setStyle(Style.EMPTY));
            } else {
                result.append(Text.literal("{" + effectStr + "}" + content));
            }

            lastEnd = matcher.end();
        }

        if (lastEnd < input.length()) {
            result.append(Text.literal(input.substring(lastEnd)));
        }

        return result;
    }

    public static Text createTypewriterText(String text) {
        return Text.literal(text).setStyle(Style.EMPTY.withColor(Formatting.WHITE));
    }

    public static Text createRainbowText(String text) {
        return Text.literal(text).setStyle(Style.EMPTY.withColor(0xFF0000));
    }
}