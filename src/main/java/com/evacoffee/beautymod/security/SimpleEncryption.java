package com.evacoffee.beautymod.security;

public class SimpleEncryption {
    private static final String KEY = "YourSecretKey123"; // In production, use a secure key management system
    
    public static String encrypt(String input) {
        // Simple XOR encryption for demonstration
        // In production, use proper encryption like AES
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            result.append((char) (input.charAt(i) ^ KEY.charAt(i % KEY.length())));
        }
        return result.toString();
    }
    
    public static String decrypt(String input) {
        // XOR decryption is the same as encryption
        return encrypt(input);
    }
}