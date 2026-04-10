package com.andgatech.AHTech.common.modularizedMachine.modularHatches;

import com.andgatech.AHTech.common.modularizedMachine.ISupportSpeedController;
import com.andgatech.AHTech.common.modularizedMachine.ModularHatchType;
import com.andgatech.AHTech.common.modularizedMachine.ModularizedMachineBase;

import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

public class SpeedControllerModule extends ModularHatchBase implements IStaticModularHatch {

    protected final int speedMultiplier;

    public SpeedControllerModule(int aID, String aName, String aNameRegional, int aTier, int speedMultiplier) {
        super(aID, aName, aNameRegional, aTier, 0, null);
        this.speedMultiplier = speedMultiplier;
    }

    public SpeedControllerModule(String aName, int aTier, int speedMultiplier, ITexture[][][] aTextures) {
        super(aName, aTier, 0, null, aTextures);
        this.speedMultiplier = speedMultiplier;
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new SpeedControllerModule(this.mName, this.mTier, this.speedMultiplier, this.mTextures);
    }

    @Override
    public ModularHatchType getType() {
        return ModularHatchType.SPEED_CONTROLLER;
    }

    public int getSpeedMultiplier() {
        return speedMultiplier;
    }

    @Override
    public void onCheckMachine(ModularizedMachineBase<?> machine) {
        if (isCompatibleWithMachine(machine) && machine instanceof ISupportSpeedController ctrl) {
            // Speed bonus is multiplicative: 1/multiplier means faster
            // e.g. multiplier=2 means speedBonus *= 0.5 (2x faster)
            ctrl.setStaticSpeedParameterValue(ctrl.getStaticSpeedParameterValue() * (1.0f / speedMultiplier));
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
