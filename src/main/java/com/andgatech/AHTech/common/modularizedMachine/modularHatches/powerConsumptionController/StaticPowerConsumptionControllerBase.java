package com.andgatech.AHTech.common.modularizedMachine.modularHatches.powerConsumptionController;

import com.andgatech.AHTech.common.modularizedMachine.ISupportPowerConsumptionController;
import com.andgatech.AHTech.common.modularizedMachine.ModularizedMachineBase;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.IStaticModularHatch;

import gregtech.api.interfaces.ITexture;

public abstract class StaticPowerConsumptionControllerBase extends PowerConsumptionControllerBase
    implements IStaticModularHatch {

    public StaticPowerConsumptionControllerBase(int aID, String aName, String aNameRegional, int aTier) {
        super(aID, aName, aNameRegional, aTier);
    }

    public StaticPowerConsumptionControllerBase(String aName, int aTier, String[] aDescription,
        ITexture[][][] aTextures) {
        super(aName, aTier, aDescription, aTextures);
    }

    @Override
    public void onCheckMachine(ModularizedMachineBase<?> machine) {
        if (machine instanceof ISupportPowerConsumptionController supportPowerConsumptionController) {
            float p = supportPowerConsumptionController.getStaticPowerConsumptionParameterValue();
            if (p <= 0) {
                throw new RuntimeException("Error: Power Consumption Multiplier is 0 at machine - " + machine);
            }
            float tp = getPowerConsumptionMultiplier();
            if (tp <= 0) {
                throw new RuntimeException(
                    "Error: Power Consumption Multiplier is 0. Please try to change your settings at " + this);
            }
            supportPowerConsumptionController.setStaticPowerConsumptionParameterValue(p * tp);
        }
    }
}
