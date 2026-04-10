package com.andgatech.AHTech.common.modularizedMachine.modularHatches.executionCore;

import static gregtech.common.misc.WirelessNetworkManager.addEUToGlobalEnergyMap;

import java.math.BigInteger;
import java.util.UUID;

import com.andgatech.AHTech.AndgateTechnology;

import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

public class PerfectExecutionCore extends ExecutionCoreBase {

    public PerfectExecutionCore(int aID, String aName, String aNameRegional, int aTier) {
        super(aID, aName, aNameRegional, aTier);
    }

    public PerfectExecutionCore(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, aDescription, aTextures);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new PerfectExecutionCore(this.mName, this.mTier, this.mDescriptionArray, this.mTextures);
    }

    private static final BigInteger NEGATIVE_ONE = BigInteger.valueOf(-1);
    private UUID ownerUUID;
    protected String costEU = "";

    @Override
    public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {
        super.onFirstTick(aBaseMetaTileEntity);
        this.ownerUUID = aBaseMetaTileEntity.getOwnerUuid();
    }

    @Override
    public void resetParameters() {
        super.resetParameters();
        costEU = "";
    }

    @Override
    public boolean done() {
        BigInteger costEUBigInt = BigInteger.valueOf(eut)
            .multiply(BigInteger.valueOf(maxProgressingTime));
        // check wireless EU at this moment
        if (!addEUToGlobalEnergyMap(ownerUUID, costEUBigInt.multiply(NEGATIVE_ONE))) {
            shutDown();
            IGregTechTileEntity mte = getBaseMetaTileEntity();
            AndgateTechnology.LOG.info(
                "Perfect Execution Core shut down because of power at x" + mte
                    .getXCoord() + " y" + mte.getYCoord() + " z" + mte.getZCoord());
            return false;
        }

        this.costEU = costEUBigInt.toString();
        maxProgressingTime = 20; // 1 second

        trySetActive();
        return true;
    }

    @Override
    public boolean useMainMachinePower() {
        return false;
    }

    @Override
    public void saveNBTData(net.minecraft.nbt.NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setString("costEU", costEU);
    }

    @Override
    public void loadNBTData(net.minecraft.nbt.NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        costEU = aNBT.getString("costEU");
    }

    // region General

    // spotless:off
    protected String[] description;

    @Override
    public String[] getDescription() {
        if (description == null || description.length == 0) {
            description = new String[] {
                // #tr Tooltips.PerfectExecutionCore.01
                // # Add a second self to your modularized machine, but more powerful.
                // #zh_CN 为你的模块化机器添加第二个自我, 但更加强大.
                "Add a second self to your modularized machine, but more powerful.",
                // #tr Tooltips.PerfectExecutionCore.02
                // # Directly use wireless EU energy. Any task is completed within 1 second.
                // #zh_CN 直接使用无线EU能源. 任何任务都在 1 秒内完成.
                "Directly uses wireless EU energy. Any task is completed within 1 second.",
            };
        }
        return description;
    }
    // spotless:on
}
