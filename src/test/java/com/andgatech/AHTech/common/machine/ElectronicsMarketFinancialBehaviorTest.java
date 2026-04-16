package com.andgatech.AHTech.common.machine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.jupiter.api.Test;

import com.andgatech.AHTech.common.modularizedMachine.modularHatches.FinancialHatch;

import gregtech.api.metatileentity.implementations.MTEHatchInputBus;

class ElectronicsMarketFinancialBehaviorTest {

    @Test
    void currencyCostScalesWithActualParallelCount() {
        assertEquals(20, ElectronicsMarket.scaleCurrencyCostForParallels(5, 4));
    }

    @Test
    void availableCurrencyCapsRecipeParallelCount() {
        assertEquals(1, ElectronicsMarket.limitParallelByAvailableCurrency(4, 7, 5));
    }

    @Test
    void singleItemRecoveryCanDropToZeroWhenChanceMisses() {
        assertEquals(0, ElectronicsMarket.calculateRecoveredStackSize(1, 0.30F, 9999));
    }

    @Test
    void singleItemRecoveryCanRemainOneWhenChanceHits() {
        assertEquals(1, ElectronicsMarket.calculateRecoveredStackSize(1, 0.30F, 0));
    }

    @Test
    void exactStoredPowerStillCountsAsSufficientForModuleMaintenance() {
        assertTrue(ElectronicsMarket.hasEnoughStoredPower(1024L, 1024L));
    }

    @Test
    void prepareFinancialStateForRecipeCheckTriggersAutoRefill() throws Exception {
        TestElectronicsMarket market = new TestElectronicsMarket();
        RecordingFinancialHatch hatch = new RecordingFinancialHatch();
        setFinancialHatches(market, hatch);

        market.prepareFinancialStateForRecipeCheck();

        assertTrue(hatch.autoRefillCalled);
    }

    private static void setFinancialHatches(ElectronicsMarket market, FinancialHatch hatch) throws Exception {
        Field field = ElectronicsMarket.class.getDeclaredField("financialHatches");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Collection<FinancialHatch> financialHatches = (Collection<FinancialHatch>) field.get(market);
        financialHatches.clear();
        financialHatches.add(hatch);
    }

    private static final class TestElectronicsMarket extends ElectronicsMarket {

        private TestElectronicsMarket() {
            super("test.electronics.market.finance");
        }
    }

    private static final class RecordingFinancialHatch extends FinancialHatch {

        private boolean autoRefillCalled;

        private RecordingFinancialHatch() {
            super("FinancialHatch", 0, null);
        }

        @Override
        public void autoRefillFromInputBus(Collection<MTEHatchInputBus> inputBuses) {
            autoRefillCalled = true;
        }
    }
}
