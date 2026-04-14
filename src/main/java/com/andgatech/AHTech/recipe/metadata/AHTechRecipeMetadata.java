package com.andgatech.AHTech.recipe.metadata;

import gregtech.api.recipe.RecipeMetadataKey;
import gregtech.api.recipe.metadata.SimpleRecipeMetadataKey;

public final class AHTechRecipeMetadata {

    public static final RecipeMetadataKey<String> SUPPLIER_ID = SimpleRecipeMetadataKey
        .create(String.class, "ahtech_supplier_id");

    private AHTechRecipeMetadata() {}
}
