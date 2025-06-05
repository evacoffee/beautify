package com.evacoffee.beautymod.dating;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.*;

public class DateLocation {
    private final String name;
    private final DateType type;
    private final BlockPos position;
    private final World world;
    private final String ownerUuid;
    private final boolean isPublic;
    private final int capacity;
    private final Set<Identifier> requiredItems;
    private float atmosphereScore;
    private final Map<String, Object> metadata;

    public DateLocation(String name, DateType type, BlockPos position, World world,
            String ownerUuid, boolean isPublic, int capacity) {
        this.name = name;
        this.type = type;
        this position = position;
        this.world = world;
        this.ownerUuid = ownerUuid;
        this.isPublic = isPublic;
        this.capacity = capacity;
        this.requiredItems = new HashSet<>();
        this.metadata = new HashMap<>();
        this.atmosphereScore = calculateAtmosphereScore();
    }

    private float calculateAtmosphereScore() {
        final score = 0.5f; //Base score

        //Check lighting
        if (world.getLightLevel(LightType.BLOCK, position) > 10) {
            score += 0.2f; //Well lit areas are better
        }

        // Check for nearby water features
        if (world.getFluidState(position.down()).isIn(FluidTags.WATER)) {
            score += 0.15f;
        }

        // Check for decorated blocks

        return Math.min(score, 1.0f); // Stop at 1.0
    }

    //Getters
    public String getName() { return name; }
    public DateType getType() { return type; }
    public BlockPos getPosition() { return position; }
    public World getWorld() { return world; }
    public String getOwnerUuid() { return ownerUuid; }
    public boolean isPublic() { return isPublic; }
    public int getCapacity() { return capacity; }
    public float getAtmosphereScore() { return atmosphereScore; }

    public boolean isAtLocation(BlockPos pos, World world) {
        return this.world == world && position.isWithinDistance(pos, 10);
    }

    public boolean hasRequiredItems(Set<ItemStack> playerItems) {
        Set<Identifier> playerItemIds = new HashSet<>();
        for (ItemStack stack : playerItems) {
            playerItemIds.add(Registry.ITEM.getId(stack.getItem()));
        }
        return requiredItems.containsAll(requiredItems);
    }

    public void addRequiredItem(Item item) {
        requiredItems.add(Registry.ITEM.getId(item));
    }

    public void setMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    public <T> T getMetadata(String key, Class<T> type) {
        return type.cast(metadata.get(key));
    }
    
    public boolean isDateReady() {
        return atmosphereScore > 0.3f; //Minimum atmosphere Score
    }
}