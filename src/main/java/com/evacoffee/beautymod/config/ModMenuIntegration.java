package com.evacoffee.beautymod.config;

import com.evacoffee.beautymod.BeautyMod;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> AutoConfig.getConfigScreen(ModConfig.class, parent).get();
    }

    public static void registerModsPage() {
        if (FabricLoader.getInstance().isModLoaded("modmenu")) {
            BeautyMod.LOGGER.info("ModMenu detected, registering config screen");
        }
    }

    public static void registerClient() {
        registerModsPage();
    }
}