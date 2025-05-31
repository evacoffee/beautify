package com.evacoffee.beautymod.dialogue;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class DialogueOption {
    private final Text displayText;
    private final String nextNodeId;
    private final Consumer<ServerPlayerEntity> onSelect;
    private final Predicate<ServerPlayerEntity> condition;
    private final String icon;
    private final Text hoverText;
    private final boolean closeDialogue;

    private DialogueOption(Builder builder) {
        this.displayText = builder.displayText;
        this.nextNodeId = builder.nextNodeId;
        this.onSelect = builder.onSelect;
        this.condition = builder.condition;
        this.icon = builder.icon;
        this.hoverText = builder.hoverText;
        this.closeDialogue = builder.closeDialogue;
    }

    public static class Builder {
        private final Text displayText;
        private String nextNodeId = "";
        private Consumer<ServerPlayerEntity> onSelect = player -> {};
        private Predicate<ServerPlayerEntity> condition = player -> true;
        private String icon;
        private Text hoverText;
        private boolean closeDialogue = false;

        public Builder(Text displayText) {
            this.displayText = displayText;
        }

        public Builder nextNode(String nextNodeId) {
            this.nextNodeId = nextNodeId != null ? nextNodeId : "";
            return this;
        }

        public Builder onSelect(Consumer<ServerPlayerEntity> onSelect) {
            this.onSelect = onSelect != null ? onSelect : player -> {};
            return this;
        }

        public Builder condition(Predicate<ServerPlayerEntity> condition) {
            this.condition = condition != null ? condition : player -> true;
            return this;
        }

        public Builder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public Builder hoverText(Text hoverText) {
            this.hoverText = hoverText;
            return this;
        }

        public Builder closeDialogue() {
            this.closeDialogue = true;
            return this;
        }

        public DialogueOption build() {
            return new DialogueOption(this);
        }
    }

    // Getters
    public Text getDisplayText() { return displayText; }
    public String getNextNodeId() { return nextNodeId; }
    public Consumer<ServerPlayerEntity> getOnSelect() { return onSelect; }
    public boolean isAvailable(ServerPlayerEntity player) { return condition.test(player); }
    public String getIcon() { return icon; }
    public Text getHoverText() { return hoverText; }
    public boolean shouldCloseDialogue() { return closeDialogue; }
}