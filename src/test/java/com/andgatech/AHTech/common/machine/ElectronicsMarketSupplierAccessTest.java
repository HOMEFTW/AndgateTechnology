package com.andgatech.AHTech.common.machine;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;

import org.junit.jupiter.api.Test;

import com.andgatech.AHTech.common.contract.ContractTier;
import com.andgatech.AHTech.common.supplier.SupplierId;

class ElectronicsMarketSupplierAccessTest {

    @Test
    void untaggedRecipeStillPassesSupplierGate() {
        TestElectronicsMarket market = new TestElectronicsMarket();
        market.structureTier = ElectronicsMarket.TIER_II;

        assertTrue(market.isSupplierRecipeAccessibleForTest(null));
    }

    @Test
    void taggedRecipeNeedsMatchingSupplierToBeActive() {
        TestElectronicsMarket market = new TestElectronicsMarket();
        market.structureTier = ElectronicsMarket.TIER_II;
        market.contractTier = ContractTier.LV1;

        assertFalse(market.isSupplierRecipeAccessibleForTest(SupplierId.SHANDONG_DEZHOU.getId()));

        market.activeSuppliers = EnumSet.of(SupplierId.SHANDONG_DEZHOU);

        assertTrue(market.isSupplierRecipeAccessibleForTest(SupplierId.SHANDONG_DEZHOU.getId()));
    }

    private static class TestElectronicsMarket extends ElectronicsMarket {

        private int structureTier = TIER_NONE;
        private ContractTier contractTier = ContractTier.NONE;
        private EnumSet<SupplierId> activeSuppliers = EnumSet.noneOf(SupplierId.class);

        private TestElectronicsMarket() {
            super("test.electronics.market.supplier");
        }

        @Override
        public int getStructureTier() {
            return structureTier;
        }

        @Override
        protected ContractTier getContractTierForUi() {
            return contractTier;
        }

        @Override
        protected EnumSet<SupplierId> getActiveSuppliersForUi() {
            return activeSuppliers.clone();
        }

        private boolean isSupplierRecipeAccessibleForTest(String supplierId) {
            return isSupplierRecipeAccessible(supplierId);
        }
    }
}
