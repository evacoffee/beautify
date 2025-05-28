package com.evacoffee.beautymod.quest;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public abstract class Quest {
    protected final Identifier id;
    protected final String title;
    protected final String description;
    protected QuestStatus status = QuestStatus.NOT_STARTED;
    protected final int requiredAffection;
    
    public Quest(Identifier id, String title, String description, int requiredAffection) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.requiredAffection = requiredAffection;
    }
    
    public abstract boolean checkCompletion(PlayerEntity player);
    
    public void onComplete(PlayerEntity player) {
        this.status = QuestStatus.COMPLETED;
        player.sendMessage(Text.of("§aQuest completed: " + title), false);
    }
    
    public void start(PlayerEntity player) {
        if (this.status == QuestStatus.NOT_STARTED) {
            this.status = QuestStatus.IN_PROGRESS;
            player.sendMessage(Text.of("§eNew Quest: " + title), false);
            player.sendMessage(Text.of("§7" + description), false);
        }
    }
    
    // Getters
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public QuestStatus getStatus() { return status; }
    public int getRequiredAffection() { return requiredAffection; }
    public Identifier getId() { return id; }
    
    public enum QuestStatus {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED
    }
}
