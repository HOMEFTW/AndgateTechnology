package com.andgatech.AHTech.common.modularizedMachine.modularHatches;

import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

public class ExecutionCore extends ExecutionCoreBase {

    public ExecutionCore(int aID, String aName, String aNameRegional, int aTier) {
        super(aID, aName, aNameRegional, aTier);
    }

    public ExecutionCore(String aName, int aTier, ITexture[][][] aTextures) {
        super(aName, aTier, aTextures);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new ExecutionCore(this.mName, this.mTier, this.mTextures);
    }

    @Override
    public boolean done() {
        return true;
    }

    @Override
    public boolean useMainMachinePower() {
        return true;
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
