package com.evacoffee.beautymod.event

import net.minecraft.server.MinecraftServer

abstract class FamilyEvent {
    abstract fun getStartMessage(): String
    abstract fun getDescription(): String
    abstract fun getDurationInTicks(): Long
    
    private var ticksActive = 0L
    
    open fun onStart(server: MinecraftServer) {}
    open fun onEnd(server: MinecraftServer) {}
    
    open fun onTick(server: MinecraftServer) {
        ticksActive++
        if (ticksActive >= getDurationInTicks()) {
            onEnd(server)
        }
    }
    
    open fun getRemainingTimeFormatted(): String {
        val ticksLeft = getDurationInTicks() - ticksActive
        val seconds = ticksLeft / 20
        val minutes = seconds / 60
        val hours = minutes / 60
        
        return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60)
    }
}