package com.andgatech.AHTech.loader;

/**
 * Central recipe loading orchestrator.
 * Recipe loading MUST happen in completeInit or later, when all registries are populated.
 */
public class RecipeLoader {

    public static void loadRecipes() {
        // Load all recipe pools here in the correct order.
        //
        // Example:
        //   YourItemRecipes.loadRecipes();
        //   YourMachineRecipes.loadRecipes();
    }

    public static void loadRecipesPostInit() {
        // Recipes that need post-init data.
    }

    public static void loadRecipesServerStarted() {
        // Recipes that need server-started state.
    }
}
