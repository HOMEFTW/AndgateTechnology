package com.andgatech.AHTech.common.modularizedMachine.modularHatches;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import com.andgatech.AHTech.common.modularizedMachine.ModularHatchType;
import com.andgatech.AHTech.common.modularizedMachine.ModularizedMachineBase;

import gregtech.api.interfaces.ITexture;

public abstract class ExecutionCoreBase extends ModularHatchBase implements IStaticModularHatch {

    protected ItemStack[] outputItems;
    protected FluidStack[] outputFluids;
    protected int maxProgressingTime;
    protected int progressedTime;
    protected long eut;

    public ExecutionCoreBase(int aID, String aName, String aNameRegional, int aTier) {
        super(aID, aName, aNameRegional, aTier, 0, null);
    }

    public ExecutionCoreBase(String aName, int aTier, ITexture[][][] aTextures) {
        super(aName, aTier, 0, null, aTextures);
    }

    @Override
    public ModularHatchType getType() {
        return ModularHatchType.EXECUTION_CORE;
    }

    @Override
    public void onCheckMachine(ModularizedMachineBase<?> machine) {
        // Execution cores are validated but don't push parameters
    }

    public abstract boolean done();

    public abstract boolean useMainMachinePower();

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setInteger("maxProgressingTime", maxProgressingTime);
        aNBT.setInteger("progressedTime", progressedTime);
        aNBT.setLong("eut", eut);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        maxProgressingTime = aNBT.getInteger("maxProgressingTime");
        progressedTime = aNBT.getInteger("progressedTime");
        eut = aNBT.getLong("eut");
    }
}
