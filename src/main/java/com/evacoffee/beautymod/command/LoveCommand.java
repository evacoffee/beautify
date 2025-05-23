package com.evacoffee.beautymod.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import com.yourname.beautymod.love.LoveData;
import net.minecraft.server.network.ServerPlayerEntity;

public class LoveCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("lovestatus").executes(ctx -> {
            ServerPlayerEntity player = ctx.getSource().getPlayer();
            int love = LoveData.getLove(player);
            ctx.getSource().sendFeedback(Text.of("❤️ Your Love Level: " + love), false);
            return 1;
        }));
    }
}
