package com.evacoffee.beautymod.pet

import com.evacoffee.beautymod.BeautyMod
import com.evacoffee.beautymod.achievement.Achievements
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.Tameable
import net.minecraft.entity.passive.*
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.Registries
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.math.Box
import net.minecraft.world.World
import java.util.*

object FamilyPetManager {
    private val familyPets = mutableMapOf<UUID, MutableSet<UUID>>() // Family ID -> Set of pet UUIDs
    private val petToFamily = mutableMapOf<UUID, UUID>() // Pet UUID -> Family ID
    private val playerPetCooldown = mutableMapOf<UUID, Long>() // Player UUID -> Cooldown end time
    
    private const val COOLDOWN_MS = 5000L
    
    fun init() {
        // Register event handlers
        ServerEntityEvents.ENTITY_LOAD.register(::onEntityLoad)
        ServerEntityEvents.ENTITY_UNLOAD.register(::onEntityUnload)
        ServerEntityEvents.ENTITY_REMOVE.register(::onEntityRemove)
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(::onPlayerChangeWorld)
        
        // Register pet interaction
        UseEntityCallback.EVENT.register(::onUseEntity)
    }
    
    private fun onEntityLoad(entity: Entity, world: World) {
        if (entity is Tameable && entity.isTamed) {
            val owner = entity.owner
            if (owner != null) {
                val familyId = getFamilyId(owner.uuid) ?: return
                addPetToFamily(familyId, entity)
            }
        }
    }
    
    private fun onEntityUnload(entity: Entity, world: World) {
        if (entity is Tameable && entity.isTamed) {
            // Handle any cleanup if needed
        }
    }
    
    private fun onEntityRemove(entity: Entity, world: World) {
        if (entity is Tameable && entity.isTamed) {
            val petUuid = entity.uuid
            val familyId = petToFamily[petUuid] ?: return
            
            familyPets[familyId]?.remove(petUuid)
            petToFamily.remove(petUuid)
        }
    }
    
    private fun onPlayerChangeWorld(player: ServerPlayerEntity) {
        // Teleport pets when owner changes dimensions
        val familyId = getFamilyId(player.uuid) ?: return
        val pets = getFamilyPets(familyId).filter { it.world != player.world }
        
        pets.forEach { pet ->
            pet.teleport(player.world, player.x, player.y, player.z, setOf(), pet.yaw, pet.pitch)
        }
    }
    
    private fun onUseEntity(
        player: PlayerEntity,
        world: World,
        hand: Hand,
        entity: Entity,
        hitResult: net.minecraft.util.hit.EntityHitResult?
    ): ActionResult {
        val stack = player.getStackInHand(hand)
        
        // Check if using pet food on a family pet
        if (stack.isIn(net.minecraft.registry.tag.ItemTags.FISHES) && entity is Tameable) {
            val familyId = getFamilyId(player.uuid) ?: return ActionResult.PASS
            val pet = entity as? AnimalEntity ?: return ActionResult.PASS
            
            if (isFamilyPet(familyId, pet) && player is ServerPlayerEntity) {
                if (!player.abilities.creativeMode) {
                    stack.decrement(1)
                }
                
                // Heal the pet
                pet.heal(4.0f)
                world.playSound(
                    null, 
                    pet.x, pet.y, pet.z,
                    SoundEvents.ENTITY_GENERIC_EAT, 
                    SoundCategory.NEUTRAL, 
                    1.0f, 
                    1.0f + (world.random.nextFloat() - world.random.nextFloat()) * 0.4f
                )
                
                // Grant achievement
                Achievements.grantAchievement(player, Achievements.PET_LOVER)
                
                // Check for Family Zoo achievement
                if (getFamilyPets(familyId).size >= 5) {
                    Achievements.grantAchievement(player, Achievements.FAMILY_ZOO)
                }
                
                return ActionResult.success(world.isClient)
            }
        }
        
        return ActionResult.PASS
    }
    
    fun addPetToFamily(familyId: UUID, pet: Tameable) {
        val petUuid = pet.uuid
        familyPets.getOrPut(familyId) { mutableSetOf() }.add(petUuid)
        petToFamily[petUuid] = familyId
    }
    
    fun getFamilyPets(familyId: UUID): List<Tameable> {
        return familyPets[familyId]?.mapNotNull { uuid ->
            val entity = BeautyMod.getServer()?.getWorld(net.minecraft.world.World.OVERWORLD)?.getEntity(uuid)
            entity as? Tameable
        }?.filter { it.isAlive } ?: emptyList()
    }
    
    fun isFamilyPet(familyId: UUID, pet: Tameable): Boolean {
        return petToFamily[pet.uuid] == familyId
    }
    
    private fun getFamilyId(playerUuid: UUID): UUID? {
        // Implement this based on your family system
        // This should return the family ID that the player belongs to
        return playerUuid // For now, just return player's UUID
    }
    
    fun writeNbt(tag: NbtCompound): NbtCompound {
        val familyPetsTag = NbtCompound()
        
        familyPets.forEach { (familyId, petUuids) ->
            val uuidArray = net.minecraft.nbt.NbtList()
            petUuids.forEach { uuid ->
                uuidArray.add(NbtString(uuid.toString()))
            }
            familyPetsTag.put(familyId.toString(), uuidArray)
        }
        
        tag.put("FamilyPets", familyPetsTag)
        return tag
    }
    
    fun readNbt(tag: NbtCompound) {
        familyPets.clear()
        petToFamily.clear()
        
        if (tag.contains("FamilyPets", net.minecraft.nbt.NbtElement.COMPOUND_TYPE.toInt())) {
            val familyPetsTag = tag.getCompound("FamilyPets")
            
            familyPetsTag.keys.forEach { familyIdStr ->
                try {
                    val familyId = UUID.fromString(familyIdStr)
                    val uuidArray = familyPetsTag.getList(familyIdStr, net.minecraft.nbt.NbtElement.STRING_TYPE.toInt())
                    
                    uuidArray.forEach { uuidElement ->
                        try {
                            val petUuid = UUID.fromString(uuidElement.asString())
                            familyPets.getOrPut(familyId) { mutableSetOf() }.add(petUuid)
                            petToFamily[petUuid] = familyId
                        } catch (e: Exception) {
                            BeautyMod.LOGGER.error("Failed to parse pet UUID: ${uuidElement.asString()}", e)
                        }
                    }
                } catch (e: Exception) {
                    BeautyMod.LOGGER.error("Failed to parse family ID: $familyIdStr", e)
                }
            }
        }
    }
}