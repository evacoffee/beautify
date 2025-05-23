package com.evacoffee.beautymod.events;

import com.evacoffee.beautymod.item.ModItems;
import com.evacoffee.beautymod.love.LoveData;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;

public class GiftHandler {
    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!world.isClient) {
                if (player.getStackInHand(hand).getItem() == Items.ROSE_BUSH ||
                    player.getStackInHand(hand).getItem() == ModItems.LIPSTICK) {
                    LoveData.addLove(player, 5); // +5 love
                    player.sendMessage(net.minecraft.text.Text.of("❤️ You gained +5 Love!"), false);
                }
            }
            return ActionResult.PASS;
        });
    }
}
