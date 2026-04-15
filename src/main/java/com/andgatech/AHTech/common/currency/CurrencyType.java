package com.andgatech.AHTech.common.currency;

import net.minecraft.item.ItemStack;

import com.andgatech.AHTech.common.ModItemList;

import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.util.GTOreDictUnificator;

public enum CurrencyType {
    COPPER(0, "copper", Materials.Copper),
    STEEL(1, "steel", Materials.Steel),
    TITANIUM(2, "titanium", Materials.Titanium),
    PLATINUM(3, "platinum", Materials.Platinum),
    NEUTRONIUM(4, "neutronium", Materials.Neutronium),
    INFINITY(5, "infinity", Materials.Infinity);

    private final int meta;
    private final String name;
    private final Materials material;

    CurrencyType(int meta, String name, Materials material) {
        this.meta = meta;
        this.name = name;
        this.material = material;
    }

    public int getMeta() {
        return meta;
    }

    public String getName() {
        return name;
    }

    public Materials getMaterial() {
        return material;
    }

    public String getTranslationKey() {
        return "ahtech.currency." + name;
    }

    public ItemStack getIngot() {
        return GTOreDictUnificator.get(OrePrefixes.ingot, material, 1L);
    }

    public ItemStack getCoinStack(int amount) {
        return getItemEntry().get(amount);
    }

    public static CurrencyType getByMeta(int meta) {
        for (CurrencyType type : values()) {
            if (type.meta == meta) {
                return type;
            }
        }
        return null;
    }

    private ModItemList getItemEntry() {
        return switch (this) {
            case COPPER -> ModItemList.CurrencyCopper;
            case STEEL -> ModItemList.CurrencySteel;
            case TITANIUM -> ModItemList.CurrencyTitanium;
            case PLATINUM -> ModItemList.CurrencyPlatinum;
            case NEUTRONIUM -> ModItemList.CurrencyNeutronium;
            case INFINITY -> ModItemList.CurrencyInfinity;
        };
    }
}
