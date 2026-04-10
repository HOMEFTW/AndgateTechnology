package com.andgatech.AHTech.common.modularizedMachine;

public interface ISupportOverclockController extends IModularizedMachine {

    void setOverclockType(OverclockType type);

    OverclockType getOverclockType();
}
