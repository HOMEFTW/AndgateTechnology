package com.andgatech.AHTech.common.modularizedMachine.modularHatches;

import com.andgatech.AHTech.common.modularizedMachine.ISupportPowerConsumptionController;
import com.andgatech.AHTech.common.modularizedMachine.ModularHatchType;
import com.andgatech.AHTech.common.modularizedMachine.ModularizedMachineBase;

import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

public class PowerConsumptionModule extends ModularHatchBase implements IStaticModularHatch {

    protected final float powerConsumptionMultiplier;

    public PowerConsumptionModule(int aID, String aName, String aNameRegional, int aTier,
        float powerConsumptionMultiplier) {
        super(aID, aName, aNameRegional, aTier, 0, null);
        this.powerConsumptionMultiplier = powerConsumptionMultiplier;
    }

    public PowerConsumptionModule(String aName, int aTier, float powerConsumptionMultiplier, ITexture[][][] aTextures) {
        super(aName, aTier, 0, null, aTextures);
        this.powerConsumptionMultiplier = powerConsumptionMultiplier;
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new PowerConsumptionModule(this.mName, this.mTier, this.powerConsumptionMultiplier, this.mTextures);
    }

    @Override
    public ModularHatchType getType() {
        return ModularHatchType.POWER_CONSUMPTION_CONTROLLER;
    }

    public float getPowerConsumptionMultiplier() {
        return powerConsumptionMultiplier;
    }

    @Override
    public void onCheckMachine(ModularizedMachineBase<?> machine) {
        if (isCompatibleWithMachine(machine) && machine instanceof ISupportPowerConsumptionController ctrl) {
            ctrl.setStaticPowerConsumptionParameterValue(
                ctrl.getStaticPowerConsumptionParameterValue() * powerConsumptionMultiplier);
        }
    }

    @Override
    public ITexture[] getTexturesActive(ITexture aBaseTexture) {
        return new ITexture[] { aBaseTexture };
    }

    @Override
    public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
        return new ITexture[] { aBaseTexture };
    }
}
