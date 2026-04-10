package com.Nxer.TwistSpaceTechnology.common.modularizedMachine.modularHatches.ParallelControllers;

import com.Nxer.TwistSpaceTechnology.common.modularizedMachine.modularHatches.ModularHatchBase;

/**
 * Stub for TST ParallelControllerBase.
 */
public abstract class ParallelControllerBase extends ModularHatchBase {

    public ParallelControllerBase(int aID, String aName, String aNameRegional, int aTier, String[] aDescription) {
        super(aID, aName, aNameRegional, aTier, 0, aDescription);
    }

    public abstract int getParallel();
}
