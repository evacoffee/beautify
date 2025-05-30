package com.evacoffee.beautymod.dating;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DateLocation {
    private final String name;
    private final DateType type;
    private final BlockPos position;
    private final World world;
    private final String ownerUuid;

    public DateLocation(String name, DateType type, BlockPos position, World world, String ownerUuid) {
        this.name = name;
        this.type = type;
        this.position = position;
        this.world = world;
        this.ownerUuid = ownerUuid;
    }

    // Getters
    public String getName() { return name; }
    public DateType getType() { return type; }
    public BlockPos getPosition() { return position; }
    public World getWorld() { return world; }
    public String getOwnerUuid() { return ownerUuid; }

    public double distanceTo(BlockPos pos) {
        return Math.sqrt(position.getSquaredDistance(pos));
    }

    public boolean isAtLocation(BlockPos pos, World world) {
        return this.world == world && distanceTo(pos) < 10; // 10 block radius
    }
}