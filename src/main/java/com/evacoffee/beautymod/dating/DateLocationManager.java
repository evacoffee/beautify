package com.evacoffee.beautymod.dating;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.Objects;

public class DateLocation {
    private final String name;
    private final DateType type;
    private final BlockPos position;
    private final RegistryKey<World> dimension;
    private final String ownerUuid;
    private int popularity; // Higher means more likely to be picked for random dates
    private boolean isPublic;

    public DateLocation(String name, DateType type, BlockPos position, World world, String ownerUuid) {
        this.name = name;
        this.type = type;
        this.position = position;
        this.dimension = world.getRegistryKey();
        this.ownerUuid = ownerUuid;
        this.popularity = 0;
        this.isPublic = false;
    }

    // Getters
    public String getName() { return name; }
    public DateType getType() { return type; }
    public BlockPos getPosition() { return position; }
    public RegistryKey<World> getDimension() { return dimension; }
    public String getOwnerUuid() { return ownerUuid; }
    public int getPopularity() { return popularity; }
    public boolean isPublic() { return isPublic; }

    // Setters
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
    public void incrementPopularity() { popularity = Math.min(popularity + 1, 100); }

    public double distanceTo(BlockPos pos) {
        return Math.sqrt(position.getSquaredDistance(pos));
    }

    public boolean isAtLocation(BlockPos pos, World world) {
        return this.dimension == world.getRegistryKey() && distanceTo(pos) < 10; // 10 block radius
    }

    public boolean isInSameDimension(World world) {
        return this.dimension == world.getRegistryKey();
    }

    // Serialization
    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("name", name);
        nbt.putString("type", type.name());
        nbt.putIntArray("pos", new int[]{position.getX(), position.getY(), position.getZ()});
        nbt.putString("dimension", dimension.getValue().toString());
        nbt.putString("owner", ownerUuid);
        nbt.putInt("popularity", popularity);
        nbt.putBoolean("isPublic", isPublic);
        return nbt;
    }

    public static DateLocation fromNbt(NbtCompound nbt, ServerWorld world) {
        String name = nbt.getString("name");
        DateType type = DateType.valueOf(nbt.getString("type"));
        int[] posArray = nbt.getIntArray("pos");
        BlockPos pos = new BlockPos(posArray[0], posArray[1], posArray[2]);
        String ownerUuid = nbt.getString("owner");
        
        DateLocation location = new DateLocation(name, type, pos, world, ownerUuid);
        location.popularity = nbt.getInt("popularity");
        location.isPublic = nbt.getBoolean("isPublic");
        
        return location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DateLocation that = (DateLocation) o;
        return name.equals(that.name) && 
               type == that.type && 
               position.equals(that.position) && 
               dimension.equals(that.dimension) && 
               ownerUuid.equals(that.ownerUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, position, dimension, ownerUuid);
    }

    @Override
    public String toString() {
        return String.format("%s (%s) at [%d, %d, %d] in %s", 
            name, 
            type, 
            position.getX(), 
            position.getY(), 
            position.getZ(),
            dimension.getValue().getPath());
    }
}