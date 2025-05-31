package com.evacoffee.beautymod.util;

import com.evacoffee.beautymod.BeautyMod;
import com.evacoffee.beautymod.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger(BeautyMod.MOD_ID);
    private static ModConfig config;
    
    public static void setConfig(ModConfig config) {
        ModLogger.config = config;
    }
    
    public static void debug(String message, Object... args) {
        if (config != null && config.enableDebugLogging) {
            LOGGER.info("[DEBUG] " + message, args);
        }
    }
    
    public static void info(String message, Object... args) {
        LOGGER.info(message, args);
    }
    
    public static void warn(String message, Object... args) {
        LOGGER.warn(message, args);
    }
    
    public static void error(String message, Throwable t) {
        LOGGER.error(message, t);
    }
    
    public static void error(String message, Object... args) {
        LOGGER.error(message, args);
    }
}