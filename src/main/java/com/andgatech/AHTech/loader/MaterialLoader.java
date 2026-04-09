package com.andgatech.AHTech.loader;

import com.andgatech.AHTech.common.init.BlockRegister;
import com.andgatech.AHTech.common.init.ItemRegister;

/**
 * Register items, blocks, and custom materials.
 * Called during preInit phase.
 */
public class MaterialLoader {

    public static void load() {
        ItemRegister.registry();
        BlockRegister.registry();

        // Add custom Bartworks materials here:
        // WerkstoffAdderRegistry.addWerkstoffAdder(new MaterialPool());
    }
}
