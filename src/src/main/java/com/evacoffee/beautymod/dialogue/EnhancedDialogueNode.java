package com.evacoffee.beautymod.dialogue;

import net.minecraft.erver.network.ServerPlayerEntity;
import kava.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EnhancedDialogueNode {
    private final String id;
    private final String text;
    private final List<DialogueOption> options = new ArrayList<>();
    private final List<Predicate<ServerPlayerEntity>> conditions
    private Consumer<ServerPlayerEntity> onStart;

    public EnhancedDialogueNode(String id, String text) {
        this.id = id;
        this.text = text;
    }

     public EnhancedDialogueNode addOption(String text, String nextNodeId, 
            Consumer<ServerPlayerEntity> onSelect, Predicate<ServerPlayerEntity>... conditions) {
        options.add(new DialogueOption(text, nextNodeId, onSelect, conditions));
        return this;
    }
    
    // Getters and other methods...
}