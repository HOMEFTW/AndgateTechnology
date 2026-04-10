package com.andgatech.AHTech.common.modularizedMachine;

public interface ISupportPowerConsumptionController extends IModularizedMachine {

    float getStaticPowerConsumptionParameterValue();

    void setStaticPowerConsumptionParameterValue(float value);
}
