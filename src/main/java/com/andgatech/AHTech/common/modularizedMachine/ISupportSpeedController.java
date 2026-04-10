package com.andgatech.AHTech.common.modularizedMachine;

public interface ISupportSpeedController extends IModularizedMachine {

    float getStaticSpeedParameterValue();

    void setStaticSpeedParameterValue(float value);

    float getDynamicSpeedParameterValue();

    void setDynamicSpeedParameterValue(float value);
}
