package com.evacoffee.beautymod.quest;

import com.evacoffee.beautymod.BeautyMod;
import com.evacoffee.beautymod.dating.DatingComponentInitializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class DatingQuest extends Quest {
    public enum Type {
        WALKING_DATE("Scenic Walk", "Take a romantic walk together", 50, Items.POPPY, 100, 50, 100),
        PICNIC_DATE("Picnic Date", "Have a romantic picnic together", 75, Items.BREAD, 0, 30, 150),
        SIGHTSEEING("Sightseeing", "Visit beautiful locations together", 100, Items.MAP, 200, 3, 200),
        STARGAZING("Stargazing", "Spend the night under the stars", 125, Items.ENDER_EYE, 0, 1, 100),
        ADVENTURE("Adventure Together", "Go on an exciting adventure", 150, Items.IRON_SWORD, 300, 2, 150);

        private final String title;
        private final String description;
        private final int baseAffection;
        private final ItemStack icon;
        private final int requiredDistance;
        private final int requiredTime; // in minutes
        private final int affectionMultiplier;

        Type(String title, String description, int baseAffection, ItemStack icon, 
             int requiredDistance, int requiredTime, int affectionMultiplier) {
            this.title = title;
            this.description = description;
            this.baseAffection = baseAffection;
            this.icon = icon;
            this.requiredDistance = requiredDistance;
            this.requiredTime = requiredTime;
            this.affectionMultiplier = affectionMultiplier;
        }

        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public int getBaseAffection() { return baseAffection; }
        public ItemStack getIcon() { return icon; }
        public int getRequiredDistance() { return requiredDistance; }
        public int getRequiredTime() { return requiredTime; }
        public int getAffectionMultiplier() { return affectionMultiplier; }
    }

    private final Type type;
    private BlockPos startPos;
    private long startTime;
    private boolean isActive = false;
    private int distanceTraveled = 0;
    private int timeSpent = 0; // in ticks (20 ticks = 1 second)
    private int affectionGained = 0;
    private final Random random = new Random();

    public DatingQuest(Identifier id, Type type) {
        super(id, type.title, type.description, type.baseAffection);
        this.type = type;
    }

    @Override
    public boolean checkCompletion(PlayerEntity player) {
        if (!isActive || !(player instanceof ServerPlayerEntity)) return false;
        
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        
        // Check if still on date with the same partner
        if (!DatingComponentInitializer.isOnDateWith(serverPlayer, getDatePartner(serverPlayer))) {
            failDate(serverPlayer, "Your date partner left!");
            return false;
        }
        
        // Update distance traveled
        if (startPos != null) {
            distanceTraveled = (int) Math.sqrt(player.getBlockPos().getSquaredDistance(startPos));
        }
        
        // Update time spent
        timeSpent = (int) ((serverPlayer.getWorld().getTime() - startTime) / 20 / 60); // Convert to minutes
        
        // Check if date requirements are met
        boolean distanceMet = type.requiredDistance <= 0 || distanceTraveled >= type.requiredDistance;
        boolean timeMet = timeSpent >= type.requiredTime;
        
        // Calculate affection gain based on progress
        float progress = Math.min(1.0f, Math.min(
            type.requiredDistance > 0 ? (float)distanceTraveled / type.requiredDistance : 1.0f,
            type.requiredTime > 0 ? (float)timeSpent / type.requiredTime : 1.0f
        ));
        
        affectionGained = (int) (type.baseAffection * progress);
        
        return distanceMet && timeMet;
    }
    
    private ServerPlayerEntity getDatePartner(ServerPlayerEntity player) {
        // Get the player's date partner
        // Implementation depends on your dating system
        return null; // Replace with actual implementation
    }
    
    private void failDate(ServerPlayerEntity player, String reason) {
        player.sendMessage(Text.literal("§cDate failed: " + reason).formatted(Formatting.RED), false);
        isActive = false;
        // Handle failed date logic
    }
    
    @Override
    public void start(PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity)) return;
        
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        
        // Check if already on a date
        if (DatingComponentInitializer.getDating(serverPlayer).isOnDate()) {
            serverPlayer.sendMessage(Text.literal("§cYou're already on a date!").formatted(Formatting.RED), false);
            return;
        }
        
        // Start the date
        this.startPos = player.getBlockPos();
        this.startTime = serverPlayer.getWorld().getTime();
        this.isActive = true;
        this.affectionGained = 0;
        
        // Notify player
        serverPlayer.sendMessage(Text.literal("§6§lDate Started: §e" + type.title + "§r")
            .formatted(Formatting.BOLD, Formatting.GOLD), false);
        serverPlayer.sendMessage(Text.literal("§7"" + type.description + """).formatted(Formatting.ITALIC), false);
        
        if (type.requiredDistance > 0) {
            serverPlayer.sendMessage(Text.literal("§7- Walk at least §a" + type.requiredDistance + " blocks").formatted(Formatting.GRAY), false);
        }
        if (type.requiredTime > 0) {
            serverPlayer.sendMessage(Text.literal("§7- Spend at least §a" + type.requiredTime + " minutes").formatted(Formatting.GRAY), false);
        }
    }
    
    @Override
    public void onComplete(PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        
        // Calculate final affection gain with multiplier
        int finalAffection = (int) (affectionGained * (1.0f + random.nextFloat() * 0.5f)); // 0-50% bonus
        
        // Notify player
        serverPlayer.sendMessage(Text.literal("§a§lDate Complete!")
            .formatted(Formatting.BOLD, Formatting.GREEN), false);
        serverPlayer.sendMessage(Text.literal("§7You gained §a+" + finalAffection + " affection§7!")
            .formatted(Formatting.GRAY), false);
        
        // Award affection
        // Add your affection system integration here
        
        // End the date
        DatingComponentInitializer.endDate(serverPlayer, getDatePartner(serverPlayer), true);
        isActive = false;
        
        // Random chance to receive a gift
        if (random.nextFloat() < 0.3f) { // 30% chance
            ItemStack gift = getRandomGift();
            if (!serverPlayer.getInventory().insertStack(gift)) {
                serverPlayer.dropItem(gift, false);
            }
            serverPlayer.sendMessage(Text.literal("§6Your date gave you a gift!").formatted(Formatting.GOLD), false);
        }
    }
    
    private ItemStack getRandomGift() {
        // Simple gift table - expand as needed
        ItemStack[] gifts = {
            new ItemStack(Items.FLOWER_POT),
            new ItemStack(Items.COOKIE, 3),
            new ItemStack(Items.POPPY),
            new ItemStack(Items.DANDELION),
            new ItemStack(Items.BLUE_ORCHID),
            new ItemStack(Items.ALLIUM),
            new ItemStack(Items.AZURE_BLUET),
            new ItemStack(Items.RED_TULIP),
            new ItemStack(Items.ORANGE_TULIP),
            new ItemStack(Items.WHITE_TULIP),
            new ItemStack(Items.PINK_TULIP),
            new ItemStack(Items.OXEYE_DAISY),
            new ItemStack(Items.CORNFLOWER),
            new ItemStack(Items.LILY_OF_THE_VALLEY)
        };
        
        return gifts[random.nextInt(gifts.length)].copy();
    }
    
    @Override
    public ItemStack getIcon() {
        return type.getIcon();
    }
    
    public int getProgress() {
        if (!isActive) return 0;
        
        float distanceProgress = type.requiredDistance > 0 ? 
            Math.min(1.0f, (float)distanceTraveled / type.requiredDistance) : 1.0f;
        float timeProgress = type.requiredTime > 0 ? 
            Math.min(1.0f, (float)timeSpent / type.requiredTime) : 1.0f;
            
        return (int) (Math.min(distanceProgress, timeProgress) * 100);
    }
    
    public String getProgressText() {
        if (!isActive) return "Not started";
        
        StringBuilder sb = new StringBuilder();
        
        if (type.requiredDistance > 0) {
            sb.append("Walked: ").append(distanceTraveled).append("/").append(type.requiredDistance).append(" blocks\n");
        }
        if (type.requiredTime > 0) {
            sb.append("Time: ").append(timeSpent).append("/").append(type.requiredTime).append(" minutes");
        }
        
        return sb.toString();
    }
}
