package com.andgatech.AHTech.config;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    public static final String GENERAL = "General";

    // region General Settings
    public static int MAX_PARALLEL_LIMIT = 256;
    public static boolean DEFAULT_BATCH_MODE = false;

    // endregion

    // region Electronics Market
    public static boolean Enable_ElectronicsMarket = true;
    public static double Stage1_BaseRecoveryRate = 0.30;
    // endregion

    // region Modularization
    public static boolean EnableModularizedMachineSystem = true;
    public static float RecoveryModuleLv1Rate = 0.50f;
    public static float RecoveryModuleLv2Rate = 0.70f;
    public static float RecoveryModuleLv3Rate = 0.90f;

    // region ModuleDefaults -- TST Standard Modules
    public static int[] ParallelOfParallelController = { 8, 128, 2048, 32768, 524288, 8388608, 134217728,
        Integer.MAX_VALUE };
    public static int[] SpeedMultiplierOfSpeedController = { 2, 4, 8, 16, 32, 64, 128, 256 };
    public static double[] PowerConsumptionMultiplierOfPowerConsumptionController = { 0.95, 0.9, 0.85, 0.8, 0.75, 0.7,
        0.5, 0.25 };
    // endregion
    // endregion

    // region Financial System
    public static boolean EnableFinancialSystem = true;
    public static boolean EnableAutoRefillFromInputBus = true;
    // endregion

    // region Machine Enables
    // Add your machine enable/disable configs here:
    // public static boolean Enable_YourMachine = true;
    // endregion

    // region Machine Parameters
    // Add your machine parameter configs here:
    // public static int YourMachine_Parallel = 256;
    // endregion

    public static void synchronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);

        // General
        MAX_PARALLEL_LIMIT = configuration.getInt(
            "MAX_PARALLEL_LIMIT",
            GENERAL,
            MAX_PARALLEL_LIMIT,
            1,
            Integer.MAX_VALUE,
            "Maximum parallel limit for all machines.");
        DEFAULT_BATCH_MODE = configuration
            .getBoolean("DEFAULT_BATCH_MODE", GENERAL, DEFAULT_BATCH_MODE, "Default batch mode for machines.");

        // Add your machine configs here:
        // Enable_YourMachine = configuration.getBoolean("EnableYourMachine", "YourMachine", true, "Enable/disable
        // YourMachine.");
        // YourMachine_Parallel = configuration.getInt("Parallel", "YourMachine", 256, 1, Integer.MAX_VALUE, "Parallel
        // of YourMachine.");

        // Electronics Market
        Enable_ElectronicsMarket = configuration.getBoolean(
            "EnableElectronicsMarket",
            "ElectronicsMarket",
            true,
            "Enable/disable Electronics Market multiblock.");
        Stage1_BaseRecoveryRate = (double) configuration
            .getFloat("Stage1BaseRecoveryRate", "ElectronicsMarket", 0.30f, 0.0f, 1.0f, "Stage I base recycling rate.");
        // Modularization
        EnableModularizedMachineSystem = configuration.getBoolean(
            "EnableModularizedMachineSystem",
            "Modularization",
            true,
            "Enable/disable the modularization machine system.");
        RecoveryModuleLv1Rate = configuration.getFloat(
            "RecoveryModuleLv1Rate",
            "Modularization",
            0.50f,
            0.0f,
            1.0f,
            "Recovery rate for Lv1 Recovery Module.");
        RecoveryModuleLv2Rate = configuration.getFloat(
            "RecoveryModuleLv2Rate",
            "Modularization",
            0.70f,
            0.0f,
            1.0f,
            "Recovery rate for Lv2 Recovery Module.");
        RecoveryModuleLv3Rate = configuration.getFloat(
            "RecoveryModuleLv3Rate",
            "Modularization",
            0.90f,
            0.0f,
            1.0f,
            "Recovery rate for Lv3 Recovery Module.");
        EnableFinancialSystem = configuration.getBoolean(
            "EnableFinancialSystem",
            "FinancialSystem",
            true,
            "Enable/disable the financial system.");
        EnableAutoRefillFromInputBus = configuration.getBoolean(
            "EnableAutoRefillFromInputBus",
            "FinancialSystem",
            true,
            "Enable/disable automatic currency refill from input buses.");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
