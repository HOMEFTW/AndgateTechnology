package com.andgatech.AHTech.common.modularizedMachine;

import java.util.Set;

public interface ISupportFunctionModule extends IModularizedMachine {

    Set<FunctionType> getInstalledFunctionTypes();

    void addFunctionType(FunctionType type);

    boolean hasFunction(FunctionType type);
}
