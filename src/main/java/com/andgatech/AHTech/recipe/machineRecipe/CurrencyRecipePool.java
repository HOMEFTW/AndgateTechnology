package com.andgatech.AHTech.recipe.machineRecipe;

import static gregtech.api.recipe.RecipeMaps.formingPressRecipes;

import com.andgatech.AHTech.AndgateTechnology;
import com.andgatech.AHTech.common.currency.CurrencyType;

import gregtech.api.enums.GTValues;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.TierEU;

public final class CurrencyRecipePool {

    private static final long[] RECIPE_EUT = { TierEU.RECIPE_LV, TierEU.RECIPE_MV, TierEU.RECIPE_EV, TierEU.RECIPE_IV,
        TierEU.RECIPE_UV, TierEU.RECIPE_UHV };

    private CurrencyRecipePool() {}

    public static void loadRecipes() {
        CurrencyType[] types = CurrencyType.values();
        for (int i = 0; i < types.length; i++) {
            CurrencyType type = types[i];
            if (type.getIngot() == null || type.getCoinStack(1) == null) {
                AndgateTechnology.LOG
                    .warn("skip currency recipe registration for {} due to missing item mapping", type);
                continue;
            }

            GTValues.RA.stdBuilder()
                .itemInputs(type.getIngot(), ItemList.Shape_Mold_Credit.get(0L))
                .itemOutputs(type.getCoinStack(1))
                .eut(RECIPE_EUT[Math.min(i, RECIPE_EUT.length - 1)])
                .duration(40)
                .addTo(formingPressRecipes);
        }
    }
}
