package com.andgatech.AHTech.common.modularizedMachine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import com.andgatech.AHTech.common.modularizedMachine.modularHatches.IDynamicModularHatch;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.IModularHatch;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.IStaticModularHatch;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.ModularHatchBase;

import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEExtendedPowerMultiBlockBase;
import gregtech.api.recipe.check.CheckRecipeResult;

/**
 * Base class for modularized multiblock machines.
 * <p>
 * Extends {@link MTEExtendedPowerMultiBlockBase} and implements {@link IModularizedMachine} to provide
 * a modular hatch system where hatches can contribute static settings (during structure check) and
 * dynamic parameters (during recipe processing).
 *
 * @param <T> the concrete type of this machine, for self-referencing generics
 */
public abstract class ModularizedMachineBase<T extends ModularizedMachineBase<T>>
    extends MTEExtendedPowerMultiBlockBase<T> implements IModularizedMachine {

    protected Map<ModularHatchType, Collection<IModularHatch>> modularHatches = new HashMap<>();
    protected Map<ModularHatchType, Collection<IStaticModularHatch>> staticModularHatches = new HashMap<>();
    protected Map<ModularHatchType, Collection<IDynamicModularHatch>> dynamicModularHatches = new HashMap<>();
    protected Collection<IModularHatch> allModularHatches = new ArrayList<>();

    protected ModularizedMachineBase(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    protected ModularizedMachineBase(String aName) {
        super(aName);
    }

    // region Structure Tier

    /**
     * Returns the structure tier of this machine. Subclasses must implement this to report
     * the tier determined during structure check (e.g., based on casing blocks).
     *
     * @return the structure tier (0 = none, 1+ = valid tiers)
     */
    public abstract int getStructureTier();

    // endregion

    // region Supported Modular Hatch Types

    /**
     * Returns the set of modular hatch types this machine supports.
     * Machines that support all types should return a collection containing {@link ModularHatchType#ALL}.
     */
    @Override
    public abstract Collection<ModularHatchType> getSupportedModularHatchTypes();

    // endregion

    // region Abstract Methods for Subclass Customization

    /**
     * Subclass-specific structure check logic. Called during {@link #checkMachine} after
     * collections are reset. Implementations should call checkPiece() or similar.
     *
     * @return true if the structure is valid
     */
    protected abstract boolean checkMachineMM(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack);

    /**
     * Whether this machine allows multiple hatches of the same type.
     * If false, {@link #checkSingleModularHatch()} will enforce at most one of each controller type.
     */
    protected abstract boolean canMultiplyModularHatchType();

    /**
     * Subclass-specific processing check logic. Called during {@link #checkProcessing}
     * after dynamic parameters have been applied.
     *
     * @return the recipe check result
     */
    @Nonnull
    protected abstract CheckRecipeResult checkProcessingMM();

    // endregion

    // region Add to Machine List

    /**
     * Overrides the GT base method to handle both standard hatches and modular hatches.
     * First tries to add as a standard hatch via super, then falls back to modular hatch handling.
     */
    @Override
    public boolean addToMachineList(IGregTechTileEntity aTileEntity, int aBaseCasingIndex) {
        boolean added = super.addToMachineList(aTileEntity, aBaseCasingIndex);
        if (!added) {
            added = addAnyModularHatchToMachineList(aTileEntity, aBaseCasingIndex);
        }
        return added;
    }

    /**
     * Attempts to add a tile entity as a modular hatch if it is one.
     *
     * @param aTileEntity      the tile entity to check
     * @param aBaseCasingIndex the casing texture index
     * @return true if the hatch was added as a modular hatch
     */
    protected boolean addAnyModularHatchToMachineList(IGregTechTileEntity aTileEntity, int aBaseCasingIndex) {
        if (aTileEntity == null) return false;
        var aMetaTileEntity = aTileEntity.getMetaTileEntity();
        if (!(aMetaTileEntity instanceof ModularHatchBase modularHatch)) return false;

        ModularHatchType type = modularHatch.getType();
        if (!getSupportedModularHatchTypes().contains(type)) return false;

        // Update texture like standard hatches
        modularHatch.updateTexture(aBaseCasingIndex);
        modularHatch.updateCraftingIcon(this.getMachineCraftingIcon());

        // Add to the main modularHatches map
        modularHatches.computeIfAbsent(type, k -> new ArrayList<>())
            .add(modularHatch);

        // Add to the flat collection of all modular hatches
        allModularHatches.add(modularHatch);

        // Add to static or dynamic sub-collections based on interface
        if (modularHatch instanceof IStaticModularHatch staticHatch) {
            staticModularHatches.computeIfAbsent(type, k -> new ArrayList<>())
                .add(staticHatch);
        }
        if (modularHatch instanceof IDynamicModularHatch dynamicHatch) {
            dynamicModularHatches.computeIfAbsent(type, k -> new ArrayList<>())
                .add(dynamicHatch);
        }

        return true;
    }

    // endregion

    // region Check Machine

    /**
     * Checks the multiblock structure and applies modular static settings.
     * <ol>
     * <li>Repairs the machine</li>
     * <li>Resets all modular hatch collections</li>
     * <li>Runs subclass structure check via {@link #checkMachineMM}</li>
     * <li>Validates modular hatch counts if not allowing multiples</li>
     * <li>Applies modular static settings</li>
     * </ol>
     */
    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        fixAllIssues();
        resetModularHatchCollections();
        if (!checkMachineMM(aBaseMetaTileEntity, aStack)) return false;
        if (!canMultiplyModularHatchType()) {
            if (!checkSingleModularHatch()) return false;
        }
        checkModularStaticSettings();
        return true;
    }

    // endregion

    // region Check Processing

    /**
     * Checks processing by first applying dynamic modular parameters, then delegating
     * to the subclass processing logic.
     */
    @Nonnull
    @Override
    public CheckRecipeResult checkProcessing() {
        checkModularDynamicParameters();
        return checkProcessingMM();
    }

    // endregion

    // region Modular Static Settings

    @Override
    public void applyModularStaticSettings() {
        for (Collection<IStaticModularHatch> hatches : staticModularHatches.values()) {
            for (IStaticModularHatch hatch : hatches) {
                hatch.onCheckMachine(this);
            }
        }
    }

    // endregion

    // region Modular Dynamic Parameters

    @Override
    public void applyModularDynamicParameters() {
        for (Collection<IDynamicModularHatch> hatches : dynamicModularHatches.values()) {
            for (IDynamicModularHatch hatch : hatches) {
                hatch.onCheckProcessing(this);
            }
        }
    }

    // endregion

    // region Collection Management

    @Override
    public void resetModularHatchCollections() {
        modularHatches.clear();
        staticModularHatches.clear();
        dynamicModularHatches.clear();
        allModularHatches.clear();
    }

    @Override
    public Collection<IModularHatch> getAllModularHatches() {
        return allModularHatches;
    }

    // endregion

    // region Single Hatch Validation

    /**
     * Checks that each controller type has at most one hatch installed.
     * This is called when {@link #canMultiplyModularHatchType()} returns false.
     *
     * @return true if all types have at most one hatch
     */
    protected boolean checkSingleModularHatch() {
        ModularHatchType[] singleTypes = { ModularHatchType.PARALLEL_CONTROLLER, ModularHatchType.SPEED_CONTROLLER,
            ModularHatchType.OVERCLOCK_CONTROLLER, ModularHatchType.POWER_CONSUMPTION_CONTROLLER,
            ModularHatchType.RECOVERY_RATE_CONTROLLER, ModularHatchType.FUNCTION_MODULE };

        for (ModularHatchType type : singleTypes) {
            Collection<IModularHatch> hatches = modularHatches.get(type);
            if (hatches != null && hatches.size() > 1) {
                return false;
            }
        }
        return true;
    }

    // endregion
}
