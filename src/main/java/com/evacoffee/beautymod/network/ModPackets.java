package com.evacoffee.beautymod.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ModPackets {
    public static final Identifier RELATIONSHIP_UPDATE = new Identifier("beautymod", "relationship_update");
    public static final Identifier DATE_INVITE = new Identifier("beautymod", "date_invite");
    
    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(DATE_INVITE, 
            (server, player, handler, buf, responseSender) -> {
                UUID targetUuid = buf.readUuid();
                server.execute(() -> {
                    ServerPlayerEntity target = player.getServer().getPlayerManager().getPlayer(targetUuid);
                    if (target != null) {
                        // Handle date invitation
                        target.sendMessage(Text.literal(player.getName().getString() + 
                            " wants to go on a date with you!"));
                    }
                });
            });
    }
    
    public static void sendRelationshipUpdate(ServerPlayerEntity player, UUID targetUuid, int relationshipLevel) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(targetUuid);
        buf.writeInt(relationshipLevel);
        ServerPlayNetworking.send(player, RELATIONSHIP_UPDATE, buf);
    }
}