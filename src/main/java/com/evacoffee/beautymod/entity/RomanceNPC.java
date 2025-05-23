package com.evacoffee.beautymod.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class RomanceNPC extends PathAwareEntity {
    public RomanceNPC(EntityType<? extends PathAwareEntity> type, World world) {
        super(type, world);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (!world.isClient) {
            player.sendMessage(Text.of("💬 NPC: Hello! Do you like beauty and love?"), false);
            // Here you can add your dialogue options & logic
            // Example: Increase love when player interacts
            // LoveData.addLove(player, 10);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        // Save custom data here
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        // Load custom data here
    }
}
