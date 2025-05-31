package com.evacoffee.beautymod.network;

import com.evacoffee.beautymod.BeautyMod;
import com.evacoffee.beautymod.config.ModConfig;
import com.evacoffee.beautymod.util.ModLogger;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class SecureNetworking {
    private static final Map<ServerPlayerEntity, Long> lastPacketTimes = new HashMap<>();
    private static final long PACKET_COOLDOWN_MS = 50; // 50ms between packets
    
    public static void registerReceivers(ModConfig config) {
        // Register secure packet handler for marriage effects
        ServerPlayNetworking.registerGlobalReceiver(
            BeautyMod.MARRIAGE_EFFECTS_PACKET,
            (server, player, handler, buf, responseSender) -> {
                if (!validatePacket(player, buf, config)) {
                    ModLogger.warn("Invalid packet received from player: {}", player.getName().getString());
                    return;
                }
                
                boolean isNearSpouse = buf.readBoolean();
                // Process the packet
                server.execute(() -> {
                    // Handle the packet on the main thread
                    // ... existing packet handling code ...
                });
            }
        );
    }
    
    private static boolean validatePacket(ServerPlayerEntity player, PacketByteBuf buf, ModConfig config) {
        try {
            // Check packet size
            if (config.enablePacketValidation && buf.readableBytes() > config.maxPacketSize) {
                return false;
            }
            
            // Check packet cooldown
            long currentTime = System.currentTimeMillis();
            Long lastPacketTime = lastPacketTimes.get(player);
            if (lastPacketTime != null && (currentTime - lastPacketTime) < PACKET_COOLDOWN_MS) {
                return false; // Too many packets too quickly
            }
            lastPacketTimes.put(player, currentTime);
            
            return true;
        } catch (Exception e) {
            ModLogger.error("Error validating packet", e);
            return false;
        }
    }
    
    public static void syncMarriageEffects(ServerPlayerEntity player, boolean isNearSpouse) {
        if (player.networkHandler == null) return;
        
        try {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBoolean(isNearSpouse);
            ServerPlayNetworking.send(player, BeautyMod.MARRIAGE_EFFECTS_PACKET, buf);
        } catch (Exception e) {
            ModLogger.error("Failed to sync marriage effects to player: " + player.getName().getString(), e);
        }
    }
}