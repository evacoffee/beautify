package com.evacoffee.beautymod.quest;

import com.evacoffee.beautymod.BeautyMod;
import com.evacoffee.beautymod.dating.DatingComponentInitializer;
import com.evacoffee.beautymod.marriage.MarriageComponentInitializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class MarriageQuest extends Quest {
    public enum Type {
        PROPOSE("Proposal Quest", "Propose to your beloved with a golden apple", 100, Items.GOLDEN_APPLE),
        WEDDING("Wedding Day", "Get married to your beloved", 200, Items.WHITE_TULIP),
        ANNIVERSARY("Anniversary", "Celebrate your first month of marriage", 300, Items.CAKE),
        HOME("Build a Home", "Set up a home with your spouse", 150, Items.OAK_DOOR),
        GIFT_EXCHANGE("Gift Exchange", "Exchange gifts with your spouse", 100, Items.GIFT);

        private final String title;
        private final String description;
        private final int rewardAffection;
        private final ItemStack icon;

        Type(String title, String description, int rewardAffection, ItemStack icon) {
            this.title = title;
            this.description = description;
            this.rewardAffection = rewardAffection;
            this.icon = icon;
        }

        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public int getRewardAffection() { return rewardAffection; }
        public ItemStack getIcon() { return icon; }
    }

    private final Type type;
    private boolean completed = false;

    public MarriageQuest(Identifier id, Type type) {
        super(id, type.title, type.description, type.rewardAffection);
        this.type = type;
    }

    @Override
    public boolean checkCompletion(PlayerEntity player) {
        if (completed) return true;
        
        if (!(player instanceof ServerPlayerEntity)) return false;
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        
        switch (type) {
            case PROPOSE:
                // Check if player has proposed to someone
                return hasProposed(serverPlayer);
                
            case WEDDING:
                // Check if player is married
                return isMarried(serverPlayer);
                
            case ANNIVERSARY:
                // Check if player has been married for 30 in-game days
                return checkMarriageDuration(serverPlayer, 30);
                
            case HOME:
                // Check if player has set a home with their spouse
                return hasSetHome(serverPlayer);
                
            case GIFT_EXCHANGE:
                // Check if player has exchanged gifts with their spouse
                return hasExchangedGifts(serverPlayer);
                
            default:
                return false;
        }
    }
    
    private boolean hasProposed(ServerPlayerEntity player) {
        // Implementation depends on your proposal tracking
        return false; // Placeholder
    }
    
    private boolean isMarried(ServerPlayerEntity player) {
        return MarriageComponentInitializer.getMarriage(player).isMarried();
    }
    
    private boolean checkMarriageDuration(ServerPlayerEntity player, int daysRequired) {
        if (!isMarried(player)) return false;
        
        long weddingDay = MarriageComponentInitializer.getMarriage(player).getWeddingDay();
        long currentDay = player.getWorld().getTimeOfDay() / 24000L;
        long daysMarried = currentDay - weddingDay;
        
        return daysMarried >= daysRequired;
    }
    
    private boolean hasSetHome(ServerPlayerEntity player) {
        return MarriageComponentInitializer.getMarriage(player).getHomePos() != null;
    }
    
    private boolean hasExchangedGifts(ServerPlayerEntity player) {
        // Implementation depends on your gift tracking
        return false; // Placeholder
    }
    
    @Override
    public void onComplete(PlayerEntity player) {
        if (completed) return;
        completed = true;
        
        if (player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            
            // Reward affection
            int affection = getRewardAffection();
            // Add affection logic here
            
            // Notify player
            serverPlayer.sendMessage(
                Text.literal("Quest Complete: " + getTitle())
                    .formatted(Formatting.GREEN), 
                false
            );
            
            serverPlayer.sendMessage(
                Text.literal("+" + affection + " affection with your spouse")
                    .formatted(Formatting.GOLD),
                false
            );
            
            // Trigger achievements if applicable
            switch (type) {
                case WEDDING:
                    BeautyMod.getMarriageManager().onWeddingDay(serverPlayer);
                    break;
                case ANNIVERSARY:
                    BeautyMod.getMarriageManager().onAnniversary(serverPlayer);
                    break;
            }
        }
    }
    
    @Override
    public ItemStack getIcon() {
        return type.getIcon();
    }
}
