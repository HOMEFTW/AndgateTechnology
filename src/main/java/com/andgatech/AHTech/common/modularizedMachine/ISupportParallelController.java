package com.andgatech.AHTech.common.modularizedMachine;

public interface ISupportParallelController extends IModularizedMachine {

    int getStaticParallelParameterValue();

    void setStaticParallelParameter(int value);

    int getDynamicParallelParameterValue();

    void setDynamicParallelParameter(int value);
}
