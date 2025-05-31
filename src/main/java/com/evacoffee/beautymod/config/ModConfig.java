package com.evacoffee.beautymod.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "beautymod")
public class ModConfig implements ConfigData {
    @ConfigEntry.Category("debug")
    @ConfigEntry.Gui.Tooltip
    public boolean enableDebugLogging = false;
    
    @ConfigEntry.Category("security")
    @ConfigEntry.Gui.Tooltip
    public boolean enablePacketValidation = true;
    
    @ConfigEntry.Category("security")
    @ConfigEntry.Gui.Tooltip
    public int maxPacketSize = 32767; // Default Minecraft packet size limit
}