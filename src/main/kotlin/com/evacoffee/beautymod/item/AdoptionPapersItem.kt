package com.evacoffee.beautymod.item

import com.evacoffee.beautymod.BeautyMod
import com.evacoffee.beautymod.family.AdoptionManager
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import java.util.*

object AdoptionPapersItem : Item(
    FabricItemSettings()
        .group(BeautyMod.ITEM_GROUP)
        .maxCount(1)
) {
    private const val PARENT_KEY = "Parent"
    private const val CHILD_KEY = "Child"
    
    init {
        Registry.register(Registry.ITEM, Identifier(BeautyMod.MOD_ID, "adoption_papers"), this)
    }
    
    override fun useOnEntity(
        stack: ItemStack,
        user: PlayerEntity,
        entity: Entity,
        hand: Hand
    ): TypedActionResult<ItemStack> {
        if (user.world.isClient || entity !is PlayerEntity) {
            return TypedActionResult.pass(stack)
        }

        val server = user.world.server ?: return TypedActionResult.pass(stack)
        val serverUser = server.playerManager.getPlayer(user.uuid) ?: return TypedActionResult.pass(stack)
        val targetPlayer = server.playerManager.getPlayer(entity.uuid) ?: return TypedActionResult.pass(stack)
        
        if (targetPlayer == user) {
            user.sendMessage(
                Text.literal("You can't use this on yourself!").formatted(Formatting.RED),
                false
            )
            return TypedActionResult.fail(stack)
        }
        
        val adoptionManager = BeautyMod.getAdoptionManager()
        
        // If papers are blank, set the parent
        if (!stack.hasNbt() || !stack.nbt!!.contains(PARENT_KEY)) {
            val nbt = stack.orCreateNbt
            nbt.putUuid(PARENT_KEY, user.uuid)
            nbt.putString("ParentName", user.name.string)
            stack.nbt = nbt
            user.sendMessage(
                Text.literal("You've prepared adoption papers as the parent.")
                    .formatted(Formatting.GREEN),
                false
            )
            return TypedActionResult.success(stack)
        }
        
        // If papers have a parent, try to complete the adoption
        val parentUuid = stack.nbt!!.getUuid(PARENT_KEY)
        val parent = server.playerManager.getPlayer(parentUuid)
        
        if (parent == null || !parent.isAlive) {
            user.sendMessage(
                Text.literal("The parent is no longer online.").formatted(Formatting.RED),
                false
            )
            return TypedActionResult.fail(stack)
        }
        
        if (parent == user) {
            user.sendMessage(
                Text.literal("You can't adopt yourself!").formatted(Formatting.RED),
                false
            )
            return TypedActionResult.fail(stack)
        }
        
        // Request adoption
        adoptionManager.requestAdoption(parent as ServerPlayerEntity, targetPlayer as ServerPlayerEntity)
        
        // Play sound
        user.world.playSound(
            null,
            user.blockPos,
            SoundEvents.ITEM_BOOK_PAGE_TURN,
            SoundCategory.PLAYERS,
            1.0f,
            1.0f
        )
        
        // Consume the item
        if (!user.abilities.creativeMode) {
            stack.decrement(1)
        }
        
        return TypedActionResult.success(if (stack.isEmpty) ItemStack.EMPTY else stack)
    }
    
    override fun appendTooltip(
        stack: ItemStack,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext
    ) {
        val nbt = stack.nbt
        if (nbt != null && nbt.contains(PARENT_KEY)) {
            val parentName = nbt.getString("ParentName")
            tooltip.add(Text.literal("Parent: $parentName").formatted(Formatting.GRAY))
            tooltip.add(Text.literal("Right-click on a player to adopt them").formatted(Formatting.ITALIC, Formatting.GRAY))
        } else {
            tooltip.add(Text.literal("Right-click to set as parent").formatted(Formatting.ITALIC, Formatting.GRAY))
            tooltip.add(Text.literal("Then right-click on a player to adopt them").formatted(Formatting.ITALIC, Formatting.GRAY))
        }
    }
    
    override fun hasGlint(stack: ItemStack): Boolean {
        return stack.hasNbt() && stack.nbt!!.contains(PARENT_KEY)
    }
}