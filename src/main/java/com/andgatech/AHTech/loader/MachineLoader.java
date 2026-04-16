package com.andgatech.AHTech.loader;

import java.util.function.Predicate;

import com.andgatech.AHTech.common.ModItemList;
import com.andgatech.AHTech.common.machine.ElectronicsMarket;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.FinancialHatch;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.RecoveryRateModule;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.executionCore.AdvExecutionCore;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.executionCore.ExecutionCore;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.executionCore.PerfectExecutionCore;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.functionModule.GeneralDisassemblyModule;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.overclockController.StaticOverclockController;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.parallelController.DynamicParallelController;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.parallelController.StaticParallelController;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.powerConsumptionController.StaticPowerConsumptionController;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.speedController.DynamicSpeedController;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.speedController.StaticSpeedController;
import com.andgatech.AHTech.common.supplier.SupplierHatch;
import com.andgatech.AHTech.common.supplier.SupplierId;
import com.andgatech.AHTech.config.Config;

import cpw.mods.fml.common.Loader;

/**
 * Register all multiblock machines and single-block machines here.
 * Called during init phase.
 */
public class MachineLoader {

    // Tier constants (GregTech voltage tiers)
    private static final int T1 = 7; // IV
    private static final int T2 = 8; // LuV
    private static final int T3 = 9; // ZPM
    private static final int T4 = 10; // UV
    private static final int T5 = 11; // UHV
    private static final int T6 = 12; // UEV
    private static final int T7 = 13; // UIV
    private static final int T8 = 14; // UMV/MAX

    // Meta ID base for modular hatches
    private static final int MODULAR_BASE = 35050;
    private static final int SUPPLIER_BASE = 35100;
    private static final int FINANCIAL_ID = 35107;
    private static final String TST_MOD_ID = "TwistSpaceTechnology";

    public static void loadMachines() {
        if (Config.Enable_ElectronicsMarket) {
            ModItemList.ElectronicsMarket
                .set(new ElectronicsMarket(35001, "ElectronicsMarket", "Electronics Market").getStackForm(1L));
        }

        if (Config.EnableModularizedMachineSystem) {
            boolean tstLoaded = isTSTLoaded();
            registerModularHatches(tstLoaded);
        }

        if (shouldRegisterSupplierHatches()) {
            registerSupplierHatches(SUPPLIER_BASE);
        }
        if (shouldRegisterFinancialHatch()) {
            ModItemList.FinancialHatch
                .set(new FinancialHatch(FINANCIAL_ID, "FinancialHatch", "Financial Hatch", 0).getStackForm(1L));
        }
    }

    /**
     * Checks if Twist Space Technology (TST) mod is loaded at runtime.
     */
    private static boolean isTSTLoaded() {
        return isTSTLoaded(Loader::isModLoaded);
    }

    static boolean isTSTLoaded(Predicate<String> modPresenceChecker) {
        return modPresenceChecker.test(TST_MOD_ID);
    }

    static boolean shouldRegisterSupplierHatches() {
        return Config.Enable_ElectronicsMarket;
    }

    static boolean shouldRegisterFinancialHatch() {
        return Config.Enable_ElectronicsMarket && Config.EnableFinancialSystem;
    }

    private static void registerModularHatches(boolean tstLoaded) {
        int id = MODULAR_BASE;

        // Standard modules that overlap with TST: only register if TST is NOT installed
        // (parallel, speed, overclock, power consumption, execution core)
        if (!tstLoaded) {
            id = registerStandardModules(id);
        } else {
            // Skip the IDs that would have been used by standard modules
            id = MODULAR_BASE + 46; // 8 static parallel + 8 dynamic parallel + 8 static speed
                                    // + 8 dynamic speed + 8 power consumption + 3 overclock + 3 execution core = 46
        }

        // AHTech-exclusive modules: always register (TST does not have these)
        id = registerAHTechExclusiveModules(id);
    }

