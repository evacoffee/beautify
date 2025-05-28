package com.evacoffee.beautymod.quest;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class DiningQuest extends Quest {
    private final Item foodItem;
    private final String restaurantName;
    private boolean hasEaten = false;
    
    public DiningQuest(Identifier id, String title, String description, 
                      int requiredAffection, Item foodItem, String restaurantName) {
        super(id, title, description, requiredAffection);
        this.foodItem = foodItem;
        this.restaurantName = restaurantName;
    }
    
    @Override
    public boolean checkCompletion(PlayerEntity player) {
        // Check if player has the required food item in their inventory
        if (hasEaten) return true;
        
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == foodItem) {
                stack.decrement(1);
                hasEaten = true;
                player.sendMessage(Text.of("§aThat was delicious! Thanks for the meal!"), false);
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void start(PlayerEntity player) {
        super.start(player);
        player.sendMessage(Text.of(String.format("§6Let's go to %s! I'm in the mood for some %s.", 
            restaurantName, 
            Registries.ITEM.getId(foodItem).getPath().replace('_', ' '))), false);
    }
    
    @Override
    public String getDescription() {
        String foodName = Registries.ITEM.getId(foodItem).getPath().replace('_', ' ');
        return String.format("%s\nTake me to %s for some %s", 
            super.getDescription(),
            restaurantName,
            foodName);
    }
    
    public Item getFoodItem() {
        return foodItem;
    }
}
