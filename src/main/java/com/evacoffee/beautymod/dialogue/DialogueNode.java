package com.evacoffee.beautymod.dialogue;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Represents a single node in a dialogue tree.
 * Contains the NPC's text and possible player responses.
 */
public class DialogueNode {
    private final String id;
    private final String npcName;
    private final Text text;
    private final List<DialogueOption> options;
    private Consumer<PlayerEntity> onEnter;

    /**
     * Creates a new dialogue node.
     * @param id Unique identifier for this node
     * @param npcName Name of the NPC speaking
     * @param text The dialogue text
     */
    public DialogueNode(String id, String npcName, String text) {
        this.id = id;
        this.npcName = npcName;
        this.text = Text.of(text);
        this.options = new ArrayList<>();
        this.onEnter = player -> {};
    }

    /**
     * Adds a response option to this dialogue node.
     * @param text The text of the response
     * @param nextNodeId The ID of the next node to go to when this response is selected
     * @param onSelect Action to perform when this response is selected
     * @return This DialogueNode for method chaining
     */
    public DialogueNode addOption(String text, String nextNodeId, Consumer<PlayerEntity> onSelect) {
        this.options.add(new DialogueOption(Text.of(text), nextNodeId, onSelect));
        return this;
    }

    /**
     * Adds a response option that ends the dialogue when selected.
     * @param text The text of the response
     * @param onSelect Action to perform when this response is selected
     * @return This DialogueNode for method chaining
     */
    public DialogueNode addExitOption(String text, Consumer<PlayerEntity> onSelect) {
        return addOption(text, null, onSelect);
    }

    /**
     * Sets an action to run when this node is entered.
     * @param action The action to run
     * @return This DialogueNode for method chaining
     */
    public DialogueNode onEnter(Consumer<PlayerEntity> action) {
        this.onEnter = action;
        return this;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getNpcName() {
        return npcName;
    }

    public Text getText() {
        return text;
    }

    public List<DialogueOption> getOptions() {
        return new ArrayList<>(options);
    }

    public Consumer<PlayerEntity> getOnEnter() {
        return onEnter;
    }

    /**
     * Represents a player response option in a dialogue.
     */
    public static class DialogueOption {
        private final Text text;
        private final String nextNodeId;
        private final Consumer<PlayerEntity> onSelect;

        public DialogueOption(Text text, String nextNodeId, Consumer<PlayerEntity> onSelect) {
            this.text = text;
            this.nextNodeId = nextNodeId;
            this.onSelect = onSelect != null ? onSelect : player -> {};
        }

        public Text getText() {
            return text;
        }

        public String getNextNodeId() {
            return nextNodeId;
        }

        public Consumer<PlayerEntity> getOnSelect() {
            return onSelect;
        }
    }
}