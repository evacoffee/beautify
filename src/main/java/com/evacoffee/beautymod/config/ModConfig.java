package com.evacoffee.beautymod.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "beautymod")
public class ModConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    public boolean enableRelationshipHud = true;
    
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
    public int defaultRelationshipLevel = 20;
    
    @ConfigEntry.Gui.Tooltip
    public boolean enableSeasonalEvents = true;
    
    @ConfigEntry.Gui.Tooltip
    public boolean enableModIntegration = true;
    
    @ConfigEntry.Gui.Tooltip
    public boolean enableJealousyMechanics = true;
    
    @ConfigEntry.Gui.Tooltip
    public int maxRelationshipLevel = 100;
    
    @ConfigEntry.Gui.Tooltip
    public boolean showHeartParticles = true;
    
    @ConfigEntry.Gui.Tooltip
    public boolean enableDateNight = true;
    
    @ConfigEntry.Gui.Tooltip
    public boolean enableMarriage = true;
    
    @ConfigEntry.Gui.Tooltip
    public boolean enableBreakups = true;
    
    @ConfigEntry.Gui.Tooltip
    public boolean debugMode = false;
}