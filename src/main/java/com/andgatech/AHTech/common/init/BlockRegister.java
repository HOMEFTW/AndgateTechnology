package com.andgatech.AHTech.common.init;

/**
 * Register all blocks and their tile entities with Forge.
 * Called from MaterialLoader.load() during preInit.
 */
public class BlockRegister {

    public static void registry() {
        registryBlocks();
        registryBlockContainers();
    }

    private static void registryBlocks() {
        // Register blocks with Forge:
        // GameRegistry.registerBlock(ModBlocks.MetaBlock01, ModBlocks.MetaBlock01.getItemClass(),
        // ModBlocks.MetaBlock01.getUnlocalizedName());
    }

    private static void registryBlockContainers() {
        // Register block variants and map to ModItemList:
        // ModItemList.MyBlock.set(ModBlocks.MetaBlock01.getItem(0));
    }
}
