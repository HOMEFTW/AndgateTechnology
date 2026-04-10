package com.andgatech.AHTech.common.modularizedMachine.modularHatches.parallelController;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.TextWidget;
import com.gtnewhorizons.modularui.common.widget.textfield.TextFieldWidget;

import gregtech.api.gui.modularui.GTUIInfos;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

public class DynamicParallelController extends DynamicParallelControllerBase {

    // region Class Constructor
    public DynamicParallelController(int aID, String aName, String aNameRegional, int aTier, int maxParallel) {
        super(aID, aName, aNameRegional, aTier);
        this.maxParallel = maxParallel;
    }

    public DynamicParallelController(String aName, int aTier, int maxParallel, ITexture[][][] aTextures) {
        super(aName, aTier, aTextures);
        this.maxParallel = maxParallel;
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new DynamicParallelController(this.mName, this.mTier, this.maxParallel, this.mTextures);
    }

    // endregion

    // region Logic
    protected final int maxParallel;
    protected int parallel = 1;

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setInteger("parallel", parallel);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        parallel = aNBT.getInteger("parallel");
    }

    @Override
    public int getMaxParallel() {
        return maxParallel;
    }

    @Override
    public int getParallel() {
        return parallel;
    }

    @Override
    public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
        if (aBaseMetaTileEntity.isClientSide()) {
            return true;
        }
        GTUIInfos.openGTTileEntityUI(aBaseMetaTileEntity, aPlayer);
        return true;
    }

    @Override
    public void addUIWidgets(ModularWindow.Builder builder, UIBuildContext buildContext) {
        builder.widget(
            TextWidget.localised("tst.DynamicParallelController.UI.text.01")
                .setPos(49, 18)
                .setSize(81, 14))
            .widget(
                new TextFieldWidget().setSetterInt(val -> parallel = val)
                    .setGetterInt(() -> parallel)
                    .setNumbers(1, maxParallel)
                    .setOnScrollNumbers(1, 4, 64)
                    .setTextAlignment(Alignment.Center)
                    .setTextColor(Color.WHITE.normal)
                    .setSize(70, 18)
                    .setPos(54, 36)
                    .setBackground(GTUITextures.BACKGROUND_TEXT_FIELD));
    }

    // endregion

    // region General

    // spotless:off
    protected String[] description;

    @Override
    public String[] getDescription() {
        if (description == null || description.length == 0) {
            description = new String[] {
                // #tr Tooltips.DynamicParallelController.01
                // # Parallel controller module with adjustable parameters.
                // #zh_CN 可调参数的并行控制器模块.
                "Parallel controller module with adjustable parameters.",
                // #tr Tooltips.DynamicParallelController.02
                // # Provides up to
                // #zh_CN 最多提供
                "Provides up to " + getMaxParallel() + " Parallel.",
            };
        }
        return description;
    }
    // spotless:on
}
