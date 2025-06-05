package com.evacoffee.beautymod.dating;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.*;

public class ConversationTopic {
    private final Identifier id;
    private final Map<String, Text> textMap;
    private final List<Text> responses;
    private final List<Integer> responseImpacts;
    private final int minRelationshipLevel;
    private final Set<Identifier> requiredTopics;
    private final Set<Identifier> blocksTopics;

    public ConversationTopic(Identifier id, Map<String, Text> textMap, List<Text> responses, 
                            List<Integer> responseImpacts, int minRelationshipLevel, 
                            Set<Identifier> requiredTopics, Set<Identifier> blocksTopics) {
        this.id = Objects.requireNonNull(id, "Topic ID cannot be null");
        this.textMap = new HashMap<>(Objects.requireNonNull(textMap, "Text map cannot be null"));
        this.responses = new ArrayList<>(Objects.requireNonNull(responses, "Responses list cannot be null"));
        this.responseImpacts = new ArrayList<>(Objects.requireNonNull(responseImpacts, "Response impacts list cannot be null"));
        this.minRelationshipLevel = Math.max(0, minRelationshipLevel);
        this.requiredTopics = requiredTopics != null ? new HashSet<>(requiredTopics) : new HashSet<>();
        this.blocksTopics = blocksTopics != null ? new HashSet<>(blocksTopics) : new HashSet<>();
        
        // Validate that responses and impacts have matching sizes
        if (this.responses.size() != this.responseImpacts.size()) {
            throw new IllegalArgumentException("Number of responses must match number of response impacts");
        }
    }

    public Identifier getId() {
        return id;
    }

    public Text getDisplayText() {
        return textMap.getOrDefault("display", Text.literal("conversation.topic." + id.getPath() + ".text"));
    }

    public List<Text> getResponses() {
        return responses;
    }

    public Text getResponseText(int index) {
        if (index < 0 || index >= responses.size()) {
            return Text.literal("conversation.response.invalid");
        }
        return responses.get(index);
    }

    public int getResponseImpact(int index) {
        if (index < 0 || index >= responseImpacts.size()) {
            return 0;
        }
        return responseImpacts.get(index);
    }

    public List<Integer> getResponseImpacts() {
        return new ArrayList<>(responseImpacts);
    }

    public int getMinRelationshipLevel() {
        return minRelationshipLevel;
    }

    public Set<Identifier> getRequiredTopics() {
        return new HashSet<>(requiredTopics);
    }

    public Set<Identifier> getBlocksTopics() {
        return new HashSet<>(blocksTopics);
    }

    public boolean canShow(int playerRelationshipLevel, Set<Identifier> completedTopics) {
        if (playerRelationshipLevel < minRelationshipLevel) {
            return false;
        }
        return completedTopics.containsAll(requiredTopics);
    }

    public static class Builder {
        private final Identifier id;
        private final Map<String, Text> textMap = new HashMap<>();
        private final List<Text> responses = new ArrayList<>();
        private final List<Integer> responseImpacts = new ArrayList<>();
        private int minRelationshipLevel = 0;
        private Set<Identifier> requiredTopics = new HashSet<>();
        private Set<Identifier> blocksTopics = new HashSet<>();

        public Builder(Identifier id) {
            this.id = id;
        }

        public Builder withText(String key, Text text) {
            textMap.put(key, text);
            return this;
        }

        public Builder withResponse(Text response, int relationshipImpact) {
            if (responses.size() >= MAX_RESPONSES) {
                throw new IllegalStateException("Cannot add more than " + MAX_RESPONSES + " responses");
            }
            if (response == null) {
                throw new IllegalArgumentException("Response text cannot be null");
            }
            responses.add(response);
            responseImpacts.add(relationshipImpact);
            return this;
        }

        public Builder withMinRelationshipLevel(int level) {
            this.minRelationshipLevel = level;
            return this;
        }

        public Builder requiresTopic(Identifier topicId) {
            this.requiredTopics.add(topicId);
            return this;
        }

        public Builder blocksTopic(Identifier topicId) {
            this.blocksTopics.add(topicId);
            return this;
        }

        public ConversationTopic build() {
            return new ConversationTopic(id, textMap, responses, responseImpacts, 
                                       minRelationshipLevel, requiredTopics, blocksTopics);
        }
    }
}
