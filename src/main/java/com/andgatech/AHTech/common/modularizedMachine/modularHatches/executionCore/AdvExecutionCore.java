package com.andgatech.AHTech.common.modularizedMachine.modularHatches.executionCore;

import static gregtech.common.misc.WirelessNetworkManager.addEUToGlobalEnergyMap;

import java.math.BigInteger;
import java.util.UUID;

import com.andgatech.AHTech.AndgateTechnology;

import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

public class AdvExecutionCore extends ExecutionCoreBase {

    public AdvExecutionCore(int aID, String aName, String aNameRegional, int aTier) {
        super(aID, aName, aNameRegional, aTier);
    }

    public AdvExecutionCore(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, aDescription, aTextures);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new AdvExecutionCore(this.mName, this.mTier, this.mDescriptionArray, this.mTextures);
    }

    private static final BigInteger NEGATIVE_ONE = BigInteger.valueOf(-1);
    private UUID ownerUUID;

    @Override
    public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {
        super.onFirstTick(aBaseMetaTileEntity);
        this.ownerUUID = aBaseMetaTileEntity.getOwnerUuid();
    }

    @Override
    public boolean done() {
        // check wireless EU at this moment
        if (!addEUToGlobalEnergyMap(
            ownerUUID,
            BigInteger.valueOf(eut)
                .multiply(BigInteger.valueOf(maxProgressingTime))
                .multiply(NEGATIVE_ONE))) {
            shutDown();
            IGregTechTileEntity mte = getBaseMetaTileEntity();
            AndgateTechnology.LOG.info(
                "Advanced Execution Core shut down because of power at x" + mte
                    .getXCoord() + " y" + mte.getYCoord() + " z" + mte.getZCoord());
            return false;
        }
        trySetActive();
        return true;
    }

    @Override
    public boolean useMainMachinePower() {
        return false;
    }

    // region General

    // spotless:off
    protected String[] description;

    @Override
    public String[] getDescription() {
        if (description == null || description.length == 0) {
            description = new String[] {
                // #tr Tooltips.AdvancedExecutionCore.01
                // # Add an execution core to your modularized machine that uses direct wireless EU energy.
                // #zh_CN 为你的模块化机器添加一颗直接使用无线EU能源的执行核心.
                "Add an execution core that uses direct wireless EU energy.",
                // #tr Tooltips.AdvancedExecutionCore.02
                // # This execution core uses the machine's mechanical and energy parameters for recipe matching and execution.
                // #zh_CN 此执行核心使用机器的机制和能源参数进行配方匹配和执行.
                "Uses the machine's parameters for recipe matching and execution.",
                // #tr Tooltips.AdvancedExecutionCore.03
                // # But it does not consume the energy of the machine, but directly uses the wireless EU energy.
                // #zh_CN 但不消耗机器的能源, 而是直接使用无线EU能源.
                "Does not consume the machine's energy, but directly uses wireless EU energy.",
            };
        }
        return description;
    }
    // spotless:on
}
