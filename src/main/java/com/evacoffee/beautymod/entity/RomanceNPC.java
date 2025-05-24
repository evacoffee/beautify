package com.evacoffee.beautymod.entity;

import com.evacoffee.beautymod.love.LoveData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class RomanceNPC extends PathAwareEntity {
    private String personality = "neutral"; // default personality

    public RomanceNPC(EntityType<? extends PathAwareEntity> type, World world) {
        super(type, world);
    }

    // Called when player right-clicks the NPC
    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (!world.isClient) {
            int love = LoveData.getLove((ServerPlayerEntity) player);
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

            switch (personality) {
                case "boy":
                    player.sendMessage(Text.of("💬 Boy: Hey, You look cute."), false);
                    LoveData.addLove(serverPlayer, 5);
                    break;
                case "girl":
                    player.sendMessage(Text.of("💬 Girl: OMG I love your style! 💅"), false);
                    LoveData.addLove(serverPlayer, 7);
                    break;
                case "shy":
                    if (love < 20) {
                        player.sendMessage(Text.of("💬 Shy: ...Hi..."), false);
                    } else {
                        player.sendMessage(Text.of("💬 Shy: I-I like you. ❤️"), false);
                        LoveData.addLove(serverPlayer, 10);
                    }
                    break;
                case "flirty":
                    player.sendMessage(Text.of("💬 Flirty: Hey handsome 😉"), false);
                    LoveData.addLove(serverPlayer, 15);
                    break;
                default:
                    player.sendMessage(Text.of("💬 NPC: Hello there!"), false);
                    LoveData.addLove(serverPlayer, 3);
            }
        }
        return ActionResult.SUCCESS;
    }

    // Save personality to world data
    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putString("Personality", this.personality);
    }

    // Load personality from saved data
    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.personality = nbt.getString("Personality");
    }

    public void setPersonality(String type) {
        this.personality = type;
    }

    public String getPersonality() {
        return personality;
    }
}
