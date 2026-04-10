package com.andgatech.AHTech.common.modularizedMachine.modularHatches.executionCore;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import com.andgatech.AHTech.common.modularizedMachine.ModularHatchType;
import com.andgatech.AHTech.common.modularizedMachine.ModularizedMachineBase;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.IStaticModularHatch;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.ModularHatchBase;

import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

public abstract class ExecutionCoreBase extends ModularHatchBase implements IExecutionCore, IStaticModularHatch {

    public ExecutionCoreBase(int aID, String aName, String aNameRegional, int aTier) {
        super(aID, aName, aNameRegional, aTier, 0, null);
    }

    public ExecutionCoreBase(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, 0, aDescription, aTextures);
    }

    @Override
    public ModularHatchType getType() {
        return ModularHatchType.EXECUTION_CORE;
    }

    // region Logic

    protected ItemStack[] outputItems;
    protected FluidStack[] outputFluids;
    protected int maxProgressingTime;
    protected int progressedTime;
    protected int boostedTime;
    protected long eut;
    protected boolean hasBeenSetup = false;
    protected boolean active = false;
    protected byte trySetInactiveTimes = 0;

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        super.onPostTick(aBaseMetaTileEntity, aTick);
        if (aBaseMetaTileEntity.isServerSide()) {
            runExecutionCoreTick(aBaseMetaTileEntity, aTick);
            if (maxProgressingTime <= 0 && active) {
                trySetInactive();
            }
        }
    }

    public void runExecutionCoreTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        if (aBaseMetaTileEntity.isServerSide()) {
            if (hasBeenSetup) {
                if (maxProgressingTime > 0) {
                    if (progressedTime < maxProgressingTime - 1) {
                        progressedTime++;
                    } else {
                        // output and finish this work
                        resetParameters();
                    }
                }
            }
        }
    }

    public IExecutionCore boostTick(int tick) {
        progressedTime += tick;
        boostedTime += tick;
        return this;
    }

    public int getNeedProgressingTime() {
        return maxProgressingTime - progressedTime;
    }

    @Override
    public boolean isIdle() {
        return hasBeenSetup && this.maxProgressingTime < 1;
    }

    @Override
    public boolean isWorking() {
        return hasBeenSetup && this.maxProgressingTime > 0;
    }

    public void shutDown() {
        outputItems = null;
        outputFluids = null;
        maxProgressingTime = 0;
        progressedTime = 0;
        eut = 0;
        setInactiveCritical();
    }

    public void resetParameters() {
        maxProgressingTime = 0;
        progressedTime = 0;
        boostedTime = 0;
        eut = 0;
        trySetInactive();
    }

    public void reset() {
        outputItems = null;
        outputFluids = null;
        maxProgressingTime = 0;
        progressedTime = 0;
        eut = 0;
        hasBeenSetup = false;
        setInactiveCritical();
    }

    public void setActive(boolean active) {
        this.active = active;
        IGregTechTileEntity mte = getBaseMetaTileEntity();
        if (mte != null) mte.setActive(active);
    }

    public void trySetActive() {
        trySetInactiveTimes = 0;
        setActive(true);
    }

    public void trySetInactive() {
        if (trySetInactiveTimes > 2) {
            trySetInactiveTimes = 0;
            setActive(false);
        } else {
            trySetInactiveTimes++;
        }
    }

    public void setInactiveCritical() {
        trySetInactiveTimes = 0;
        setActive(false);
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setBoolean("MH_active", active);
        aNBT.setByte("trySetInactiveTimes", trySetInactiveTimes);
        aNBT.setInteger("maxProgressingTime", maxProgressingTime);
        aNBT.setInteger("progressedTime", progressedTime);
        aNBT.setInteger("boostedTime", boostedTime);
        aNBT.setLong("eut", eut);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        active = aNBT.getBoolean("MH_active");
        trySetInactiveTimes = aNBT.getByte("trySetInactiveTimes");
        maxProgressingTime = aNBT.getInteger("maxProgressingTime");
        progressedTime = aNBT.getInteger("progressedTime");
        boostedTime = aNBT.getInteger("boostedTime");
        eut = aNBT.getLong("eut");
    }

    // region Getter and Setter

    public ItemStack[] getOutputItems() {
        return outputItems;
    }

    public ExecutionCoreBase setOutputItems(ItemStack[] outputItems) {
        this.outputItems = outputItems;
        return this;
    }

    public FluidStack[] getOutputFluids() {
        return outputFluids;
    }

    public ExecutionCoreBase setOutputFluids(FluidStack[] outputFluids) {
        this.outputFluids = outputFluids;
        return this;
    }

    public long getMaxProgressingTime() {
        return maxProgressingTime;
    }

    public ExecutionCoreBase setMaxProgressingTime(int maxProgressingTime) {
        this.maxProgressingTime = maxProgressingTime;
        return this;
    }

    public long getProgressedTime() {
        return progressedTime;
    }

    public long getEut() {
        return eut;
    }

    public ExecutionCoreBase setEut(long eut) {
        this.eut = eut;
        return this;
    }

    public boolean isHasBeenSetup() {
        return hasBeenSetup;
    }

    // endregion

    @Override
    public void onCheckMachine(ModularizedMachineBase<?> machine) {
        // do nothing - execution cores are validated but don't push parameters
    }

    @Override
    public ITexture[] getTexturesActive(ITexture aBaseTexture) {
        return new ITexture[] { aBaseTexture };
    }

    @Override
    public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
        return new ITexture[] { aBaseTexture };
    }
}
