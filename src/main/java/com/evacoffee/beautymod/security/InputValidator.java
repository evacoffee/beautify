package com.evacoffee.beautymod.security;

import com.evacoffee.beautymod.util.ModLogger;

public class InputValidator {
    private static final int MAX_INPUT_LENGTH = 100;
    private static final String ALLOWED_CHARACTERS = "[a-zA-Z0-9 _\\-]+";
    
    public static boolean isValidInput(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        
        if (input.length() > MAX_INPUT_LENGTH) {
            ModLogger.warn("Input exceeds maximum length: " + input);
            return false;
        }
        
        if (!input.matches(ALLOWED_CHARACTERS)) {
            ModLogger.warn("Input contains invalid characters: " + input);
            return false;
        }
        
        return true;
    }
    
    public static String sanitizeInput(String input) {
        if (input == null) return "";
        // Remove any characters not in the allowed set
        return input.replaceAll("[^a-zA-Z0-9 _\\-]", "");
    }
}