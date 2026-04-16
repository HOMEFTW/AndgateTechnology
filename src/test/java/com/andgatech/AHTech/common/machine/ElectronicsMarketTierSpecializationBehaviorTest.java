package com.andgatech.AHTech.common.machine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import com.andgatech.AHTech.common.modularizedMachine.modularHatches.RecoveryRateModule;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.executionCore.ExecutionCore;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.parallelController.StaticParallelController;
import com.andgatech.AHTech.config.Config;

import gregtech.api.enums.TierEU;
import gregtech.api.recipe.metadata.EmptyRecipeMetadataStorage;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.recipe.check.SimpleCheckRecipeResult;
import gregtech.api.util.GTRecipe;

class ElectronicsMarketTierSpecializationBehaviorTest {

    @Test
    void tierTwoRejectsRecipesAboveConfiguredVoltageLimit() throws Exception {
        int originalVoltageTier = Config.Stage2_MaxVoltageTier;
        try {
            Config.Stage2_MaxVoltageTier = 8;
            TestElectronicsMarket market = new TestElectronicsMarket(ElectronicsMarket.TIER_II);

            GTRecipe recipe = newRecipe(Math.toIntExact(TierEU.UHV), 2);

            assertEquals(
                SimpleCheckRecipeResult.ofFailure("voltage_exceeded"),
                market.validateRecipeAccessForTest(recipe));
        } finally {
            Config.Stage2_MaxVoltageTier = originalVoltageTier;
        }
    }

    @Test
    void tierThreeAcceptsRecipesAboveStageTwoVoltageLimit() throws Exception {
        int originalVoltageTier = Config.Stage2_MaxVoltageTier;
        try {
            Config.Stage2_MaxVoltageTier = 8;
            TestElectronicsMarket market = new TestElectronicsMarket(ElectronicsMarket.TIER_III);

            GTRecipe recipe = newRecipe(Math.toIntExact(TierEU.UHV), 2);

            assertEquals(CheckRecipeResultRegistry.SUCCESSFUL, market.validateRecipeAccessForTest(recipe));
        } finally {
            Config.Stage2_MaxVoltageTier = originalVoltageTier;
        }
    }

    @Test
    void tierTwoAllowsHighTierStandardParallelController() {
        TestElectronicsMarket market = new TestElectronicsMarket(ElectronicsMarket.TIER_II);
        StaticParallelController module =
            new StaticParallelController("test.parallel.t8", 8, 8, null);

        assertTrue(module.isCompatibleWithMachine(market));
    }

    @Test
    void tierTwoAllowsHighTierExecutionCore() {
        TestElectronicsMarket market = new TestElectronicsMarket(ElectronicsMarket.TIER_II);
        ExecutionCore module = new ExecutionCore("test.execution.core.t8", 8, null, null);

        assertTrue(module.isCompatibleWithMachine(market));
    }

    @Test
    void tierTwoStillRejectsTierThreeRecoveryModule() {
        TestElectronicsMarket market = new TestElectronicsMarket(ElectronicsMarket.TIER_II);
        RecoveryRateModule module = new RecoveryRateModule("test.recovery.t3", 3, 0.90F, null);

        assertFalse(module.isCompatibleWithMachine(market));
    }

    @Test
    void tierTwoStillAcceptsTierTwoRecoveryModule() {
        TestElectronicsMarket market = new TestElectronicsMarket(ElectronicsMarket.TIER_II);
        RecoveryRateModule module = new RecoveryRateModule("test.recovery.t2", 2, 0.70F, null);

        assertTrue(module.isCompatibleWithMachine(market));
    }

    @Test
    void tierOneKeepsOriginalStandardModuleTierGate() {
        TestElectronicsMarket market = new TestElectronicsMarket(ElectronicsMarket.TIER_I);
        StaticParallelController module =
            new StaticParallelController("test.parallel.t8.stage1", 8, 8, null);

        assertFalse(module.isCompatibleWithMachine(market));
    }

    private static GTRecipe newRecipe(int eut, int specialValue) throws Exception {
        GTRecipe recipe = (GTRecipe) getUnsafe().allocateInstance(GTRecipe.class);
        recipe.mEUt = eut;
        recipe.mSpecialValue = specialValue;

        Field metadataStorageField = GTRecipe.class.getDeclaredField("metadataStorage");
        metadataStorageField.setAccessible(true);
        metadataStorageField.set(recipe, EmptyRecipeMetadataStorage.INSTANCE);
        return recipe;
    }

    private static sun.misc.Unsafe getUnsafe() throws Exception {
        Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (sun.misc.Unsafe) field.get(null);
    }

    private static final class TestElectronicsMarket extends ElectronicsMarket {

        private final int structureTier;

        private TestElectronicsMarket(int structureTier) {
            super("test.electronics.market.tier.specialization");
            this.structureTier = structureTier;
        }

        @Override
        public int getStructureTier() {
            return structureTier;
        }

        private Object validateRecipeAccessForTest(GTRecipe recipe) {
            return validateRecipeAccess(recipe);
        }
    }
}
