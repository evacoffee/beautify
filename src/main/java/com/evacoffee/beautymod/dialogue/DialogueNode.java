package com.evacoffee.beautymod.dialogue;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Represents a node in a dialogue tree.
 * Each node contains text spoken by an NPC and a list of possible responses.
 */
public class DialogueNode {
    private final String id;
    private final String npcName;
    private final Text text;
    private final List<DialogueOption> options;
    private final Map<String, Object> metadata;
    private final Predicate<PlayerEntity> condition;
    private final String speakerTexture;
    private final String backgroundTexture;
    private final String soundEvent;
    private final float volume;
    private final float pitch;
    private final int displayTime;
    private final boolean isQuestion;

    private DialogueNode(Builder builder) {
        this.id = builder.id;
        this.npcName = builder.npcName;
        this.text = builder.text != null ? builder.text : Text.empty();
        this.options = new ArrayList<>(builder.options);
        this.metadata = new HashMap<>(builder.metadata);
        this.condition = builder.condition;
        this.speakerTexture = builder.speakerTexture;
        this.backgroundTexture = builder.backgroundTexture;
        this.soundEvent = builder.soundEvent;
        this.volume = builder.volume;
        this.pitch = builder.pitch;
        this.displayTime = builder.displayTime;
        this.isQuestion = builder.isQuestion;
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
    
    public Map<String, Object> getMetadata() { 
        return new HashMap<>(metadata); 
    }
    
    public String getSpeakerTexture() { 
        return speakerTexture; 
    }
    
    public String getBackgroundTexture() { 
        return backgroundTexture; 
    }
    
    public String getSoundEvent() { 
        return soundEvent; 
    }
    
    public float getVolume() { 
        return volume; 
    }
    
    public float getPitch() { 
        return pitch; 
    }
    
    public int getDisplayTime() { 
        return displayTime; 
    }
    
    public boolean isQuestion() { 
        return isQuestion; 
    }
    
    public boolean isAvailable(PlayerEntity player) {
        return condition == null || condition.test(player);
    }
    
    /**
     * Gets the list of available options for the given player.
     * Filters out options that don't meet their requirements or conditions.
     */
    public List<DialogueOption> getAvailableOptions(PlayerEntity player) {
        if (player == null) return Collections.emptyList();
        List<DialogueOption> available = new ArrayList<>();
        for (DialogueOption option : options) {
            if (option.isAvailable(player)) {
                available.add(option);
            }
        }
        return available;
    }
    
    /**
     * Registers this node with the DialogueManager.
     * Must be called for the node to be accessible in dialogues.
     */
    public void register() {
        DialogueManager.registerNode(this);
    }
    
    /**
     * Creates a new builder for DialogueNode.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder class for creating DialogueNode instances.
     */
    public static class Builder {
        private String id;
        private String npcName = "NPC";
        private Text text = Text.empty();
        private final List<DialogueOption> options = new ArrayList<>();
        private final Map<String, Object> metadata = new HashMap<>();
        private Predicate<PlayerEntity> condition = p -> true;
        private String speakerTexture = null;
        private String backgroundTexture = null;
        private String soundEvent = null;
        private float volume = 1.0f;
        private float pitch = 1.0f;
        private int displayTime = -1; // -1 means no auto-advance
        private boolean isQuestion = true;
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder npcName(String npcName) {
            this.npcName = npcName != null ? npcName : "NPC";
            return this;
        }
        
        public Builder text(String text) {
            this.text = text != null ? Text.literal(text) : Text.empty();
            return this;
        }
        
        public Builder text(Text text) {
            this.text = text != null ? text : Text.empty();
            return this;
        }
        
        public Builder addOption(DialogueOption option) {
            if (option != null) {
                this.options.add(option);
            }
            return this;
        }
        
        public Builder addOptions(DialogueOption... options) {
            if (options != null) {
                for (DialogueOption option : options) {
                    if (option != null) {
                        this.options.add(option);
                    }
                }
            }
            return this;
        }
        
        public Builder metadata(String key, Object value) {
            if (key != null) {
                this.metadata.put(key, value);
            }
            return this;
        }
        
        public Builder condition(Predicate<PlayerEntity> condition) {
            this.condition = condition != null ? condition : p -> true;
            return this;
        }
        
        public Builder speakerTexture(String texturePath) {
            this.speakerTexture = texturePath;
            return this;
        }
        
        public Builder backgroundTexture(String texturePath) {
            this.backgroundTexture = texturePath;
            return this;
        }
        
        public Builder sound(String soundEvent, float volume, float pitch) {
            this.soundEvent = soundEvent;
            this.volume = volume > 0 ? volume : 1.0f;
            this.pitch = pitch > 0 ? pitch : 1.0f;
            return this;
        }
        
        public Builder displayTime(int ticks) {
            this.displayTime = Math.max(-1, ticks);
            return this;
        }
        
        public Builder isQuestion(boolean isQuestion) {
            this.isQuestion = isQuestion;
            return this;
        }
        
        public DialogueNode build() {
            if (id == null || id.isEmpty()) {
                throw new IllegalStateException("Dialogue node must have a non-null, non-empty ID");
            }
            return new DialogueNode(this);
        }
    }
    
    public String getNpcName() { 
        return npcName; 
    }
    
    public String getText() { 
        return text; 
    }
    
    public List<DialogueOption> getOptions() { 
        return new ArrayList<>(options); 
    }
}