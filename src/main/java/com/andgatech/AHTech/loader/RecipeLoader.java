package com.andgatech.AHTech.loader;

import com.andgatech.AHTech.config.Config;
import com.andgatech.AHTech.recipe.commonRecipe.ShapedCraftRecipePool;
import com.andgatech.AHTech.recipe.machineRecipe.CurrencyRecipePool;
import com.andgatech.AHTech.recipe.machineRecipe.ElectronicsMarketRecipePool;
import com.andgatech.AHTech.recipe.machineRecipe.RecyclingRecipeGenerator;

/**
 * Central recipe loading orchestrator.
 * Recipe loading MUST happen in completeInit or later, when all registries are populated.
 */
public class RecipeLoader {

    public static void loadRecipes() {
        // Load all recipe pools here in the correct order.
        ShapedCraftRecipePool.loadRecipes();

        if (Config.Enable_ElectronicsMarket) {
            ElectronicsMarketRecipePool.loadRecipes();
        }
        if (Config.EnableFinancialSystem) {
            CurrencyRecipePool.loadRecipes();
        }
    }

    public static void loadRecipesPostInit() {
        // Recipes that need post-init data.
    }

    public static void loadRecipesServerStarted() {
        // Recipes that need server-started state.
        if (Config.Enable_ElectronicsMarket) {
            RecyclingRecipeGenerator.generateRecyclingRecipes();
        }
    }
}
