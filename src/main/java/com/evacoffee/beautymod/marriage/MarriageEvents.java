package com.evacoffee.beautymod.event;

import com.evacoffee.beautymod.BeautyMod;
import com.evacoffee.beautymod.marriage.MarriageComponent;
import com.evacoffee.beautymod.marriage.MarriagePerk;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MarriageEvents {
    public static final Identifier MARRIAGE_EFFECTS_PACKET = new Identifier(BeautyMod.MOD_ID, "marriage_effects");
    private static final int TICKS_PER_DAY = 24000;
    private static final int TICKS_PER_HOUR = 1000;

    public static void register() {
        // Player death notification
        ServerPlayerEvents.AFTER_DEATH.register((player, damageSource) -> {
            MarriageComponent marriage = BeautyMod.MARRIAGE_COMPONENT.get(player);
            if (marriage.isMarried()) {
                ServerPlayerEntity spouse = player.getServer().getPlayerManager().getPlayer(marriage.getSpouseUuid());
                if (spouse != null) {
                    spouse.sendMessage(Text.of("§cYour spouse has died!"), false);
                    // Apply a strength buff to the spouse when their partner dies
                    spouse.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.STRENGTH, 600, 1, false, false
                    ));
                }
            }
        });

        // Daily tick for anniversary checks
        ServerTickEvents.START_WORLD_TICK.register(world -> {
            if (world.getTime() % TICKS_PER_DAY == 0) {
                world.getPlayers().forEach(player -> {
                    if (player instanceof ServerPlayerEntity) {
                        MarriageComponent marriage = BeautyMod.MARRIAGE_COMPONENT.get(player);
                        if (marriage.isMarried()) {
                            // Check for anniversaries
                            if (marriage.checkAnniversary(world.getTime())) {
                                int years = marriage.getYearsMarried(world.getTime());
                                player.sendMessage(
                                    Text.of("§6§lHappy Anniversary! §eYou've been married for " + years + " year" + (years > 1 ? "s" : "") + "!"),
                                    false
                                );
                                // Award XP for anniversary
                                marriage.addMarriageXP(100 * years);
                                
                                // Notify spouse
                                ServerPlayerEntity spouse = world.getServer().getPlayerManager().getPlayer(marriage.getSpouseUuid());
                                if (spouse != null) {
                                    spouse.sendMessage(
                                        Text.of("§6§lHappy Anniversary! §eYou've been married for " + years + " year" + (years > 1 ? "s" : "") + "!"),
                                        false
                                    );
                                }
                            }
                            
                            // Apply marriage buffs
                            applyMarriageBuffs((ServerPlayerEntity) player, marriage);
                        }
                    }
                });
            }
            
            // Apply proximity buffs more frequently
            if (world.getTime() % (TICKS_PER_HOUR / 4) == 0) {
                world.getPlayers().forEach(player -> {
                    if (player instanceof ServerPlayerEntity) {
                        MarriageComponent marriage = BeautyMod.MARRIAGE_COMPONENT.get(player);
                        if (marriage.isMarried() && marriage.hasPerk(MarriagePerk.SOUL_BOND)) {
                            ServerPlayerEntity spouse = world.getServer().getPlayerManager().getPlayer(marriage.getSpouseUuid());
                            if (spouse != null && spouse.world == player.world && 
                                player.distanceTo(spouse) < 16.0) {
                                
                                // Apply regeneration when near spouse
                                player.addStatusEffect(new StatusEffectInstance(
                                    StatusEffects.REGENERATION, 300, 0, false, false
                                ));
                                
                                // Sync visual effects to client
                                syncMarriageEffects((ServerPlayerEntity) player, true);
                            } else {
                                syncMarriageEffects((ServerPlayerEntity) player, false);
                            }
                        }
                    }
                });
            }
        });
    }
    
    private static void applyMarriageBuffs(ServerPlayerEntity player, MarriageComponent marriage) {
        if (marriage.hasPerk(MarriagePerk.ETERNAL_BOND)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.LUCK, Integer.MAX_VALUE, 1, false, false
            ));
        }
        
        if (marriage.hasPerk(MarriagePerk.LUCKY_CHARM)) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.LUCK, 600, 0, false, false
            ));
        }
    }
    
    private static void syncMarriageEffects(ServerPlayerEntity player, boolean isNearSpouse) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(isNearSpouse);
        ServerPlayNetworking.send(player, MARRIAGE_EFFECTS_PACKET, buf);
    }
}