package com.andgatech.AHTech.common.modularizedMachine;

import java.util.Collection;

import com.andgatech.AHTech.common.modularizedMachine.modularHatches.IModularHatch;

public interface IModularizedMachine {

    Collection<ModularHatchType> getSupportedModularHatchTypes();

    void resetModularStaticSettings();

    void applyModularStaticSettings();

    default void checkModularStaticSettings() {
        resetModularStaticSettings();
        applyModularStaticSettings();
    }

    void resetModularDynamicParameters();

    void applyModularDynamicParameters();

    default void checkModularDynamicParameters() {
        resetModularDynamicParameters();
        applyModularDynamicParameters();
    }

    Collection<IModularHatch> getAllModularHatches();

    void resetModularHatchCollections();
}
