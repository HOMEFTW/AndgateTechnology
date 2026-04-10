package com.andgatech.AHTech.recipe.machineRecipe;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import com.andgatech.AHTech.AndgateTechnology;
import com.andgatech.AHTech.recipe.recipeMap.AHTechRecipeMaps;

import gregtech.api.enums.GTValues;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.TierEU;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTUtility;

/**
 * Automatically generates recycling (reverse) recipes for the Electronics Market.
 *
 * <p>
 * At server startup this class scans all GregTech RecipeMaps and the Forge
 * CraftingManager to build a mapping of output items to their input materials.
 * For each unique output a single recycling recipe is registered in
 * {@link AHTechRecipeMaps#ElectronicsMarketRecipes} that takes the output item
 * as input and yields the original input materials as outputs.
 *
 * <p>
 * The actual recovery rate (how many outputs the player receives) is applied
 * at runtime by the machine's ProcessingLogic based on structure tier and voltage;
 * the recipes themselves declare full (100%) outputs.
 */
public class RecyclingRecipeGenerator {

    // ========================================================================
    // ItemStackKey -- deduplicates items by (item, damage) ignoring stack size
    // ========================================================================

    /**
     * Lightweight key for deduplicating ItemStacks by item type and damage value.
     * Stack size is intentionally excluded so that recipes producing different
     * quantities of the same item are merged into a single recycling recipe.
     */
    private static final class ItemStackKey {

        private final Item item;
        private final int damage;
        private final int hashCode;

