package com.evacoffee.beautymod.achievement

import com.evacoffee.beautymod.BeautyMod
import com.evacoffee.beautymod.family.AdoptionManager
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.advancement.Advancement
import net.minecraft.advancement.AdvancementFrame
import net.minecraft.advancement.criterion.ImpossibleCriterion
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.predicate.entity.EntityPredicate
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Identifier

object Achievements {
    private val ACHIEVEMENTS = mutableListOf<Achievement>()
    
    // Define achievements
    val NEW_FAMILY = register("new_family", "New Family", Items.OAK_SAPLING)
    val BIG_HAPPY_FAMILY = register("big_happy_family", "Big Happy Family", Items.CAKE)
    val FAMILY_REUNION = register("family_reunion", "Family Reunion", Items.TOTEM_OF_UNDYING)
    val PET_LOVER = register("pet_lover", "Pet Lover", Items.BONE)
    val FAMILY_ZOO = register("family_zoo", "Family Zoo", Items.LEAD)
    
    private fun register(id: String, name: String, icon: net.minecraft.item.Item): Achievement {
        return Advancement.Builder.create()
            .apply {
                display(
                    icon,
                    Text.translatable("achievement.beautymod.$id"),
                    Text.translatable("achievement.beautymod.${id}.desc"),
                    null,
                    AdvancementFrame.TASK,
                    true,
                    true,
                    false
                )
                criteriaMerger(net.minecraft.advancement.CriterionMerger.OR)
                criterion("impossible", ImpossibleCriterion.Conditions.INSTANCE)
            }
            .build(Identifier(BeautyMod.MOD_ID, id))
            .also { ACHIEVEMENTS.add(it) }
    }
    
    fun register() {
        // Register advancement trigger listeners
        ServerLifecycleEvents.SERVER_STARTING.register { server ->
            val advancementManager = server.advancementManager
            ACHIEVEMENTS.forEach { advancementManager.addAdvancement(it) }
        }
        
        // Track family size for achievements
        ServerEntityEvents.ENTITY_LOAD.register { entity, _ ->
            if (entity is ServerPlayerEntity) {
                val familySize = AdoptionManager.getFamilySize(entity.uuid)
                checkFamilyAchievements(entity, familySize)
            }
        }
    }
    
    private fun checkFamilyAchievements(player: ServerPlayerEntity, familySize: Int) {
        when (familySize) {
            1 -> grantAchievement(player, NEW_FAMILY)
            in 5..Int.MAX_VALUE -> grantAchievement(player, BIG_HAPPY_FAMILY)
        }
    }
    
    private fun grantAchievement(player: ServerPlayerEntity, advancement: Achievement) {
        player.advancementTracker.apply {
            if (!getProgress(advancement).isDone) {
                grantCriterion(advancement, "impossible")
            }
        }
    }
}