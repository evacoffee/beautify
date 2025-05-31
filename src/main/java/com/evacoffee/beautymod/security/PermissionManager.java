package com.evacoffee.beautymod.security;

import com.evacoffee.beautymod.BeautyMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PermissionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("BeautyMod/PermissionManager");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String PERMISSIONS_FILE = "permissions.json";
    
    // Thread-safe data structures
    private static final Map<UUID, Set<String>> permissions = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> groupPermissions = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> inheritance = new ConcurrentHashMap<>();
    
    // Thread pool for async operations
    private static final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "BeautyMod-PermissionManager");
        t.setDaemon(true);
        return t;
    });
    
    // Data class for JSON serialization
    private static class PermissionData {
        public Map<String, List<String>> playerPermissions = new HashMap<>();
        public Map<String, Set<String>> groupPermissions = new HashMap<>();
        public Map<String, Set<String>> inheritance = new HashMap<>();
    }
    
    /**
     * Initialize the permission system
     */
    public static void initialize() {
        // Load default groups and permissions
        initializeDefaultGroups();
        
        // Load saved permissions
        loadPermissionsAsync().exceptionally(e -> {
            LOGGER.error("Failed to load permissions", e);
            return null;
        });
        
        // Auto-save every 5 minutes
        new Timer("PermissionSaver", true).scheduleAtFixedRate(
            new TimerTask() {
                @Override
                public void run() {
                    savePermissionsAsync();
                }
            },
            5 * 60 * 1000,  // Initial delay
            5 * 60 * 1000   // Repeat every 5 minutes
        );
    }
    
    /**
     * Shutdown the permission system
     */
    public static void shutdown() {
        // Save synchronously on shutdown
        savePermissionsAsync().join();
        executor.shutdown();
    }
    
    private static void initializeDefaultGroups() {
        // Default group - all players
        groupPermissions.put("default", ConcurrentHashMap.newKeySet());
        
        // VIP group
        Set<String> vipPerms = ConcurrentHashMap.newKeySet();
        vipPerms.add("beautymod.style.vip");
        groupPermissions.put("vip", vipPerms);
        
        // Moderator group
        Set<String> modPerms = ConcurrentHashMap.newKeySet();
        modPerms.add("beautymod.mod.chat");
        modPerms.add("beautymod.mod.kick");
        groupPermissions.put("moderator", modPerms);
        
        // Admin group
        Set<String> adminPerms = ConcurrentHashMap.newKeySet();
        adminPerms.add("beautymod.admin");
        adminPerms.add("beautymod.mod.ban");
        groupPermissions.put("admin", adminPerms);
        
        // Set up inheritance
        inheritance.put("vip", ConcurrentHashMap.newKeySet(Set.of("default")));
        inheritance.put("moderator", ConcurrentHashMap.newKeySet(Set.of("vip")));
        inheritance.put("admin", ConcurrentHashMap.newKeySet(Set.of("moderator")));
    }
    
    /**
     * Add a permission to a player
     */
    public static void addPermission(UUID playerId, String permission) {
        if (playerId == null || permission == null) return;
        permissions.computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet()).add(permission.toLowerCase());
    }
    
    /**
     * Remove a permission from a player
     */
    public static void removePermission(UUID playerId, String permission) {
        if (playerId == null || permission == null) return;
        permissions.computeIfPresent(playerId, (k, perms) -> {
            perms.remove(permission.toLowerCase());
            return perms.isEmpty() ? null : perms;
        });
    }
    
    /**
     * Add a player to a group
     */
    public static void addToGroup(UUID playerId, String group) {
        if (playerId == null || group == null) return;
        permissions.computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet());
        // Group memberships are stored as permissions
        addPermission(playerId, "group." + group.toLowerCase());
    }
    
    /**
     * Remove a player from a group
     */
    public static void removeFromGroup(UUID playerId, String group) {
        if (playerId == null || group == null) return;
        removePermission(playerId, "group." + group.toLowerCase());
    }
    
    /**
     * Check if a player has a permission
     */
    public static boolean hasPermission(ServerPlayerEntity player, String permission) {
        if (player == null || permission == null) return false;
        
        // OPs have all permissions
        if (player.hasPermissionLevel(4)) return true;
        
        UUID playerId = player.getUuid();
        permission = permission.toLowerCase();
        
        // Check direct permissions
        if (permissions.getOrDefault(playerId, Collections.emptySet()).contains(permission)) {
            return true;
        }
        
        // Check group permissions
        return getPlayerGroups(playerId).stream()
            .anyMatch(group -> hasGroupPermission(group, permission, new HashSet<>()));
    }
    
    /**
     * Get all groups a player is in
     */
    public static Set<String> getPlayerGroups(UUID playerId) {
        Set<String> groups = new HashSet<>();
        Set<String> playerPerms = permissions.getOrDefault(playerId, Collections.emptySet());
        
        for (String perm : playerPerms) {
            if (perm.startsWith("group.")) {
                groups.add(perm.substring(6)); // Remove "group." prefix
            }
        }
        
        return groups;
    }
    
    /**
     * Check if a group has a permission (with inheritance)
     */
    private static boolean hasGroupPermission(String group, String permission, Set<String> checked) {
        if (checked.contains(group)) return false;
        checked.add(group);
        
        // Check current group
        if (groupPermissions.getOrDefault(group, Collections.emptySet()).contains(permission)) {
            return true;
        }
        
        // Check inherited groups
        for (String parent : inheritance.getOrDefault(group, Collections.emptySet())) {
            if (hasGroupPermission(parent, permission, checked)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Save permissions to disk asynchronously
     */
    public static CompletableFuture<Void> savePermissionsAsync() {
        return CompletableFuture.runAsync(() -> {
            Path saveFile = getSaveFile();
            try {
                PermissionData data = new PermissionData();
                
                // Save player permissions
                permissions.forEach((playerId, perms) -> {
                    data.playerPermissions.put(playerId.toString(), new ArrayList<>(perms));
                });
                
                // Save group data
                data.groupPermissions.putAll(groupPermissions);
                data.inheritance.putAll(inheritance);
                
                // Write to file
                String json = GSON.toJson(data);
                Files.createDirectories(saveFile.getParent());
                Files.writeString(saveFile, json);
                
                LOGGER.debug("Saved permissions to disk");
            } catch (Exception e) {
                LOGGER.error("Failed to save permissions to file", e);
            }
        }, executor);
    }
    
    /**
     * Load permissions from disk asynchronously
     */
    public static CompletableFuture<Void> loadPermissionsAsync() {
        return CompletableFuture.runAsync(() -> {
            Path saveFile = getSaveFile();
            if (!Files.exists(saveFile)) {
                LOGGER.info("No permission file found, using default permissions");
                return;
            }

            try {
                String json = Files.readString(saveFile);
                Type type = new TypeToken<PermissionData>(){}.getType();
                PermissionData data = GSON.fromJson(json, type);
                
                if (data != null) {
                    // Clear existing data
                    permissions.clear();
                    groupPermissions.clear();
                    inheritance.clear();
                    
                    // Load player permissions
                    data.playerPermissions.forEach((playerId, perms) -> {
                        permissions.put(UUID.fromString(playerId), ConcurrentHashMap.newKeySet(perms));
                    });
                    
                    // Load group data
                    data.groupPermissions.forEach((group, perms) -> {
                        groupPermissions.put(group, ConcurrentHashMap.newKeySet(perms));
                    });
                    
                    // Load inheritance
                    data.inheritance.forEach((group, parents) -> {
                        inheritance.put(group, ConcurrentHashMap.newKeySet(parents));
                    });
                    
                    LOGGER.info("Loaded permissions for {} players and {} groups", 
                        data.playerPermissions.size(), data.groupPermissions.size());
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load permissions from file", e);
            }
        }, executor);
    }
    
    /**
     * Get the path where permissions are saved
     */
    private static Path getSaveFile() {
        return MinecraftServer.getServer().getSavePath(WorldSavePath.ROOT)
                .resolve("beautymod/" + PERMISSIONS_FILE);
    }
}