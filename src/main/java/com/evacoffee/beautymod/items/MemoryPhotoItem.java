package com.evacoffee.beautymod.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MemoryPhotoItem extends Item {
    private static final String MEMORY_TAG = "MemoryData";
    private static final String PHOTO_ID = "PhotoId";
    private static final String CAPTION = "Caption";
    private static final String TIMESTAMP = "Timestamp";
    private static final String LOCATION = "Location";
    private static final String PARTICIPANTS = "Participants";
    
    public MemoryPhotoItem(Settings settings) {
        super(settings);
    }
    
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient) return TypedActionResult.pass(user.getStackInHand(hand));
        
        ItemStack stack = user.getStackInHand(hand);
        if (!stack.hasNbt() || !stack.getNbt().contains(MEMORY_TAG)) {
            // Create a new memory photo
            if (user.isSneaking()) {
                createMemoryPhoto(stack, user, "A beautiful memory", world, user.getBlockPos());
                return TypedActionResult.success(stack);
            }
            return TypedActionResult.pass(stack);
        }
        
        // View existing memory
        viewMemoryPhoto(stack, user);
        return TypedActionResult.success(stack);
    }
    
    private void createMemoryPhoto(ItemStack stack, PlayerEntity creator, String caption, 
                                 World world, BlockPos location) {
        NbtCompound tag = stack.getOrCreateNbt();
        NbtCompound memoryTag = new NbtCompound();
        
        memoryTag.putUuid(PHOTO_ID, UUID.randomUUID());
        memoryTag.putString(CAPTION, caption);
        memoryTag.putLong(TIMESTAMP, world.getTime());
        
        // Store location
        NbtCompound posTag = new NbtCompound();
        posTag.putInt("x", location.getX());
        posTag.putInt("y", location.getY());
        posTag.putInt("z", location.getZ());
        memoryTag.put(LOCATION, posTag);
        
        // Store participants (example: nearby players)
        List<UUID> participants = new ArrayList<>();
        world.getPlayers().stream()
            .filter(p -> p.distanceTo(creator) < 10) // Within 10 blocks
            .forEach(p -> participants.add(p.getUuid()));
        
        NbtList participantsList = new NbtList();
        participants.forEach(uuid -> {
            NbtCompound uuidTag = new NbtCompound();
            uuidTag.putUuid("id", uuid);
            participantsList.add(uuidTag);
        });
        memoryTag.put(PARTICIPANTS, participantsList);
        
        tag.put(MEMORY_TAG, memoryTag);
        stack.setNbt(tag);
        
        // Update display
        stack.setCustomName(Text.literal("Photo: ").append(Text.literal(caption)));
    }
    
    private void viewMemoryPhoto(ItemStack stack, PlayerEntity viewer) {
        NbtCompound tag = stack.getNbt();
        if (tag == null || !tag.contains(MEMORY_TAG)) return;
        
        NbtCompound memoryTag = tag.getCompound(MEMORY_TAG);
        String caption = memoryTag.getString(CAPTION);
        long timestamp = memoryTag.getLong(TIMESTAMP);
        
        // Format time (simplified)
        String timeString = String.format("Taken at: %tF %<tR", timestamp);
        
        // Send message to player
        viewer.sendMessage(Text.literal("=== Memory Photo ==="), false);
        viewer.sendMessage(Text.literal("Caption: " + caption), false);
        viewer.sendMessage(Text.literal(timeString), false);
        
        // Show location if available
        if (memoryTag.contains(LOCATION, NbtElement.COMPOUND_TYPE)) {
            NbtCompound posTag = memoryTag.getCompound(LOCATION);
            BlockPos pos = new BlockPos(
                posTag.getInt("x"),
                posTag.getInt("y"),
                posTag.getInt("z")
            );
            viewer.sendMessage(Text.literal(String.format("Location: %d, %d, %d", 
                pos.getX(), pos.getY(), pos.getZ())), false);
        }
        
        // Show participants if available
        if (memoryTag.contains(PARTICIPANTS, NbtElement.LIST_TYPE)) {
            NbtList participants = memoryTag.getList(PARTICIPANTS, NbtElement.COMPOUND_TYPE);
            if (!participants.isEmpty()) {
                List<Text> names = new ArrayList<>();
                for (int i = 0; i < participants.size(); i++) {
                    UUID playerId = participants.getCompound(i).getUuid("id");
                    // In a real implementation, you'd look up the player's name
                    names.add(Text.literal("Player " + playerId.toString().substring(0, 8)));
                }
                viewer.sendMessage(Text.literal("With: ").append(Text.of(String.join(", ", 
                    names.stream().map(Text::getString).toList()))), false);
            }
        }
    }
    
    public static boolean hasMemory(ItemStack stack) {
        return stack.hasNbt() && stack.getNbt() != null && 
               stack.getNbt().contains(MEMORY_TAG);
    }
}