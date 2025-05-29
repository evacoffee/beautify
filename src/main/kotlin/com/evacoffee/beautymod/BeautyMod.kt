package com.evacoffee.beautymod

import com.evacoffee.beautymod.command.FamilyCommand
import com.evacoffee.beautymod.family.AdoptionManager
import com.evacoffee.beautymod.family.FamilyTree
import com.evacoffee.beautymod.item.AdoptionPapersItem
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
        
        // Register commands
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            FamilyCommand.register(dispatcher)
        }
        
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
        
        val adoptionFile = File(saveDir, ADOPTION_DATA)
        adoptionManager = AdoptionManager()
        familyTree = FamilyTree()
        
        if (adoptionFile.exists()) {
            try {
                val nbt = net.minecraft.nbt.NbtIo.readCompressed(adoptionFile)
                adoptionManager.readNbt(nbt)
                LOGGER.info("Loaded adoption data")
            } catch (e: Exception) {
                LOGGER.error("Failed to load adoption data", e)
            }
        }
    }
    
    private fun saveData(server: MinecraftServer) {
        val saveDir = File(server.runDirectory, DATA_DIR)
        if (!saveDir.exists()) {
            saveDir.mkdirs()
        }
        
        try {
            val nbt = net.minecraft.nbt.NbtCompound()
            adoptionManager.writeNbt(nbt)
            net.minecraft.nbt.NbtIo.writeCompressed(nbt, File(saveDir, ADOPTION_DATA))
            LOGGER.info("Saved adoption data")
        } catch (e: Exception) {
            LOGGER.error("Failed to save adoption data", e)
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