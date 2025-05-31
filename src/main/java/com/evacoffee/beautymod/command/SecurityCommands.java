package com.evacoffee.beautymod.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.*;

public class SecurityCommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("block")
            .then(argument("player", EntityArgument.player())
                .executes(context -> blockPlayer(
                    context.getSource().getPlayer(),
                    EntityArgument.getPlayer(context, "player")
                ))));
                
        dispatcher.register(literal("unblock")
            .then(argument("player", EntityArgument.player())
                .executes(context -> unblockPlayer(
                    context.getSource().getPlayer(),
                    EntityArgument.getPlayer(context, "player")
                ))));
                
        dispatcher.register(literal("permission")
            .requires(source -> source.hasPermissionLevel(2))
            .then(argument("player", EntityArgument.player())
                .then(argument("permission", StringArgumentType.string())
                    .then(literal("grant")
                        .executes(context -> grantPermission(
                            EntityArgument.getPlayer(context, "player"),
                            StringArgumentType.getString(context, "permission")
                        )))
                    .then(literal("revoke")
                        .executes(context -> revokePermission(
                            EntityArgument.getPlayer(context, "player"),
                            StringArgumentType.getString(context, "permission")
                        ))))));
    }
    
    private static int blockPlayer(ServerPlayerEntity source, ServerPlayerEntity target) {
        AntiHarassmentManager.blockPlayer(source.getUuid(), target.getUuid());
        source.sendMessage(Text.literal("Blocked " + target.getName().getString()));
        return 1;
    }
    
    private static int unblockPlayer(ServerPlayerEntity source, ServerPlayerEntity target) {
        AntiHarassmentManager.unblockPlayer(source.getUuid(), target.getUuid());
        source.sendMessage(Text.literal("Unblocked " + target.getName().getString()));
        return 1;
    }
    
    private static int grantPermission(ServerPlayerEntity target, String permission) {
        PermissionManager.addPermission(target.getUuid(), permission);
        return 1;
    }
    
    private static int revokePermission(ServerPlayerEntity target, String permission) {
        PermissionManager.removePermission(target.getUuid(), permission);
        return 1;
    }
}