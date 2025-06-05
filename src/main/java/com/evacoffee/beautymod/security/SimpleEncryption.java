package com.evacoffee.beautymod.security;

public class SimpleEncryption {
    private static final String KEY = "BigbackfoodiE27";
    
    public static String encrypt(String input) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            result.append((char) (input.charAt(i) ^ KEY.charAt(i % KEY.length())));
        }
        return result.toString();
    }
    
    public static String decrypt(String input) {
        return encrypt(input);
    }
}