    /**
     * Registers standard modular hatches that overlap with TST's modules.
     * Only called when TST is NOT installed.
     */
    private static int registerStandardModules(int id) {
        ModItemList.StaticParallelControllerT1.set(
            new StaticParallelController(
                id++,
                "StaticParallelControllerT1",
                "Static Parallel Controller T1",
                T1,
                Config.ParallelOfParallelController[0]).getStackForm(1L));
        ModItemList.StaticParallelControllerT2.set(
            new StaticParallelController(
                id++,
                "StaticParallelControllerT2",
                "Static Parallel Controller T2",
                T2,
                Config.ParallelOfParallelController[1]).getStackForm(1L));
        ModItemList.StaticParallelControllerT3.set(
            new StaticParallelController(
                id++,
                "StaticParallelControllerT3",
                "Static Parallel Controller T3",
                T3,
                Config.ParallelOfParallelController[2]).getStackForm(1L));
        ModItemList.StaticParallelControllerT4.set(
            new StaticParallelController(
                id++,
                "StaticParallelControllerT4",
                "Static Parallel Controller T4",
                T4,
                Config.ParallelOfParallelController[3]).getStackForm(1L));
        ModItemList.StaticParallelControllerT5.set(
            new StaticParallelController(
                id++,
                "StaticParallelControllerT5",
                "Static Parallel Controller T5",
                T5,
                Config.ParallelOfParallelController[4]).getStackForm(1L));
        ModItemList.StaticParallelControllerT6.set(
            new StaticParallelController(
                id++,
                "StaticParallelControllerT6",
                "Static Parallel Controller T6",
                T6,
                Config.ParallelOfParallelController[5]).getStackForm(1L));
        ModItemList.StaticParallelControllerT7.set(
            new StaticParallelController(
                id++,
                "StaticParallelControllerT7",
                "Static Parallel Controller T7",
                T7,
                Config.ParallelOfParallelController[6]).getStackForm(1L));
        ModItemList.StaticParallelControllerT8.set(
            new StaticParallelController(
                id++,
                "StaticParallelControllerT8",
                "Static Parallel Controller T8",
                T8,
                Config.ParallelOfParallelController[7]).getStackForm(1L));
        // endregion

        // region Dynamic Parallel Controllers (35058-35065)
        ModItemList.DynamicParallelControllerT1.set(
            new DynamicParallelController(
                id++,
                "DynamicParallelControllerT1",
                "Dynamic Parallel Controller T1",
                T1,
                Config.ParallelOfParallelController[0]).getStackForm(1L));
        ModItemList.DynamicParallelControllerT2.set(
            new DynamicParallelController(
                id++,
                "DynamicParallelControllerT2",
                "Dynamic Parallel Controller T2",
                T2,
                Config.ParallelOfParallelController[1]).getStackForm(1L));
        ModItemList.DynamicParallelControllerT3.set(
            new DynamicParallelController(
                id++,
                "DynamicParallelControllerT3",
                "Dynamic Parallel Controller T3",
                T3,
                Config.ParallelOfParallelController[2]).getStackForm(1L));
        ModItemList.DynamicParallelControllerT4.set(
            new DynamicParallelController(
                id++,
                "DynamicParallelControllerT4",
                "Dynamic Parallel Controller T4",
                T4,
                Config.ParallelOfParallelController[3]).getStackForm(1L));
        ModItemList.DynamicParallelControllerT5.set(
            new DynamicParallelController(
                id++,
                "DynamicParallelControllerT5",
                "Dynamic Parallel Controller T5",
                T5,
                Config.ParallelOfParallelController[4]).getStackForm(1L));
        ModItemList.DynamicParallelControllerT6.set(
            new DynamicParallelController(
                id++,
                "DynamicParallelControllerT6",
                "Dynamic Parallel Controller T6",
                T6,
                Config.ParallelOfParallelController[5]).getStackForm(1L));
        ModItemList.DynamicParallelControllerT7.set(
            new DynamicParallelController(
                id++,
                "DynamicParallelControllerT7",
                "Dynamic Parallel Controller T7",
                T7,
                Config.ParallelOfParallelController[6]).getStackForm(1L));
        ModItemList.DynamicParallelControllerT8.set(
            new DynamicParallelController(
                id++,
                "DynamicParallelControllerT8",
                "Dynamic Parallel Controller T8",
                T8,
                Config.ParallelOfParallelController[7]).getStackForm(1L));
        // endregion

        // region Static Speed Controllers (35066-35073)
        ModItemList.StaticSpeedControllerT1.set(
            new StaticSpeedController(
                id++,
                "StaticSpeedControllerT1",
                "Static Speed Controller T1",
                T1,
                Config.SpeedMultiplierOfSpeedController[0]).getStackForm(1L));
        ModItemList.StaticSpeedControllerT2.set(
            new StaticSpeedController(
                id++,
                "StaticSpeedControllerT2",
                "Static Speed Controller T2",
                T2,
                Config.SpeedMultiplierOfSpeedController[1]).getStackForm(1L));
        ModItemList.StaticSpeedControllerT3.set(
            new StaticSpeedController(
                id++,
                "StaticSpeedControllerT3",
                "Static Speed Controller T3",
                T3,
                Config.SpeedMultiplierOfSpeedController[2]).getStackForm(1L));
        ModItemList.StaticSpeedControllerT4.set(
            new StaticSpeedController(
                id++,
                "StaticSpeedControllerT4",
                "Static Speed Controller T4",
                T4,
                Config.SpeedMultiplierOfSpeedController[3]).getStackForm(1L));
        ModItemList.StaticSpeedControllerT5.set(
            new StaticSpeedController(
                id++,
                "StaticSpeedControllerT5",
                "Static Speed Controller T5",
                T5,
                Config.SpeedMultiplierOfSpeedController[4]).getStackForm(1L));
        ModItemList.StaticSpeedControllerT6.set(
            new StaticSpeedController(
                id++,
                "StaticSpeedControllerT6",
                "Static Speed Controller T6",
                T6,
                Config.SpeedMultiplierOfSpeedController[5]).getStackForm(1L));
        ModItemList.StaticSpeedControllerT7.set(
            new StaticSpeedController(
                id++,
                "StaticSpeedControllerT7",
                "Static Speed Controller T7",
                T7,
                Config.SpeedMultiplierOfSpeedController[6]).getStackForm(1L));
        ModItemList.StaticSpeedControllerT8.set(
            new StaticSpeedController(
                id++,
                "StaticSpeedControllerT8",
                "Static Speed Controller T8",
                T8,
                Config.SpeedMultiplierOfSpeedController[7]).getStackForm(1L));
        // endregion

        // region Dynamic Speed Controllers (35074-35081)
        ModItemList.DynamicSpeedControllerT1.set(
            new DynamicSpeedController(
                id++,
                "DynamicSpeedControllerT1",
                "Dynamic Speed Controller T1",
                T1,
                Config.SpeedMultiplierOfSpeedController[0]).getStackForm(1L));
        ModItemList.DynamicSpeedControllerT2.set(
            new DynamicSpeedController(
                id++,
                "DynamicSpeedControllerT2",
                "Dynamic Speed Controller T2",
                T2,
                Config.SpeedMultiplierOfSpeedController[1]).getStackForm(1L));
        ModItemList.DynamicSpeedControllerT3.set(
            new DynamicSpeedController(
                id++,
                "DynamicSpeedControllerT3",
                "Dynamic Speed Controller T3",
                T3,
                Config.SpeedMultiplierOfSpeedController[2]).getStackForm(1L));
        ModItemList.DynamicSpeedControllerT4.set(
            new DynamicSpeedController(
                id++,
                "DynamicSpeedControllerT4",
                "Dynamic Speed Controller T4",
                T4,
                Config.SpeedMultiplierOfSpeedController[3]).getStackForm(1L));
        ModItemList.DynamicSpeedControllerT5.set(
            new DynamicSpeedController(
                id++,
                "DynamicSpeedControllerT5",
                "Dynamic Speed Controller T5",
                T5,
                Config.SpeedMultiplierOfSpeedController[4]).getStackForm(1L));
        ModItemList.DynamicSpeedControllerT6.set(
            new DynamicSpeedController(
                id++,
                "DynamicSpeedControllerT6",
                "Dynamic Speed Controller T6",
                T6,
                Config.SpeedMultiplierOfSpeedController[5]).getStackForm(1L));
        ModItemList.DynamicSpeedControllerT7.set(
            new DynamicSpeedController(
                id++,
                "DynamicSpeedControllerT7",
                "Dynamic Speed Controller T7",
                T7,
                Config.SpeedMultiplierOfSpeedController[6]).getStackForm(1L));
        ModItemList.DynamicSpeedControllerT8.set(
            new DynamicSpeedController(
                id++,
                "DynamicSpeedControllerT8",
                "Dynamic Speed Controller T8",
                T8,
                Config.SpeedMultiplierOfSpeedController[7]).getStackForm(1L));
        // endregion

        // region Power Consumption Controllers (35082-35089)
        ModItemList.StaticPowerConsumptionControllerT1.set(
            new StaticPowerConsumptionController(
                id++,
                "StaticPowerConsumptionControllerT1",
                "Static Power Consumption Controller T1",
                T1,
                (float) Config.PowerConsumptionMultiplierOfPowerConsumptionController[0]).getStackForm(1L));
        ModItemList.StaticPowerConsumptionControllerT2.set(
            new StaticPowerConsumptionController(
                id++,
                "StaticPowerConsumptionControllerT2",
                "Static Power Consumption Controller T2",
                T2,
                (float) Config.PowerConsumptionMultiplierOfPowerConsumptionController[1]).getStackForm(1L));
        ModItemList.StaticPowerConsumptionControllerT3.set(
            new StaticPowerConsumptionController(
                id++,
                "StaticPowerConsumptionControllerT3",
                "Static Power Consumption Controller T3",
                T3,
                (float) Config.PowerConsumptionMultiplierOfPowerConsumptionController[2]).getStackForm(1L));
        ModItemList.StaticPowerConsumptionControllerT4.set(
            new StaticPowerConsumptionController(
                id++,
                "StaticPowerConsumptionControllerT4",
                "Static Power Consumption Controller T4",
                T4,
                (float) Config.PowerConsumptionMultiplierOfPowerConsumptionController[3]).getStackForm(1L));
        ModItemList.StaticPowerConsumptionControllerT5.set(
            new StaticPowerConsumptionController(
                id++,
                "StaticPowerConsumptionControllerT5",
                "Static Power Consumption Controller T5",
                T5,
                (float) Config.PowerConsumptionMultiplierOfPowerConsumptionController[4]).getStackForm(1L));
        ModItemList.StaticPowerConsumptionControllerT6.set(
            new StaticPowerConsumptionController(
                id++,
                "StaticPowerConsumptionControllerT6",
                "Static Power Consumption Controller T6",
                T6,
                (float) Config.PowerConsumptionMultiplierOfPowerConsumptionController[5]).getStackForm(1L));
        ModItemList.StaticPowerConsumptionControllerT7.set(
            new StaticPowerConsumptionController(
                id++,
                "StaticPowerConsumptionControllerT7",
                "Static Power Consumption Controller T7",
                T7,
                (float) Config.PowerConsumptionMultiplierOfPowerConsumptionController[6]).getStackForm(1L));
        ModItemList.StaticPowerConsumptionControllerT8.set(
            new StaticPowerConsumptionController(
                id++,
                "StaticPowerConsumptionControllerT8",
                "Static Power Consumption Controller T8",
                T8,
                (float) Config.PowerConsumptionMultiplierOfPowerConsumptionController[7]).getStackForm(1L));
        // endregion

        // region Overclock Controllers (35090-35092)
        ModItemList.LowSpeedPerfectOverclockController.set(
            new StaticOverclockController(
                id++,
                "LowSpeedPerfectOverclockController",
                "Low Speed Perfect Overclock Controller",
                T1,
                2,
                2).getStackForm(1L));
        ModItemList.PerfectOverclockController.set(
            new StaticOverclockController(id++, "PerfectOverclockController", "Perfect Overclock Controller", T2, 4, 4)
                .getStackForm(1L));
        ModItemList.SingularityPerfectOverclockController.set(
            new StaticOverclockController(
                id++,
                "SingularityPerfectOverclockController",
                "Singularity Perfect Overclock Controller",
                T3,
                8,
                4).getStackForm(1L));
        // endregion

        // region Execution Cores (35093-35095)
        ModItemList.ExecutionCoreNormal
            .set(new ExecutionCore(id++, "ExecutionCoreNormal", "Execution Core", T1).getStackForm(1L));
        ModItemList.AdvExecutionCore
            .set(new AdvExecutionCore(id++, "AdvExecutionCore", "Advanced Execution Core", T2).getStackForm(1L));
        ModItemList.PerfectExecutionCore
            .set(new PerfectExecutionCore(id++, "PerfectExecutionCore", "Perfect Execution Core", T3).getStackForm(1L));
        // endregion

        return id;
    }

