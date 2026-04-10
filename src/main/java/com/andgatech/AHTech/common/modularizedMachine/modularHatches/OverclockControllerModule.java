package com.andgatech.AHTech.common.modularizedMachine.modularHatches;

import com.andgatech.AHTech.common.modularizedMachine.ISupportOverclockController;
import com.andgatech.AHTech.common.modularizedMachine.ModularHatchType;
import com.andgatech.AHTech.common.modularizedMachine.ModularizedMachineBase;
import com.andgatech.AHTech.common.modularizedMachine.OverclockType;

import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

public class OverclockControllerModule extends ModularHatchBase implements IStaticModularHatch {

    protected final OverclockType overclockType;

    public OverclockControllerModule(int aID, String aName, String aNameRegional, int aTier, int timeReduction,
        int powerIncrease) {
        super(aID, aName, aNameRegional, aTier, 0, null);
        this.overclockType = OverclockType.checkOverclockType(timeReduction, powerIncrease);
    }

    public OverclockControllerModule(String aName, int aTier, OverclockType overclockType, ITexture[][][] aTextures) {
        super(aName, aTier, 0, null, aTextures);
        this.overclockType = overclockType;
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new OverclockControllerModule(this.mName, this.mTier, this.overclockType, this.mTextures);
    }

    @Override
    public ModularHatchType getType() {
        return ModularHatchType.OVERCLOCK_CONTROLLER;
    }

    public OverclockType getOverclockType() {
        return overclockType;
    }

    @Override
    public void onCheckMachine(ModularizedMachineBase<?> machine) {
        if (isCompatibleWithMachine(machine) && machine instanceof ISupportOverclockController ctrl) {
            ctrl.setOverclockType(overclockType);
        }
    }

    @Override
    public ITexture[] getTexturesActive(ITexture aBaseTexture) {
        return new ITexture[] { aBaseTexture };
    }

    @Override
    public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
        return new ITexture[] { aBaseTexture };
    }
}
