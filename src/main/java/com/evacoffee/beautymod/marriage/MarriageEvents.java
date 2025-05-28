package com.evacoffee.beautymod.marriage;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MarriageEvents {
    public static final Identifier MARRIAGE_EFFECTS_PACKET = new Identifier("beautymod", "marriage_effects");
    private static final Map<UUID, Integer> playerTicks = new HashMap<>();

    public static void register() {
        // Register server tick event for proximity checks
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getPlayerManager().getPlayerList().forEach(player -> {
                int ticks = playerTicks.getOrDefault(player.getUuid(), 0) + 1;
                playerTicks.put(player.getUuid(), ticks);
                
                // Check every second (20 ticks)
                if (ticks % 20 == 0) {
                    checkMarriageEffects(player);
                }
            });
        });

        // Handle player death
        ServerPlayerEvents.AFTER_DEATH.register((player, damageSource) -> {
            if (player.world.isClient) return;
            
            MarriageComponent marriage = BeautyMod.MARRIAGE_COMPONENT.get(player);
            if (marriage.isMarried()) {
                ServerPlayerEntity spouse = player.getServer().getPlayerManager().getPlayer(marriage.getSpouseUuid());
                if (spouse != null) {
                    spouse.sendMessage(Text.literal("§cYour spouse has died!"), false);
                    // Apply sadness effect to spouse
                    spouse.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 600, 1, false, false));
                }
            }
        });
    }

    private static void checkMarriageEffects(ServerPlayerEntity player) {
        MarriageComponent marriage = BeautyMod.MARRIAGE_COMPONENT.get(player);
        if (!marriage.isMarried()) return;

        ServerPlayerEntity spouse = player.getServer().getPlayerManager().getPlayer(marriage.getSpouseUuid());
        if (spouse == null) return;

        double distance = player.squaredDistanceTo(spouse);
        boolean isNear = distance < 100.0; // 10 blocks

        if (isNear) {
            // Apply shared buffs
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 0, true, false));
            spouse.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 0, true, false));

            // Apply perk effects
            marriage.getUnlockedPerks().forEach(perk -> {
                switch (perk) {
                    case LUCKY_CHARM:
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, 100, 0, true, false));
                        spouse.addStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, 100, 0, true, false));
                        break;
                    case SOUL_BOND:
                        // Share health when one is low
                        if (player.getHealth() < player.getMaxHealth() / 2) {
                            player.heal(1.0f);
                            spouse.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 100, 0, true, false));
                        }
                        break;
                }
            });

            // Add XP for time spent together
            if (player.age % 600 == 0) { // Every 30 seconds
                marriage.addMarriageXP(5);
                BeautyMod.MARRIAGE_COMPONENT.get(spouse).addMarriageXP(5);
            }
        }

        syncMarriageEffects(player, isNear);
        syncMarriageEffects(spouse, isNear);
    }

    private static void syncMarriageEffects(ServerPlayerEntity player, boolean isNearSpouse) {
        if (player.networkHandler == null) return;
        
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(isNearSpouse);
        ServerPlayNetworking.send(player, MARRIAGE_EFFECTS_PACKET, buf);
    }
}