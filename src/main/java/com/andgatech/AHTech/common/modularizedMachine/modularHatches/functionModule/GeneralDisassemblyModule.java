package com.andgatech.AHTech.common.modularizedMachine.modularHatches.functionModule;

import com.andgatech.AHTech.common.modularizedMachine.FunctionType;

import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

public class GeneralDisassemblyModule extends FunctionModuleBase {

    public GeneralDisassemblyModule(int aID, String aName, String aNameRegional, int aTier) {
        super(aID, aName, aNameRegional, aTier);
    }

    public GeneralDisassemblyModule(String aName, int aTier, ITexture[][][] aTextures) {
        super(aName, aTier, aTextures);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GeneralDisassemblyModule(this.mName, this.mTier, this.mTextures);
    }

    @Override
    public FunctionType getFunctionType() {
        return FunctionType.GENERAL_DISASSEMBLY;
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
