package com.evacoffee.beautymod.dating;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class DatingComponentInitializer implements EntityComponentInitializer {
    public static final ComponentKey<DatingComponent> DATING = 
        ComponentRegistry.getOrCreate(new Identifier("beautymod", "dating"), DatingComponent.class);
    
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(DATING, player -> new DatingComponent(), RespawnCopyStrategy.ALWAYS_COPY);
    }
    
    public static DatingComponent getDating(PlayerEntity player) {
        return DATING.get(player);
    }
    
    public static boolean isOnDateWith(ServerPlayerEntity player1, ServerPlayerEntity player2) {
        DatingComponent dating = getDating(player1);
        return dating.isOnDate() && dating.getCurrentDatePartner().equals(player2.getUuid());
    }
    
    public static void startDate(ServerPlayerEntity player1, ServerPlayerEntity player2, BlockPos location) {
        DatingComponent p1Dating = getDating(player1);
        DatingComponent p2Dating = getDating(player2);
        
        p1Dating.startDate(player1, player2, location);
        p2Dating.startDate(player2, player1, location);
    }
    
    public static void endDate(ServerPlayerEntity player1, ServerPlayerEntity player2, boolean successful) {
        DatingComponent p1Dating = getDating(player1);
        DatingComponent p2Dating = getDating(player2);
        
        p1Dating.endDate(player1, successful);
        p2Dating.endDate(player2, successful);
    }
}
