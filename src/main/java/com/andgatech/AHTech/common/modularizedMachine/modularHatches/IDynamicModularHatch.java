package com.andgatech.AHTech.common.modularizedMachine.modularHatches;

import com.andgatech.AHTech.common.modularizedMachine.ModularizedMachineBase;

public interface IDynamicModularHatch extends IModularHatch {

    void onCheckProcessing(ModularizedMachineBase<?> machine);
}
