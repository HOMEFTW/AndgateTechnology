package com.andgatech.AHTech.common.modularizedMachine.modularHatches;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import com.andgatech.AHTech.common.modularizedMachine.ModularizedMachineBase;

import gregtech.api.interfaces.ITexture;
import gregtech.api.metatileentity.implementations.MTEHatch;

public abstract class ModularHatchBase extends MTEHatch implements IModularHatch {

    protected final int moduleTier;

    public ModularHatchBase(int aID, String aName, String aNameRegional, int aTier, int aInvSlotCount,
        String[] aDescription, ITexture... aTextures) {
        super(aID, aName, aNameRegional, aTier, aInvSlotCount, aDescription, aTextures);
        this.moduleTier = aTier;
    }

    public ModularHatchBase(String aName, int aTier, int aInvSlotCount, String[] aDescription,
        ITexture[][][] aTextures) {
        super(aName, aTier, aInvSlotCount, aDescription, aTextures);
        this.moduleTier = aTier;
    }

    public int getModuleTier() {
        return moduleTier;
    }

    public boolean isCompatibleWithMachine(ModularizedMachineBase<?> machine) {
        return machine.getStructureTier() >= moduleTier;
    }

    /**
     * Returns the maintenance EU/t cost for this module.
     * Scales with module tier: tier 7 (IV) = 1024, tier 8 (LuV) = 2048, etc.
     * Modules with tier 0 (e.g. supplier/financial hatches) have no maintenance cost.
     * Subclasses can override for custom costs.
     */
    public long getMaintenanceEUt() {
        if (moduleTier <= 0) return 0;
        return (long) Math.pow(2, moduleTier) * 8;
    }

    @Override
    public boolean willExplodeInRain() {
        return false;
    }

    @Override
    public boolean isFacingValid(ForgeDirection facing) {
        return true;
    }

    @Override
    public boolean isAccessAllowed(EntityPlayer aPlayer) {
        return true;
    }

    @Override
    public boolean isValidSlot(int aIndex) {
        return false;
    }

    @Override
    public boolean isLiquidInput(ForgeDirection side) {
        return false;
    }

    @Override
    public boolean isFluidInputAllowed(FluidStack aFluid) {
        return false;
    }
}
