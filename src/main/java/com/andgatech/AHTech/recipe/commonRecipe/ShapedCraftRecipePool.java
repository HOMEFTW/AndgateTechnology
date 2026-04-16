package com.andgatech.AHTech.recipe.commonRecipe;

import com.andgatech.AHTech.common.ModItemList;

import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.util.GTModHandler;
import gregtech.api.util.GTOreDictUnificator;

/**
 * Example recipe pool for shaped crafting recipes.
 * Add your crafting table recipes here.
 */
public class ShapedCraftRecipePool {

    public static void loadContractRecipes() {
        long recipeBits = GTModHandler.RecipeBits.DISMANTLEABLE | GTModHandler.RecipeBits.REVERSIBLE;

        GTModHandler.addCraftingRecipe(
            ModItemList.ContractLv1.get(1),
            recipeBits,
            new Object[] { "FGF", " C ", "PPP", 'F', ItemList.Field_Generator_LV.get(1), 'G',
                GTOreDictUnificator.get(OrePrefixes.gearGtSmall, Materials.Tin, 1), 'C', ItemList.Circuit_Basic.get(1),
                'P', GTOreDictUnificator.get(OrePrefixes.plate, Materials.Copper, 1) });

        GTModHandler.addCraftingRecipe(
            ModItemList.ContractLv2.get(1),
            recipeBits,
            new Object[] { "FGF", " C ", "PPP", 'F', ItemList.Field_Generator_IV.get(1), 'G',
                ModItemList.ContractLv1.get(1), 'C', ItemList.Circuit_Advanced.get(1), 'P',
                GTOreDictUnificator.get(OrePrefixes.plate, Materials.RedAlloy, 1) });

        GTModHandler.addCraftingRecipe(
            ModItemList.ContractLv3.get(1),
            recipeBits,
            new Object[] { "FGF", " C ", "PPP", 'F', ItemList.Field_Generator_ZPM.get(1), 'G',
                ModItemList.ContractLv2.get(1), 'C', ItemList.Circuit_Elite.get(1), 'P',
                GTOreDictUnificator.get(OrePrefixes.plate, Materials.Titanium, 1) });

        GTModHandler.addCraftingRecipe(
            ModItemList.ContractLv4.get(1),
            recipeBits,
            new Object[] { "FGF", " C ", "PPP", 'F', ItemList.Field_Generator_UHV.get(1), 'G',
                ModItemList.ContractLv3.get(1), 'C', ItemList.Circuit_Master.get(1), 'P',
                GTOreDictUnificator.get(OrePrefixes.plate, Materials.Osmium, 1) });
    }

    public static void loadSupplierRecipes() {
        long recipeBits = GTModHandler.RecipeBits.DISMANTLEABLE | GTModHandler.RecipeBits.REVERSIBLE;

        GTModHandler.addCraftingRecipe(
            ModItemList.SupplierShandongDezhou.get(1),
            recipeBits,
            new Object[] { "CBC", "DHD", "CBC", 'C', ModItemList.ContractLv1.get(1), 'B', ItemList.Circuit_Basic.get(1),
                'D', ItemList.Hatch_DataAccess_EV.get(1), 'H',
                GTOreDictUnificator.get(OrePrefixes.plate, Materials.Copper, 1) });

        GTModHandler.addCraftingRecipe(
            ModItemList.SupplierYadepian.get(1),
            recipeBits,
            new Object[] { "CBC", "DHD", "CBC", 'C', ModItemList.ContractLv1.get(1), 'B', ItemList.Circuit_Basic.get(1),
                'D', ItemList.Hatch_DataAccess_EV.get(1), 'H',
                GTOreDictUnificator.get(OrePrefixes.plate, Materials.Silver, 1) });

        GTModHandler.addCraftingRecipe(
            ModItemList.SupplierErfa.get(1),
            recipeBits,
            new Object[] { "CBC", "DHD", "CBC", 'C', ModItemList.ContractLv1.get(1), 'B', ItemList.Circuit_Basic.get(1),
                'D', ItemList.Hatch_DataAccess_EV.get(1), 'H',
                GTOreDictUnificator.get(OrePrefixes.plate, Materials.Steel, 1) });

        GTModHandler.addCraftingRecipe(
            ModItemList.SupplierChaola.get(1),
            recipeBits,
            new Object[] { "CBC", "DHD", "CBC", 'C', ModItemList.ContractLv2.get(1), 'B',
                ItemList.Circuit_Advanced.get(1), 'D', ItemList.Hatch_DataAccess_LuV.get(1), 'H',
                GTOreDictUnificator.get(OrePrefixes.plate, Materials.Titanium, 1) });

        GTModHandler.addCraftingRecipe(
            ModItemList.SupplierHuangweida.get(1),
            recipeBits,
            new Object[] { "CBC", "DHD", "CBC", 'C', ModItemList.ContractLv2.get(1), 'B',
                ItemList.Circuit_Advanced.get(1), 'D', ItemList.Hatch_DataAccess_LuV.get(1), 'H',
                GTOreDictUnificator.get(OrePrefixes.plate, Materials.Aluminium, 1) });

        GTModHandler.addCraftingRecipe(
            ModItemList.SupplierGaiguang.get(1),
            recipeBits,
            new Object[] { "CBC", "DHD", "CBC", 'C', ModItemList.ContractLv2.get(1), 'B',
                ItemList.Circuit_Advanced.get(1), 'D', ItemList.Hatch_DataAccess_LuV.get(1), 'H',
                GTOreDictUnificator.get(OrePrefixes.plate, Materials.Glass, 1) });

        GTModHandler.addCraftingRecipe(
            ModItemList.SupplierDitong.get(1),
            recipeBits,
            new Object[] { "CBC", "DHD", "CBC", 'C', ModItemList.ContractLv3.get(1), 'B', ItemList.Circuit_Elite.get(1),
                'D', ItemList.Hatch_DataAccess_UV.get(1), 'H',
                GTOreDictUnificator.get(OrePrefixes.plate, Materials.TungstenSteel, 1) });
    }
}
