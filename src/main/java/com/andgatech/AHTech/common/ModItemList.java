package com.andgatech.AHTech.common;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import gregtech.api.util.GTUtility;

/**
 * Enum mapping symbolic names to ItemStack references.
 * Each machine, item, and block should have an entry here.
 * Set the ItemStack during machine registration in MachineLoader.
 */
public enum ModItemList {

    // region Items
    // Add your items here, e.g.:
    // MyCustomItem,
    // endregion

    // region Machines
    // Add your machines here, e.g.:
    // MyFirstMachine,
    ElectronicsMarket,
    // endregion

    ;

    private ItemStack mStack;

    public void set(ItemStack aStack) {
        this.mStack = aStack;
    }

    public void set(Item aItem, long aAmount) {
        this.mStack = new ItemStack(aItem, (int) aAmount, 0);
    }

    public void set(Item aItem) {
        this.set(aItem, 1);
    }

    public void set(Block aBlock, long aAmount) {
        this.mStack = new ItemStack(aBlock, (int) aAmount, 0);
    }

    public void set(Block aBlock) {
        this.set(aBlock, 1);
    }

    @Nullable
    public ItemStack get(long aAmount) {
        if (mStack == null) return null;
        return GTUtility.copyAmountUnsafe((int) aAmount, mStack);
    }

    @Nullable
    public ItemStack get() {
        return get(1);
    }

    @Nullable
    public ItemStack getWildcard() {
        if (mStack == null) return null;
        return GTUtility.copyAmountUnsafe(1, mStack);
    }

    public boolean isStackEqual(ItemStack aStack) {
        return mStack != null && GTUtility.areStacksEqual(mStack, aStack, true);
    }
}
