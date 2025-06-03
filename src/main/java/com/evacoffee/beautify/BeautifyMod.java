package com.evacoffee.beautify;

import com.evacoffee.beautify.network.CustomizationNetworkHandler;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeautifyMod implements ModInitializer {
    public static final String MOD_ID = "beautify";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("BeautifyMod initializing...");

        //Server-side network recivers
        CustomizationNetworkHandler.registerServerReceivers();

        // Cardinal Components are typically initialized via their own entrypoint
        // in fabric.mod.json: "cardinal-components-entity": ["com.evacoffee.beautify.customization.component.CustomizationComponents"]

        LOGGER.info("BeautifyMod initialized.");
    }
}