package com.andgatech.AHTech.common.modularizedMachine.modularHatches;

import com.andgatech.AHTech.common.modularizedMachine.ModularizedMachineBase;

public interface IStaticModularHatch extends IModularHatch {

    void onCheckMachine(ModularizedMachineBase<?> machine);
}
