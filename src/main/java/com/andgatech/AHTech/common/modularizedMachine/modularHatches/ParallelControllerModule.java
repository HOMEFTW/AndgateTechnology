package com.andgatech.AHTech.common.modularizedMachine.modularHatches;

import com.andgatech.AHTech.common.modularizedMachine.ISupportParallelController;
import com.andgatech.AHTech.common.modularizedMachine.ModularHatchType;
import com.andgatech.AHTech.common.modularizedMachine.ModularizedMachineBase;

import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

public class ParallelControllerModule extends ModularHatchBase implements IStaticModularHatch {

    protected final int parallel;

    public ParallelControllerModule(int aID, String aName, String aNameRegional, int aTier, int parallel) {
        super(aID, aName, aNameRegional, aTier, 0, null);
        this.parallel = parallel;
    }

    public ParallelControllerModule(String aName, int aTier, int parallel, ITexture[][][] aTextures) {
        super(aName, aTier, 0, null, aTextures);
        this.parallel = parallel;
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new ParallelControllerModule(this.mName, this.mTier, this.parallel, this.mTextures);
    }

    @Override
    public ModularHatchType getType() {
        return ModularHatchType.PARALLEL_CONTROLLER;
    }

    public int getParallel() {
        return parallel;
    }

    @Override
    public void onCheckMachine(ModularizedMachineBase<?> machine) {
        if (isCompatibleWithMachine(machine) && machine instanceof ISupportParallelController ctrl) {
            ctrl.setStaticParallelParameter(ctrl.getStaticParallelParameterValue() + parallel);
        }
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
