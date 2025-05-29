package com.evacoffee.beautymod

import com.evacoffee.beautymod.achievement.Achievements
import com.evacoffee.beautymod.command.FamilyCommand
import com.evacoffee.beautymod.family.AdoptionManager
import com.evacoffee.beautymod.family.FamilyTree
import com.evacoffee.beautymod.item.AdoptionPapersItem
import com.evacoffee.beautymod.pet.FamilyPetManager
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.server.MinecraftServer
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

object BeautyMod : ModInitializer {
    // Mod metadata
    const val MOD_ID = "beautymod"
    const val MOD_NAME = "BeautyMod"
    val LOGGER: Logger = LoggerFactory.getLogger(MOD_NAME)
    
    // Data files
    private const val DATA_DIR = "beautymod"
    private const val ADOPTION_DATA = "adoptions.dat"
    private const val PETS_DATA = "family_pets.dat"
    
    // Networking
    val ADOPTION_REQUEST_PACKET = Identifier(MOD_ID, "adoption_request")
    val ADOPTION_RESPONSE_PACKET = Identifier(MOD_ID, "adoption_response")
    
    // Managers
    lateinit var adoptionManager: AdoptionManager
        private set
    lateinit var familyTree: FamilyTree
        private set
    
    // Item Group
    val ITEM_GROUP: ItemGroup = FabricItemGroup.builder()
        .icon { ItemStack(Items.ADOPTION_PAPERS) }
        .displayName(Text.translatable("itemGroup.beautymod.main"))
        .build()
    
    // Items
    object Items {
        val ADOPTION_PAPERS = AdoptionPapersItem
    }
    
    // Server instance
    private var server: MinecraftServer? = null
    
    override fun onInitialize() {
        LOGGER.info("Initializing $MOD_NAME")
        
        // Register item group
        Registry.register(
            Registries.ITEM_GROUP,
            Identifier(MOD_ID, "main"),
            ITEM_GROUP
        )
        
        // Register items
        Items.ADOPTION_PAPERS // This registers the item through its init block
        
        // Initialize managers
        adoptionManager = AdoptionManager()
        familyTree = FamilyTree()
        
        // Register commands
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            FamilyCommand.register(dispatcher)
        }
        
        // Initialize achievements
        Achievements.register()
        
        // Initialize family pets
        FamilyPetManager.init()
        
        // Server lifecycle events
        ServerLifecycleEvents.SERVER_STARTING.register { server ->
            this.server = server
            loadData(server)
        }
        
        ServerLifecycleEvents.SERVER_STOPPING.register { server ->
            saveData(server)
            this.server = null
        }
        
        // Register network receivers
        registerNetworkHandlers()
        
        LOGGER.info("$MOD_NAME has been initialized!")
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
                val nbt = net.minecraft.nbt.NbtIo.readCompressed(adoptionFile)
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
                val nbt = net.minecraft.nbt.NbtIo.readCompressed(petsFile)
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
            net.minecraft.nbt.NbtIo.writeCompressed(adoptionNbt, File(saveDir, ADOPTION_DATA))
            LOGGER.info("Saved adoption data")
        } catch (e: Exception) {
            LOGGER.error("Failed to save adoption data", e)
        }
        
        // Save family pets data
        try {
            val petsNbt = net.minecraft.nbt.NbtCompound()
            FamilyPetManager.writeNbt(petsNbt)
            net.minecraft.nbt.NbtIo.writeCompressed(petsNbt, File(saveDir, PETS_DATA))
            LOGGER.info("Saved family pets data")
        } catch (e: Exception) {
            LOGGER.error("Failed to save family pets data", e)
        }
    }
    
    private fun registerNetworkHandlers() {
        // Handle adoption request responses from clients
        ServerPlayNetworking.registerGlobalReceiver(ADOPTION_RESPONSE_PACKET) { server, player, _, buf, _ ->
            val accepted = buf.readBoolean()
            server.execute {
                if (accepted) {
                    adoptionManager.acceptAdoption(player)
                } else {
                    adoptionManager.denyAdoption(player)
                }
            }
        }
    }
    
    // Helper function to get the server instance
    fun getServer(): MinecraftServer? = server
}