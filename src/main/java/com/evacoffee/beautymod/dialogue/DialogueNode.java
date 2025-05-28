package com.evacoffee.beautymod.dialogue;

import java.util.ArrayList;
import java.util.List;

public class DialogueNode {
    private final String message;
    private final List<DialogueNode> options = new ArrayList<>();
    private int affectionRequirement = 0;
    
    public DialogueNode(String message) {
        this.message = message;
    }
    
    public void addOption(DialogueNode option) {
        this.options.add(option);
    }
    
    public void addResponse(String response) {
        this.options.add(new DialogueNode(response));
    }
    
    public void setAffectionRequirement(int amount) {
        this.affectionRequirement = amount;
    }
    
    public int getAffectionRequirement() {
        return affectionRequirement;
    }
    
    public String getMessage() {
        return message;
    }
    
    public List<DialogueNode> getOptions() {
        return options;
    }
}
