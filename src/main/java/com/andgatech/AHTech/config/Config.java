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
    public static double Stage2_BaseRecoveryRate = 0.60;
    public static double Stage3_BaseRecoveryRate = 0.90;
    public static double VoltageBonusPerTier = 0.02;
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
        Enable_ElectronicsMarket = configuration.getBoolean("EnableElectronicsMarket", "ElectronicsMarket", true, "Enable/disable Electronics Market multiblock.");
        Stage1_BaseRecoveryRate = (double) configuration.getFloat("Stage1BaseRecoveryRate", "ElectronicsMarket", 0.30f, 0.0f, 1.0f, "Stage I base recycling rate.");
        Stage2_BaseRecoveryRate = (double) configuration.getFloat("Stage2BaseRecoveryRate", "ElectronicsMarket", 0.60f, 0.0f, 1.0f, "Stage II base recycling rate.");
        Stage3_BaseRecoveryRate = (double) configuration.getFloat("Stage3BaseRecoveryRate", "ElectronicsMarket", 0.90f, 0.0f, 1.0f, "Stage III base recycling rate.");
        VoltageBonusPerTier = (double) configuration.getFloat("VoltageBonusPerTier", "ElectronicsMarket", 0.02f, 0.0f, 1.0f, "Recovery rate bonus per voltage tier.");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
