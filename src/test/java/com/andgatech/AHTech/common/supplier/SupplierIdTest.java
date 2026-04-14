package com.andgatech.AHTech.common.supplier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.andgatech.AHTech.common.contract.ContractTier;

import org.junit.jupiter.api.Test;

class SupplierIdTest {

    @Test
    void contractTierMustMeetSupplierMinimum() {
        assertTrue(SupplierId.SHANDONG_DEZHOU.isUnlockedBy(ContractTier.LV1));
        assertTrue(SupplierId.SHANDONG_DEZHOU.isUnlockedBy(ContractTier.LV4));
        assertFalse(SupplierId.CHAOLA.isUnlockedBy(ContractTier.LV1));
        assertTrue(SupplierId.CHAOLA.isUnlockedBy(ContractTier.LV2));
        assertFalse(SupplierId.DITONG.isUnlockedBy(ContractTier.LV2));
        assertTrue(SupplierId.DITONG.isUnlockedBy(ContractTier.LV3));
    }
}
