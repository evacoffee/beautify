package com.evacoffee.beautymod.event.family

import com.evacoffee.beautymod.BeautyMod
import com.evacoffee.beautymod.event.FamilyEvent
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.*

class FamilyAnniversaryEvent(private val familyId: UUID) : FamilyEvent() {
    private val rewardsGiven = mutableSetOf<UUID>()
    
    override fun getStartMessage() = "Happy Family Anniversary! Check your inventory for a special gift!"
    override fun getDescription() = "Celebrates the anniversary of your family's creation"
    override fun getDurationInTicks() = 24000L // 20 minutes
    
    override fun onStart(server: MinecraftServer) {
        giveRewards(server)
    }
    
    override fun onTick(server: MinecraftServer) {
        super.onTick(server)
        
        // Give rewards to players who join during the event
        if (server.ticks % 200 == 0L) { // Every 10 seconds
            giveRewards(server)
        }
    }
    
    private fun giveRewards(server: MinecraftServer) {
        val familyMembers = BeautyMod.adoptionManager.getFamilyMembers(familyId)
        
        familyMembers.forEach { playerUuid ->
            if (!rewardsGiven.contains(playerUuid)) {
                val player = server.playerManager.getPlayer(playerUuid) ?: return@forEach
                
                // Give special cake
                val cake = ItemStack(Items.CAKE)
                val name = Text.literal("Family Anniversary Cake")
                    .formatted(Formatting.LIGHT_PURPLE)
                    .styled { it.withItalic(true) }
                cake.setCustomName(name)
                
                if (player.giveItemStack(cake)) {
                    player.sendMessage(
                        Text.literal("🎂 You received a special Family Anniversary Cake!")
                            .formatted(Formatting.GOLD),
                        false
                    )
                    rewardsGiven.add(playerUuid)
                }
            }
        }
    }
}