        ItemStackKey(ItemStack stack) {
            this.item = stack.getItem();
            this.damage = stack.getItemDamage();
            this.hashCode = Objects.hash(item, damage);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof ItemStackKey other)) return false;
            return item == other.item && damage == other.damage;
        }
    }

    // ========================================================================
    // Public entry point
    // ========================================================================

    /**
     * Main entry point. Scans all GT RecipeMaps and Forge CraftingManager to
     * generate recycling recipes. Should be called from
     * {@code RecipeLoader.loadRecipesServerStarted()}.
     */
    public static void generateRecyclingRecipes() {
        AndgateTechnology.LOG.info("Starting auto-parse recycling recipe generation...");

        // Map: output item key -> input-material array from the first recipe that
        // produces this output. Subsequent recipes for the same output are ignored
        // (first-wins deduplication).
        Map<ItemStackKey, ItemStack[]> outputToInputs = new HashMap<>();

        int gtCount = scanGTRecipeMaps(outputToInputs);
        int craftingCount = scanCraftingManager(outputToInputs);

        int registered = buildRecyclingRecipes(outputToInputs);

        AndgateTechnology.LOG.info(
            "Recycling recipe generation complete. Scanned {} GT recipes, {} crafting recipes. Registered {} recycling recipes.",
            gtCount,
            craftingCount,
            registered);
    }

    // ========================================================================
    // GT RecipeMap scanner
    // ========================================================================

    /**
     * Iterates all GT RecipeMaps via reflection and builds output-to-inputs
     * mappings from each recipe that has exactly one output item and no fluid
     * outputs. This ensures we only generate clean recycling recipes where the
     * original recipe produced a single item from known inputs.
     *
     * @return the number of GT recipes successfully scanned
     */
    private static int scanGTRecipeMaps(Map<ItemStackKey, ItemStack[]> outputToInputs) {
        int count = 0;

        try {
            // Use reflection to iterate all static RecipeMap fields in RecipeMaps.
            // This is more robust than hardcoding individual recipe map names,
            // and automatically picks up any recipe maps added by other GTNH mods.
            for (Field field : RecipeMaps.class.getDeclaredFields()) {
                if (!RecipeMap.class.isAssignableFrom(field.getType())) continue;
                field.setAccessible(true);

                try {
                    RecipeMap<?> recipeMap = (RecipeMap<?>) field.get(null);
                    if (recipeMap == null) continue;

                    Collection<GTRecipe> recipes = recipeMap.getAllRecipes();
                    if (recipes == null || recipes.isEmpty()) continue;

                    for (GTRecipe recipe : recipes) {
                        try {
                            count += processGTRecipe(recipe, outputToInputs);
                        } catch (Exception e) {
                            // Skip individual bad recipes without crashing
                            AndgateTechnology.LOG
                                .warn("Skipping bad GT recipe in {}: {}", field.getName(), e.getMessage());
                        }
                    }
                } catch (IllegalAccessException e) {
                    AndgateTechnology.LOG.warn("Cannot access RecipeMap field {}: {}", field.getName(), e.getMessage());
                }
            }
        } catch (Exception e) {
            AndgateTechnology.LOG.warn("Error scanning GT RecipeMaps: {}", e.getMessage());
        }

        return count;
    }

    /**
     * Processes a single GTRecipe: if it has exactly one item output and no fluid
     * outputs, records the mapping from that output to the recipe's item inputs.
     *
     * @return 1 if the recipe was recorded, 0 otherwise
     */
    private static int processGTRecipe(GTRecipe recipe, Map<ItemStackKey, ItemStack[]> outputToInputs) {
        // Must have exactly one item output
        if (recipe.mOutputs == null || recipe.mOutputs.length != 1) return 0;
        // Skip recipes with fluid outputs -- they are not clean assembly recipes
        if (recipe.mFluidOutputs != null && recipe.mFluidOutputs.length > 0) return 0;

        ItemStack output = recipe.mOutputs[0];
        if (output == null || output.getItem() == null) return 0;

        ItemStack[] inputs = recipe.mInputs;
        if (inputs == null || inputs.length == 0) return 0;

        // Filter out null entries from input array and copy each at 1x
        List<ItemStack> validInputs = new ArrayList<>();
        for (ItemStack input : inputs) {
            if (input != null && input.getItem() != null) {
                validInputs.add(GTUtility.copyAmountUnsafe(1, input));
            }
        }
        if (validInputs.isEmpty()) return 0;

        ItemStackKey key = new ItemStackKey(output);
        // Keep the first mapping found for each output item
        outputToInputs.putIfAbsent(key, validInputs.toArray(new ItemStack[0]));
        return 1;
    }

    // ========================================================================
    // Forge CraftingManager scanner
    // ========================================================================

    /**
     * Iterates all recipes in the Forge CraftingManager and extracts
     * output-to-inputs mappings. Handles ShapedRecipes, ShapelessRecipes,
     * ShapedOreRecipe, and ShapelessOreRecipe.
     *
     * @return the number of crafting recipes successfully scanned
     */
    private static int scanCraftingManager(Map<ItemStackKey, ItemStack[]> outputToInputs) {
        int count = 0;

        try {
            List<IRecipe> recipeList = CraftingManager.getInstance()
                .getRecipeList();
            if (recipeList == null) return 0;

            for (IRecipe recipe : recipeList) {
                try {
                    ItemStack output = recipe.getRecipeOutput();
                    if (output == null || output.getItem() == null) continue;

                    ItemStack[] ingredients = extractIngredients(recipe);
                    if (ingredients == null || ingredients.length == 0) continue;

                    ItemStackKey key = new ItemStackKey(output);
                    outputToInputs.putIfAbsent(key, ingredients);
                    count++;
                } catch (Exception e) {
                    // Skip bad recipes without crashing
                    AndgateTechnology.LOG.warn(
                        "Skipping crafting recipe ({}): {}",
                        recipe.getClass()
                            .getSimpleName(),
                        e.getMessage());
                }
            }
        } catch (Exception e) {
            AndgateTechnology.LOG.warn("Error scanning CraftingManager: {}", e.getMessage());
        }

        return count;
    }

    /**
     * Extracts input ingredients from a crafting recipe, handling all common
     * recipe types in the GTNH mod ecosystem.
     *
     * @param recipe the IRecipe to extract ingredients from
     * @return an array of unified ItemStack inputs, or null if extraction fails
     */
    private static ItemStack[] extractIngredients(IRecipe recipe) {
        try {
            if (recipe instanceof ShapedRecipes shaped) {
                return consolidateIngredients(shaped.recipeItems);
            } else if (recipe instanceof ShapelessRecipes shapeless) {
                return consolidateIngredients(shapeless.recipeItems.toArray(new ItemStack[0]));
            } else if (recipe instanceof ShapedOreRecipe shapedOre) {
                return extractOreIngredients(Arrays.asList(shapedOre.getInput()));
            } else if (recipe instanceof ShapelessOreRecipe shapelessOre) {
                return extractOreIngredients(shapelessOre.getInput());
            }
            // Unknown recipe type -- skip silently
        } catch (Exception e) {
            AndgateTechnology.LOG.warn(
                "Failed to extract ingredients from {}: {}",
                recipe.getClass()
                    .getSimpleName(),
                e.getMessage());
        }
        return null;
    }

    /**
     * Consolidates an array of input ItemStacks by merging duplicate items
     * (same item + damage) and unifying each via the ore dictionary.
     *
     * @param rawInputs the raw input array (may contain nulls)
     * @return consolidated and unified ItemStack array, or null if empty
     */
    private static ItemStack[] consolidateIngredients(ItemStack[] rawInputs) {
        if (rawInputs == null || rawInputs.length == 0) return null;

        Map<ItemStackKey, ItemStack> merged = new HashMap<>();
        for (ItemStack input : rawInputs) {
            if (input == null || input.getItem() == null) continue;

            ItemStack unified = GTOreDictUnificator.get(false, input, true);
            if (unified == null || unified.getItem() == null) continue;

            ItemStackKey key = new ItemStackKey(unified);
            ItemStack existing = merged.get(key);
            if (existing != null) {
                existing.stackSize += unified.stackSize;
            } else {
                merged.put(key, unified.copy());
            }
        }

        return merged.isEmpty() ? null
            : merged.values()
                .toArray(new ItemStack[0]);
    }

    /**
     * Extracts ingredients from an ore-dictionary input list (used by
     * ShapedOreRecipe and ShapelessOreRecipe). Each entry can be either
     * an ItemStack or an {@code ArrayList<ItemStack>} (ore dict list).
     *
     * @param inputs the list of input objects
     * @return consolidated and unified ItemStack array, or null if empty
     */
    private static ItemStack[] extractOreIngredients(List<?> inputs) {
        if (inputs == null || inputs.isEmpty()) return null;

        Map<ItemStackKey, ItemStack> merged = new HashMap<>();
        for (Object entry : inputs) {
            try {
                ItemStack stack = resolveOreEntry(entry);
                if (stack == null) continue;

                ItemStackKey key = new ItemStackKey(stack);
                ItemStack existing = merged.get(key);
                if (existing != null) {
                    existing.stackSize += stack.stackSize;
                } else {
                    merged.put(key, stack.copy());
                }
            } catch (Exception ignored) {
                // Skip unresolvable entries
            }
        }

        return merged.isEmpty() ? null
            : merged.values()
                .toArray(new ItemStack[0]);
    }

    /**
     * Resolves a single ore dictionary entry to an ItemStack.
     * Handles both direct ItemStack entries and ArrayList&lt;ItemStack&gt; entries
     * (the standard Forge ore dictionary representation).
     *
     * @param entry either an ItemStack or an ArrayList&lt;ItemStack&gt;
     * @return a unified ItemStack, or null if resolution fails
     */
    @SuppressWarnings("unchecked")
    private static ItemStack resolveOreEntry(Object entry) {
        if (entry instanceof ItemStack stack) {
            if (stack.getItem() == null) return null;
            return GTOreDictUnificator.get(false, stack, true);
        } else if (entry instanceof ArrayList<?>list) {
            if (list.isEmpty()) return null;
            // Use the first valid item from the ore dictionary list
            for (Object obj : list) {
                if (obj instanceof ItemStack candidate && candidate.getItem() != null) {
                    return GTOreDictUnificator.get(false, candidate, true);
                }
            }
        }
        return null;
    }

    // ========================================================================
    // Recipe builder -- creates recycling recipes from the collected mappings
    // ========================================================================

    /**
     * Iterates the output-to-inputs map and registers one recycling recipe per
     * unique output item in {@link AHTechRecipeMaps#ElectronicsMarketRecipes}.
     *
     * <p>
     * Each recycling recipe takes the original output item (1x) as input and
     * produces the original input materials as outputs. The actual recovery rate
     * is applied at runtime by the machine's ProcessingLogic based on structure
     * tier and voltage, so recipes declare full (100%) outputs here.
     *
     * @return the number of recycling recipes registered
     */
    private static int buildRecyclingRecipes(Map<ItemStackKey, ItemStack[]> outputToInputs) {
        int registered = 0;
        int skipped = 0;

        for (Map.Entry<ItemStackKey, ItemStack[]> entry : outputToInputs.entrySet()) {
            try {
                ItemStack[] recipeOutputs = entry.getValue();
                if (recipeOutputs == null || recipeOutputs.length == 0) {
                    skipped++;
                    continue;
                }

                // Reconstruct the input item (the item to recycle) from the key
                ItemStackKey key = entry.getKey();
                ItemStack inputStack = new ItemStack(key.item, 1, key.damage);

                if (inputStack.getItem() == null) {
                    skipped++;
                    continue;
                }

                // Copy and validate all outputs
                ItemStack[] outputs = Arrays.copyOf(recipeOutputs, recipeOutputs.length);
                boolean allValid = true;
                for (ItemStack out : outputs) {
                    if (out == null || out.getItem() == null) {
                        allValid = false;
                        break;
                    }
                }
                if (!allValid) {
                    skipped++;
                    continue;
                }

                // Register the recycling recipe
                GTValues.RA.stdBuilder()
                    .itemInputs(GTUtility.copyAmountUnsafe(1, inputStack))
                    .itemOutputs(outputs)
                    .specialValue(1)
                    .eut(TierEU.RECIPE_LV)
                    .duration(200)
                    .addTo(AHTechRecipeMaps.ElectronicsMarketRecipes);

                registered++;
            } catch (Exception e) {
                skipped++;
                AndgateTechnology.LOG.warn("Failed to register recycling recipe: {}", e.getMessage());
            }
        }

        if (skipped > 0) {
            AndgateTechnology.LOG.warn("Skipped {} invalid recycling recipe candidates.", skipped);
        }

        return registered;
    }

    // ========================================================================
    // Circuit board detection
    // ========================================================================

    /**
     * Checks whether the given ItemStack is a GT circuit board or PCB.
     *
     * <p>
     * Detection criteria:
     * <ul>
     * <li>Item unlocalizedName contains: "circuit_board", "circuitboard", "pcb", "boardraw"</li>
     * <li>Item is one of GT's known circuit board ItemList entries</li>
     * </ul>
     *
     * @param stack the ItemStack to check
     * @return true if the item is a circuit board or PCB
     */
    public static boolean isCircuitBoard(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return false;

        // Check unlocalized name patterns
        try {
            String unlocalizedName = stack.getUnlocalizedName()
                .toLowerCase();
            if (unlocalizedName.contains("circuit_board") || unlocalizedName.contains("circuitboard")
                || unlocalizedName.contains("pcb")
                || unlocalizedName.contains("boardraw")) {
                return true;
            }
        } catch (Exception ignored) {
            // Some mods may throw from getUnlocalizedName
        }

        // Check against known GT ItemList circuit board entries
        try {
            if (stack.getItem() == ItemList.Circuit_Board_Coated.getItem()
                || stack.getItem() == ItemList.Circuit_Board_Coated_Basic.getItem()
                || stack.getItem() == ItemList.Circuit_Board_Epoxy.getItem()
                || stack.getItem() == ItemList.Circuit_Board_Epoxy_Advanced.getItem()
                || stack.getItem() == ItemList.Circuit_Board_Fiberglass.getItem()
                || stack.getItem() == ItemList.Circuit_Board_Fiberglass_Advanced.getItem()
                || stack.getItem() == ItemList.Circuit_Board_Multifiberglass.getItem()
                || stack.getItem() == ItemList.Circuit_Board_Multifiberglass_Elite.getItem()
                || stack.getItem() == ItemList.Circuit_Board_Phenolic.getItem()
                || stack.getItem() == ItemList.Circuit_Board_Phenolic_Good.getItem()
                || stack.getItem() == ItemList.Circuit_Board_Plastic.getItem()
                || stack.getItem() == ItemList.Circuit_Board_Plastic_Advanced.getItem()
                || stack.getItem() == ItemList.Circuit_Board_Wetware.getItem()
                || stack.getItem() == ItemList.Circuit_Board_Wetware_Extreme.getItem()
                || stack.getItem() == ItemList.Circuit_Board_Bio.getItem()
                || stack.getItem() == ItemList.Circuit_Board_Bio_Ultra.getItem()
                || stack.getItem() == ItemList.Circuit_Board_Optical.getItem()) {
                return true;
            }
        } catch (Exception ignored) {
            // ItemList entries may not be initialized in certain contexts
        }

        return false;
    }
}
