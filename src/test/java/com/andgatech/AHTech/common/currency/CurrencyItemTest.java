package com.andgatech.AHTech.common.currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.junit.jupiter.api.Test;

class CurrencyItemTest {

    @Test
    void currencyHelpersRecognizeCurrencyStacks() {
        CurrencyItem currencyItem = new CurrencyItem();
        ItemStack copper = new ItemStack(currencyItem, 8, CurrencyType.COPPER.getMeta());
        ItemStack steel = new ItemStack(currencyItem, 4, CurrencyType.STEEL.getMeta());
        ItemStack normalItem = new ItemStack(new Item(), 1);

        assertTrue(CurrencyItem.isCurrency(copper));
        assertTrue(CurrencyItem.isCurrency(steel));
        assertFalse(CurrencyItem.isCurrency(normalItem));
        assertFalse(CurrencyItem.isCurrency(null));
        assertEquals(CurrencyType.COPPER, CurrencyItem.getCurrencyType(copper));
        assertEquals(CurrencyType.STEEL, CurrencyItem.getCurrencyType(steel));
        assertNull(CurrencyItem.getCurrencyType(normalItem));
        assertNull(CurrencyItem.getCurrencyType(null));
    }
}
