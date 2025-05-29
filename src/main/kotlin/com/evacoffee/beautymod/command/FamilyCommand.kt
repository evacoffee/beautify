package com.evacoffee.beautymod.command

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.server.command.ServerCommandSource

object FamilyCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        // Implementation for family-related commands
        // /family adopt <player>
        // /family tree [player]
    }
}