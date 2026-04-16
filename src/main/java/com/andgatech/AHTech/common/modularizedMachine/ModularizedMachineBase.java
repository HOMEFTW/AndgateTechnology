package com.andgatech.AHTech.common.modularizedMachine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.andgatech.AHTech.common.modularizedMachine.modularHatches.IDynamicModularHatch;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.IModularHatch;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.IStaticModularHatch;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.ModularHatchBase;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.executionCore.ExecutionCoreBase;

import cpw.mods.fml.common.Loader;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEExtendedPowerMultiBlockBase;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.util.shutdown.ShutDownReason;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;

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

    /**
     * Stores discovered TST modular hatches. Only used when TST is installed.
     * TST hatches do not implement AHTech's IStaticModularHatch, so they are handled
     * separately through an adapter pattern.
     */
    protected final List<Object> tstModularHatches = new ArrayList<>();

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
     * First checks AHTech modular hatches, then falls back to TST modular hatches if TST is loaded.
     *
     * @param aTileEntity      the tile entity to check
     * @param aBaseCasingIndex the casing texture index
     * @return true if the hatch was added as a modular hatch
     */
    protected boolean addAnyModularHatchToMachineList(IGregTechTileEntity aTileEntity, int aBaseCasingIndex) {
        if (aTileEntity == null) return false;
        var aMetaTileEntity = aTileEntity.getMetaTileEntity();

        // Try AHTech modular hatches first
        if (aMetaTileEntity instanceof ModularHatchBase modularHatch) {
            ModularHatchType type = modularHatch.getType();
            Collection<ModularHatchType> supportedTypes = getSupportedModularHatchTypes();
            if (!supportedTypes.contains(ModularHatchType.ALL) && !supportedTypes.contains(type)) return false;

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

        // Try TST modular hatches if TST is loaded
        if (isTSTLoaded()) {
            return addTSTModularHatchToMachineList(aTileEntity, aBaseCasingIndex);
        }

        return false;
    }

    // endregion

    // region TST Compatibility

    /**
     * Checks if Twist Space Technology (TST) mod is loaded at runtime.
     * All TST class references must be guarded by this check.
     */
    protected static boolean isTSTLoaded() {
        return isTSTLoaded(Loader::isModLoaded);
    }

    public static boolean isTSTLoaded(Predicate<String> modLoadedPredicate) {
        return modLoadedPredicate.test("TwistSpaceTechnology");
    }

    /**
     * Attempts to add a TST modular hatch to the machine's TST hatch list.
     * TST hatches are stored separately because they don't implement AHTech's
     * {@link IStaticModularHatch} interface (different onCheckMachine parameter types).
     *
     * @param aTileEntity      the tile entity to check
     * @param aBaseCasingIndex the casing texture index
     * @return true if the hatch was recognized as a TST modular hatch and added
     */
    protected boolean addTSTModularHatchToMachineList(IGregTechTileEntity aTileEntity, int aBaseCasingIndex) {
        if (aTileEntity == null) return false;
        IMetaTileEntity mte = aTileEntity.getMetaTileEntity();
        if (mte == null) return false;

        // Check if this is a TST ModularHatchBase (all TST module hatches extend this)
        if (mte instanceof com.Nxer.TwistSpaceTechnology.common.modularizedMachine.modularHatches.ModularHatchBase) {
            tstModularHatches.add(mte);
            return true;
        }
        return false;
    }

    /**
     * Applies the effects of all discovered TST modular hatches to this machine.
     * Called after {@link #applyModularStaticSettings()} so that TST hatches
     * can stack with any AHTech hatches already applied.
     */
    protected void applyTSTModularHatches() {
        if (tstModularHatches.isEmpty()) return;
        for (Object hatch : tstModularHatches) {
            applyTSTHatchEffect(hatch);
        }
    }

    /**
     * Applies the effect of a single TST modular hatch by checking its concrete type
     * and calling the corresponding AHTech support interface method.
     * Uses instanceof checks against TST base classes (guarded by compileOnly dependency).
     */
    protected void applyTSTHatchEffect(Object hatch) {
        // Parallel Controller
        if (hatch instanceof com.Nxer.TwistSpaceTechnology.common.modularizedMachine.modularHatches.ParallelControllers.StaticParallelControllerBase spc) {
            if (this instanceof ISupportParallelController ctrl) {
                int current = ctrl.getStaticParallelParameterValue();
                if (current == Integer.MAX_VALUE) return;
                int toAdd = spc.getParallel();
                if (current >= Integer.MAX_VALUE - toAdd) {
                    ctrl.setStaticParallelParameter(Integer.MAX_VALUE);
                } else {
                    ctrl.setStaticParallelParameter(current + toAdd);
                }
            }
        }
        // Speed Controller
        else if (hatch instanceof com.Nxer.TwistSpaceTechnology.common.modularizedMachine.modularHatches.SpeedConstrollers.StaticSpeedControllerBase ssc) {
            if (this instanceof ISupportSpeedController ctrl) {
                float current = ctrl.getStaticSpeedParameterValue();
                float speedBonus = ssc.getSpeedBonus();
                if (current > 0 && speedBonus > 0) {
                    ctrl.setStaticSpeedParameterValue(current * speedBonus);
                }
            }
        }
        // Overclock Controller
        else if (hatch instanceof com.Nxer.TwistSpaceTechnology.common.modularizedMachine.modularHatches.OverclockControllers.StaticOverclockControllerBase soc) {
            if (this instanceof ISupportOverclockController ctrl) {
                com.Nxer.TwistSpaceTechnology.common.misc.OverclockType tstType = soc.getOverclockType();
                // Convert TST OverclockType to AHTech OverclockType by matching timeReduction/powerIncrease values
                OverclockType ahtechType = OverclockType
                    .checkOverclockType(tstType.timeReduction, tstType.powerIncrease);
                ctrl.setOverclockType(ahtechType);
            }
        }
        // Power Consumption Controller
        else if (hatch instanceof com.Nxer.TwistSpaceTechnology.common.modularizedMachine.modularHatches.PowerConsumptionControllers.StaticPowerConsumptionControllerBase spcc) {
            if (this instanceof ISupportPowerConsumptionController ctrl) {
                float current = ctrl.getStaticPowerConsumptionParameterValue();
                float multiplier = spcc.getPowerConsumptionMultiplier();
                if (current > 0 && multiplier > 0) {
                    ctrl.setStaticPowerConsumptionParameterValue(current * multiplier);
                }
            }
        }
        // Execution Core - TST execution cores need deeper integration (shared progress management),
        // so for now we only discover and record them.
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
        CheckRecipeResult result = checkProcessingMM();
        if (result.wasSuccessful()) {
            offloadCurrentRecipeIfPossible();
        }
        return result;
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

    /**
     * Overrides the default to also apply TST modular hatch effects after AHTech static settings.
     */
    @Override
    public void checkModularStaticSettings() {
        resetModularStaticSettings();
        applyModularStaticSettings();
        applyTSTModularHatches();
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
        resetExecutionCores();
        modularHatches.clear();
        staticModularHatches.clear();
        dynamicModularHatches.clear();
        allModularHatches.clear();
        tstModularHatches.clear();
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

    // region Execution Core Integration

    protected List<ExecutionCoreBase> getExecutionCores() {
        List<ExecutionCoreBase> executionCores = new ArrayList<>();
        Collection<IModularHatch> registeredCores = modularHatches.get(ModularHatchType.EXECUTION_CORE);
        if (registeredCores == null) {
            return executionCores;
        }

        for (IModularHatch hatch : registeredCores) {
            if (hatch instanceof ExecutionCoreBase executionCore) {
                executionCores.add(executionCore);
            }
        }
        return executionCores;
    }

    protected void offloadCurrentRecipeIfPossible() {
        if (mMaxProgresstime <= 0) {
            return;
        }

        for (ExecutionCoreBase executionCore : getExecutionCores()) {
            if (executionCore.isIdle() && offloadCurrentRecipeToExecutionCore(executionCore)) {
                return;
            }
        }
    }

    protected boolean offloadCurrentRecipeToExecutionCore(ExecutionCoreBase executionCore) {
        if ((executionCore == null) || (mMaxProgresstime <= 0)) {
            return false;
        }
        if (!executionCore.isHasBeenSetup() && !executionCore.setup(this)) {
            return false;
        }

        executionCore.setOutputItems(mOutputItems)
            .setOutputFluids(mOutputFluids)
            .setMaxProgressingTime(mMaxProgresstime)
            .setEut(Math.abs(lEUt));

        if (!executionCore.done()) {
            return false;
        }

        mOutputItems = null;
        mOutputFluids = null;
        mProgresstime = 0;
        mMaxProgresstime = 0;
        mEUt = 0;
        lEUt = 0;
        return true;
    }

    public void mergeOutputItems(ItemStack[] outputs) {
        if ((outputs == null) || (outputs.length == 0)) {
            return;
        }
        addItemOutputs(outputs);
    }

    public void mergeOutputFluids(FluidStack[] outputs) {
        if ((outputs == null) || (outputs.length == 0)) {
            return;
        }
        addFluidOutputs(outputs);
    }

    protected boolean hasWorkingExecutionCore() {
        for (ExecutionCoreBase executionCore : getExecutionCores()) {
            if (executionCore.isWorking()) {
                return true;
            }
        }
        return false;
    }

    protected long getExecutionCoreMainPowerUsage() {
        long totalUsage = 0;
        for (ExecutionCoreBase executionCore : getExecutionCores()) {
            if (executionCore.isWorking() && executionCore.useMainMachinePower()) {
                totalUsage += executionCore.getEut();
            }
        }
        return totalUsage;
    }

    protected long getActualExecutionCoreEnergyUsage() {
        long executionCoreUsage = getExecutionCoreMainPowerUsage();
        if (executionCoreUsage <= 0) {
            return 0;
        }
        return (long) (executionCoreUsage * (10000.0 / Math.max(1000, mEfficiency)));
    }

    protected void resetExecutionCores() {
        for (ExecutionCoreBase executionCore : getExecutionCores()) {
            executionCore.reset();
        }
    }

    protected void shutDownExecutionCores() {
        for (ExecutionCoreBase executionCore : getExecutionCores()) {
            executionCore.shutDown();
        }
    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        super.onPostTick(aBaseMetaTileEntity, aTick);
        if (!aBaseMetaTileEntity.isServerSide()) {
            return;
        }

        long executionCoreEnergyUsage = getActualExecutionCoreEnergyUsage();
        if ((executionCoreEnergyUsage > 0) && !drainEnergyInput(executionCoreEnergyUsage)) {
            shutDownExecutionCores();
            stopMachine(ShutDownReasonRegistry.POWER_LOSS);
            return;
        }

        if ((mMaxProgresstime <= 0) && hasWorkingExecutionCore()) {
            aBaseMetaTileEntity.setActive(true);
        }
    }

    @Override
    public void stopMachine(@Nonnull ShutDownReason reason) {
        shutDownExecutionCores();
        super.stopMachine(reason);
    }

    // endregion
}
