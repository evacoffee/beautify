package com.evacoffee.beautymod.dating.activities;

import com.evacoffee.beautymod.dating.DateActivity;
import net.minecraft.text.Text;
import java.util.*;

public class MiniGameActivity extends DateActivity {
    private final String gameType;
    private final Map<UUID, Integer> scores = new HashMap<>();
    private boolean gameStarted = false;

    public MiniGameActivity(UUID dateId, DateLocation location, List<UUID> participants, String gameType) {
        super(dateId, location, participants, 1200); // 60 seconds for minigame
        this.gameType = gameType;
        participants.forEach(id -> scores.put(id, 0));
    }

    @Override
    public boolean canStart() {
        return allParticipantsPresent() && participants.size() >= 2;
    }

    @Override
    public void onStart() {
        gameStarted = true;
        broadcastMessage(Text.of("Starting " + gameType + " minigame!"));
        // Initialize minigame specific logic here
    }

    @Override
    public void tick() {
        super.tick();
        if (gameStarted) {
            // Update minigame state
            if (progress % 200 == 0) { // Every 10 seconds
                broadcastMessage(Text.of(gameType + " minigame in progress..."));
            }
        }
    }

    public void updateScore(UUID playerId, int points) {
        scores.put(playerId, scores.getOrDefault(playerId, 0) + points);
    }

    @Override
    public void onComplete() {
        // Determine winner(s)
        UUID winner = scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
        
        if (winner != null) {
            PlayerEntity winnerPlayer = location.getWorld().getPlayerByUuid(winner);
            if (winnerPlayer != null) {
                broadcastMessage(Text.of(winnerPlayer.getName().getString() + " wins the " + gameType + " minigame!"));
            }
        }
        
        // Award relationship points based on participation and performance
        scores.forEach((playerId, score) -> {
            // Integrate with relationship system
        });
    }

    @Override
    public void onCancel() {
        broadcastMessage(Text.of(gameType + " minigame was cancelled."));
    }

    private void broadcastMessage(Text message) {
        participants.stream()
                .map(location.getWorld()::getPlayerByUuid)
                .filter(Objects::nonNull)
                .forEach(player -> player.sendMessage(message, false));
    }
}