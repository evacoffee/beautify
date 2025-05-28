package com.evacoffee.beautymod.marriage;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MarriageEvents {
    private static final Map<UUID, Long> lastHeartbeat = new HashMap<>();
    private static final long HEARTBEAT_INTERVAL = 20 * 5; // 5 seconds in ticks

    public static void registerEvents() {
        // Register player tick event for proximity effects
        ServerTickEvents.START_PLAYER_TICK.register(player -> {
            if (!(player instanceof ServerPlayerEntity)) return;
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            MarriageComponent marriage = MarriageComponentInitializer.get(serverPlayer);
            
            if (!marriage.isMarried()) return;
            
            // Check if spouse is online and nearby
            UUID spouseUuid = marriage.getSpouseUuid();
            if (spouseUuid != null) {
                ServerPlayerEntity spouse = serverPlayer.getServer().getPlayerManager().getPlayer(spouseUuid);
                if (spouse != null && serverPlayer.getWorld() == spouse.getWorld()) {
                    double distance = serverPlayer.squaredDistanceTo(spouse);
                    
                    // Apply proximity-based effects
                    if (distance <= 256) { // 16 blocks
                        applyProximityEffects(serverPlayer, spouse, marriage);
                    }
                    
                    // Heartbeat effect when very close
                    if (distance <= 16) { // 4 blocks
                        long currentTime = serverPlayer.getWorld().getTime();
                        if (currentTime - lastHeartbeat.getOrDefault(serverPlayer.getUuid(), 0L) > HEARTBEAT_INTERVAL) {
                            lastHeartbeat.put(serverPlayer.getUuid(), currentTime);
                            serverPlayer.getWorld().playSound(
                                null, 
                                serverPlayer.getX(), 
                                serverPlayer.getY(), 
                                serverPlayer.getZ(), 
                                SoundEvents.ENTITY_PLAYER_LEVELUP, 
                                SoundCategory.PLAYERS, 
                                0.5f, 
                                1.5f
                            );
                        }
                    }
                }
            }
        });

        // Register entity interaction event for spouse interactions
        UseEntityCallback.EVENT.register((player, world, hand, target, hit) -> {
            if (!(player instanceof ServerPlayerEntity) || !(target instanceof PlayerEntity)) {
                return ActionResult.PASS;
            }

            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            ServerPlayerEntity targetPlayer = (ServerPlayerEntity) target;
            MarriageComponent marriage = MarriageComponentInitializer.get(serverPlayer);
            
            // Check if interacting with spouse
            if (marriage.isMarried() && marriage.getSpouseUuid().equals(targetPlayer.getUuid())) {
                // Play heart particles or other effects
                world.playSound(
                    null, 
                    player.getX(), 
                    player.getY(), 
                    player.getZ(), 
                    SoundEvents.ENTITY_VILLAGER_YES, 
                    SoundCategory.PLAYERS, 
                    1.0f, 
                    1.0f
                );
                
                // Send a message to both players
                String[] loveMessages = {
                    "You feel a spark with %s!",
                    "Your heart skips a beat when near %s!",
                    "You share a special moment with %s!"
                };
                
                String message = String.format(
                    loveMessages[world.random.nextInt(loveMessages.length)],
                    targetPlayer.getEntityName()
                );
                
                player.sendMessage(Text.literal(message).formatted(net.minecraft.util.Formatting.LIGHT_PURPLE), false);
                targetPlayer.sendMessage(Text.literal(message.replace("%s", player.getEntityName())).formatted(net.minecraft.util.Formatting.LIGHT_PURPLE), false);
                
                return ActionResult.SUCCESS;
            }
            
            return ActionResult.PASS;
        });

        // Register block break event for shared mining
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (!(player instanceof ServerPlayerEntity)) return true;
            
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            MarriageComponent marriage = MarriageComponentInitializer.get(serverPlayer);
            
            if (!marriage.isMarried()) return true;
            
            // Check if spouse is nearby for shared mining benefits
            UUID spouseUuid = marriage.getSpouseUuid();
            if (spouseUuid != null) {
                ServerPlayerEntity spouse = serverPlayer.getServer().getPlayerManager().getPlayer(spouseUuid);
                if (spouse != null && serverPlayer.getWorld() == spouse.getWorld() && 
                    serverPlayer.squaredDistanceTo(spouse) <= 256) {
                    
                    // Apply fortune effect if both have the perk
                    if (marriage.hasPerk(MarriagePerk.SHARED_LOOT) && 
                        MarriageComponentInitializer.get(spouse).hasPerk(MarriagePerk.SHARED_LOOT)) {
                        
                        // Double drops or other benefits
                        // Note: Actual drop modification would need to be handled in a mixin
                        player.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.LUCK, 100, 0, true, false
                        ));
                    }
                }
            }
            
            return true;
        });
    }

    private static void applyProximityEffects(ServerPlayerEntity player1, ServerPlayerEntity player2, MarriageComponent marriage1) {
        MarriageComponent marriage2 = MarriageComponentInitializer.get(player2);
        
        // Apply effects based on unlocked perks
        if (marriage1.hasPerk(MarriagePerk.SHARED_XP) && marriage2.hasPerk(MarriagePerk.SHARED_XP)) {
            // Apply XP bonus when both spouses are nearby
            player1.addStatusEffect(new StatusEffectInstance(
                StatusEffects.EXPERIENCE_BOOST, 100, 0, true, false
            ));
            player2.addStatusEffect(new StatusEffectInstance(
                StatusEffects.EXPERIENCE_BOOST, 100, 0, true, false
            ));
        }
        
        if (marriage1.hasPerk(MarriagePerk.SOUL_BOND) && marriage2.hasPerk(MarriagePerk.SOUL_BOND)) {
            // Apply regeneration when both spouses are nearby
            player1.addStatusEffect(new StatusEffectInstance(
                StatusEffects.REGENERATION, 100, 0, true, false
            ));
            player2.addStatusEffect(new StatusEffectInstance(
                StatusEffects.REGENERATION, 100, 0, true, false
            ));
        }
        
        // Add more perk effects as needed
    }
}