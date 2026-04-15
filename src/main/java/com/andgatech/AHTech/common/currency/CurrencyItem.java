package com.andgatech.AHTech.common.currency;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import com.andgatech.AHTech.AndgateTechnology;

public class CurrencyItem extends Item {

    public CurrencyItem() {
        setMaxStackSize(64);
        setHasSubtypes(true);
        setCreativeTab(CreativeTabs.tabMisc);
        setUnlocalizedName("ahtech_currency");
        setTextureName(AndgateTechnology.RESOURCE_ROOT_ID + ":currency");
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        CurrencyType type = getCurrencyType(stack);
        if (type == null) {
            return super.getUnlocalizedName(stack);
        }
        return "item." + type.getTranslationKey();
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void getSubItems(Item item, CreativeTabs tab, List variants) {
        for (CurrencyType type : CurrencyType.values()) {
            variants.add(new ItemStack(item, 1, type.getMeta()));
        }
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        CurrencyType type = getCurrencyType(stack);
        if (type != null) {
            tooltip.add(StatCollector.translateToLocal(type.getTranslationKey()));
        }
    }

    public static boolean isCurrency(ItemStack stack) {
        return stack != null && stack.getItem() instanceof CurrencyItem;
    }

    public static CurrencyType getCurrencyType(ItemStack stack) {
        if (!isCurrency(stack)) {
            return null;
        }
        return CurrencyType.getByMeta(stack.getItemDamage());
    }
}
