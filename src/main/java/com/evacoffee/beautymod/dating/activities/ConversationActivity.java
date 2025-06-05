package com.evacoffee.beautymod.dating.activities;

import com.evacoffee.beautymod.BeautyMod;
import com.evacoffee.beautymod.client.gui.screen.ConversationScreen;
import com.evacoffee.beautymod.dating.DateActivity;
import com.evacoffee.beautymod.dating.ConversationTopic;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Collectors;

public class ConversationActivity extends DateActivity {
    public static final Identifier RESPONSE_PACKET_ID = new Identifier(BeautyMod.MOD_ID, "conversation_response");
    private final List<ConversationTopic> conversationTopics;
    private int currentTopicIndex = -1;
    private final Map<UUID, Map<ConversationTopic, Integer>> playerResponses = new HashMap<>();
    private final Map<UUID, Integer> relationshipScores = new HashMap<>();
    private final List<UUID> activeParticipants = new ArrayList<>();
    private boolean isActive = false;

    public ConversationActivity(UUID dateId, DateLocation location, List<UUID> participants, List<ConversationTopic> topics) {
        super(dateId, location, participants, topics.size() * 200); // 10 secs per topic
        this.conversationTopics = topics;
        participants.forEach(id -> {
            playerResponses.put(id, new HashMap<>());
            relationshipScores.put(id, 0);
            activeParticipants.add(id);
        });
        
        // Register packet handler on client
        if (MinecraftClient.getInstance() != null) {
            registerClientPacketHandler();
        }
    }

    @Environment(EnvType.CLIENT)
    private void registerClientPacketHandler() {
        ClientPlayNetworking.registerGlobalReceiver(RESPONSE_PACKET_ID, (client, handler, buf, responseSender) -> {
            UUID playerId = buf.readUuid();
            int topicIndex = buf.readInt();
            int responseIndex = buf.readInt();
            
            client.execute(() -> {
                if (currentTopicIndex == topicIndex && activeParticipants.contains(playerId)) {
                    handleResponse(playerId, conversationTopics.get(topicIndex), responseIndex);
                }
            });
        });
    }

    @Override
    public boolean canStart() {
        return allParticipantsPresent() && !conversationTopics.isEmpty();
    }

    @Override
    public void onStart() {
        isActive = true;
        participants.forEach(playerId -> {
            PlayerEntity player = location.getWorld().getPlayerByUuid(playerId);
            if (player != null) {
                player.sendMessage(Text.translatable("conversation.start"), false);
                
                // Open conversation screen for the local player
                if (MinecraftClient.getInstance() != null && 
                    player.getUuid().equals(MinecraftClient.getInstance().player.getUuid())) {
                    openConversationScreen();
                }
            }
        });
        startNextTopic();
    }
    
    @Environment(EnvType.CLIENT)
    private void openConversationScreen() {
        MinecraftClient.getInstance().setScreen(new ConversationScreen(
            this,
            conversationTopics,
            currentTopicIndex >= 0 ? conversationTopics.get(currentTopicIndex) : null,
            currentTopicIndex,
            relationshipScores.getOrDefault(MinecraftClient.getInstance().player.getUuid(), 0)
        ));
    }

    @Override
    public void tick() {
        super.tick();
        if (progress % 200 == 0 && currentTopicIndex < conversationTopics.size()) {
            startNextTopic();
        }
    }

    private void startNextTopic() {
        currentTopicIndex++;
        if (currentTopicIndex >= conversationTopics.size()) {
            complete();
            return;
        }
        
        ConversationTopic topic = conversationTopics.get(currentTopicIndex);
        activeParticipants.forEach(playerId -> {
            PlayerEntity player = location.getWorld().getPlayerByUuid(playerId);
            if (player != null) {
                player.sendMessage(Text.translatable("conversation.topic", topic.getDisplayText()), false);
                
                // Update screen for local player
                if (player.getUuid().equals(MinecraftClient.getInstance().player.getUuid())) {
                    MinecraftClient.getInstance().execute(() -> 
                        ((ConversationScreen)MinecraftClient.getInstance().currentScreen)
                            .updateTopic(topic, currentTopicIndex)
                    );
                }
            }
        });
    }
}

    public void handleResponse(UUID playerId, ConversationTopic topic, int responseIndex) {
        if (!isActive || !activeParticipants.contains(playerId)) return;
        
        // Store response
        playerResponses.get(playerId).put(topic, responseIndex);
        
        // Update relationship score based on response
        int relationshipChange = topic.getResponseImpact(responseIndex);
        int newScore = relationshipScores.get(playerId) + relationshipChange;
        relationshipScores.put(playerId, Math.max(0, Math.min(100, newScore)));
        
        // Notify all participants
        PlayerEntity player = location.getWorld().getPlayerByUuid(playerId);
        if (player != null) {
            Text message = Text.translatable("conversation.response", 
                player.getDisplayName(), 
                topic.getResponseText(responseIndex));
            broadcastMessage(message);
        }
        
        // If all players have responded, move to next topic
        if (allPlayersResponded()) {
            startNextTopic();
        }
    }
    
    private boolean allPlayersResponded() {
        return activeParticipants.stream().allMatch(playerId -> 
            playerResponses.get(playerId).containsKey(conversationTopics.get(currentTopicIndex))
        );
    }
    
    public void sendResponse(int responseIndex) {
        if (MinecraftClient.getInstance().player == null) return;
        
        UUID playerId = MinecraftClient.getInstance().player.getUuid();
        if (!activeParticipants.contains(playerId)) return;
        
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(playerId);
        buf.writeInt(currentTopicIndex);
        buf.writeInt(responseIndex);
        ClientPlayNetworking.send(RESPONSE_PACKET_ID, buf);
    }

    @Override
    public void onComplete() {
        isActive = false;
        broadcastMessage(Text.translatable("conversation.end"));
        
        // Close screen for local player
        if (MinecraftClient.getInstance().player != null && 
            activeParticipants.contains(MinecraftClient.getInstance().player.getUuid())) {
            MinecraftClient.getInstance().execute(() -> {
                if (MinecraftClient.getInstance().currentScreen instanceof ConversationScreen) {
                    MinecraftClient.getInstance().setScreen(null);
                }
            });
        }
        
        // Update relationship scores
        relationshipScores.forEach((playerId, score) -> {
            // Integrate with your relationship system here
            // Example: RelationshipManager.get(playerId).addPoints(score);
        });
    }

    @Override
    public void onCancel() {
        isActive = false;
        broadcastMessage(Text.translatable("conversation.cancelled"));
        
        // Close screen for local player
        if (MinecraftClient.getInstance().player != null && 
            activeParticipants.contains(MinecraftClient.getInstance().player.getUuid())) {
            MinecraftClient.getInstance().execute(() -> {
                if (MinecraftClient.getInstance().currentScreen instanceof ConversationScreen) {
                    MinecraftClient.getInstance().setScreen(null);
                }
            });
        }
    }
    
    private void broadcastMessage(Text message) {
        activeParticipants.stream()
            .map(location.getWorld()::getPlayerByUuid)
            .filter(Objects::nonNull)
            .forEach(player -> player.sendMessage(message, false));
    }