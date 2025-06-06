package com.evacoffee.beautymod.client.gui.screen;

import com.evacoffee.beautymod.BeautyMod;
import com.evacoffee.beautymod.client.text.TextEffect;
import com.evacoffee.beautymod.client.text.TextEffectRenderer;
import com.evacoffee.beautymod.dating.ConversationTopic;
import com.evacoffee.beautymod.dating.activites.ConversationActivity;
import com.mojang.blaz3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class CoversationScreen extends Screen {
    private static final Identifier TEXTURE = new Identifier(BeautyMod.MOD_ID, "textures/gui/conversation_bg.png");
    private static final int WINDOW_WIDTH = 248;
    private static final int WINDOW_HEIGHT = 166;
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int HIGHLIGHT_COLOR = 0xFFFFA0;

    private final ConversationActivity activity;
    private final Map<Text, TextEffectRenderer> textEffects = newHashMap<>();
    private float time = 0;
    private ConversationTopic currentTopic;
    private int currentTopicIndex;
    private int relationshipScore;
    private TextFieldWidget npcDialogue;
    private ButtonWidget[] responseButtons;
    private int x, y;

    public ConversationScreen(ConversationActivity activity, ConversationTopic topic,
                            int topicIndex, int relationshipScore) {
        super(Text.translatable("conversation.title"));
        this.activity = activity;
        this.currentTopic = topic;
        this.currentTopicIndex = topicIndex;
        this.relationshipScore = relationshipScore;
    }

    @Override
    protected void init() {
        super.init();
        this.x = (this.width - WINDOW_WIDTH) / 2;
        this.y = (this.height - WINDOW_HEIGHT) / 2;

        //NPC dialouge space
        this.npcDialogue = new TextFieldWidget(
            this.textRenderer,
            x + 10, y + 10,
            WINDOW_WIDTH - 20, 60,
            Text.empty()
        );
        this.npcDialogue.setEditable(false);
        this.npcDialogue.setMaxLength(500);
        this.npcDialogue.setVisible(true);

        //Response buttons
        this.responseButtons = new ButtonWidget[4];
        for (int i = 0; i < responseButtons.length; i++) {
            final int response Index = i;
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
        this.textEffects.clear();

        if (topic == null) {
            this.npcDialogue.setText("");
            for (ButtonWidget button : responseButtons) {
                button.visible = false;
            }
            return;
        }

        //Create effect for npc dialougee
        TextEffect typewriterEffect = new TextEffect.TypewriterEffect();
        textEffects.put(topic.getDisplayText(),
            new TextEffectRenderer(typewirterEffect, topic.getDisplayText()));

        // Create effects for every response
        for (Text response : topic.getResponses()) {
            TextEffect waveEffect = new TextEffect.WaveEffect(new String[]{"1.5f", "2.0f"});
            textEffects.put(response, new TextEffectRenderer(waveEffect, response));
        }

        //Update response buttons
        List<Text> responses = topic.getResponses();
        for (int i = 0; i < responseButtons.length; i++) {
            ButtonWidget button = responseButtons[i];
            if (i < responses.size()) {
                button.setMessage(Text.empty());
                button.visible = true;
                button.active = true;
            } else {
                button.visible = false;
                button.active = false;
            }
        }
    }

    private void handleResponse(int responseIndex) {
        if (activity != null && currentTopic != null &&
        responseIndex >= 0 && responseIndex < currentTopic.getResponses().size()) {
        activity.sendResponse(responseIndex);

        //Disable buttons after choosing
        for (ButtonWidget button : responseButtons) {
            button.active = false;
        }
    }
}

@Override
public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    // Update time for animations
    time += delta;
    
    // Update text effects
    for (TextEffectRenderer renderer : textEffects.values()) {
        renderer.update(delta);
    }
    
    // Render background
    renderBackground(context);
    
    // Draw window background
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    context.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
    context.fill(x, y, x + WINDOW_WIDTH, y + WINDOW_HEIGHT, 0xFF000000);
    context.fill(x + 1, y + 1, x + WINDOW_WIDTH - 1, y + WINDOW_HEIGHT - 1, 0xFF404040);
    
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
    context.fill(x + 5, y + 15, x + WINDOW_WIDTH - 5, y + 75, 0xFF202020);
    drawBorder(context, x + 5, y + 15, WINDOW_WIDTH - 10, 60, 0xFF606060);
    
    // Render NPC dialogue with effect
    if (currentTopic != null) {
            TextEffectRenderer renderer = textEffects.get(currentTopic.getDisplayText());
            if (renderer != null) {
                renderer.render(context, textRenderer, x + 15, y + 25, TEXT_COLOR);
            }
        }
        
    // Render response buttons with effects
    super.render(context, mouseX, mouseY, delta);
    
    // Render response text on top of buttons
    if (currentTopic != null) {
            List<Text> responses = currentTopic.getResponses();
            for (int i = 0; i < Math.min(responses.size(), responseButtons.length); i++) {
                ButtonWidget button = responseButtons[i];
                if (button.visible) {
                    Text response = responses.get(i);
                    TextEffectRenderer renderer = textEffects.get(response);
                    if (renderer != null) {
                        renderer.render(
                            context, 
                            textRenderer,
                            button.getX() + 5,
                            button.getY() + 5,
                            button.active ? TEXT_COLOR : 0xA0A0A0
                        );
                    }
                }
            }
        }
        
    // Render tooltips
    renderTooltips(context, mouseX, mouseY);
    }
    
    private void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        // Top
        context.fill(x, y, x + width, y + 1, color);
        // Bottom
        context.fill(x, y + height - 1, x + width, y + height, color);
        // Left
        context.fill(x, y, x + 1, y + height, color);
        // Right
        context.fill(x + width - 1, y, x + width, y + height, color);
    }
    
    private void renderTooltips(DrawContext context, int mouseX, int mouseY) {
        if (currentTopic == null) return;
        
        // Check if hovering over response buttons
        List<Text> responses = currentTopic.getResponses();
        for (int i = 0; i < Math.min(responses.size(), responseButtons.length); i++) {
            ButtonWidget button = responseButtons[i];
            if (button.isHovered() && button.visible) {
                int responseIndex = i;
                if (responseIndex >= 0 && responseIndex < responses.size()) {
                    int impact = currentTopic.getResponseImpact(responseIndex);
                    if (impact != 0) {
                        Text tooltip = Text.translatable(
                            impact > 0 ? "conversation.impact.positive" : "conversation.impact.negative",
                            Math.abs(impact)
                        );
                        context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
                    }
                }
                break;
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
