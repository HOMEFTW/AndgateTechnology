package com.andgatech.AHTech.recipe.machineRecipe;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import com.andgatech.AHTech.AndgateTechnology;
import com.andgatech.AHTech.common.supplier.SupplierId;
import com.andgatech.AHTech.recipe.metadata.AHTechRecipeMetadata;
import com.andgatech.AHTech.recipe.recipeMap.AHTechRecipeMaps;

import gregtech.api.enums.GTValues;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.enums.TierEU;
import gregtech.api.interfaces.IRecipeMap;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTUtility;
import tectech.thing.CustomItemList;

/**
 * Hardcoded special recipes for the Electronics Market multiblock.
 *
 * <p>
 * Cable Disassembly: GT cables are broken down into rubber + wire.
 * Laser Vacuum Tube: Vanilla glass + Osmiridium foil -> advanced vacuum tube.
 */
public class ElectronicsMarketRecipePool {

    // The set of wire materials for which cables exist in standard GT.
    // Each entry maps to a GT cable registered in the ore dictionary.
    private static final Materials[] CABLE_MATERIALS = { Materials.Copper, Materials.AnnealedCopper, Materials.Gold,
        Materials.Aluminium, Materials.Tin, Materials.Silver, Materials.Nickel, Materials.Iron, Materials.Platinum,
        Materials.Osmium, Materials.Iridium, Materials.Electrum, Materials.Tungsten, Materials.TungstenSteel,
        Materials.HSSG, Materials.NiobiumTitanium, Materials.VanadiumGallium, Materials.YttriumBariumCuprate,
        Materials.Naquadah, Materials.NaquadahAlloy, Materials.Duranium, Materials.Tritanium, Materials.Superconductor,
        Materials.SuperconductorMV, Materials.SuperconductorHV, Materials.SuperconductorEV, Materials.SuperconductorIV,
        Materials.SuperconductorLuV, Materials.SuperconductorZPM, Materials.SuperconductorUV,
        Materials.SuperconductorUHV, Materials.SuperconductorUEV, Materials.SuperconductorUIV,
        Materials.SuperconductorUMV, Materials.Longasssuperconductornameforuvwire,
        Materials.Longasssuperconductornameforuhvwire, };

    // Cable size multipliers: each size has a different rubber yield.
    private static final OrePrefixes[] CABLE_PREFIXES = { OrePrefixes.cableGt01, OrePrefixes.cableGt02,
        OrePrefixes.cableGt04, OrePrefixes.cableGt08, OrePrefixes.cableGt12, OrePrefixes.cableGt16, };

    // Rubber output counts per cable size.
    // Matches GT's cableGtNN.mSecondaryMaterial definitions:
    // cableGt01 = 1 rubber plate, cableGt02 = 1, cableGt04 = 2,
    // cableGt08 = 3, cableGt12 = 4, cableGt16 = 5
    private static final int[] RUBBER_PER_CABLE = { 1, 1, 2, 3, 4, 5 };

    public static void loadRecipes() {
        final IRecipeMap EM = AHTechRecipeMaps.ElectronicsMarketRecipes;

        loadCableDisassemblyRecipes(EM);
        loadLaserVacuumTubeRecipe(EM);
    }

    // ========================================================================
    // Cable Disassembly: GT Cable -> Rubber + Wire
    // ========================================================================

    private static void loadCableDisassemblyRecipes(IRecipeMap EM) {
        int count = 0;

        for (int i = 0; i < CABLE_PREFIXES.length; i++) {
            OrePrefixes cablePrefix = CABLE_PREFIXES[i];
            OrePrefixes wirePrefix = getWirePrefixForCable(cablePrefix);
            int rubberAmount = RUBBER_PER_CABLE[i];

            for (Materials mat : CABLE_MATERIALS) {
                ItemStack cableStack = GTOreDictUnificator.get(cablePrefix, mat, 1);
                if (cableStack == null || cableStack.getItem() == null) continue;

                ItemStack wireStack = GTOreDictUnificator.get(wirePrefix, mat, 1);
                if (wireStack == null || wireStack.getItem() == null) continue;

                ItemStack rubberStack = GTOreDictUnificator.get(OrePrefixes.plate, Materials.Rubber, rubberAmount);
                if (rubberStack == null) {
                    // Fall back to dust if plate is unavailable
                    rubberStack = Materials.Rubber.getDust(rubberAmount);
                }

                GTValues.RA.stdBuilder()
                    .itemInputs(GTUtility.copyAmountUnsafe(1, cableStack))
                    .itemOutputs(wireStack, rubberStack)
                    .metadata(AHTechRecipeMetadata.SUPPLIER_ID, SupplierId.SHANDONG_DEZHOU.getId())
                    .eut(TierEU.RECIPE_LV)
                    .duration(100)
                    .addTo(EM);
                count++;
            }
        }

        AndgateTechnology.LOG.info("Registered {} cable disassembly recipes.", count);
    }

    /**
     * Maps a cable prefix to its corresponding wire prefix.
     * cableGt01 -> wireGt01, cableGt02 -> wireGt02, etc.
     */
    private static OrePrefixes getWirePrefixForCable(OrePrefixes cablePrefix) {
        if (cablePrefix == OrePrefixes.cableGt01) return OrePrefixes.wireGt01;
        if (cablePrefix == OrePrefixes.cableGt02) return OrePrefixes.wireGt02;
        if (cablePrefix == OrePrefixes.cableGt04) return OrePrefixes.wireGt04;
        if (cablePrefix == OrePrefixes.cableGt08) return OrePrefixes.wireGt08;
        if (cablePrefix == OrePrefixes.cableGt12) return OrePrefixes.wireGt12;
        if (cablePrefix == OrePrefixes.cableGt16) return OrePrefixes.wireGt16;
        return OrePrefixes.wireGt01;
    }

    // ========================================================================
    // Laser Vacuum Tube: Vanilla Glass + Osmiridium Foil -> Laser Vacuum Tube
    // Stage II+ only (specialValue = 2)
    // ========================================================================

    private static void loadLaserVacuumTubeRecipe(IRecipeMap EM) {
        // Laser Vacuum Pipe (TecTech, Meta ID 15465) — merged into GT5-Unofficial.
        // Registered as tectech.thing.CustomItemList.LASERpipe
        ItemStack glassInput = new ItemStack(Blocks.glass, 1);
        ItemStack osmiridiumFoil = GTOreDictUnificator.get(OrePrefixes.foil, Materials.Osmiridium, 1);
        ItemStack outputTube = CustomItemList.LASERpipe.get(1);

        if (osmiridiumFoil == null || outputTube == null) {
            AndgateTechnology.LOG
                .warn("Laser Vacuum Tube recipe skipped: missing Osmiridium foil or Laser Vacuum Pipe item.");
            return;
        }

        // specialValue(2) signals Stage II+ requirement
        GTValues.RA.stdBuilder()
            .itemInputs(glassInput, osmiridiumFoil)
            .itemOutputs(outputTube)
            .metadata(AHTechRecipeMetadata.SUPPLIER_ID, SupplierId.GAIGUANG.getId())
            .specialValue(2)
            .eut(TierEU.RECIPE_HV)
            .duration(200)
            .addTo(EM);

        AndgateTechnology.LOG.info("Registered Laser Vacuum Tube recipe (Stage II+).");
    }
}
