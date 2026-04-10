package com.andgatech.AHTech.common.modularizedMachine.modularHatches.functionModule;

import com.andgatech.AHTech.common.modularizedMachine.FunctionType;
import com.andgatech.AHTech.common.modularizedMachine.ISupportFunctionModule;
import com.andgatech.AHTech.common.modularizedMachine.ModularHatchType;
import com.andgatech.AHTech.common.modularizedMachine.ModularizedMachineBase;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.IStaticModularHatch;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.ModularHatchBase;

import gregtech.api.interfaces.ITexture;

public abstract class FunctionModuleBase extends ModularHatchBase implements IStaticModularHatch {

    public FunctionModuleBase(int aID, String aName, String aNameRegional, int aTier) {
        super(aID, aName, aNameRegional, aTier, 0, null);
    }

    public FunctionModuleBase(String aName, int aTier, ITexture[][][] aTextures) {
        super(aName, aTier, 0, null, aTextures);
    }

    @Override
    public ModularHatchType getType() {
        return ModularHatchType.FUNCTION_MODULE;
    }

    public abstract FunctionType getFunctionType();

    @Override
    public void onCheckMachine(ModularizedMachineBase<?> machine) {
        if (isCompatibleWithMachine(machine) && machine instanceof ISupportFunctionModule fm) {
            fm.addFunctionType(getFunctionType());
        }
    }
}
