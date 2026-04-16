package com.andgatech.AHTech.common.modularizedMachine.modularHatches.parallelController;

import com.andgatech.AHTech.common.modularizedMachine.ISupportParallelController;
import com.andgatech.AHTech.common.modularizedMachine.ModularizedMachineBase;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.IStaticModularHatch;

import gregtech.api.interfaces.ITexture;

public abstract class StaticParallelControllerBase extends ParallelControllerBase implements IStaticModularHatch {

    public StaticParallelControllerBase(int aID, String aName, String aNameRegional, int aTier) {
        super(aID, aName, aNameRegional, aTier, null);
    }

    public StaticParallelControllerBase(String aName, int aTier, ITexture[][][] aTextures) {
        super(aName, aTier, null, aTextures);
    }

    @Override
    public void onCheckMachine(ModularizedMachineBase<?> machine) {
        if (isCompatibleWithMachine(machine) && machine instanceof ISupportParallelController parallelSupporter) {
            int p = parallelSupporter.getStaticParallelParameterValue();
            if (p == Integer.MAX_VALUE) return;
            int tp = getParallel();
            if (p >= Integer.MAX_VALUE - tp) {
                parallelSupporter.setStaticParallelParameter(Integer.MAX_VALUE);
            } else {
                parallelSupporter.setStaticParallelParameter(p + tp);
            }
        }
    }
}
