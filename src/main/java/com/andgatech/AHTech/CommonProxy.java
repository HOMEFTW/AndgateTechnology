package com.andgatech.AHTech;

import com.andgatech.AHTech.config.Config;
import com.andgatech.AHTech.loader.ContractLoader;
import com.andgatech.AHTech.loader.MachineLoader;
import com.andgatech.AHTech.loader.MaterialLoader;
import com.andgatech.AHTech.loader.RecipeLoader;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());
        AndgateTechnology.LOG.info(Tags.MODNAME + " at version " + Tags.VERSION);

        MaterialLoader.load();
        ContractLoader.loadContracts();
    }

    public void init(FMLInitializationEvent event) {
        MachineLoader.loadMachines();
    }

    public void postInit(FMLPostInitializationEvent event) {
        // Register networking, cross-mod integration, etc.
    }

    public void complete(FMLLoadCompleteEvent event) {
        RecipeLoader.loadRecipes();
    }

    public void serverStarting(FMLServerStartingEvent event) {
        AndgateTechnology.LOG.info("Ok, " + Tags.MODNAME + " at version " + Tags.VERSION + " load success.");
    }

    public void serverStarted(FMLServerStartedEvent event) {
        RecipeLoader.loadRecipesServerStarted();
    }

}
