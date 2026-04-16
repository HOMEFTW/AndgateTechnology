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
    protected ModularizedMachineBase<?> mainMachine;
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
            if (hasBeenSetup && mainMachine != null) {
                if (maxProgressingTime > 0) {
                    if (progressedTime < maxProgressingTime - 1) {
                        progressedTime++;
                    } else {
                        // output and finish this work
                        if (outputItems != null && outputItems.length > 0) {
                            mainMachine.mergeOutputItems(outputItems);
                            outputItems = null;
                        }

                        if (outputFluids != null && outputFluids.length > 0) {
                            mainMachine.mergeOutputFluids(outputFluids);
                            outputFluids = null;
                        }

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
        mainMachine = null;
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
        saveExecutionState(aNBT);
    }

    protected void saveExecutionState(NBTTagCompound aNBT) {
        aNBT.setBoolean("MH_active", active);
        aNBT.setByte("trySetInactiveTimes", trySetInactiveTimes);
        aNBT.setInteger("maxProgressingTime", maxProgressingTime);
        aNBT.setInteger("progressedTime", progressedTime);
        aNBT.setInteger("boostedTime", boostedTime);
        aNBT.setLong("eut", eut);
        saveNBTDataItemStacks(aNBT);
        saveNBTDataFluidStacks(aNBT);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        loadExecutionState(aNBT);
    }

    protected void loadExecutionState(NBTTagCompound aNBT) {
        active = aNBT.getBoolean("MH_active");
        trySetInactiveTimes = aNBT.getByte("trySetInactiveTimes");
        maxProgressingTime = aNBT.getInteger("maxProgressingTime");
        progressedTime = aNBT.getInteger("progressedTime");
        boostedTime = aNBT.getInteger("boostedTime");
        eut = aNBT.getLong("eut");
        loadNBTDataItemStacks(aNBT);
        loadNBTDataFluidStacks(aNBT);
    }

    protected void saveNBTDataItemStacks(NBTTagCompound aNBT) {
        if (outputItems != null && outputItems.length > 0) {
            aNBT.setInteger("outputItemsLength", outputItems.length);
            for (int i = 0; i < outputItems.length; i++) {
                ItemStack stack = outputItems[i];
                if (stack == null) {
                    continue;
                }
                NBTTagCompound stackTag = new NBTTagCompound();
                stack.writeToNBT(stackTag);
                aNBT.setTag("outputItems" + i, stackTag);
            }
        }
    }

    protected void saveNBTDataFluidStacks(NBTTagCompound aNBT) {
        if (outputFluids != null && outputFluids.length > 0) {
            aNBT.setInteger("outputFluidsLength", outputFluids.length);
            for (int i = 0; i < outputFluids.length; i++) {
                FluidStack stack = outputFluids[i];
                if (stack == null) {
                    continue;
                }
                NBTTagCompound stackTag = new NBTTagCompound();
                stack.writeToNBT(stackTag);
                aNBT.setTag("outputFluids" + i, stackTag);
            }
        }
    }

    protected void loadNBTDataItemStacks(NBTTagCompound aNBT) {
        int length = aNBT.getInteger("outputItemsLength");
        if (length > 0) {
            outputItems = new ItemStack[length];
            for (int i = 0; i < length; i++) {
                if (!aNBT.hasKey("outputItems" + i)) {
                    continue;
                }
                outputItems[i] = ItemStack.loadItemStackFromNBT(aNBT.getCompoundTag("outputItems" + i));
            }
        }
    }

    protected void loadNBTDataFluidStacks(NBTTagCompound aNBT) {
        int length = aNBT.getInteger("outputFluidsLength");
        if (length > 0) {
            outputFluids = new FluidStack[length];
            for (int i = 0; i < length; i++) {
                if (!aNBT.hasKey("outputFluids" + i)) {
                    continue;
                }
                outputFluids[i] = FluidStack.loadFluidStackFromNBT(aNBT.getCompoundTag("outputFluids" + i));
            }
        }
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

    public boolean setup(ModularizedMachineBase<?> machine) {
        if ((machine == null) || hasBeenSetup) {
            return false;
        }
        mainMachine = machine;
        hasBeenSetup = true;
        return true;
    }

    // endregion

    @Override
    public void onCheckMachine(ModularizedMachineBase<?> machine) {
        // Execution cores don't push parameters, but still need tier validation.
        // Incompatible cores are silently ignored (present in structure but inactive).
        if (!isCompatibleWithMachine(machine)) {
            return;
        }
        setup(machine);
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
