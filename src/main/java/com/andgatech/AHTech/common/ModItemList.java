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
    ContractLv1,
    ContractLv2,
    ContractLv3,
    ContractLv4,
    // endregion

    // region Machines
    // Add your machines here, e.g.:
    // MyFirstMachine,
    ElectronicsMarket,
    // endregion

    // region Modular Hatches - Parallel Controllers
    StaticParallelControllerT1,
    StaticParallelControllerT2,
    StaticParallelControllerT3,
    StaticParallelControllerT4,
    StaticParallelControllerT5,
    StaticParallelControllerT6,
    StaticParallelControllerT7,
    StaticParallelControllerT8,
    DynamicParallelControllerT1,
    DynamicParallelControllerT2,
    DynamicParallelControllerT3,
    DynamicParallelControllerT4,
    DynamicParallelControllerT5,
    DynamicParallelControllerT6,
    DynamicParallelControllerT7,
    DynamicParallelControllerT8,
    // endregion

    // region Modular Hatches - Speed Controllers
    StaticSpeedControllerT1,
    StaticSpeedControllerT2,
    StaticSpeedControllerT3,
    StaticSpeedControllerT4,
    StaticSpeedControllerT5,
    StaticSpeedControllerT6,
    StaticSpeedControllerT7,
    StaticSpeedControllerT8,
    DynamicSpeedControllerT1,
    DynamicSpeedControllerT2,
    DynamicSpeedControllerT3,
    DynamicSpeedControllerT4,
    DynamicSpeedControllerT5,
    DynamicSpeedControllerT6,
    DynamicSpeedControllerT7,
    DynamicSpeedControllerT8,
    // endregion

    // region Modular Hatches - Power Consumption Controllers
    StaticPowerConsumptionControllerT1,
    StaticPowerConsumptionControllerT2,
    StaticPowerConsumptionControllerT3,
    StaticPowerConsumptionControllerT4,
    StaticPowerConsumptionControllerT5,
    StaticPowerConsumptionControllerT6,
    StaticPowerConsumptionControllerT7,
    StaticPowerConsumptionControllerT8,
    // endregion

    // region Modular Hatches - Overclock Controllers
    LowSpeedPerfectOverclockController,
    PerfectOverclockController,
    SingularityPerfectOverclockController,
    // endregion

    // region Modular Hatches - Execution Cores
    ExecutionCoreNormal,
    AdvExecutionCore,
    PerfectExecutionCore,
    // endregion

    // region Modular Hatches - Recovery Rate
    RecoveryRateLv1,
    RecoveryRateLv2,
    RecoveryRateLv3,
    // endregion

    // region Modular Hatches - Function Modules
    GeneralDisassemblyModule,
    // endregion

    // region Supplier Hatches
    SupplierShandongDezhou,
    SupplierYadepian,
    SupplierErfa,
    SupplierChaola,
    SupplierHuangweida,
    SupplierGaiguang,
    SupplierDitong,
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
