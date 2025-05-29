package com.evacoffee.beautymod.event.holiday

import com.evacoffee.beautymod.event.FamilyEvent
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class NewYearsEvent : FamilyEvent() {
    override fun getStartMessage() = "Happy New Year! Enjoy special buffs with your family!"
    override fun getDescription() = "Grants regeneration and fire resistance to family members near each other"
    override fun getDurationInTicks() = 24000L // 20 minutes
    
    override fun onStart(server: MinecraftServer) {
        broadcastToAll(server, "🎆 Happy New Year from the BeautyMod team! 🎆")
    }
    
    override fun onTick(server: MinecraftServer) {
        super.onTick(server)
        
        if (server.ticks % 100 == 0L) { // Every 5 seconds
            server.playerManager.playerList.forEach { player ->
                if (isNearFamily(player)) {
                    player.addStatusEffect(StatusEffectInstance(
                        StatusEffects.REGENERATION, 
                        120, // 6 seconds
                        0,
                        false,
                        false,
                        true
                    ))
                    player.addStatusEffect(StatusEffects.FIRE_RESISTANCE, 120, 0, false, false, true)
                }
            }
        }
    }
    
    private fun isNearFamily(player: ServerPlayerEntity): Boolean {
        val world = player.world
        val pos = player.blockPos
        val familyMembers = getNearbyFamilyMembers(player, 10.0)
        return familyMembers.isNotEmpty()
    }
    
    private fun broadcastToAll(server: MinecraftServer, message: String) {
        val text = Text.literal(message).formatted(Formatting.GOLD, Formatting.BOLD)
        server.playerManager.broadcast(text, false)
    }
}