package com.andgatech.AHTech.recipe.commonRecipe;

import net.minecraft.item.ItemStack;

import gregtech.api.enums.GT_Values;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.util.GT_ModHandler;
import gregtech.api.util.GT_Utility;

/**
 * Example recipe pool for shaped crafting recipes.
 * Add your crafting table recipes here.
 */
public class ShapedCraftRecipePool {

    public static void loadRecipes() {
        // Example: shaped crafting recipe
        // GT_ModHandler.addCraftingRecipe(
        //     ModItemList.MyItem.get(1),
        //     new Object[] { "ABA", " C ", "   ", 'A', Items.iron_ingot, 'B', Items.gold_ingot, 'C', Items.diamond });

        // Example: GregTech assembler recipe
        // GT_Values.RA.stdBuilder()
        //     .itemInputs(new ItemStack(Items.iron_ingot, 1), new ItemStack(Items.gold_ingot, 1))
        //     .itemOutputs(ModItemList.MyItem.get(1))
        //     .duration(100).eut(30)
        //     .addTo(gregtech.api.recipe.RecipeMaps.assemblerRecipes);
    }
}
