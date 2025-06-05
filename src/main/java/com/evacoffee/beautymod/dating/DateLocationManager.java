package com.evacoffee.beautymod.dating;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DateLocationManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String SAVE_FILE = "date_locations.json";

    private final Map<UUID, DateLocation> locations = new ConcurrentHashMap<>();
    private final Path savePath;
    private final MinecraftServer server;

    public DateLocationManager(MinecraftServer server) {
        this.server = server;
        this.savePath = server.getRunDirectory().toPath()
                .resolve("saves")
                .resolve(server.getSaveProperties().getLevelName())
                .resolve(BeautyMod.MOD_ID)
                .resolve(SAVE_FILE);
    }

    public boolean removeLocation(UUID id) {
        if (locations.remove(id) != null) {
            saveLocations();
            return true;
        }
        return false;
    }

    public Optional<DateLocation> getLocation(UUID id) {
        return Optional.ofNullable(locations.get(id));
    }

    public List<DateLocation> getPublicLocations() {
        return locations.values().stream()
                .filter(DateLocations::isPublic)
                .toList();
    }

    public List<DateLocation> getPlayerLocations(String playerUuid) {
        return locations.values().stream()
                .filter(loc -> loc.getOwnerUuid().equals(playerUuid))
                .toList();
    }

    public void loadLocations() {
        if (!Files.exist(savePath)) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(savePath)) {
            List<DateLocationSaveData> savedData = GSON.fromJson(
                reader,
                new TypeToken<List<DateLocationSaveData>>(){}.getType()
            );

            locations.clear();
            for (DateLocationSaveData data : savedData) {
                World world  = server.getWorld(RegistryKey.of(Registry.WORLD_KEY, new Identifer(data.dimension)));
                if (world == null) {
                    DateLocation loc = new DateLocation(
                        data.name,
                        DateType.byId(data.typeId),
                        new BlockPos(data.x, data.y, data.z),
                        world,
                        data.ownerUuid,
                        data.isPublic,
                        data.capacity
                    );
                    locations.put(data.id, loc);
                }
            }
        } catch (IOException e) {
            BeautyMod.LOGGER.error("Failed to load date locations", e);
        }
    }

    public void saveLocations() {
        try {
            Files.createDirectories(savePath.getParent());
            List<DateLocationSaveData> saveData = locations.entrySet().stream()
                .map(entry -> new DateLocationSaveData(entry.getKey(), entry.getValue()))
                .toList();
                
            try (Writer writer = Files.newBufferedWriter(savePath)) {
                GSON.toJson(saveData, writer);
            }
        } catch (IOException e) {
            BeautyMod.LOGGER.error("Failed to save date locations", e);
        }
    }

    private static class DateLocationSaveData {
        final UUID id;
        final String name;
        final String typeId;
        final String dimension;
        final int x, y, z;
        final String ownerUuid;
        final boolean isPublic;
        final int capacity;

        DateLocationSaveData(UUID id, DateLocation location) {
            this.id = id;
            this.name = location.getName();
            this.typeId = location.getType().getId();
            this.dimension = location.getWorld().getRegistryKey().getValue().toString();
            BlockPos pos = location.getPosition();
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
            this.ownerUuid = location.getOwnerUuid();
            this.isPublic = location.isPublic();
            this.capacity = location.getCapacity();
        }
    }
}           