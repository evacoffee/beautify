package com.evacoffee.beautymod.client.gui.screen;

import com.evacoffee.beautymod.BeautyMod;
import com.evacoffee.beautymod.dating.ConversationTopic;
import com.evacoffee.beautymod.dating.activities.ConversationActivity;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.List;
import java.util.Objects;

public class ConversationScreen extends Screen {
    private static final Identifier TEXTURE = new Identifier(BeautyMod.MOD_ID, "textures/gui/conversation_bg.png");
    private static final int WINDOW_WIDTH = 248;
    private static final int WINDOW_HEIGHT = 166;
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int HIGHLIGHT_COLOR = 0xFFFFA0;
    
    private final ConversationActivity activity;
    private final List<ConversationTopic> topics;
    private ConversationTopic currentTopic;
    private int currentTopicIndex;
    private int relationshipScore;
    private TextFieldWidget npcDialogue;
    private ButtonWidget[] responseButtons;
    private int x, y;

    public ConversationScreen(ConversationActivity activity, List<ConversationTopic> topics, 
                             ConversationTopic currentTopic, int currentTopicIndex, int relationshipScore) {
        super(Text.translatable("conversation.title"));
        this.activity = activity;
        this.topics = topics;
        this.currentTopic = currentTopic;
        this.currentTopicIndex = currentTopicIndex;
        this.relationshipScore = relationshipScore;
    }

    @Override
    protected void init() {
        super.init();
        this.x = (this.width - WINDOW_WIDTH) / 2;
        this.y = (this.height - WINDOW_HEIGHT) / 2;

        // NPC dialogue area
        this.npcDialogue = new TextFieldWidget(
            this.textRenderer,
            x + 10, y + 10,
            WINDOW_WIDTH - 20, 60,
            Text.empty()
        );
        this.npcDialogue.setEditable(false);
        this.npcDialogue.setMaxLength(500);
        this.npcDialogue.setVisible(true);
        
        // Response buttons
        this.responseButtons = new ButtonWidget[4];
        for (int i = 0; i < responseButtons.length; i++) {
            final int responseIndex = i;
            this.responseButtons[i] = ButtonWidget.builder(
                Text.empty(),
                button -> handleResponse(responseIndex)
            )
            .dimensions(x + 10, y + 80 + (i * 22), WINDOW_WIDTH - 20, 20)
            .build();
            this.addDrawableChild(this.responseButtons[i]);
        }
        
        updateTopic(currentTopic, currentTopicIndex);
    }

    public void updateTopic(ConversationTopic topic, int topicIndex) {
        this.currentTopic = topic;
        this.currentTopicIndex = topicIndex;
        
        if (topic == null) {
            this.npcDialogue.setText("");
            for (ButtonWidget button : responseButtons) {
                button.visible = false;
            }
            return;
        }
        
        // Update NPC dialogue
        this.npcDialogue.setText(topic.getDisplayText());
        
        // Update response buttons
        List<Text> responses = topic.getResponses();
        for (int i = 0; i < responseButtons.length; i++) {
            ButtonWidget button = responseButtons[i];
            if (i < responses.size()) {
                button.setMessage(responses.get(i));
                button.visible = true;
                button.active = true;
            } else {
                button.visible = false;
            }
        }
    }

    private void handleResponse(int responseIndex) {
        if (activity != null && currentTopic != null && 
            responseIndex >= 0 && responseIndex < currentTopic.getResponses().size()) {
            activity.sendResponse(responseIndex);
            
            // Disable buttons after selection
            for (ButtonWidget button : responseButtons) {
                button.active = false;
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render background
        renderBackground(context);
        
        // Draw window background
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        context.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        fill(context, x, y, x + WINDOW_WIDTH, y + WINDOW_HEIGHT, 0xFF000000);
        fill(context, x + 1, y + 1, x + WINDOW_WIDTH - 1, y + WINDOW_HEIGHT - 1, 0xFF404040);
        
        // Render title
        context.drawTextWithShadow(
            this.textRenderer,
            Text.translatable("conversation.title"),
            x + 10, y + 2, HIGHLIGHT_COLOR
        );
        
        // Render relationship score
        context.drawTextWithShadow(
            this.textRenderer,
            Text.translatable("conversation.relationship", relationshipScore),
            x + WINDOW_WIDTH - 100, y + 2, TEXT_COLOR
        );
        
        // Render NPC dialogue area
        fill(context, x + 5, y + 15, x + WINDOW_WIDTH - 5, y + 75, 0xFF202020);
        drawBorder(context, x + 5, y + 15, WINDOW_WIDTH - 10, 60, 0xFF606060);
        
        // Render NPC dialogue text
        if (currentTopic != null) {
            List<Text> lines = this.textRenderer.wrapLines(currentTopic.getDisplayText(), WINDOW_WIDTH - 30);
            for (int i = 0; i < Math.min(lines.size(), 4); i++) {
                context.drawText(this.textRenderer, lines.get(i), 
                    x + 15, y + 25 + (i * 10), TEXT_COLOR, false);
            }
        }
        
        // Render response buttons
        super.render(context, mouseX, mouseY, delta);
        
        // Render tooltips
        for (ButtonWidget button : responseButtons) {
            if (button.isHovered() && button.visible) {
                // Show relationship impact tooltip
                int responseIndex = -1;
                for (int i = 0; i < responseButtons.length; i++) {
                    if (responseButtons[i] == button) {
                        responseIndex = i;
                        break;
                    }
                }
                
                if (responseIndex >= 0 && currentTopic != null && 
                    responseIndex < currentTopic.getResponses().size()) {
                    int impact = currentTopic.getResponseImpact(responseIndex);
                    if (impact != 0) {
                        Text tooltip = Text.translatable(
                            impact > 0 ? "conversation.impact.positive" : "conversation.impact.negative",
                            Math.abs(impact)
                        );
                        context.drawTooltip(this.textRenderer, tooltip, mouseX, mouseY);
                    }
                }
            }
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        if (activity != null && activity.isActive()) {
            // Don't allow closing the screen if the conversation is still active
            return;
        }
        super.close();
    }
}
