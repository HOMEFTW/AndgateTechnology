package com.andgatech.AHTech.common.contract;

import java.util.Iterator;
import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import com.andgatech.AHTech.AndgateTechnology;

public class ContractItem extends Item {

    private final ContractTier contractTier;

    public ContractItem(ContractTier contractTier) {
        this.contractTier = contractTier;
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.tabMisc);
        setUnlocalizedName("contract." + contractTier.name().toLowerCase());
        setTextureName(AndgateTechnology.RESOURCE_ROOT_ID + ":contract_" + contractTier.name().toLowerCase());
    }

    public ContractTier getContractTier() {
        return contractTier;
    }

    public static ContractTier getTier(ItemStack stack) {
        if (stack != null && stack.getItem() instanceof ContractItem contractItem) {
            return contractItem.getContractTier();
        }
        return ContractTier.NONE;
    }

    public static ContractTier findHighestTier(Iterable<ItemStack> stacks) {
        ContractTier highest = ContractTier.NONE;
        if (stacks == null) {
            return highest;
        }
        Iterator<ItemStack> iterator = stacks.iterator();
        while (iterator.hasNext()) {
            ContractTier tier = getTier(iterator.next());
            if (tier.getTier() > highest.getTier()) {
                highest = tier;
            }
        }
        return highest;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        tooltip.add(StatCollector.translateToLocal(contractTier.getTranslationKey()));
    }
}
