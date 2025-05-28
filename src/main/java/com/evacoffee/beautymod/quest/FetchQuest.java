package com.evacoffee.beautymod.quest;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

public class FetchQuest extends Quest {
    private final Item targetItem;
    private final int requiredCount;
    private int currentCount;
    
    public FetchQuest(Identifier id, String title, String description, int requiredAffection, Item targetItem, int requiredCount) {
        super(id, title, description, requiredAffection);
        this.targetItem = targetItem;
        this.requiredCount = requiredCount;
        this.currentCount = 0;
    }
    
    @Override
    public boolean checkCompletion(PlayerEntity player) {
        if (status != QuestStatus.IN_PROGRESS) return false;
        
        // Check player's inventory for the required items
        int found = 0;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == targetItem) {
                found += stack.getCount();
                if (found >= requiredCount) {
                    currentCount = requiredCount;
                    return true;
                }
            }
        }
        
        currentCount = Math.min(found, requiredCount);
        return false;
    }
    
    @Override
    public void onComplete(PlayerEntity player) {
        // Remove the items from the player's inventory
        int toRemove = requiredCount;
        for (int i = 0; i < player.getInventory().size() && toRemove > 0; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == targetItem) {
                int removeAmount = Math.min(toRemove, stack.getCount());
                stack.decrement(removeAmount);
                toRemove -= removeAmount;
            }
        }
        
        super.onComplete(player);
    }
    
    @Override
    public String getDescription() {
        String itemName = Registries.ITEM.getId(targetItem).getPath().replace('_', ' ');
        return String.format("%s\nProgress: %d/%d %s", 
            super.getDescription(), 
            currentCount, 
            requiredCount, 
            itemName);
    }
}
