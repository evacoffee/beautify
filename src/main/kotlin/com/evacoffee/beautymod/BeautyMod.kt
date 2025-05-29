package com.evacoffee.beautymod

import com.evacoffee.beautymod.achievement.Achievements
import com.evacoffee.beautymod.command.FamilyCommand
import com.evacoffee.beautymod.command.LoreCommand
import com.evacoffee.beautymod.event.EventManager
import com.evacoffee.beautymod.family.AdoptionManager
import com.evacoffee.beautymod.family.FamilyTree
import com.evacoffee.beautymod.item.Items
import com.evacoffee.beautymod.lore.LoreManager
import com.evacoffee.beautymod.pet.FamilyPetManager
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.nbt.NbtIo
import net.minecraft.server.MinecraftServer
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.util.*

object BeautyMod : ModInitializer {
    const val MOD_ID = "beautymod"
    val LOGGER: Logger = LogManager.getLogger(MOD_ID)
    
    // Managers
    lateinit var adoptionManager: AdoptionManager
        private set
    lateinit var familyTree: FamilyTree
        private set
    
    // Server instance
    private var server: MinecraftServer? = null
    
    // Data files
    const val DATA_DIR = "beautymod"
    private const val ADOPTION_DATA = "adoptions.dat"
    private const val PETS_DATA = "family_pets.dat"
    
    // Networking
    val ADOPTION_REQUEST_PACKET = Identifier(MOD_ID, "adoption_request")
    val ADOPTION_RESPONSE_PACKET = Identifier(MOD_ID, "adoption_response")
    
    override fun onInitialize() {
        LOGGER.info("Initializing Beauty Mod")
        
        // Register items
        Items.ADOPTION_PAPERS // This registers the item through its init block
        
        // Initialize managers
        adoptionManager = AdoptionManager()
        familyTree = FamilyTree()
        
        // Register commands
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            FamilyCommand.register(dispatcher)
            LoreCommand.register(dispatcher)
        }
        
        // Initialize achievements
        Achievements.register()
        
        // Initialize family pets
        FamilyPetManager.init()
        
        // Initialize event system
        EventManager.initialize()
        
        // Initialize lore system
        LoreManager.initialize()
        
        // Server lifecycle events
        ServerLifecycleEvents.SERVER_STARTING.register { server ->
            this.server = server
            loadData(server)
            LoreManager.loadData(server)
        }
        
        ServerLifecycleEvents.SERVER_STOPPING.register { server ->
            saveData(server)
            LoreManager.saveData(server)
            this.server = null
        }
        
        // Register network handlers
        registerNetworkHandlers()
        
        LOGGER.info("Beauty Mod initialized successfully")
    }
    
    private fun loadData(server: MinecraftServer) {
        val saveDir = File(server.runDirectory, DATA_DIR)
        if (!saveDir.exists()) {
            saveDir.mkdirs()
        }
        
        // Load adoption data
        val adoptionFile = File(saveDir, ADOPTION_DATA)
        if (adoptionFile.exists()) {
            try {
                val nbt = NbtIo.readCompressed(adoptionFile)
                adoptionManager.readNbt(nbt)
                LOGGER.info("Loaded adoption data")
            } catch (e: Exception) {
                LOGGER.error("Failed to load adoption data", e)
            }
        }
        
        // Load family pets data
        val petsFile = File(saveDir, PETS_DATA)
        if (petsFile.exists()) {
            try {
                val nbt = NbtIo.readCompressed(petsFile)
                FamilyPetManager.readNbt(nbt)
                LOGGER.info("Loaded family pets data")
            } catch (e: Exception) {
                LOGGER.error("Failed to load family pets data", e)
            }
        }
    }
    
    private fun saveData(server: MinecraftServer) {
        val saveDir = File(server.runDirectory, DATA_DIR)
        if (!saveDir.exists()) {
            saveDir.mkdirs()
        }
        
        // Save adoption data
        try {
            val adoptionNbt = net.minecraft.nbt.NbtCompound()
            adoptionManager.writeNbt(adoptionNbt)
            NbtIo.writeCompressed(adoptionNbt, File(saveDir, ADOPTION_DATA))
            LOGGER.info("Saved adoption data")
        } catch (e: Exception) {
            LOGGER.error("Failed to save adoption data", e)
        }
        
        // Save family pets data
        try {
            val petsNbt = net.minecraft.nbt.NbtCompound()
            FamilyPetManager.writeNbt(petsNbt)
            NbtIo.writeCompressed(petsNbt, File(saveDir, PETS_DATA))
            LOGGER.info("Saved family pets data")
        } catch (e: Exception) {
            LOGGER.error("Failed to save family pets data", e)
        }
    }
    
    private fun registerNetworkHandlers() {
        // Register adoption request packet handler
        ServerPlayNetworking.registerGlobalReceiver(ADOPTION_REQUEST_PACKET) { server, player, _, buf, _ ->
            val targetUuid = buf.readUuid()
            server.execute {
                adoptionManager.handleAdoptionRequest(player, targetUuid)
            }
        }
        
        // Register adoption response packet handler
        ServerPlayNetworking.registerGlobalReceiver(ADOPTION_RESPONSE_PACKET) { server, player, _, buf, _ ->
            val requesterUuid = buf.readUuid()
            val accepted = buf.readBoolean()
            server.execute {
                adoptionManager.handleAdoptionResponse(player, requesterUuid, accepted)
            }
        }
    }
}