    /**
     * Registers AHTech-exclusive modular hatches (recovery rate, function modules).
     * Always registered regardless of TST presence.
     */
    private static int registerAHTechExclusiveModules(int id) {
        ModItemList.RecoveryRateLv1.set(
            new RecoveryRateModule(id++, "RecoveryRateLv1", "Recovery Rate Lv1", T1, Config.RecoveryModuleLv1Rate)
                .getStackForm(1L));
        ModItemList.RecoveryRateLv2.set(
            new RecoveryRateModule(id++, "RecoveryRateLv2", "Recovery Rate Lv2", T2, Config.RecoveryModuleLv2Rate)
                .getStackForm(1L));
        ModItemList.RecoveryRateLv3.set(
            new RecoveryRateModule(id++, "RecoveryRateLv3", "Recovery Rate Lv3", T3, Config.RecoveryModuleLv3Rate)
                .getStackForm(1L));
        // endregion

        // region Function Modules (35099)
        ModItemList.GeneralDisassemblyModule.set(
            new GeneralDisassemblyModule(id++, "GeneralDisassemblyModule", "General Disassembly Module", T1)
                .getStackForm(1L));
        // endregion

        return id;
    }

    private static int registerSupplierHatches(int id) {
        // 供应商不使用 tier 体系，传 0 以避免 isCompatibleWithMachine 误判
        registerSupplier(id++, ModItemList.SupplierShandongDezhou, SupplierId.SHANDONG_DEZHOU);
        registerSupplier(id++, ModItemList.SupplierYadepian, SupplierId.YADEPIAN);
        registerSupplier(id++, ModItemList.SupplierErfa, SupplierId.ERFA);
        registerSupplier(id++, ModItemList.SupplierChaola, SupplierId.CHAOLA);
        registerSupplier(id++, ModItemList.SupplierHuangweida, SupplierId.HUANGWEIDA);
        registerSupplier(id++, ModItemList.SupplierGaiguang, SupplierId.GAIGUANG);
        registerSupplier(id++, ModItemList.SupplierDitong, SupplierId.DITONG);
        return id;
    }

    private static void registerSupplier(int id, ModItemList itemEntry, SupplierId supplierId) {
        itemEntry.set(
            new SupplierHatch(id, "Supplier." + supplierId.name(), "Supplier " + supplierId.name(), 0, supplierId)
                .getStackForm(1L));
    }

    public static void loadMachinePostInit() {
        // Register machines that need post-init data here.
    }
}
