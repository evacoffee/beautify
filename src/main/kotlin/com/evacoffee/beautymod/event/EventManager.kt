package com.evacoffee.beautymod.event

import com.evacoffee.beautymod.BeautyMod
import com.evacoffee.beautymod.family.AdoptionManager
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.time.LocalDate
import java.util.*
import kotlin.math.abs

object EventManager {
    private val activeEvents = mutableSetOf<FamilyEvent>()
    private var lastCheckDay = -1
    
    fun initialize() {
        ServerTickEvents.END_SERVER_TICK.register(::onServerTick)
    }
    
    private fun onServerTick(server: MinecraftServer) {
        val currentDay = LocalDate.now().dayOfYear
        
        // Only check once per day
        if (currentDay != lastCheckDay) {
            lastCheckDay = currentDay
            checkForEvents(server)
        }
        
        // Update active events
        activeEvents.forEach { it.onTick(server) }
    }
    
    private fun checkForEvents(server: MinecraftServer) {
        val today = LocalDate.now()
        
        // Clear previous events
        activeEvents.clear()
        
        // Check for holidays
        getHolidayEvents(today).forEach { event ->
            activeEvents.add(event)
            broadcastEventStart(server, event)
        }
        
        // Check for family anniversaries
        checkFamilyAnniversaries(server, today)
    }
    
    private fun getHolidayEvents(date: LocalDate): List<FamilyEvent> {
        val events = mutableListOf<FamilyEvent>()
        
        // New Year's (Jan 1)
        if (date.monthValue == 1 && date.dayOfMonth == 1) {
            events.add(NewYearsEvent())
        }
        // Valentine's Day (Feb 14)
        else if (date.monthValue == 2 && date.dayOfMonth == 14) {
            events.add(ValentinesDayEvent())
        }
        // Halloween (Oct 31)
        else if (date.monthValue == 10 && date.dayOfMonth == 31) {
            events.add(HalloweenEvent())
        }
        // Christmas (Dec 24-26)
        else if (date.monthValue == 12 && date.dayOfMonth in 24..26) {
            events.add(ChristmasEvent())
        }
        
        return events
    }
    
    private fun checkFamilyAnniversaries(server: MinecraftServer, date: LocalDate) {
        val adoptionManager = BeautyMod.adoptionManager
        val calendar = Calendar.getInstance()
        
        adoptionManager.getAllFamilies().forEach { family ->
            family.getAnniversaryDate()?.let { anniversaryDate ->
                calendar.time = Date(anniversaryDate)
                val anniversaryMonth = calendar.get(Calendar.MONTH) + 1
                val anniversaryDay = calendar.get(Calendar.DAY_OF_MONTH)
                
                if (date.monthValue == anniversaryMonth && date.dayOfMonth == anniversaryDay) {
                    val event = FamilyAnniversaryEvent(family.id)
                    activeEvents.add(event)
                    broadcastFamilyEvent(server, family.id, event)
                }
            }
        }
    }
    
    private fun broadcastEventStart(server: MinecraftServer, event: FamilyEvent) {
        val message = Text.literal("🎉 ").formatted(Formatting.GOLD)
            .append(Text.literal(event.getStartMessage()).formatted(Formatting.YELLOW))
        server.playerManager.broadcast(message, false)
    }
    
    private fun broadcastFamilyEvent(server: MinecraftServer, familyId: UUID, event: FamilyEvent) {
        val familyMembers = AdoptionManager.getFamilyMembers(familyId)
        val message = Text.literal("👨‍👩‍👧‍👦 ").formatted(Formatting.LIGHT_PURPLE)
            .append(Text.literal(event.getStartMessage()).formatted(Formatting.YELLOW))
        
        familyMembers.forEach { playerUuid ->
            server.playerManager.getPlayer(playerUuid)?.sendMessage(message, false)
        }
    }
    
    fun isEventActive(eventType: Class<out FamilyEvent>): Boolean {
        return activeEvents.any { it::class == eventType }
    }
    
    fun getActiveEvents(): List<FamilyEvent> {
        return activeEvents.toList()
    }
}