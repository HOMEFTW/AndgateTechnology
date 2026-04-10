package com.andgatech.AHTech.common.modularizedMachine.modularHatches.parallelController;

import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

public class StaticParallelController extends StaticParallelControllerBase {

    public StaticParallelController(int aID, String aName, String aNameRegional, int aTier, int parallel) {
        super(aID, aName, aNameRegional, aTier);
        this.parallel = parallel;
    }

    public StaticParallelController(String aName, int aTier, int parallel, ITexture[][][] aTextures) {
        super(aName, aTier, aTextures);
        this.parallel = parallel;
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new StaticParallelController(this.mName, this.mTier, this.parallel, this.mTextures);
    }

    protected final int parallel;

    @Override
    public int getParallel() {
        return parallel;
    }

    // spotless:off
    protected String[] description;

    @Override
    public String[] getDescription() {
        if (description == null || description.length == 0) {
            description = new String[] {
                // #tr Tooltips.StaticParallelController.01
                // # Parallel controller module with a fixed parameter.
                // #zh_CN 固定参数的并行控制器模块.
                "Parallel controller module with a fixed parameter.",
                // #tr Tooltips.StaticParallelController.02
                // # Provides
                // #zh_CN 提供
                "Provides " + getParallel() + " Parallel.",
            };
        }
        return description;
    }
    // spotless:on
}
