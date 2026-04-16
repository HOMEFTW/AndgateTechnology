package com.andgatech.AHTech.common.modularizedMachine.modularHatches;

import java.util.Collection;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;

import com.andgatech.AHTech.common.currency.CurrencyItem;
import com.andgatech.AHTech.common.currency.CurrencyType;
import com.andgatech.AHTech.common.modularizedMachine.ModularHatchType;
import com.andgatech.AHTech.config.Config;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.FakeSyncWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchInputBus;

public class FinancialHatch extends ModularHatchBase {

    private static final int INVENTORY_SIZE = 9;
    private final int[] syncedCurrencyCounts = new int[CurrencyType.values().length];

    public FinancialHatch(int aID, String aName, String aNameRegional, int aTier) {
        super(aID, aName, aNameRegional, aTier, INVENTORY_SIZE, null);
    }

    public FinancialHatch(String aName, int aTier, ITexture[][][] aTextures) {
        super(aName, aTier, INVENTORY_SIZE, null, aTextures);
    }

    @Override
    public ModularHatchType getType() {
        return ModularHatchType.FINANCIAL;
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new FinancialHatch(mName, mTier, mTextures);
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
    public boolean isValidSlot(int aIndex) {
        return true;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isItemValidForSlot(int aIndex, ItemStack aStack) {
        return CurrencyItem.isCurrency(aStack);
    }

    @Override
    public boolean allowPutStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
        ItemStack aStack) {
        return side == aBaseMetaTileEntity.getFrontFacing() && isItemValidForSlot(aIndex, aStack);
    }

    @Override
    public boolean allowPullStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
        ItemStack aStack) {
        return side == aBaseMetaTileEntity.getFrontFacing();
    }

    public int countCurrency(CurrencyType type) {
        int total = 0;
        for (int i = 0; i < mInventory.length; i++) {
            if (CurrencyItem.getCurrencyType(mInventory[i]) == type) {
                total += mInventory[i].stackSize;
            }
        }
        return total;
    }

    public int consumeCurrency(CurrencyType type, int amount) {
        int remaining = amount;
        for (int i = 0; i < mInventory.length && remaining > 0; i++) {
            ItemStack stack = mInventory[i];
            if (CurrencyItem.getCurrencyType(stack) != type) {
                continue;
            }
            int consumed = Math.min(remaining, stack.stackSize);
            stack.stackSize -= consumed;
            remaining -= consumed;
            if (stack.stackSize <= 0) {
                mInventory[i] = null;
            }
        }
        normalizeInventory();
        return amount - remaining;
    }

    public void autoRefillFromInputBus(Collection<MTEHatchInputBus> inputBuses) {
        if (!Config.EnableAutoRefillFromInputBus) {
            return;
        }
        for (MTEHatchInputBus inputBus : inputBuses) {
            if (inputBus == null) {
                continue;
            }
            for (int slot = 0; slot < inputBus.getSizeInventory(); slot++) {
                ItemStack stack = inputBus.getStackInSlot(slot);
                if (!CurrencyItem.isCurrency(stack)) {
                    continue;
                }
                ItemStack remaining = insertCurrency(stack);
                inputBus.setInventorySlotContents(slot, remaining);
            }
        }
    }

    private ItemStack insertCurrency(ItemStack stack) {
        if (!CurrencyItem.isCurrency(stack)) {
            return stack;
        }
        ItemStack remaining = stack.copy();
        for (int i = 0; i < mInventory.length && remaining.stackSize > 0; i++) {
            ItemStack existing = mInventory[i];
            if (!canStackTogether(existing, remaining)) {
                continue;
            }
            int moved = Math.min(remaining.stackSize, existing.getMaxStackSize() - existing.stackSize);
            if (moved <= 0) {
                continue;
            }
            existing.stackSize += moved;
            remaining.stackSize -= moved;
        }
        for (int i = 0; i < mInventory.length && remaining.stackSize > 0; i++) {
            if (mInventory[i] != null) {
                continue;
            }
            int moved = Math.min(remaining.stackSize, remaining.getMaxStackSize());
            mInventory[i] = new ItemStack(remaining.getItem(), moved, remaining.getItemDamage());
            remaining.stackSize -= moved;
        }
        normalizeInventory();
        return remaining.stackSize > 0 ? remaining : null;
    }

    private boolean canStackTogether(ItemStack left, ItemStack right) {
        return left != null && right != null
            && left.getItem() == right.getItem()
            && left.getItemDamage() == right.getItemDamage()
            && left.stackSize < left.getMaxStackSize();
    }

    private void normalizeInventory() {
        for (int i = 0; i < mInventory.length; i++) {
            if (mInventory[i] != null && mInventory[i].stackSize <= 0) {
                mInventory[i] = null;
            }
        }
    }

    @Override
    public void addUIWidgets(ModularWindow.Builder builder, UIBuildContext buildContext) {
        super.addUIWidgets(builder, buildContext);

        builder.widget(
            new TextWidget(StatCollector.translateToLocal("AHTech.FinancialHatch.Title")).setDefaultColor(0xFFD700)
                .setPos(6, 4)
                .setSize(70, 10));

        CurrencyType[] values = CurrencyType.values();
        for (int i = 0; i < values.length; i++) {
            CurrencyType type = values[i];
            int x = i < 3 ? 6 : 120;
            int y = 18 + ((i % 3) * 12);
            int index = i;
            builder
                .widget(
                    new TextWidget().setStringSupplier(() -> formatCurrencyLine(type, syncedCurrencyCounts[index]))
                        .setDefaultColor(0xE0E0E0)
                        .setPos(x, y)
                        .setSize(50, 10))
                .widget(
                    new FakeSyncWidget.IntegerSyncer(
                        () -> countCurrency(type),
                        val -> syncedCurrencyCounts[index] = val));
        }

        builder.widget(
            new TextWidget()
                .setStringSupplier(
                    () -> StatCollector.translateToLocalFormatted(
                        "AHTech.FinancialHatch.AutoRefill",
                        Config.EnableAutoRefillFromInputBus ? StatCollector.translateToLocal("AHTech.Common.Enabled")
                            : StatCollector.translateToLocal("AHTech.Common.Disabled")))
                .setDefaultColor(0x55FFFF)
                .setPos(6, 56)
                .setSize(160, 10));
    }

    @Override
    public String[] getDescription() {
        return new String[] { StatCollector.translateToLocal("AHTech.FinancialHatch.Description") };
    }

    private String formatCurrencyLine(CurrencyType type, int count) {
        return getCurrencyShortLabel(type) + " x" + count;
    }

    private String getCurrencyShortLabel(CurrencyType type) {
        return switch (type) {
            case COPPER -> "Cu";
            case STEEL -> "St";
            case TITANIUM -> "Ti";
            case PLATINUM -> "Pt";
            case NEUTRONIUM -> "Nt";
            case INFINITY -> "If";
        };
    }
}
