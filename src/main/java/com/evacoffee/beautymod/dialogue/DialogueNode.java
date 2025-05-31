package com.evacoffee.beautymod.dialogue;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class DialogueNode {
    private final String id;
    private String npcName;
    private String text;
    private final List<DialogueOption> options = new ArrayList<>();
    private Consumer<ServerPlayerEntity> onStart;
    private Predicate<ServerPlayerEntity> condition;
    private String speakerTexture;
    private String backgroundTexture;
    private String soundEvent;
    private float volume = 1.0f;
    private float pitch = 1.0f;
    private final Map<String, Object> metadata = new HashMap<>();

    private DialogueNode(Builder builder) {
        this.id = builder.id;
        this.npcName = builder.npcName;
        this.text = builder.text;
        this.options.addAll(builder.options);
        this.onStart = builder.onStart;
        this.condition = builder.condition;
        this.speakerTexture = builder.speakerTexture;
        this.backgroundTexture = builder.backgroundTexture;
        this.soundEvent = builder.soundEvent;
        this.volume = builder.volume;
        this.pitch = builder.pitch;
        this.metadata.putAll(builder.metadata);
    }

    public static class Builder {
        private final String id;
        private String npcName = "";
        private String text = "";
        private final List<DialogueOption> options = new ArrayList<>();
        private Consumer<ServerPlayerEntity> onStart = player -> {};
        private Predicate<ServerPlayerEntity> condition = player -> true;
        private String speakerTexture;
        private String backgroundTexture;
        private String soundEvent;
        private float volume = 1.0f;
        private float pitch = 1.0f;
        private final Map<String, Object> metadata = new HashMap<>();

        public Builder(String id) {
            this.id = id;
        }

        public Builder npcName(String npcName) {
            this.npcName = npcName;
            return this;
        }

        public Builder text(String text) {
            this.text = text;
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
                    addOption(option);
                }
            }
            return this;
        }

        public Builder onStart(Consumer<ServerPlayerEntity> onStart) {
            this.onStart = onStart != null ? onStart : player -> {};
            return this;
        }

        public Builder condition(Predicate<ServerPlayerEntity> condition) {
            this.condition = condition != null ? condition : player -> true;
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

        public Builder metadata(String key, Object value) {
            if (key != null) {
                this.metadata.put(key, value);
            }
            return this;
        }

        public DialogueNode build() {
            return new DialogueNode(this);
        }
    }

    // Getters
    public String getId() { return id; }
    public String getNpcName() { return npcName; }
    public String getText() { return text; }
    public List<DialogueOption> getOptions() { return new ArrayList<>(options); }
    public Consumer<ServerPlayerEntity> getOnStart() { return onStart; }
    public boolean isAvailable(ServerPlayerEntity player) { return condition.test(player); }
    public String getSpeakerTexture() { return speakerTexture; }
    public String getBackgroundTexture() { return backgroundTexture; }
    public String getSoundEvent() { return soundEvent; }
    public float getVolume() { return volume; }
    public float getPitch() { return pitch; }
    public <T> T getMetadata(String key, Class<T> type) { 
        return type.cast(metadata.get(key)); 
    }
}