package com.evacoffee.beautymod;

import com.evacoffee.beautymod.entity.ModEntities;
import com.evacoffee.beautymod.entity.client.RomanceNPCRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class BeautyModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.ROMANCE_NPC, RomanceNPCRenderer::new);
    }
}
