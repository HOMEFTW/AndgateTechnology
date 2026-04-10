package com.Nxer.TwistSpaceTechnology.common.modularizedMachine.modularHatches.PowerConsumptionControllers;

import com.Nxer.TwistSpaceTechnology.common.modularizedMachine.modularHatches.ModularHatchBase;

/**
 * Stub for TST PowerConsumptionControllerBase.
 */
public abstract class PowerConsumptionControllerBase extends ModularHatchBase {

    public PowerConsumptionControllerBase(int aID, String aName, String aNameRegional, int aTier) {
        super(aID, aName, aNameRegional, aTier, 0, null);
    }

    public abstract float getPowerConsumptionMultiplier();
}
