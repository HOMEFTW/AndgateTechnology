package com.andgatech.AHTech.common.modularizedMachine.modularHatches.executionCore;

import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

public class ExecutionCore extends ExecutionCoreBase {

    public ExecutionCore(int aID, String aName, String aNameRegional, int aTier) {
        super(aID, aName, aNameRegional, aTier);
    }

    public ExecutionCore(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, aDescription, aTextures);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new ExecutionCore(this.mName, this.mTier, this.mDescriptionArray, this.mTextures);
    }

    @Override
    public boolean done() {
        trySetActive();
        return true;
    }

    @Override
    public boolean useMainMachinePower() {
        return true;
    }

    // region General

    // spotless:off
    protected String[] description;

    @Override
    public String[] getDescription() {
        if (description == null || description.length == 0) {
            description = new String[] {
                // #tr Tooltips.ExecutionCore.01
                // # Add an actual execution core to your modularized machine.
                // #zh_CN 为你的模块化机器添加一颗实际执行核心.
                "Add an actual execution core to your modularized machine.",
                // #tr Tooltips.ExecutionCore.02
                // # Machines that support multiple execution cores distribute actual production tasks to these execution cores.
                // #zh_CN 支持多执行核心的机器将实际生产任务分配到这些执行核心上.
                "Machines that support multiple execution cores distribute actual production tasks to these execution cores.",
                // #tr Tooltips.ExecutionCore.03
                // # Multiple execution cores share the machine's input/output and energy and logics.
                // #zh_CN 多个执行核心共同使用机器的输入输出和能源和逻辑.
                "Multiple execution cores share the machine's input/output and energy and logics.",
            };
        }
        return description;
    }
    // spotless:on
}
