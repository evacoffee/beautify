package com.evacoffee.beautymod.command;

import com.evacoffee.beautymod.character.DatingEvents;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class DatingCommand implements Command<ServerCommandSource> {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                net.minecraft.server.command.CommandManager.literal("date")
                    .executes(new DatingCommand())
            );
        });
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player != null) {
            // Example: Trigger a random dating event
            DatingEvents.Gender playerGender = DatingEvents.Gender.values()[
                player.getRandom().nextInt(DatingEvents.Gender.values().length)
            ];
            DatingEvents.Gender npcGender = DatingEvents.Gender.values()[
                player.getRandom().nextInt(DatingEvents.Gender.values().length)
            ];
            
            DatingEvents.triggerDatingEvent(player, playerGender, npcGender);
            return 1;
        }
        return 0;
    }
}