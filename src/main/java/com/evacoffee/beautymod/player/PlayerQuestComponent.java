package com.evacoffee.beautymod.player;

import com.evacoffee.beautymod.quest.QuestManager;
import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerQuestComponent implements Component {
    private QuestManager questManager;
    private final ServerPlayerEntity player;
    
    public PlayerQuestComponent(ServerPlayerEntity player) {
        this.player = player;
        this.questManager = new QuestManager(player);
    }
    
    public QuestManager getQuestManager() {
        return questManager;
    }
    
    @Override
    public void readFromNbt(NbtCompound tag) {
        if (questManager == null) {
            questManager = new QuestManager(player);
        }
        questManager.readFromNbt(tag);
    }
    
    @Override
    public void writeToNbt(NbtCompound tag) {
        if (questManager != null) {
            questManager.writeToNbt(tag);
        }
    }
}
