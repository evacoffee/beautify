package com.evacoffee.beautymod.particles;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class ParticleHandler {
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                ServerWorld world = (ServerWorld) player.getWorld();
                Vec3d pos = player.getPos();
                world.spawnParticles(net.minecraft.particle.ParticleTypes.HEART,
                    pos.x, pos.y + 2, pos.z,
                    0, 0.5, 0.5, 0.5, 0.05);
            }
        });
    }
}
