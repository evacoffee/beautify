package com.evacoffee.beautymod.lore

import com.evacoffee.beautymod.BeautyMod
import net.minecraft.nbt.NbtIo
import net.minecraft.server.MinecraftServer
import java.io.File
import java.util.*

object LoreManager {
    private const val LORE_DATA_FILE = "player_lore.dat"
    private val playerLoreData = mutableMapOf<UUID, PlayerLoreData>()
    
    fun initialize() {
        CharacterLore.registerDefaults()
    }
    
    fun getPlayerLore(playerUuid: UUID): PlayerLoreData {
        return playerLoreData.getOrPut(playerUuid) { PlayerLoreData(playerUuid) }
    }
    
    fun setPlayerBackground(playerUuid: UUID, backgroundId: Identifier) {
        val loreData = getPlayerLore(playerUuid)
        loreData.backgroundId = backgroundId
        loreData.discoveredLore.add(backgroundId)
    }
    
    fun addFamilyEvent(event: FamilyHistoryEvent) {
        event.participants.forEach { playerUuid ->
            val loreData = getPlayerLore(playerUuid)
            loreData.familyHistory.add(event)
        }
    }
    
    // Save/Load
    fun loadData(server: MinecraftServer) {
        val saveDir = File(server.runDirectory, BeautyMod.DATA_DIR)
        if (!saveDir.exists()) return
        
        val file = File(saveDir, LORE_DATA_FILE)
        if (!file.exists()) return
        
        try {
            val nbt = NbtIo.readCompressed(file)
            nbt?.let { root ->
                root.keys.forEach { key ->
                    val playerUuid = UUID.fromString(key)
                    val playerData = root.getCompound(key)
                    playerLoreData[playerUuid] = PlayerLoreData.fromNbt(playerUuid, playerData)
                }
            }
            BeautyMod.LOGGER.info("Loaded player lore data")
        } catch (e: Exception) {
            BeautyMod.LOGGER.error("Failed to load player lore data", e)
        }
    }
    
    fun saveData(server: MinecraftServer) {
        val saveDir = File(server.runDirectory, BeautyMod.DATA_DIR)
        if (!saveDir.exists()) {
            saveDir.mkdirs()
        }
        
        try {
            val root = net.minecraft.nbt.NbtCompound()
            playerLoreData.forEach { (uuid, data) ->
                root.put(uuid.toString(), data.toNbt())
            }
            
            NbtIo.writeCompressed(root, File(saveDir, LORE_DATA_FILE))
            BeautyMod.LOGGER.info("Saved player lore data")
        } catch (e: Exception) {
            BeautyMod.LOGGER.error("Failed to save player lore data", e)
        }
    }
}