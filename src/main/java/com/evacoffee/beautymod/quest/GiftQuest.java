package com.evacoffee.beautymod.quest;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class GiftQuest extends Quest {
    private final Item giftItem;
    private final int requiredCount;
    private int currentCount = 0;
    private final String npcName;
    
    public GiftQuest(Identifier id, String title, String description, int requiredAffection, 
                    Item giftItem, int requiredCount, String npcName) {
        super(id, title, description, requiredAffection);
        this.giftItem = giftItem;
        this.requiredCount = requiredCount;
        this.npcName = npcName;
    }
    
    @Override
    public boolean checkCompletion(PlayerEntity player) {
        // This quest is completed when the player gives the item to the NPC
        // The actual completion is handled in TalkToNpcCommand when the gift is given
        return currentCount >= requiredCount;
    }
    
    public boolean checkAndConsumeGift(PlayerEntity player) {
        // Check player's inventory for the required gift
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == giftItem) {
                stack.decrement(1);
                currentCount++;
                
                if (currentCount < requiredCount) {
                    player.sendMessage(Text.of(String.format("§aThanks! Just %d more %s please!", 
                        requiredCount - currentCount, 
                        Registries.ITEM.getId(giftItem).getPath().replace('_', ' '))), false);
                } else {
                    player.sendMessage(Text.of("§aThank you so much! You're so thoughtful!"), false);
                    return true;
                }
                break;
            }
        }
        return false;
    }
    
    @Override
    public String getDescription() {
        String itemName = Registries.ITEM.getId(giftItem).getPath().replace('_', ' ');
        return String.format("%s\nBring %d %s to %s\nProgress: %d/%d", 
            super.getDescription(),
            requiredCount, 
            itemName,
            npcName,
            currentCount, 
            requiredCount);
    }
    
    public String getNpcName() {
        return npcName;
    }
    
    public Item getGiftItem() {
        return giftItem;
    }
}
