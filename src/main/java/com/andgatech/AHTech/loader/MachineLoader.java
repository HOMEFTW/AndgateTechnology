package com.andgatech.AHTech.loader;

import com.andgatech.AHTech.common.ModItemList;
import com.andgatech.AHTech.common.machine.ElectronicsMarket;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.ExecutionCore;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.GeneralDisassemblyModule;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.OverclockControllerModule;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.ParallelControllerModule;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.PowerConsumptionModule;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.RecoveryRateModule;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.SpeedControllerModule;
import com.andgatech.AHTech.config.Config;

/**
 * Register all multiblock machines and single-block machines here.
 * Called during init phase.
 */
public class MachineLoader {

    public static void loadMachines() {
        // Register multiblock controllers here.
        // Each machine needs a unique meta ID (use a range that doesn't conflict with other mods).
        //
        // Example:
        // new YourMachine(19001, "yourmachine", "Your Machine").getStackForm(1L);
        // ModItemList.YourMachine.set(new YourMachine(19001, "yourmachine", "Your Machine").getStackForm(1L));
        //
        // The meta ID range for your mod should be carefully chosen to avoid conflicts.
        // TST uses 18791-19080, so pick a different range.

        if (Config.Enable_ElectronicsMarket) {
            ModItemList.ElectronicsMarket
                .set(new ElectronicsMarket(35001, "ElectronicsMarket", "Electronics Market").getStackForm(1L));
        }

        if (Config.EnableModularizedMachineSystem) {
            // Performance modules - Lv1 for framework validation
            ModItemList.ParallelControllerLv1.set(
                new ParallelControllerModule(35050, "ParallelControllerLv1", "Parallel Controller Lv1", 1, 4)
                    .getStackForm(1L));
            ModItemList.SpeedControllerLv1.set(
                new SpeedControllerModule(35051, "SpeedControllerLv1", "Speed Controller Lv1", 1, 2).getStackForm(1L));
            ModItemList.OverclockControllerLv1.set(
                new OverclockControllerModule(35052, "OverclockControllerLv1", "Overclock Controller Lv1", 1, 2, 4)
                    .getStackForm(1L));
            ModItemList.PowerConsumptionLv1.set(
                new PowerConsumptionModule(35053, "PowerConsumptionLv1", "Power Consumption Lv1", 1, 0.8f)
                    .getStackForm(1L));

            // Recovery rate modules (3 tiers)
            ModItemList.RecoveryRateLv1.set(
                new RecoveryRateModule(35054, "RecoveryRateLv1", "Recovery Rate Lv1", 1, Config.RecoveryModuleLv1Rate)
                    .getStackForm(1L));
            ModItemList.RecoveryRateLv2.set(
                new RecoveryRateModule(35055, "RecoveryRateLv2", "Recovery Rate Lv2", 2, Config.RecoveryModuleLv2Rate)
                    .getStackForm(1L));
            ModItemList.RecoveryRateLv3.set(
                new RecoveryRateModule(35056, "RecoveryRateLv3", "Recovery Rate Lv3", 3, Config.RecoveryModuleLv3Rate)
                    .getStackForm(1L));

            // Execution cores
            ModItemList.ExecutionCoreNormal
                .set(new ExecutionCore(35060, "ExecutionCoreNormal", "Execution Core", 1).getStackForm(1L));

            // Function modules
            ModItemList.GeneralDisassemblyModule.set(
                new GeneralDisassemblyModule(35070, "GeneralDisassemblyModule", "General Disassembly Module", 1)
                    .getStackForm(1L));
        }
    }

    public static void loadMachinePostInit() {
        // Register machines that need post-init data here.
    }
}
