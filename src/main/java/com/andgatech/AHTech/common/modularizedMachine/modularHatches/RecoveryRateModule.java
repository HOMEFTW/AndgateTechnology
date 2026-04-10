package com.andgatech.AHTech.common.modularizedMachine.modularHatches;

import com.andgatech.AHTech.common.modularizedMachine.ISupportRecoveryRateController;
import com.andgatech.AHTech.common.modularizedMachine.ModularHatchType;
import com.andgatech.AHTech.common.modularizedMachine.ModularizedMachineBase;

import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

public class RecoveryRateModule extends ModularHatchBase implements IStaticModularHatch {

    protected final float recoveryRate;

    public RecoveryRateModule(int aID, String aName, String aNameRegional, int aTier, float recoveryRate) {
        super(aID, aName, aNameRegional, aTier, 0, null);
        this.recoveryRate = recoveryRate;
    }

    public RecoveryRateModule(String aName, int aTier, float recoveryRate, ITexture[][][] aTextures) {
        super(aName, aTier, 0, null, aTextures);
        this.recoveryRate = recoveryRate;
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new RecoveryRateModule(this.mName, this.mTier, this.recoveryRate, this.mTextures);
    }

    @Override
    public ModularHatchType getType() {
        return ModularHatchType.RECOVERY_RATE_CONTROLLER;
    }

    public float getRecoveryRate() {
        return recoveryRate;
    }

    @Override
    public void onCheckMachine(ModularizedMachineBase<?> machine) {
        if (isCompatibleWithMachine(machine) && machine instanceof ISupportRecoveryRateController ctrl) {
            ctrl.setRecoveryRate(Math.max(ctrl.getRecoveryRate(), this.recoveryRate));
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
