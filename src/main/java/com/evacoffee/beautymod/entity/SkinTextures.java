package com.evacoffee.beautymod.entity;

import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class SkinTextures {

    // A map of character IDs to their texture file paths
    public static final Map<String, Identifier> SKIN_MAP = new HashMap<>();

    static {
        SKIN_MAP.put("luna", new Identifier("beautymod", "textures/entity/npcs/luna.png"));
        SKIN_MAP.put("kai", new Identifier("beautymod", "textures/entity/npcs/kai.png"));
        SKIN_MAP.put("zara", new Identifier("beautymod", "textures/entity/npcs/zara.png"));
        SKIN_MAP.put("jett", new Identifier("beautymod", "textures/entity/npcs/jett.png"));
        SKIN_MAP.put("mimi", new Identifier("beautymod", "textures/entity/npcs/mimi.png"));
        SKIN_MAP.put("river", new Identifier("beautymod", "textures/entity/npcs/river.png"));
        SKIN_MAP.put("nyra", new Identifier("beautymod", "textures/entity/npcs/nyra.png"));
        SKIN_MAP.put("blaze", new Identifier("beautymod", "textures/entity/npcs/blaze.png"));
    }

    // Helper method to get skin for a given character
    public static Identifier getSkin(String npcId) {
        return SKIN_MAP.getOrDefault(npcId, new Identifier("beautymod", "textures/entity/npcs/default.png"));
    }
}
