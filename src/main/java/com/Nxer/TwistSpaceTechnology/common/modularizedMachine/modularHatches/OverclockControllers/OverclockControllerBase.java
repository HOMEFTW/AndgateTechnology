package com.Nxer.TwistSpaceTechnology.common.modularizedMachine.modularHatches.OverclockControllers;

import com.Nxer.TwistSpaceTechnology.common.misc.OverclockType;
import com.Nxer.TwistSpaceTechnology.common.modularizedMachine.modularHatches.ModularHatchBase;

/**
 * Stub for TST OverclockControllerBase.
 */
public abstract class OverclockControllerBase extends ModularHatchBase {

    public OverclockControllerBase(int aID, String aName, String aNameRegional, int aTier) {
        super(aID, aName, aNameRegional, aTier, 0, null);
    }

    public abstract OverclockType getOverclockType();
}
