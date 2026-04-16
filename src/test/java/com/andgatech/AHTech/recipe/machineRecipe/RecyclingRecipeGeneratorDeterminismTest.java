package com.andgatech.AHTech.recipe.machineRecipe;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.junit.jupiter.api.Test;

class RecyclingRecipeGeneratorDeterminismTest {

    @Test
    void canonicalRecipeComparisonPrefersSimplerCandidate() {
        Item sharedItem = new Item();

        ItemStack[] existing = { new ItemStack(sharedItem, 1, 5), new ItemStack(sharedItem, 1, 7) };
        ItemStack[] candidate = { new ItemStack(sharedItem, 1, 5) };

        assertTrue(RecyclingRecipeGenerator.shouldReplaceRecipeCandidate(existing, candidate));
        assertFalse(RecyclingRecipeGenerator.shouldReplaceRecipeCandidate(candidate, existing));
    }

    @Test
    void canonicalRecipeComparisonBreaksTiesDeterministically() {
        Item itemA = new Item().setUnlocalizedName("candidate");
        Item itemB = new Item().setUnlocalizedName("existing");

        ItemStack[] existing = { new ItemStack(itemB, 1, 0) };
        ItemStack[] candidate = { new ItemStack(itemA, 1, 0) };

        assertTrue(RecyclingRecipeGenerator.shouldReplaceRecipeCandidate(existing, candidate));
        assertFalse(RecyclingRecipeGenerator.shouldReplaceRecipeCandidate(candidate, existing));
    }
}
