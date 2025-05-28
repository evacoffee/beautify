package com.evacoffee.beautymod.command;

import com.evacoffee.beautymod.marriage.MarriageComponentInitializer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import static net.minecraft.server.command.CommandManager.literal;

public class HomeCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("sethome")
            .executes(HomeCommand::setHome));
            
        dispatcher.register(literal("home")
            .executes(HomeCommand::goHome));
    }
    
    private static int setHome(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();
        
        // Check if player is married
        if (!MarriageComponentInitializer.getMarriage(player).isMarried()) {
            source.sendError(Text.literal("You need to be married to set a home!"));
            return 0;
        }
        
        // Set home at current position
        BlockPos pos = player.getBlockPos();
        MarriageComponentInitializer.getMarriage(player).setHomePos(pos);
        
        // Notify player
        player.sendMessage(Text.literal("§aHome set at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ())
            .formatted(Formatting.GREEN), false);
            
        // Play a sound effect
        player.playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 1.0f, 1.0f);
        
        return Command.SINGLE_SUCCESS;
    }
    
    private static int goHome(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();
        
        // Check if player is married
        if (!MarriageComponentInitializer.getMarriage(player).isMarried()) {
            source.sendError(Text.literal("You need to be married to use this command!"));
            return 0;
        }
        
        // Get home position
        BlockPos homePos = MarriageComponentInitializer.getMarriage(player).getHomePos();
        if (homePos == null) {
            source.sendError(Text.literal("You haven't set a home yet! Use /sethome"));
            return 0;
        }
        
        // Check if already at home
        if (player.getBlockPos().isWithinDistance(homePos, 5.0)) {
            source.sendError(Text.literal("You're already at your home!"));
            return 0;
        }
        
        // Check cooldown (5 minutes)
        long lastTeleport = player.getWorld().getTime() - (player.getLastTeleportTime() * 50);
        if (lastTeleport < 6000 && !player.hasPermissionLevel(2)) { // 5 minutes in ticks
            source.sendError(Text.literal("You must wait " + formatTime(6000 - lastTeleport) + " before teleporting again!")
                .formatted(Formatting.RED));
            return 0;
        }
        
        // Teleport to home
        ServerWorld world = player.getServerWorld();
        Vec3d teleportPos = new Vec3d(homePos.getX() + 0.5, homePos.getY(), homePos.getZ() + 0.5);
        
        // Find safe teleport location
        while (world.getBlockState(homePos).isAir() && homePos.getY() > world.getBottomY()) {
            homePos = homePos.down();
        }
        homePos = homePos.up(); // Move up to avoid suffocation
        
        // Ensure the destination is safe
        if (!world.getBlockState(homePos).isAir() || !world.getBlockState(homePos.up()).isAir()) {
            homePos = world.getTopPosition(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING, homePos);
        }
        
        player.teleport(world, teleportPos.x, homePos.getY(), teleportPos.z, player.getYaw(), player.getPitch());
        
        // Play effects
        player.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f);
        player.sendMessage(Text.literal("§aWelcome home!")
            .formatted(Formatting.GREEN), false);
            
        return Command.SINGLE_SUCCESS;
    }
    
    private static String formatTime(long ticks) {
        long seconds = ticks / 20;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        if (minutes > 0) {
            return String.format("%d minute%s and %d second%s", 
                minutes, minutes == 1 ? "" : "s",
                seconds, seconds == 1 ? "" : "s");
        } else {
            return String.format("%d second%s", seconds, seconds == 1 ? "" : "s");
        }
    }
}
