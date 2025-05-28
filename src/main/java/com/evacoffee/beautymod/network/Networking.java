package com.evacoffee.beautymod.network;

import com.evacoffee.beautymod.BeautyMod;
import com.evacoffee.beautymod.event.MarriageEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class Networking {
    // Register all server-side packet receivers
    public static void registerServerReceivers() {
        // Register marriage effects packet handler
        ServerPlayNetworking.registerGlobalReceiver(
            MarriageEvents.MARRIAGE_EFFECTS_PACKET,
            (server, player, handler, buf, responseSender) -> {
                // Client-side handling would go here
                boolean isNearSpouse = buf.readBoolean();
                // Handle the effect on client side if needed
            }
        );
    }
    
    // Helper method to sync marriage effects to client
    public static void syncMarriageEffects(ServerPlayerEntity player, boolean isNearSpouse) {
        if (player.networkHandler == null) return;
        
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(isNearSpouse);
        ServerPlayNetworking.send(player, MarriageEvents.MARRIAGE_EFFECTS_PACKET, buf);
    }
}
