package com.evacoffee.beautymod.marriage;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import java.util.*;

/**
 * Simple anniversary tracker for player marriages.
 * Tracks marriage dates and notifies players on their anniversary.
 */
public class AnniversaryManager {
    // Maps player UUID to their partner's UUID
    private final Map<UUID, UUID> marriages = new HashMap<>();
    // Tracks when players got married (in millis since epoch)
    private final Map<UUID, Long> marriageTimes = new HashMap<>();
    
    /**
     * Register a marriage between two players
     */
    public void onMarriage(UUID player1, UUID player2) {
        long now = System.currentTimeMillis();
        marriages.put(player1, player2);
        marriages.put(player2, player1);
        marriageTimes.put(player1, now);
        marriageTimes.put(player2, now);
    }
    
    /**
     * Handle a divorce between two players
     */
    public void onDivorce(UUID player1, UUID player2) {
        marriages.remove(player1);
        marriages.remove(player2);
        marriageTimes.remove(player1);
        marriageTimes.remove(player2);
    }
    
    /**
     * Check if two players are married
     */
    public boolean areMarried(UUID player1, UUID player2) {
        return marriages.getOrDefault(player1, null) != null && 
               marriages.get(player1).equals(player2);
    }
    
    /**
     * Call this regularly (e.g., once per minute) to check for anniversaries
     */
    public void checkAnniversaries(MinecraftServer server) {
        long now = System.currentTimeMillis();
        
        // Track processed players to avoid duplicate messages
        Set<UUID> processed = new HashSet<>();
        
        for (Map.Entry<UUID, Long> entry : new HashMap<>(marriageTimes).entrySet()) {
            UUID playerId = entry.getKey();
            UUID partnerId = marriages.get(playerId);
            
            // Skip if we've already processed this pair
            if (processed.contains(playerId)) continue;
            
            long marriageTime = entry.getValue();
            long yearsMarried = (now - marriageTime) / (1000L * 60 * 60 * 24 * 365);
            
            // Check if today is their anniversary (within 1 day)
            long daysSinceAnniversary = ((now - marriageTime) / (1000L * 60 * 60 * 24)) % 365;
            
            if (daysSinceAnniversary == 0) {
                notifyAnniversary(server, playerId, partnerId, (int) yearsMarried);
                processed.add(playerId);
                processed.add(partnerId);
            }
        }
    }
    
    private void notifyAnniversary(MinecraftServer server, UUID player1Id, UUID player2Id, int years) {
        ServerPlayerEntity player1 = server.getPlayerManager().getPlayer(player1Id);
        ServerPlayerEntity player2 = server.getPlayerManager().getPlayer(player2Id);
        
        String message = "§6§lHappy " + years + " year anniversary! §r§7❤";
        if (player1 != null) player1.sendMessage(Text.literal(message), false);
        if (player2 != null) player2.sendMessage(Text.literal(message), false);
    }
}