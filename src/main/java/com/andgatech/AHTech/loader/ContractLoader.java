package com.andgatech.AHTech.loader;

import net.minecraft.item.ItemStack;

import com.andgatech.AHTech.common.ModItemList;
import com.andgatech.AHTech.common.contract.ContractItem;
import com.andgatech.AHTech.common.contract.ContractTier;

import cpw.mods.fml.common.registry.GameRegistry;

public final class ContractLoader {

    private ContractLoader() {}

    public static void loadContracts() {
        register(ContractTier.LV1, ModItemList.ContractLv1);
        register(ContractTier.LV2, ModItemList.ContractLv2);
        register(ContractTier.LV3, ModItemList.ContractLv3);
        register(ContractTier.LV4, ModItemList.ContractLv4);
    }

    private static void register(ContractTier contractTier, ModItemList modItemEntry) {
        ContractItem item = new ContractItem(contractTier);
        GameRegistry.registerItem(
            item,
            "contract_" + contractTier.name()
                .toLowerCase());
        modItemEntry.set(new ItemStack(item, 1));
    }
}
