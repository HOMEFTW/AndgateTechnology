package com.andgatech.AHTech.loader;

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
    }

    public static void loadMachinePostInit() {
        // Register machines that need post-init data here.
    }
}
