package com.andgatech.AHTech.loader;

import net.minecraft.item.ItemStack;

import com.andgatech.AHTech.common.ModItemList;
import com.andgatech.AHTech.common.currency.CurrencyItem;
import com.andgatech.AHTech.common.currency.CurrencyType;

import cpw.mods.fml.common.registry.GameRegistry;

public final class CurrencyLoader {

    private CurrencyLoader() {}

    public static void loadCurrencies() {
        CurrencyItem currencyItem = new CurrencyItem();
        GameRegistry.registerItem(currencyItem, "currency");

        registerCurrencyEntry(ModItemList.CurrencyCopper, currencyItem, CurrencyType.COPPER);
        registerCurrencyEntry(ModItemList.CurrencySteel, currencyItem, CurrencyType.STEEL);
        registerCurrencyEntry(ModItemList.CurrencyTitanium, currencyItem, CurrencyType.TITANIUM);
        registerCurrencyEntry(ModItemList.CurrencyPlatinum, currencyItem, CurrencyType.PLATINUM);
        registerCurrencyEntry(ModItemList.CurrencyNeutronium, currencyItem, CurrencyType.NEUTRONIUM);
        registerCurrencyEntry(ModItemList.CurrencyInfinity, currencyItem, CurrencyType.INFINITY);
    }

    private static void registerCurrencyEntry(ModItemList entry, CurrencyItem item, CurrencyType type) {
        entry.set(new ItemStack(item, 1, type.getMeta()));
    }
}
