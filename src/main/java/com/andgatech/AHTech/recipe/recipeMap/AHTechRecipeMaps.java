package com.andgatech.AHTech.recipe.recipeMap;

import gregtech.api.enums.GTUITextures;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMapBuilder;
import gregtech.api.recipe.backend.RecipeMapBackend;

public class AHTechRecipeMaps {

    public static final RecipeMap<RecipeMapBackend> ElectronicsMarketRecipes = RecipeMapBuilder
        .of("ahtech.recipe.ElectronicsMarketRecipes")
        .maxIO(9, 9, 4, 4)
        .progressBar(GTUITextures.PROGRESSBAR_ARROW_MULTIPLE)
        .neiHandlerInfo(
            builder -> builder.setMaxRecipesPerPage(4))
        .build();
}
