package com.andgatech.AHTech.common.modularizedMachine.modularHatches.overclockController;

import com.andgatech.AHTech.common.modularizedMachine.ModularHatchType;
import com.andgatech.AHTech.common.modularizedMachine.OverclockType;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.ModularHatchBase;

import gregtech.api.interfaces.ITexture;

public abstract class OverclockControllerBase extends ModularHatchBase {

    public OverclockControllerBase(int aID, String aName, String aNameRegional, int aTier) {
        super(aID, aName, aNameRegional, aTier, 0, null);
    }

    public OverclockControllerBase(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, 0, aDescription, aTextures);
    }

    @Override
    public ModularHatchType getType() {
        return ModularHatchType.OVERCLOCK_CONTROLLER;
    }

    public abstract OverclockType getOverclockType();

    @Override
    public ITexture[] getTexturesActive(ITexture aBaseTexture) {
        return new ITexture[] { aBaseTexture };
    }

    @Override
    public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
        return new ITexture[] { aBaseTexture };
    }
}
