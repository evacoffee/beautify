package com.evacoffee.beautymod.lore

import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import java.util.*

data class CharacterBackground(
    val id: Identifier,
    val title: String,
    val description: String,
    val effects: List<LoreEffect> = emptyList(),
    val familyBonus: (UUID) -> Text = { Text.empty() }
)

data class LoreEffect(
    val description: String,
    val applyEffect: (ServerPlayerEntity) -> Unit
)

object CharacterLore {
    private val backgrounds = mutableMapOf<Identifier, CharacterBackground>()
    
    // Register default backgrounds
    fun registerDefaults() {
        register(Identifier(BeautyMod.MOD_ID, "noble"), createNobleBackground())
        register(Identifier(BeautyMod.MOD_ID, "wanderer"), createWandererBackground())
        register(Identifier(BeautyMod.MOD_ID, "scholar"), createScholarBackground())
        // Add more backgrounds as needed
    }
    
    fun register(id: Identifier, background: CharacterBackground) {
        backgrounds[id] = background
    }
    
    fun getBackground(id: Identifier): CharacterBackground? = backgrounds[id]
    
    fun getAllBackgrounds(): Collection<CharacterBackground> = backgrounds.values
    
    // Background Creators
    private fun createNobleBackground(): CharacterBackground {
        return CharacterBackground(
            id = Identifier(BeautyMod.MOD_ID, "noble"),
            title = "lore.beautymod.noble.title",
            description = "lore.beautymod.noble.desc",
            effects = listOf(
                LoreEffect("lore.beautymod.noble.effect.trade") { player ->
                    // Better trades with villagers
                    // Implementation for trade bonuses
                }
            ),
            familyBonus = { familyId ->
                Text.translatable("lore.beautymod.noble.bonus")
                    .formatted(Formatting.GOLD)
            }
        )
    }
    
    private fun createWandererBackground(): CharacterBackground {
        return CharacterBackground(
            id = Identifier(BeautyMod.MOD_ID, "wanderer"),
            title = "lore.beautymod.wanderer.title",
            description = "lore.beautymod.wanderer.desc",
            effects = listOf(
                LoreEffect("lore.beautymod.wanderer.effect.speed") { player ->
                    // Movement speed bonus
                    // Implementation for speed effect
                }
            )
        )
    }
    
    private fun createScholarBackground(): CharacterBackground {
        return CharacterBackground(
            id = Identifier(BeautyMod.MOD_ID, "scholar"),
            title = "lore.beautymod.scholar.title",
            description = "lore.beautymod.scholar.desc",
            effects = listOf(
                LoreEffect("lore.beautymod.scholar.effect.xp") { player ->
                    // XP gain bonus
                    // Implementation for XP boost
                }
            )
        )
    }
}

// Player Lore Data
class PlayerLoreData(
    val playerUuid: UUID,
    var backgroundId: Identifier? = null,
    var discoveredLore: MutableSet<Identifier> = mutableSetOf(),
    var familyHistory: MutableList<FamilyHistoryEvent> = mutableListOf()
) {
    fun toNbt(): NbtCompound {
        val nbt = NbtCompound()
        backgroundId?.let { nbt.putString("BackgroundId", it.toString()) }
        
        val loreArray = NbtCompound()
        discoveredLore.forEachIndexed { index, id ->
            loreArray.putString(index.toString(), id.toString())
        }
        nbt.put("DiscoveredLore", loreArray)
        
        val historyArray = NbtCompound()
        familyHistory.forEachIndexed { index, event ->
            historyArray.put(index.toString(), event.toNbt())
        }
        nbt.put("FamilyHistory", historyArray)
        
        return nbt
    }
    
    companion object {
        fun fromNbt(playerUuid: UUID, nbt: NbtCompound): PlayerLoreData {
            val data = PlayerLoreData(playerUuid)
            
            if (nbt.contains("BackgroundId")) {
                data.backgroundId = Identifier.tryParse(nbt.getString("BackgroundId"))
            }
            
            if (nbt.contains("DiscoveredLore")) {
                val loreArray = nbt.getCompound("DiscoveredLore")
                loreArray.keys.forEach { key ->
                    Identifier.tryParse(loreArray.getString(key))?.let { id ->
                        data.discoveredLore.add(id)
                    }
                }
            }
            
            if (nbt.contains("FamilyHistory")) {
                val historyArray = nbt.getCompound("FamilyHistory")
                historyArray.keys.forEach { key ->
                    data.familyHistory.add(FamilyHistoryEvent.fromNbt(historyArray.getCompound(key)))
                }
            }
            
            return data
        }
    }
}

data class FamilyHistoryEvent(
    val eventType: String,
    val timestamp: Long,
    val participants: List<UUID>,
    val additionalData: NbtCompound = NbtCompound()
) {
    fun toNbt(): NbtCompound {
        val nbt = NbtCompound()
        nbt.putString("EventType", eventType)
        nbt.putLong("Timestamp", timestamp)
        
        val participantsArray = NbtCompound()
        participants.forEachIndexed { index, uuid ->
            participantsArray.putUuid(index.toString(), uuid)
        }
        nbt.put("Participants", participantsArray)
        
        nbt.put("AdditionalData", additionalData)
        return nbt
    }
    
    companion object {
        fun fromNbt(nbt: NbtCompound): FamilyHistoryEvent {
            val eventType = nbt.getString("EventType")
            val timestamp = nbt.getLong("Timestamp")
            
            val participants = mutableListOf<UUID>()
            val participantsArray = nbt.getCompound("Participants")
            participantsArray.keys.forEach { key ->
                participantsArray.getUuid(key)?.let { participants.add(it) }
            }
            
            val additionalData = nbt.getCompound("AdditionalData")
            
            return FamilyHistoryEvent(eventType, timestamp, participants, additionalData)
        }
    }
}