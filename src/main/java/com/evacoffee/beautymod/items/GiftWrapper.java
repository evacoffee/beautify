package com.evacoffee.beautymod.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class GiftWrapper {
    public static final String GIFT_TAG = "WrappedGift";
    public static final String GIFT_ITEM = "GiftItem";
    public static final String GIFT_GIVER = "GiftGiver";
    public static final String GIFT_MESSAGE = "GiftMessage";
    
    public static ItemStack wrapGift(ItemStack gift, String giverName, String message) {
        if (gift.isEmpty()) return ItemStack.EMPTY;
        
        ItemStack wrappedGift = new ItemStack(ModItems.WRAPPED_GIFT);
        NbtCompound tag = wrappedGift.getOrCreateNbt();
        
        // Store the original item
        NbtCompound giftTag = new NbtCompound();
        gift.writeNbt(giftTag);
        tag.put(GIFT_ITEM, giftTag);
        
        // Store giver info
        tag.putString(GIFT_GIVER, giverName);
        if (message != null && !message.isEmpty()) {
            tag.putString(GIFT_MESSAGE, message);
        }
        
        // Set custom name
        String itemName = gift.getName().getString();
        wrappedGift.setCustomName(Text.translatable("item.beautymod.wrapped_gift.format", 
            giverName, itemName));
            
        return wrappedGift;
    }
    
    public static ItemStack unwrapGift(ItemStack wrappedGift) {
        if (!isWrappedGift(wrappedGift)) return ItemStack.EMPTY;
        
        NbtCompound tag = wrappedGift.getNbt();
        if (tag == null || !tag.contains(GIFT_ITEM)) return ItemStack.EMPTY;
        
        return ItemStack.fromNbt(tag.getCompound(GIFT_ITEM));
    }
    
    public static boolean isWrappedGift(ItemStack stack) {
        return stack.hasNbt() && 
               stack.getNbt() != null && 
               stack.getNbt().contains(GIFT_ITEM);
    }
    
    public static String getGiftGiver(ItemStack wrappedGift) {
        if (!isWrappedGift(wrappedGift)) return "";
        return wrappedGift.getNbt().getString(GIFT_GIVER);
    }
    
    public static String getGiftMessage(ItemStack wrappedGift) {
        if (!isWrappedGift(wrappedGift)) return "";
        return wrappedGift.getNbt().getString(GIFT_MESSAGE);
    }
    
    public static ItemStack createGiftItem() {
        return new ItemStack(ModItems.GIFT_BOX);
    }
}