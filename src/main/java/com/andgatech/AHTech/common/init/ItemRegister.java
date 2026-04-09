package com.andgatech.AHTech.common.init;

/**
 * Register all items with Forge.
 * Called from MaterialLoader.load() during preInit.
 */
public class ItemRegister {

    public static void registry() {
        registryItems();
        registryItemContainers();
    }

    private static void registryItems() {
        // Register items with Forge:
        // GameRegistry.registerItem(ModItems.MetaItem01, ModItems.MetaItem01.getUnlocalizedName());
    }

    private static void registryItemContainers() {
        // Register item variants and map to ModItemList:
        // ModItems.MetaItem01.addItem(0, "MyItem");
        // ModItemList.MyItem.set(ModItems.MetaItem01.getItem(0));
    }
}
