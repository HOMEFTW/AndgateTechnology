package com.andgatech.AHTech.recipe.recipeMap;

import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMapBackend;
import gregtech.api.recipe.RecipeMapBuilder;

public class AHTechRecipeMaps {

    public static final RecipeMap<RecipeMapBackend> ElectronicsMarketRecipes = RecipeMapBuilder
        .of("ahtech.recipe.ElectronicsMarketRecipes")
        .maxIO(9, 9, 4, 4)
        .progressBar(GTUITextures.PROGRESSBAR_ARROW_MULTIPLE)
        .neiHandlerInfo(builder -> builder.setMaxRecipesPerPage(4))
        .build();
}
