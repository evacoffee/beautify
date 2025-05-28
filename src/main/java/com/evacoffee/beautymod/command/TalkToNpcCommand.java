package com.evacoffee.beautymod.command;

import com.evacoffee.beautymod.BeautyMod;
import com.evacoffee.beautymod.dialogue.DialogueManager;
import com.evacoffee.beautymod.dialogue.DialogueNode;
import com.evacoffee.beautymod.memory.VillagerMemory;
import com.evacoffee.beautymod.player.AffectionComponent;
import com.evacoffee.beautymod.player.PlayerComponentInitializer;
import com.evacoffee.beautymod.quest.QuestManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class TalkToNpcCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("talk")
            .then(argument("npc", EntityArgumentType.entity())
                .executes(TalkToNpcCommand::execute)));
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();
        VillagerEntity npc = (VillagerEntity) EntityArgumentType.getEntity(context, "npc");
        String npcName = npc.getName().getString();
        
        // Check if player is holding a gift
        ItemStack heldItem = player.getStackInHand(Hand.MAIN_HAND);
        if (!heldItem.isEmpty() && isGiftItem(heldItem.getItem())) {
            return handleGift(player, npc, heldItem);
        }
        
        // Get or create dialogue for this NPC
        DialogueNode dialogue = DialogueManager.getDialogueFor(npc);
        
        // Display dialogue options with affection level
        AffectionComponent affection = PlayerComponentInitializer.getAffection(player);
        int affectionLevel = affection.getAffection(npc.getUuidAsString());
        
        player.sendMessage(Text.literal("\n=== " + npcName + " ===")
            .formatted(Formatting.GOLD, Formatting.BOLD), false);
            
        player.sendMessage(Text.literal("Affection: " + getAffectionMeter(affectionLevel))
            .formatted(getAffectionColor(affectionLevel)), false);
            
        player.sendMessage(Text.literal("\"" + dialogue.getText() + "\"")
            .formatted(Formatting.ITALIC, Formatting.YELLOW), false);
            
        // Display response options
        for (int i = 0; i < dialogue.getResponses().size(); i++) {
            player.sendMessage(Text.literal("[" + (i+1) + "] " + dialogue.getResponses().get(i).getText())
                .formatted(Formatting.GREEN), false);
        }
        
        // Add a memory about this interaction
        BeautyMod.addMemory(player, npcName, 
            VillagerMemory.MemoryType.SPECIAL_MOMENT, 
            "You had a conversation with " + npcName);
        
        // Check for active quests
        QuestManager questManager = BeautyMod.getQuestManager();
        if (questManager.hasActiveQuests(player)) {
            player.sendMessage(Text.literal("\n[!] You have active quests! Type /quests to check them.")
                .formatted(Formatting.GOLD), false);
        }
        
        // Show gift hint if holding a giftable item
        if (!heldItem.isEmpty() && isGiftableItem(heldItem.getItem())) {
            player.sendMessage(Text.literal("\n[!] You're holding a " + heldItem.getName().getString() + 
                ". Right-click the villager to give it as a gift!")
                .formatted(Formatting.LIGHT_PURPLE), false);
        }
        
        return Command.SINGLE_SUCCESS;
    }
    
    private static int handleGift(ServerPlayerEntity player, VillagerEntity npc, ItemStack gift) {
        String npcName = npc.getName().getString();
        Item giftItem = gift.getItem();
        
        // Calculate affection gain based on gift type
        int affectionGain = calculateAffectionGain(giftItem);
        
        // Update affection
        AffectionComponent affection = PlayerComponentInitializer.getAffection(player);
        String npcId = npc.getUuidAsString();
        int currentAffection = affection.getAffection(npcId);
        affection.addAffection(npcId, affectionGain);
        
        // Add to memory
        BeautyMod.addMemory(player, npcName, 
            VillagerMemory.MemoryType.GIFT, 
            "You gave " + npcName + " a " + giftItem.getName().getString());
            
        // Check if this is a new favorite gift
        if (affectionGain >= 10) {
            BeautyMod.addGiftPreference(player, npcName, giftItem);
        }
        
        // Consume the gift (if not in creative mode)
        if (!player.isCreative()) {
            gift.decrement(1);
        }
        
        // Send response
        String[] thanks = {
            "Thanks! I love " + giftItem.getName().getString() + "!",
            "For me? You shouldn't have!",
            "This is perfect, thank you!",
            "You remembered my favorite!"
        };
        
        player.sendMessage(Text.literal("\n" + npcName + " says: " + 
            thanks[player.getRandom().nextInt(thanks.length)])
            .formatted(Formatting.LIGHT_PURPLE), false);
            
        player.sendMessage(Text.literal("(" + npcName + "'s affection " + 
            (affectionGain > 0 ? "increased by " + affectionGain : "didn't change") + "!)")
            .formatted(Formatting.GRAY), false);
            
        return Command.SINGLE_SUCCESS;
    }
    
    private static boolean isGiftItem(Item item) {
        // Basic check - expand this with more giftable items
        return item == Items.POPPY || 
               item == Items.DANDELION ||
               item == Items.BLUE_ORCHID ||
               item == Items.ALLIUM ||
               item == Items.AZURE_BLUET ||
               item == Items.RED_TULIP ||
               item == Items.ORANGE_TULIP ||
               item == Items.WHITE_TULIP ||
               item == Items.PINK_TULIP ||
               item == Items.OXEYE_DAISY ||
               item == Items.CORNFLOWER ||
               item == Items.LILY_OF_THE_VALLEY ||
               item == Items.WITHER_ROSE ||
               item == Items.SUNFLOWER ||
               item == Items.LILAC ||
               item == Items.ROSE_BUSH ||
               item == Items.PEONY ||
               item == Items.APPLE ||
               item == Items.GOLDEN_APPLE ||
               item == Items.ENCHANTED_GOLDEN_APPLE ||
               item == Items.GOLD_NUGGET ||
               item == Items.GOLD_INGOT ||
               item == Items.EMERALD;
    }
    
    private static boolean isGiftableItem(Item item) {
        // Items that can be given as gifts but might not be in the main gift list yet
        return isGiftItem(item) || item.isFood();
    }
    
    private static int calculateAffectionGain(Item item) {
        // Base affection gain based on item type
        if (item == Items.WITHER_ROSE) return -10; // Oops, bad gift!
        if (item == Items.GOLDEN_APPLE) return 10;
        if (item == Items.ENCHANTED_GOLDEN_APPLE) return 20;
        if (item == Items.EMERALD) return 5;
        if (item == Items.GOLD_INGOT) return 4;
        if (item == Items.GOLD_NUGGET) return 1;
        if (item.isFood()) return 3; // Most food gives some affection
        if (item.getDefaultStack().isIn(net.minecraft.tag.ItemTags.FLOWERS)) return 8; // Flowers are always good
        return 2; // Default for other items
    }
    
    private static String getAffectionMeter(int level) {
        int hearts = level / 20; // Each heart is 20 points
        return "❤".repeat(Math.max(0, Math.min(10, hearts))) + 
               "§7" + "❤".repeat(Math.max(0, 10 - Math.min(10, hearts)));
    }
    
    private static Formatting getAffectionColor(int level) {
        if (level >= 100) return Formatting.DARK_RED;
        if (level >= 80) return Formatting.RED;
        if (level >= 60) return Formatting.GOLD;
        if (level >= 40) return Formatting.YELLOW;
        if (level >= 20) return Formatting.GREEN;
        return Formatting.GRAY;
    }
    
    private static int askOut(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) return 0;
        
        HitResult hit = player.raycast(5.0, 0.0f, false);
        
        if (hit.getType() == HitResult.Type.ENTITY && 
            ((EntityHitResult) hit).getEntity() instanceof VillagerEntity) {
            
            VillagerEntity villager = (VillagerEntity) ((EntityHitResult) hit).getEntity();
            String villagerName = villager.getName().getString();
            int affection = PlayerComponentInitializer.getAffection(player).getAffection(villagerName);
            
            if (affection < 50) {
                player.sendMessage(Text.of("§cI don't know you well enough yet. Let's be friends first!"), false);
                return 0;
            }
            
            // Random date type
            String[] dateTypes = {"a romantic walk", "dinner at the tavern", "a picnic in the meadow", "stargazing"};
            String dateType = dateTypes[RANDOM.nextInt(dateTypes.length)];
            
            player.sendMessage(Text.of(String.format("§6%s smiles and says: I'd love to go on %s with you!\n" +
                "§7(Use /acceptdate to confirm or /rejectdate to decline)", 
                villagerName, dateType)), false);
            
            // Store the pending date
            // In a real implementation, you'd store this in the player's data
            
            return 1;
        }
        
        player.sendMessage(Text.of("You need to be looking at a villager to ask them out."), false);
        return 0;
    }
    
    private static int listQuests(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) return 0;
        
        QuestManager questManager = PlayerComponentInitializer.getQuests(player).getQuestManager();
        if (questManager == null) {
            player.sendMessage(Text.of("§cNo quests available."), false);
            return 0;
        }
        
        player.sendMessage(Text.of("§6=== Your Active Quests ==="), false);
        var activeQuests = questManager.getActiveQuests();
        
        if (activeQuests.isEmpty()) {
            player.sendMessage(Text.of("§7No active quests. Talk to villagers to find some!"), false);
        } else {
            activeQuests.forEach(quest -> {
                player.sendMessage(Text.of("§e" + quest.getTitle() + "§r\n" + quest.getDescription()), false);
            });
        }
        
        return 1;
    }
    
    private static int startQuest(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) return 0;
        
        String questId = StringArgumentType.getString(context, "quest_id");
        PlayerComponentInitializer.getQuests(player).getQuestManager()
            .startQuest(new Identifier(BeautyMod.MOD_ID, questId));
        
        return 1;
    }
}
