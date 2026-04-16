package com.andgatech.AHTech.loader;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.andgatech.AHTech.CommonProxy;
import com.andgatech.AHTech.common.modularizedMachine.ModularizedMachineBase;
import com.andgatech.AHTech.config.Config;

class LoaderConfigBehaviorTest {

    @Test
    void supplierHatchesStillRegisterWhenMarketEnabledButModularSystemDisabled() {
        boolean originalMarket = Config.Enable_ElectronicsMarket;
        boolean originalModular = Config.EnableModularizedMachineSystem;
        try {
            Config.Enable_ElectronicsMarket = true;
            Config.EnableModularizedMachineSystem = false;

            assertTrue(MachineLoader.shouldRegisterSupplierHatches());
        } finally {
            Config.Enable_ElectronicsMarket = originalMarket;
            Config.EnableModularizedMachineSystem = originalModular;
        }
    }

    @Test
    void financialInfrastructureRequiresMarketAndFinancialSystem() {
        boolean originalMarket = Config.Enable_ElectronicsMarket;
        boolean originalFinancial = Config.EnableFinancialSystem;
        try {
            Config.Enable_ElectronicsMarket = true;
            Config.EnableFinancialSystem = true;
            assertTrue(MachineLoader.shouldRegisterFinancialHatch());
            assertTrue(CommonProxy.shouldLoadCurrencies());
            assertTrue(RecipeLoader.shouldLoadCurrencyRecipes());

            Config.Enable_ElectronicsMarket = false;
            assertFalse(MachineLoader.shouldRegisterFinancialHatch());
            assertFalse(CommonProxy.shouldLoadCurrencies());
            assertFalse(RecipeLoader.shouldLoadCurrencyRecipes());
        } finally {
            Config.Enable_ElectronicsMarket = originalMarket;
            Config.EnableFinancialSystem = originalFinancial;
        }
    }

    @Test
    void supplierRecipesOnlyLoadWhenElectronicsMarketEnabled() {
        boolean originalMarket = Config.Enable_ElectronicsMarket;
        try {
            Config.Enable_ElectronicsMarket = true;
            assertTrue(RecipeLoader.shouldLoadSupplierRecipes());

            Config.Enable_ElectronicsMarket = false;
            assertFalse(RecipeLoader.shouldLoadSupplierRecipes());
        } finally {
            Config.Enable_ElectronicsMarket = originalMarket;
        }
    }

    @Test
    void tstDetectionUsesLoaderReportedModState() {
        assertTrue(MachineLoader.isTSTLoaded(modId -> "TwistSpaceTechnology".equals(modId)));
        assertFalse(MachineLoader.isTSTLoaded(modId -> false));
    }

    @Test
    void modularizedMachineBaseTstDetectionUsesLoaderReportedModState() {
        assertTrue(ModularizedMachineBase.isTSTLoaded(modId -> "TwistSpaceTechnology".equals(modId)));
        assertFalse(ModularizedMachineBase.isTSTLoaded(modId -> false));
    }
}
