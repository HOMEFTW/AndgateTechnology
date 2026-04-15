package com.andgatech.AHTech.common.modularizedMachine.modularHatches;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.item.ItemStack;

import org.junit.jupiter.api.Test;

import com.andgatech.AHTech.common.currency.CurrencyItem;
import com.andgatech.AHTech.common.currency.CurrencyType;

class FinancialHatchTest {

    @Test
    void countCurrencySumsAcrossAllStacksOfSameType() {
        FinancialHatch hatch = new FinancialHatch("FinancialHatch", 0, null);
        CurrencyItem currencyItem = new CurrencyItem();

        hatch.setInventorySlotContents(0, new ItemStack(currencyItem, 10, CurrencyType.COPPER.getMeta()));
        hatch.setInventorySlotContents(1, new ItemStack(currencyItem, 24, CurrencyType.COPPER.getMeta()));
        hatch.setInventorySlotContents(2, new ItemStack(currencyItem, 7, CurrencyType.STEEL.getMeta()));

        assertEquals(34, hatch.countCurrency(CurrencyType.COPPER));
        assertEquals(7, hatch.countCurrency(CurrencyType.STEEL));
        assertEquals(0, hatch.countCurrency(CurrencyType.PLATINUM));
    }

    @Test
    void consumeCurrencyRemovesFromMultipleStacksInOrder() {
        FinancialHatch hatch = new FinancialHatch("FinancialHatch", 0, null);
        CurrencyItem currencyItem = new CurrencyItem();

        hatch.setInventorySlotContents(0, new ItemStack(currencyItem, 12, CurrencyType.COPPER.getMeta()));
        hatch.setInventorySlotContents(1, new ItemStack(currencyItem, 15, CurrencyType.COPPER.getMeta()));
        hatch.setInventorySlotContents(2, new ItemStack(currencyItem, 9, CurrencyType.STEEL.getMeta()));

        assertEquals(20, hatch.consumeCurrency(CurrencyType.COPPER, 20));
        assertEquals(7, hatch.countCurrency(CurrencyType.COPPER));
        assertEquals(9, hatch.countCurrency(CurrencyType.STEEL));
    }

    @Test
    void consumeCurrencyStopsAtAvailableAmount() {
        FinancialHatch hatch = new FinancialHatch("FinancialHatch", 0, null);
        CurrencyItem currencyItem = new CurrencyItem();

        hatch.setInventorySlotContents(0, new ItemStack(currencyItem, 6, CurrencyType.INFINITY.getMeta()));

        assertEquals(6, hatch.consumeCurrency(CurrencyType.INFINITY, 9));
        assertEquals(0, hatch.countCurrency(CurrencyType.INFINITY));
    }
}
