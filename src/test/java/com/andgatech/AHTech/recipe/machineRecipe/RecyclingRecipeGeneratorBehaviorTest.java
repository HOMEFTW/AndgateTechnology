package com.andgatech.AHTech.recipe.machineRecipe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.junit.jupiter.api.Test;

import gregtech.api.util.GTRecipe;

class RecyclingRecipeGeneratorBehaviorTest {

    @Test
    void buildRecyclingRecipesUsesForwardOutputCountAsReverseInputCount() throws Exception {
        Item outputItem = new Item().setUnlocalizedName("ahtech.test.multiOutputProduct");

        ItemStack forwardOutput = new ItemStack(outputItem, 4, 0);
        ItemStack reverseInput = RecyclingRecipeGenerator.createRecyclingInputStack(forwardOutput);

        assertEquals(4, reverseInput.stackSize);
        assertEquals(outputItem, reverseInput.getItem());
    }

    @Test
    void processGTRecipeSkipsRecipesWithFluidInputs() throws Exception {
        Item inputItem = new Item().setUnlocalizedName("ahtech.test.forwardInput");
        Item outputItem = new Item().setUnlocalizedName("ahtech.test.forwardOutput");

        GTRecipe recipe = newRecipe(
            new ItemStack[] { new ItemStack(inputItem, 1, 0) },
            new ItemStack[] { new ItemStack(outputItem, 1, 0) },
            new FluidStack[1]);

        Map<Object, ItemStack[]> outputToInputs = new HashMap<>();

        int processed = invokeProcessGTRecipe(recipe, outputToInputs);

        assertEquals(0, processed);
        assertTrue(outputToInputs.isEmpty());
    }

    private static int invokeProcessGTRecipe(GTRecipe recipe, Map<Object, ItemStack[]> outputToInputs)
        throws Exception {
        Method method = RecyclingRecipeGenerator.class.getDeclaredMethod("processGTRecipe", GTRecipe.class, Map.class);
        method.setAccessible(true);
        return (int) method.invoke(null, recipe, outputToInputs);
    }

    private static GTRecipe newRecipe(ItemStack[] inputs, ItemStack[] outputs, FluidStack[] fluidInputs)
        throws Exception {
        GTRecipe recipe = (GTRecipe) getUnsafe().allocateInstance(GTRecipe.class);
        recipe.mInputs = inputs;
        recipe.mOutputs = outputs;
        recipe.mFluidInputs = fluidInputs;
        recipe.mFluidOutputs = null;
        return recipe;
    }

    private static sun.misc.Unsafe getUnsafe() throws Exception {
        Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (sun.misc.Unsafe) field.get(null);
    }
}
