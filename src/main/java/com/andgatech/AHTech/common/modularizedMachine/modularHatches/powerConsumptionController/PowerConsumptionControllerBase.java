package com.andgatech.AHTech.common.modularizedMachine.modularHatches.powerConsumptionController;

import com.andgatech.AHTech.common.modularizedMachine.ModularHatchType;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.ModularHatchBase;

import gregtech.api.interfaces.ITexture;

public abstract class PowerConsumptionControllerBase extends ModularHatchBase {

    public PowerConsumptionControllerBase(int aID, String aName, String aNameRegional, int aTier) {
        super(aID, aName, aNameRegional, aTier, 0, null);
    }

    public PowerConsumptionControllerBase(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, 0, aDescription, aTextures);
    }

    @Override
    public ModularHatchType getType() {
        return ModularHatchType.POWER_CONSUMPTION_CONTROLLER;
    }

    public abstract float getPowerConsumptionMultiplier();

    @Override
    public ITexture[] getTexturesActive(ITexture aBaseTexture) {
        return new ITexture[] { aBaseTexture };
    }

    @Override
    public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
        return new ITexture[] { aBaseTexture };
    }
}
