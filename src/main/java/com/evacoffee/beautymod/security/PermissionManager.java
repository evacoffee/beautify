package com.evacoffee.beautymod.security;

import com.evacoffee.beautymod.util.ModLogger;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PermissionManager {
    private static final Map<UUID, Set<Permission>> playerPermissions = new HashMap<>();
    
    public static boolean hasPermission(ServerPlayerEntity player, Permission permission) {
        // OP players have all permissions
        if (player.hasPermissionLevel(4)) {
            return true;
        }
        
        return playerPermissions.getOrDefault(player.getUuid(), new HashSet<>()).contains(permission);
    }
    
    public static void addPermission(UUID playerId, Permission permission) {
        playerPermissions.computeIfAbsent(playerId, k -> new HashSet<>()).add(permission);
    }
    
    public static void removePermission(UUID playerId, Permission permission) {
        playerPermissions.computeIfPresent(playerId, (k, v) -> {
            v.remove(permission);
            return v.isEmpty() ? null : v;
        });
    }
    
    public enum Permission {
        MARRIAGE_REQUEST("beautymod.marriage.request"),
        MARRIAGE_ACCEPT("beautymod.marriage.accept"),
        MARRIAGE_DENY("beautymod.marriage.deny"),
        GIVE_GIFT("beautymod.gift.give");
        
        private final String permissionNode;
        
        Permission(String permissionNode) {
            this.permissionNode = permissionNode;
        }
        
        public String getNode() {
            return permissionNode;
        }
    }
}