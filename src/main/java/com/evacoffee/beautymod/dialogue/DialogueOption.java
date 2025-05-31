package com.evacoffee.beautymod.dialogue;

import net.minecraft.text.Text;
import net.minecraft.text.MutableText;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.*;

public class DialogueOption {
    private final Text text;
    private final Consumer<PlayerEntity> onSelect;
    private final String nextNodeId;
    private final List<Requirement> requirements;
    private final Predicate<PlayerEntity> condition;
    private final int cooldownTicks;
    private final Map<UUID, Long> cooldownEndTimes;
    private final boolean hasVisualEffect;
    private final String visualEffectType;
    private final String soundEvent;
    private final float volume;
    private final float pitch;

    public DialogueOption(Text text, Consumer<PlayerEntity> onSelect, String nextNodeId, 
                         List<Requirement> requirements, Predicate<PlayerEntity> condition, 
                         int cooldownTicks, boolean hasVisualEffect, String visualEffectType,
                         String soundEvent, float volume, float pitch) {
        this.text = text != null ? text : Text.empty();
        this.onSelect = onSelect != null ? onSelect : p -> {};
        this.nextNodeId = nextNodeId;
        this.requirements = requirements != null ? new ArrayList<>(requirements) : new ArrayList<>();
        this.condition = condition != null ? condition : p -> true;
        this.cooldownTicks = Math.max(0, cooldownTicks);
        this.cooldownEndTimes = new HashMap<>();
        this.hasVisualEffect = hasVisualEffect;
        this.visualEffectType = visualEffectType != null ? visualEffectType : "none";
        this.soundEvent = soundEvent;
        this.volume = volume > 0 ? volume : 1.0f;
        this.pitch = pitch > 0 ? pitch : 1.0f;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static DialogueOption of(String text, String nextNodeId) {
        return builder().text(text).nextNodeId(nextNodeId).build();
    }

    public static DialogueOption of(String text, Consumer<PlayerEntity> onSelect, String nextNodeId) {
        return builder().text(text).onSelect(onSelect).nextNodeId(nextNodeId).build();
    }

    public static DialogueOption exit(String text) {
        return builder().text(text).build();
    }

    public static DialogueOption exit(String text, Consumer<PlayerEntity> onSelect) {
        return builder().text(text).onSelect(onSelect).build();
    }

    public boolean isAvailable(PlayerEntity player) {
        if (player == null) return false;
        
        // Check cooldown
        if (isOnCooldown(player)) {
            return false;
        }
        
        // Check condition
        if (!condition.test(player)) {
            return false;
        }
        
        // Check requirements
        for (Requirement req : requirements) {
            if (!req.isMet(player)) {
                return false;
            }
        }
        
        return true;
    }

    public boolean isOnCooldown(PlayerEntity player) {
        if (cooldownTicks <= 0) return false;
        Long endTime = cooldownEndTimes.get(player.getUuid());
        if (endTime == null) return false;
        return player.getWorld().getTime() < endTime;
    }

    public long getRemainingCooldownTicks(PlayerEntity player) {
        if (cooldownTicks <= 0) return 0;
        Long endTime = cooldownEndTimes.get(player.getUuid());
        if (endTime == null) return 0;
        return Math.max(0, endTime - player.getWorld().getTime());
    }

    public void startCooldown(PlayerEntity player) {
        if (cooldownTicks > 0) {
            cooldownEndTimes.put(player.getUuid(), player.getWorld().getTime() + cooldownTicks);
        }
    }

    public void playVisualEffects(ServerPlayerEntity player) {
        if (!hasVisualEffect || !(player.getWorld() instanceof ServerWorld world)) {
            return;
        }

        Vec3d pos = player.getPos();
        switch (visualEffectType.toLowerCase()) {
            case "hearts" -> world.spawnParticles(
                ParticleTypes.HEART, 
                pos.x, pos.y + 2.0, pos.z, 
                5, 1, 1, 1, 0.1
            );
            case "angry" -> world.spawnParticles(
                ParticleTypes.ANGRY_VILLAGER, 
                pos.x, pos.y + 2.0, pos.z, 
                10, 0.5, 0.5, 0.5, 0.1
            );
            case "happy" -> world.spawnParticles(
                ParticleTypes.HAPPY_VILLAGER, 
                pos.x, pos.y + 2.0, pos.z, 
                15, 0.5, 0.5, 0.5, 0.1
            );
            case "magic" -> world.spawnParticles(
                ParticleTypes.ENCHANT, 
                pos.x, pos.y + 1.5, pos.z, 
                20, 0.5, 0.5, 0.5, 0.1
            );
        }
    }


    public void playSound(ServerPlayerEntity player) {
        if (soundEvent != null && !soundEvent.isEmpty()) {
            player.playSound(new Identifier(soundEvent), volume, pitch);
        }
    }

    // Getters
    public Text getText() { return text; }
    public Consumer<PlayerEntity> getOnSelect() { return onSelect; }
    public String getNextNodeId() { return nextNodeId; }
    public List<Requirement> getRequirements() { return new ArrayList<>(requirements); }
    public Predicate<PlayerEntity> getCondition() { return condition; }
    public int getCooldownTicks() { return cooldownTicks; }
    public boolean hasVisualEffect() { return hasVisualEffect; }
    public String getVisualEffectType() { return visualEffectType; }

    public static class Builder {
        private Text text = Text.empty();
        private Consumer<PlayerEntity> onSelect = p -> {};
        private String nextNodeId = null;
        private final List<Requirement> requirements = new ArrayList<>();
        private Predicate<PlayerEntity> condition = p -> true;
        private int cooldownTicks = 0;
        private boolean hasVisualEffect = false;
        private String visualEffectType = "none";
        private String soundEvent = null;
        private float volume = 1.0f;
        private float pitch = 1.0f;

        public Builder text(String text) {
            this.text = Text.literal(text);
            return this;
        }

        public Builder text(Text text) {
            this.text = text != null ? text : Text.empty();
            return this;
        }

        public Builder onSelect(Consumer<PlayerEntity> onSelect) {
            this.onSelect = onSelect != null ? onSelect : p -> {};
            return this;
        }

        public Builder nextNodeId(String nextNodeId) {
            this.nextNodeId = nextNodeId;
            return this;
        }

        public Builder addRequirement(Requirement requirement) {
            if (requirement != null) {
                this.requirements.add(requirement);
            }
            return this;
        }

        public Builder condition(Predicate<PlayerEntity> condition) {
            this.condition = condition != null ? condition : p -> true;
            return this;
        }

        public Builder cooldownTicks(int cooldownTicks) {
            this.cooldownTicks = Math.max(0, cooldownTicks);
            return this;
        }

        public Builder withVisualEffect(String effectType) {
            this.hasVisualEffect = true;
            this.visualEffectType = effectType != null ? effectType : "none";
            return this;
        }

        public Builder withSound(String soundEvent, float volume, float pitch) {
            this.soundEvent = soundEvent;
            this.volume = volume > 0 ? volume : 1.0f;
            this.pitch = pitch > 0 ? pitch : 1.0f;
            return this;
        }

        public DialogueOption build() {
            return new DialogueOption(
                text, onSelect, nextNodeId, 
                new ArrayList<>(requirements), condition, 
                cooldownTicks, hasVisualEffect, visualEffectType,
                soundEvent, volume, pitch
            );
        }
    }

    public static class Requirement {
        private final String description;
        private final Predicate<PlayerEntity> check;
        private final String failMessage;

        public Requirement(String description, Predicate<PlayerEntity> check, String failMessage) {
            this.description = description != null ? description : "";
            this.check = check != null ? check : p -> true;
            this.failMessage = failMessage != null ? failMessage : "";
        }

        public boolean isMet(PlayerEntity player) {
            return check.test(player);
        }

        public String getDescription() {
            return description;
        }

        public String getFailMessage() {
            return failMessage;
        }

        public static Requirement of(String description, Predicate<PlayerEntity> check, String failMessage) {
            return new Requirement(description, check, failMessage);
        }
    }
}