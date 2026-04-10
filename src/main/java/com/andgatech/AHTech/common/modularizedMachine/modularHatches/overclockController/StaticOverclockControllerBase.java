package com.andgatech.AHTech.common.modularizedMachine.modularHatches.overclockController;

import com.andgatech.AHTech.common.modularizedMachine.ISupportOverclockController;
import com.andgatech.AHTech.common.modularizedMachine.ModularizedMachineBase;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.IStaticModularHatch;

import gregtech.api.interfaces.ITexture;

public abstract class StaticOverclockControllerBase extends OverclockControllerBase implements IStaticModularHatch {

    public StaticOverclockControllerBase(int aID, String aName, String aNameRegional, int aTier) {
        super(aID, aName, aNameRegional, aTier);
    }

    public StaticOverclockControllerBase(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, aDescription, aTextures);
    }

    @Override
    public void onCheckMachine(ModularizedMachineBase<?> machine) {
        if (machine instanceof ISupportOverclockController supportOverclockController) {
            supportOverclockController.setOverclockType(getOverclockType());
        }
    }
}
