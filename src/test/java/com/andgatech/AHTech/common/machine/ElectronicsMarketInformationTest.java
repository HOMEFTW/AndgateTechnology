package com.andgatech.AHTech.common.machine;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.andgatech.AHTech.common.contract.ContractTier;
import com.andgatech.AHTech.common.modularizedMachine.FunctionType;
import com.andgatech.AHTech.common.supplier.SupplierId;

class ElectronicsMarketInformationTest {

    @Test
    void getInfoDataReturnsStablePanelFriendlyLines() {
        TestElectronicsMarket market = new TestElectronicsMarket();
        market.structureTier = ElectronicsMarket.TIER_II;
        market.parallel = 33;
        market.speedBonus = 2.5F;
        market.recoveryRate = 0.7F;
        market.contractTier = ContractTier.LV2;
        market.activeSuppliers = EnumSet.of(SupplierId.SHANDONG_DEZHOU, SupplierId.YADEPIAN);
        market.perfectOverclock = true;
        market.financialStatus = "copperx12 | steelx3";
        market.addFunctionType(FunctionType.GENERAL_DISASSEMBLY);

        assertArrayEquals(
            new String[] {
                "Electronics Market",
                "Status: Idle",
                "Stage: II | Contract: Lv2",
                "Suppliers: 2 | Parallel: 33",
                "Speed Bonus: 250%",
                "Recovery Rate: 70% | Perfect Overclock: ON",
                "Modules: General Disassembly",
                "Finance: copperx12 | steelx3" },
            market.getInfoData());
    }

    @Test
    void reportMetricsMatchesIndustrialInformationScreenNeeds() {
        TestElectronicsMarket market = new TestElectronicsMarket();
        market.structureTier = ElectronicsMarket.TIER_III;
        market.parallel = 257;
        market.speedBonus = 4.0F;
        market.recoveryRate = 0.9F;
        market.contractTier = ContractTier.NONE;
        market.perfectOverclock = false;
        market.financialStatus = "No Hatch";

        assertEquals(
            Arrays.asList(
                "Electronics Market",
                "Status: Idle",
                "Stage: III | Contract: None",
                "Suppliers: 0 | Parallel: 257",
                "Speed Bonus: 400%",
                "Recovery Rate: 90% | Perfect Overclock: OFF",
                "Modules: None",
                "Finance: No Hatch"),
            market.reportMetrics());
    }

    private static class TestElectronicsMarket extends ElectronicsMarket {

        private int structureTier = TIER_NONE;
        private int parallel = 1;
        private float speedBonus = 1.0F;
        private float recoveryRate = 0.0F;
        private ContractTier contractTier = ContractTier.NONE;
        private EnumSet<SupplierId> activeSuppliers = EnumSet.noneOf(SupplierId.class);
        private boolean perfectOverclock;
        private String financialStatus = "Empty";

        private TestElectronicsMarket() {
            super("test.electronics.market");
        }

        @Override
        public int getStructureTier() {
            return structureTier;
        }

        @Override
        public int getMaxParallelRecipes() {
            return parallel;
        }

        @Override
        public float getSpeedBonus() {
            return speedBonus;
        }

        @Override
        public float getRecoveryRate() {
            return recoveryRate;
        }

        @Override
        protected ContractTier getContractTierForUi() {
            return contractTier;
        }

        @Override
        protected EnumSet<SupplierId> getActiveSuppliersForUi() {
            return activeSuppliers.clone();
        }

        @Override
        public boolean isEnablePerfectOverclock() {
            return perfectOverclock;
        }

        @Override
        protected String getFinancialStatusLine() {
            return financialStatus;
        }
    }
}
