package com.evacoffee.beautymod.network;

import com.evacoffee.beautymod.util.RateLimiter;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SecurePacketHandler {
    private static final Logger LOGGER = LogManager.getLogger("Beautify/SecurePacketHandler");
    private static final long RATE_LIMIT = 100;
    private static final long RATE_LIMIT_INTERVAL = 1;
    private static final int MAX_PACKET_SIZE = 32767;
    
    private static final RateLimiter<UUID> rateLimiter = new RateLimiter<>(RATE_LIMIT, RATE_LIMIT_INTERVAL, TimeUnit.SECONDS);
    
    public static void registerServerReceiver(Identifier channelId, PacketCallback callback) {
        ServerPlayNetworking.registerGlobalReceiver(channelId, 
            (server, player, handler, buf, responseSender) -> {
                try {
                    if (!rateLimiter.tryAcquire(player.getUuid())) {
                        LOGGER.warn("Rate limit exceeded for player: {}", player.getName().getString());
                        return;
                    }
                    
                    if (buf.readableBytes() > MAX_PACKET_SIZE) {
                        LOGGER.warn("Oversized packet from player: {} ({} bytes)", 
                            player.getName().getString(), buf.readableBytes());
                        return;
                    }
                    
                    server.execute(() -> {
                        try {
                            callback.onPacket(server, player, handler, buf, responseSender);
                        } catch (Exception e) {
                            LOGGER.error("Error processing packet from player: " + player.getName().getString(), e);
                        }
                    });
                    
                } catch (Exception e) {
                    LOGGER.error("Error handling packet from player: " + player.getName().getString(), e);
                }
            });
    }
    
    @FunctionalInterface
    public interface PacketCallback {
        void onPacket(MinecraftServer server, ServerPlayerEntity player, 
                     ServerPlayNetworkHandler handler, PacketByteBuf buf, 
                     PacketSender responseSender) throws Exception;
    }
}