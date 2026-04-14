package com.andgatech.AHTech.common.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import net.minecraft.item.ItemStack;

import org.junit.jupiter.api.Test;

class ContractTierTest {

    @Test
    void fromTierFallsBackToNoneForUnknownValues() {
        assertEquals(ContractTier.NONE, ContractTier.fromTier(-1));
        assertEquals(ContractTier.NONE, ContractTier.fromTier(0));
        assertEquals(ContractTier.LV1, ContractTier.fromTier(1));
        assertEquals(ContractTier.LV2, ContractTier.fromTier(2));
        assertEquals(ContractTier.LV3, ContractTier.fromTier(3));
        assertEquals(ContractTier.LV4, ContractTier.fromTier(4));
        assertEquals(ContractTier.NONE, ContractTier.fromTier(999));
    }

    @Test
    void findHighestTierUsesStrongestContractInInventory() {
        ContractItem lv1 = new ContractItem(ContractTier.LV1);
        ContractItem lv3 = new ContractItem(ContractTier.LV3);

        ContractTier highestTier = ContractItem.findHighestTier(
            Arrays.asList(
                null,
                new ItemStack(lv1, 1),
                new ItemStack(lv3, 1),
                null));

        assertEquals(ContractTier.LV3, highestTier);
    }
}
