package com.Nxer.TwistSpaceTechnology.common.modularizedMachine.modularHatches.SpeedConstrollers;

import com.Nxer.TwistSpaceTechnology.common.modularizedMachine.modularHatches.ModularHatchBase;

/**
 * Stub for TST SpeedControllerBase.
 */
public abstract class SpeedControllerBase extends ModularHatchBase {

    public SpeedControllerBase(int aID, String aName, String aNameRegional, int aTier) {
        super(aID, aName, aNameRegional, aTier, 0, null);
    }

    public float getSpeedBonus() {
        return 1F / getSpeedMultiplier();
    }

    public abstract int getSpeedMultiplier();
}
