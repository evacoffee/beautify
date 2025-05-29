package com.evacoffee.beautymod.command

import com.evacoffee.beautymod.lore.CharacterLore
import com.evacoffee.beautymod.lore.LoreManager
import com.mojang.brigadier.CommandDispatcher
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object LoreCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(
            literal("lore")
                .then(literal("backgrounds")
                    .executes { context ->
                        listBackgrounds(context.source)
                        1
                    }
                )
                .then(literal("set")
                    .then(CommandManager.argument("target", EntityArgumentType.player())
                    .then(CommandManager.argument("background", StringArgumentType.string())
                        .suggests { context, builder ->
                            val backgrounds = CharacterLore.getAllBackgrounds()
                            for (bg in backgrounds) {
                                builder.suggest(bg.id.toString())
                            }
                            builder.buildFuture()
                        }
                        .executes { context ->
                            val target = EntityArgumentType.getPlayer(context, "target")
                            val backgroundId = Identifier.tryParse(StringArgumentType.getString(context, "background"))
                            setBackground(context.source, target, backgroundId)
                            1
                        }
                    )
                )
            )
        )
    }
    
    private fun listBackgrounds(source: ServerCommandSource) {
        val backgrounds = CharacterLore.getAllBackgrounds()
        source.sendFeedback(Text.literal("Available Backgrounds:").formatted(Formatting.GOLD), false)
        
        backgrounds.forEach { background ->
            source.sendFeedback(Text.literal("- ${background.id}").formatted(Formatting.WHITE), false)
            source.sendFeedback(Text.translatable(background.description).formatted(Formatting.GRAY), false)
        }
        return backgrounds.size
    }
    
    private fun setBackground(source: ServerCommandSource, target: ServerPlayerEntity, backgroundId: Identifier?) {
        if (backgroundId == null) {
            source.sendError(Text.literal("Invalid background ID"))
            return
        }
        
        val background = CharacterLore.getBackground(backgroundId)
        if (background == null) {
            source.sendError(Text.literal("Background not found: $backgroundId"))
            return
        }
        
        LoreManager.setPlayerBackground(target.uuid, backgroundId)
        source.sendFeedback(
            Text.translatable("lore.beautymod.background_selected", background.title)
                .formatted(Formatting.GREEN),
            true
        )
    }
}