package com.andgatech.AHTech.common.supplier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import com.andgatech.AHTech.common.modularizedMachine.ModularHatchType;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.ModularHatchBase;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

public class SupplierHatch extends ModularHatchBase {

    private final SupplierId supplierId;

    public SupplierHatch(int aID, String aName, String aNameRegional, int aTier, SupplierId supplierId) {
        super(aID, aName, aNameRegional, aTier, 0, null);
        this.supplierId = supplierId;
    }

    public SupplierHatch(String aName, int aTier, SupplierId supplierId, ITexture[][][] aTextures) {
        super(aName, aTier, 0, null, aTextures);
        this.supplierId = supplierId;
    }

    public SupplierId getSupplierId() {
        return supplierId;
    }

    @Override
    public ModularHatchType getType() {
        return ModularHatchType.SUPPLIER;
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new SupplierHatch(mName, mTier, supplierId, mTextures);
    }

    @Override
    public ITexture[] getTexturesActive(ITexture aBaseTexture) {
        return new ITexture[] { aBaseTexture };
    }

    @Override
    public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
        return new ITexture[] { aBaseTexture };
    }

    @Override
    public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
        openGui(aPlayer);
        return true;
    }

    @Override
    public void addUIWidgets(ModularWindow.Builder builder, UIBuildContext buildContext) {
        super.addUIWidgets(builder, buildContext);

        int colorRgb = chatColorToRgb(supplierId.getColor());
        int startX = 6;
        int y = 10;

        // 供应商名称（品牌色）
        builder.widget(
            new TextWidget(StatCollector.translateToLocal(supplierId.getNameKey())).setDefaultColor(colorRgb)
                .setPos(startX, y)
                .setSize(164, 12));
        y += 16;

        // 分隔线
        builder.widget(
            new TextWidget(EnumChatFormatting.DARK_GRAY + "---------------------------").setDefaultColor(0x555555)
                .setPos(startX, y)
                .setSize(164, 8));
        y += 12;

        // 标语（灰色斜体风格）
        String tagline = StatCollector.translateToLocal(supplierId.getTaglineKey());
        builder.widget(
            new TextWidget(EnumChatFormatting.ITALIC + tagline).setDefaultColor(0xAAAAAA)
                .setPos(startX, y)
                .setSize(164, 10));
        y += 18;

        // 合同要求
        String contractName = StatCollector.translateToLocal(
            supplierId.getMinimumContractTier()
                .getTranslationKey());
        builder.widget(
            new TextWidget(StatCollector.translateToLocalFormatted("AHTech.Supplier.GUI.ContractLine", contractName))
                .setDefaultColor(0x55FFFF)
                .setPos(startX, y)
                .setSize(164, 10));
    }

    @Override
    public String[] getDescription() {
        return new String[] { supplierId.getColor() + StatCollector.translateToLocal(supplierId.getNameKey()),
            EnumChatFormatting.GRAY + StatCollector.translateToLocal(supplierId.getTaglineKey()) };
    }

    private static int chatColorToRgb(EnumChatFormatting formatting) {
        return switch (formatting) {
            case GOLD -> 0xFFAA00;
            case BLUE -> 0x5555FF;
            case WHITE -> 0xFFFFFF;
            case DARK_PURPLE -> 0xAA00AA;
            case GREEN -> 0x55FF55;
            case YELLOW -> 0xFFFF55;
            case RED -> 0xFF5555;
            default -> 0xFFFFFF;
        };
    }
}
