// MarriageShopScreen.java
package com.evacoffee.beautymod.marriage.shop;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

public class MarriageShopScreenHandler extends ScreenHandler {
    private final Inventory inventory;

    public MarriageShopScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(9));
    }

    public MarriageShopScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(null, syncId); // Replace null with your ScreenHandlerType
        this.inventory = inventory;
        
        // Shop inventory
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(inventory, i, 8 + i * 18, 20) {
                @Override
                public boolean canInsert(ItemStack stack) {
                    return false; // Prevent inserting items
                }
            });
        }

        // Player inventory
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 51 + y * 18));
            }
        }

        // Player hotbar
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 109));
        }

        // Add shop items
        inventory.setStack(0, new ItemStack(Items.DIAMOND_RING));
        // Add more shop items...
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY; // Disable shift-clicking
    }
}