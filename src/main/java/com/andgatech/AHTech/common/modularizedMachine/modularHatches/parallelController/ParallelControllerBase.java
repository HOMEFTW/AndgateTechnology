package com.andgatech.AHTech.common.modularizedMachine.modularHatches.parallelController;

import com.andgatech.AHTech.common.modularizedMachine.ModularHatchType;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.ModularHatchBase;

import gregtech.api.interfaces.ITexture;

public abstract class ParallelControllerBase extends ModularHatchBase {

    public ParallelControllerBase(int aID, String aName, String aNameRegional, int aTier, String[] aDescription) {
        super(aID, aName, aNameRegional, aTier, 0, aDescription);
    }

    public ParallelControllerBase(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, 0, aDescription, aTextures);
    }

    @Override
    public ModularHatchType getType() {
        return ModularHatchType.PARALLEL_CONTROLLER;
    }

    public abstract int getParallel();

    @Override
    public ITexture[] getTexturesActive(ITexture aBaseTexture) {
        return new ITexture[] { aBaseTexture };
    }

    @Override
    public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
        return new ITexture[] { aBaseTexture };
    }
}
