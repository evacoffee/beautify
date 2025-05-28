package com.evacoffee.beautymod.marriage;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class MarriageComponentInitializer implements EntityComponentInitializer {
    public static final ComponentKey<MarriageComponent> MARRIAGE = 
        ComponentRegistry.getOrCreate(new Identifier("beautymod", "marriage"), MarriageComponent.class);
    
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(MARRIAGE, player -> new MarriageComponent(), RespawnCopyStrategy.ALWAYS_COPY);
    }
    
    public static MarriageComponent getMarriage(PlayerEntity player) {
        return MARRIAGE.get(player);
    }
    
    public static boolean isMarried(ServerPlayerEntity player, ServerPlayerEntity spouse) {
        MarriageComponent playerMarriage = getMarriage(player);
        return playerMarriage.isMarried() && 
               playerMarriage.getSpouseUuid() != null &&
               playerMarriage.getSpouseUuid().equals(spouse.getUuid());
    }
    
    public static boolean marry(ServerPlayerEntity player1, ServerPlayerEntity player2, long weddingDay) {
        MarriageComponent p1 = getMarriage(player1);
        MarriageComponent p2 = getMarriage(player2);
        
        if (p1.isMarried() || p2.isMarried()) {
            return false;
        }
        
        return p1.marry(player1, player2, weddingDay) && 
               p2.marry(player2, player1, weddingDay);
    }
    
    public static void divorce(ServerPlayerEntity player) {
        MarriageComponent marriage = getMarriage(player);
        marriage.divorce();
    }
}
