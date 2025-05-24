package com.evacoffee.beautymod.entity.client;

import com.evacoffee.beautymod.entity.RomanceNPC;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class RomanceNPCRenderer extends MobEntityRenderer<RomanceNPC, BipedEntityModel<RomanceNPC>> {

    public RomanceNPCRenderer(EntityRendererFactory.Context context) {
        super(context, new BipedEntityModel<>(context.getPart(EntityModelLayers.PLAYER)), 0.5f);
    }

    @Override
    public Identifier getTexture(RomanceNPC entity) {
        String personality = entity.getPersonality();
        return new Identifier("beautymod", "textures/entity/romance_npc/" + personality + ".png");
    }
}
