package com.andgatech.AHTech.recipe.metadata;

import com.andgatech.AHTech.common.currency.CurrencyType;

import gregtech.api.recipe.RecipeMetadataKey;
import gregtech.api.recipe.metadata.SimpleRecipeMetadataKey;

public final class AHTechRecipeMetadata {

    public static final RecipeMetadataKey<String> SUPPLIER_ID = SimpleRecipeMetadataKey
        .create(String.class, "ahtech_supplier_id");
    public static final RecipeMetadataKey<CurrencyType> CURRENCY_TYPE = SimpleRecipeMetadataKey
        .create(CurrencyType.class, "ahtech_currency_type");
    public static final RecipeMetadataKey<Integer> CURRENCY_COST = SimpleRecipeMetadataKey
        .create(Integer.class, "ahtech_currency_cost");

    private AHTechRecipeMetadata() {}
}
