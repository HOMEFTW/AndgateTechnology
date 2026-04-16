package com.andgatech.AHTech.common.modularizedMachine.modularHatches.parallelController;

import com.andgatech.AHTech.common.modularizedMachine.ISupportParallelController;
import com.andgatech.AHTech.common.modularizedMachine.ModularizedMachineBase;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.IDynamicModularHatch;

import gregtech.api.interfaces.ITexture;

public abstract class DynamicParallelControllerBase extends ParallelControllerBase implements IDynamicModularHatch {

    public DynamicParallelControllerBase(int aID, String aName, String aNameRegional, int aTier) {
        super(aID, aName, aNameRegional, aTier, null);
    }

    public DynamicParallelControllerBase(String aName, int aTier, ITexture[][][] aTextures) {
        super(aName, aTier, null, aTextures);
    }

    @Override
    public void onCheckProcessing(ModularizedMachineBase<?> machine) {
        if (isCompatibleWithMachine(machine) && machine instanceof ISupportParallelController parallelSupporter) {
            int p = parallelSupporter.getDynamicParallelParameterValue();
            if (p == Integer.MAX_VALUE) return;
            int tp = getParallel();
            if (p >= Integer.MAX_VALUE - tp) {
                parallelSupporter.setDynamicParallelParameter(Integer.MAX_VALUE);
            } else {
                parallelSupporter.setDynamicParallelParameter(p + tp);
            }
        }
    }

    public abstract int getMaxParallel();
}
