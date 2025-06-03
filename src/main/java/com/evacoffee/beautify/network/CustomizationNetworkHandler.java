package com.evacoffee.beautify.network;

import com.evacoffee.beautify.BeautifyMod;
import com.evacoffee.beautify.customization.component.CustomizationComponent;
import com.evacoffee.beautify.customization.component.CustomizationComponents;
import com.evacoffee.beautify.customization.data.CustomizationData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class CustomizationNewtworkHandler {
    public static final Identifier S2C_CUSTOMIZATION_UPDATE_ID = new Identifier(BeautifyMod.MOD_ID, "s2c_customization_update");
    public static final Identifier C2S_CUSTOMIZATION_UPDATE_ID = new Identifier(BeautifyMod.MOD_ID, "c2s_customization_update");

    // Server-side registration
    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(C2S_CUSTOMIZATION_UPDATE_ID,
            (server, player, handler, buf, responseSender) -> {
                CustomizationData data = CustomizationData.fromNbt(buf.readNbt());
                server.execute(() -> {
                    CustomizationComponent component = CustomizationComponents.get(player);
                    component.setData(data);
                    // Sync with other players who are tracking this player
                    sendToTrackingPlayers(player, data);
                });
            });
    }

    // Client-side registration
    public static void registerClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(S2C_CUSTOMIZATION_UPDATE_ID,
            (client, handler, buf, responseSender) -> {
                UUID playerUuid = buf.readUuid();
                CustomizationData data = CustomizationData.fromNbt(buf.readNbt());
                client.execute(() -> {
                    if (client.world != null) {
                        PlayerEntity targetPlayer = client.world.getPlayerByUuid(playerUuid);
                        if (targetPlayer != null) {
                            CustomizationComponent component = CustomizationComponents.get(targetPlayer);
                            component.setData(data);
                        }
                    }
                });
            });
    }

    // Client sends update to server
    public static void sendUpdateToServer(CustomizationData data) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeNbt(data.toNbt());
        ClientPlayNetworking.send(C2S_CUSTOMIZATION_UPDATE_ID, buf);
    }

    // Server sends update to all relevant clients
    public static void sendToTrackingPlayers(ServerPlayerEntity sourcePlayer, CustomizationData data) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(sourcePlayer.getUuid());
        buf.writeNbt(data.toNbt());

        sourcePlayer.getServerWorld().getPlayers(player -> player != sourcePlayer).forEach(trackingPlayer -> {
            ServerPlayNetworking.send(trackingPlayer, S2C_CUSTOMIZATION_UPDATE_ID, buf);
        });
        // Also send to the source player if they need to confirm their own changes client-side
        // ServerPlayNetworking.send(sourcePlayer, S2C_CUSTOMIZATION_UPDATE_ID, buf); 
    }

    // Call this when a player logs in or when their tracking status changes
    public static void sendFullSyncToPlayer(ServerPlayerEntity targetPlayer, ServerPlayerEntity sourcePlayer) {
        CustomizationData data = CustomizationComponents.get(sourcePlayer).getData();
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(sourcePlayer.getUuid());
        buf.writeNbt(data.toNbt());
        ServerPlayNetworking.send(targetPlayer, S2C_CUSTOMIZATION_UPDATE_ID, buf);
    }
}

