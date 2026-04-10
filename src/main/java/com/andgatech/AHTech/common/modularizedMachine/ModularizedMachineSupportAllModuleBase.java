package com.andgatech.AHTech.common.modularizedMachine;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraft.nbt.NBTTagCompound;

import com.andgatech.AHTech.common.modularizedMachine.modularHatches.IModularHatch;
import com.andgatech.AHTech.config.Config;
import com.google.common.collect.ImmutableList;

import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;

/**
 * Extended modularized machine base that supports all modular hatch types.
 * <p>
 * Implements {@link ISupportAllModularHatches} which aggregates all ISupport* interfaces,
 * providing fields and getters/setters for parallel, speed, overclock, power consumption,
 * recovery rate, and function modules.
 *
 * @param <T> the concrete type of this machine
 */
public abstract class ModularizedMachineSupportAllModuleBase<T extends ModularizedMachineSupportAllModuleBase<T>>
    extends ModularizedMachineBase<T> implements ISupportAllModularHatches {

    // region Modular Fields

    public OverclockType overclockType = OverclockType.NormalOverclock;
    public int staticParallel = 0;
    public int dynamicParallel = 0;
    public float staticPowerConsumptionMultiplier = 1;
    public float staticSpeedBonus = 1;
    public float dynamicSpeedBonus = 1;
    public float recoveryRate = 0;
    public final Set<FunctionType> installedFunctionTypes = new HashSet<>();

    // endregion

    protected ModularizedMachineSupportAllModuleBase(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    protected ModularizedMachineSupportAllModuleBase(String aName) {
        super(aName);
    }

    // region Supported Modular Hatch Types

    @Override
    public Collection<ModularHatchType> getSupportedModularHatchTypes() {
        return ImmutableList.of(ModularHatchType.ALL);
    }

    // endregion

    // region Reset Static Settings

    @Override
    public void resetModularStaticSettings() {
        staticParallel = 0;
        staticSpeedBonus = 1;
        staticPowerConsumptionMultiplier = 1;
        overclockType = OverclockType.NormalOverclock;
        recoveryRate = 0;
        installedFunctionTypes.clear();
    }

    // endregion

    // region Reset Dynamic Parameters

    @Override
    public void resetModularDynamicParameters() {
        dynamicParallel = 0;
        dynamicSpeedBonus = 1;
    }

    // endregion

    // region GT Framework Integration

    /**
     * Returns the max parallel recipes. Combines static and dynamic parallel values plus 1 base.
     * Handles integer overflow by capping at Integer.MAX_VALUE.
     */
    public int getMaxParallelRecipes() {
        long total = (long) dynamicParallel + staticParallel + 1;
        return total > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) total;
    }

    /**
     * Returns the speed bonus multiplier. Combines static and dynamic speed values.
     */
    public float getSpeedBonus() {
        return staticSpeedBonus * dynamicSpeedBonus;
    }

    /**
     * Returns the EU modifier (power consumption multiplier).
     */
    public float getEuModifier() {
        return staticPowerConsumptionMultiplier;
    }

    /**
     * Returns whether perfect overclock is enabled based on the current overclock type.
     */
    public boolean isEnablePerfectOverclock() {
        return overclockType.isPerfectOverclock();
    }

    // endregion

    // region Recovery Rate (with Stage 1 Special Case)

    /**
     * Returns the recovery rate. For structure tier 1, uses the config base rate.
     * For other tiers, returns the rate set by modular hatches.
     */
    @Override
    public float getRecoveryRate() {
        if (getStructureTier() == 1) return (float) Config.Stage1_BaseRecoveryRate;
        return recoveryRate;
    }

    @Override
    public void setRecoveryRate(float value) {
        this.recoveryRate = value;
    }

    // endregion

    // region Function Module Support

    @Override
    public Set<FunctionType> getInstalledFunctionTypes() {
        return installedFunctionTypes;
    }

    @Override
    public boolean hasFunction(FunctionType type) {
        return installedFunctionTypes.contains(type);
    }

    @Override
    public void addFunctionType(FunctionType type) {
        installedFunctionTypes.add(type);
    }

    // endregion

    // region ISupportParallelController

    @Override
    public int getStaticParallelParameterValue() {
        return staticParallel;
    }

    @Override
    public void setStaticParallelParameter(int value) {
        this.staticParallel = value;
    }

    @Override
    public int getDynamicParallelParameterValue() {
        return dynamicParallel;
    }

    @Override
    public void setDynamicParallelParameter(int value) {
        this.dynamicParallel = value;
    }

    // endregion

    // region ISupportSpeedController

    @Override
    public float getStaticSpeedParameterValue() {
        return staticSpeedBonus;
    }

    @Override
    public void setStaticSpeedParameterValue(float value) {
        this.staticSpeedBonus = value;
    }

    @Override
    public float getDynamicSpeedParameterValue() {
        return dynamicSpeedBonus;
    }

    @Override
    public void setDynamicSpeedParameterValue(float value) {
        this.dynamicSpeedBonus = value;
    }

    // endregion

    // region ISupportOverclockController

    @Override
    public void setOverclockType(OverclockType type) {
        this.overclockType = type;
    }

    @Override
    public OverclockType getOverclockType() {
        return overclockType;
    }

    // endregion

    // region ISupportPowerConsumptionController

    @Override
    public float getStaticPowerConsumptionParameterValue() {
        return staticPowerConsumptionMultiplier;
    }

    @Override
    public void setStaticPowerConsumptionParameterValue(float value) {
        this.staticPowerConsumptionMultiplier = value;
    }

    // endregion

    // region Abstract Methods

    @Override
    public abstract int getStructureTier();

    // endregion

    // region NBT

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setInteger("staticParallel", staticParallel);
        aNBT.setInteger("dynamicParallel", dynamicParallel);
        aNBT.setFloat("staticSpeedBonus", staticSpeedBonus);
        aNBT.setFloat("dynamicSpeedBonus", dynamicSpeedBonus);
        aNBT.setFloat("staticPowerConsumptionMultiplier", staticPowerConsumptionMultiplier);
        aNBT.setFloat("recoveryRate", recoveryRate);
        aNBT.setInteger("overclockType", overclockType.getID());

        // Save function types
        int[] functionTypeIds = installedFunctionTypes.stream()
            .mapToInt(FunctionType::ordinal)
            .toArray();
        aNBT.setIntArray("installedFunctionTypes", functionTypeIds);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        staticParallel = aNBT.getInteger("staticParallel");
        dynamicParallel = aNBT.getInteger("dynamicParallel");
        staticSpeedBonus = aNBT.getFloat("staticSpeedBonus");
        if (staticSpeedBonus <= 0) staticSpeedBonus = 1;
        dynamicSpeedBonus = aNBT.getFloat("dynamicSpeedBonus");
        if (dynamicSpeedBonus <= 0) dynamicSpeedBonus = 1;
        staticPowerConsumptionMultiplier = aNBT.getFloat("staticPowerConsumptionMultiplier");
        if (staticPowerConsumptionMultiplier <= 0) staticPowerConsumptionMultiplier = 1;
        recoveryRate = aNBT.getFloat("recoveryRate");
        if (recoveryRate < 0) recoveryRate = 0;
        overclockType = OverclockType.getFromID(aNBT.getInteger("overclockType"));

        // Load function types
        installedFunctionTypes.clear();
        int[] functionTypeIds = aNBT.getIntArray("installedFunctionTypes");
        FunctionType[] allTypes = FunctionType.values();
        for (int id : functionTypeIds) {
            if (id >= 0 && id < allTypes.length) {
                installedFunctionTypes.add(allTypes[id]);
            }
        }
    }

    // endregion

    // region Check Processing

    @Nonnull
    @Override
    protected CheckRecipeResult checkProcessingMM() {
        // Delegate to the ProcessingLogic-based checkProcessing from GT base
        // If processingLogic exists, use it; otherwise fall back to legacy checkRecipe
        if (this.processingLogic != null) {
            return super.checkProcessing();
        }
        return checkRecipe(mInventory[1]) ? CheckRecipeResultRegistry.SUCCESSFUL : CheckRecipeResultRegistry.NO_RECIPE;
    }

    // endregion

    // region Collection Management Override

    @Override
    public Collection<IModularHatch> getAllModularHatches() {
        return allModularHatches;
    }

    // endregion
}
