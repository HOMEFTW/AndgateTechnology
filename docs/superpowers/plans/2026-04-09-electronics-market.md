# Electronics Market Multiblock Machine Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement the 美弱南电子市场 (Electronics Market) — a three-stage multiblock machine that recycles machine materials with configurable recovery rates.

**Architecture:** Single controller block (Meta ID 35001) extending `GTCM_MultiMachineBase<T>`. Structure tier (I/II/III) detected via `ofBlocksTiered()` on tiered casing blocks. Custom RecipeMap for all recipes. Recipe generation combines hardcoded special recipes with auto-parsed GT RecipeMaps + Forge CraftingManager recipes. Voltage affects speed and parallel.

**Tech Stack:** GTNH 2.8.4 / GT5-Unofficial / MC 1.7.10 / Java 17 syntax via Jabel

---

## File Structure

```
src/main/java/com/andgatech/AHTech/
├── common/
│   ├── machine/
│   │   └── ElectronicsMarket.java           ← Multiblock controller (NEW)
│   ├── ModItemList.java                     ← Add ElectronicsMarket enum entry (MODIFY)
│   └── init/
│       └── (no changes needed)
├── recipe/
│   ├── recipeMap/
│   │   └── AHTechRecipeMaps.java            ← Custom RecipeMap definitions (NEW)
│   └── machineRecipe/
│       ├── ElectronicsMarketRecipePool.java ← Hardcoded special recipes (NEW)
│       └── RecyclingRecipeGenerator.java    ← Auto-parse recycling recipes (NEW)
├── loader/
│   ├── MachineLoader.java                   ← Register machine (MODIFY)
│   ├── RecipeLoader.java                    ← Load recipe pools (MODIFY)
│   └── MaterialLoader.java                  ← No change
├── config/
│   └── Config.java                          ← Add recovery rate configs (MODIFY)
└── util/
    └── LanguageManager.java                 ← No change
```

---

### Task 1: Add Config Fields

**Files:**
- Modify: `src/main/java/com/andgatech/AHTech/config/Config.java`

- [ ] **Step 1: Add recovery rate and enable config fields to Config.java**

Add these fields after the existing `DEFAULT_BATCH_MODE` field:

```java
// region Electronics Market
public static boolean Enable_ElectronicsMarket = true;
public static double Stage1_BaseRecoveryRate = 0.30;
public static double Stage2_BaseRecoveryRate = 0.60;
public static double Stage3_BaseRecoveryRate = 0.90;
public static double VoltageBonusPerTier = 0.02;
// endregion
```

Add corresponding loading code inside `synchronizeConfiguration()`:

```java
// Electronics Market
Enable_ElectronicsMarket = configuration.getBoolean("EnableElectronicsMarket", "ElectronicsMarket", true, "Enable/disable Electronics Market multiblock.");
Stage1_BaseRecoveryRate = (double) configuration.getFloat("Stage1BaseRecoveryRate", "ElectronicsMarket", 0.30f, 0.0f, 1.0f, "Stage I base recycling rate.");
Stage2_BaseRecoveryRate = (double) configuration.getFloat("Stage2BaseRecoveryRate", "ElectronicsMarket", 0.60f, 0.0f, 1.0f, "Stage II base recycling rate.");
Stage3_BaseRecoveryRate = (double) configuration.getFloat("Stage3BaseRecoveryRate", "ElectronicsMarket", 0.90f, 0.0f, 1.0f, "Stage III base recycling rate.");
VoltageBonusPerTier = (double) configuration.getFloat("VoltageBonusPerTier", "ElectronicsMarket", 0.02f, 0.0f, 1.0f, "Recovery rate bonus per voltage tier.");
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/andgatech/AHTech/config/Config.java
git commit -m "feat: add Electronics Market config fields"
```

---

### Task 2: Create Custom RecipeMap

**Files:**
- Create: `src/main/java/com/andgatech/AHTech/recipe/recipeMap/AHTechRecipeMaps.java`

- [ ] **Step 1: Create the AHTechRecipeMaps class**

```java
package com.andgatech.AHTech.recipe.recipeMap;

import gregtech.api.enums.GTUITextures;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMapBuilder;
import gregtech.api.recipe.backend.RecipeMapBackend;

public class AHTechRecipeMaps {

    public static final RecipeMap<RecipeMapBackend> ElectronicsMarketRecipes = RecipeMapBuilder
        .of("ahtech.recipe.ElectronicsMarketRecipes")
        .maxIO(9, 9, 4, 4)
        .progressBar(GTUITextures.PROGRESSBAR_ARROW_MULTIPLE)
        .neiHandlerInfo(
            builder -> builder.setMaxRecipesPerPage(4))
        .build();
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/andgatech/AHTech/recipe/recipeMap/AHTechRecipeMaps.java
git commit -m "feat: add Electronics Market RecipeMap"
```

---

### Task 3: Create Multiblock Controller

**Files:**
- Create: `src/main/java/com/andgatech/AHTech/common/machine/ElectronicsMarket.java`

- [ ] **Step 1: Create the ElectronicsMarket multiblock controller**

This is the main machine class. Key design:
- Structure tier (1/2/3) detected via `ofBlocksTiered()` on GT casing blocks
- `getRecipeMap()` returns `AHTechRecipeMaps.ElectronicsMarketRecipes`
- Voltage affects speed and parallel via `euModifier` and `maxParallel`
- Tier field controls which recipes are available

```java
package com.andgatech.AHTech.common.machine;

import java.util.Arrays;
import java.util.Collection;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import com.gtnewhorizon.structurelib.alignment.IConstructable;
import com.gtnewhorizon.structurelib.alignment.ISurvivalConstructable;
import com.gtnewhorizons.gtnhintergalactic.block.IGBlocks;

import gregtech.api.enums.GTValues;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEExtendedPowerMultiBlockBase;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTStructureUtility;
import gregtech.api.util.HatchElementBuilder;
import gregtech.api.util.MultiblockTooltipBuilder;

import com.andgatech.AHTech.AndgateTechnology;
import com.andgatech.AHTech.common.GTCM_MultiMachineBase;
import com.andgatech.AHTech.recipe.recipeMap.AHTechRecipeMaps;

public class ElectronicsMarket extends GTCM_MultiMachineBase<ElectronicsMarket>
    implements IConstructable, ISurvivalConstructable {

    private static final String STRUCTURE_PIECE_MAIN = "mainElectronicsMarket";
    private final int horizontalOffSet = 3;
    private final int verticalOffSet = 10;
    private final int depthOffSet = 0;
    private static IStructureDefinition<ElectronicsMarket> STRUCTURE_DEFINITION = null;

    private int structureTier = 0;

    public ElectronicsMarket(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public ElectronicsMarket(String aName) {
        super(aName);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new ElectronicsMarket(this.mName);
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return AHTechRecipeMaps.ElectronicsMarketRecipes;
    }

    @Nonnull
    @Override
    public Collection<RecipeMap<?>> getAvailableRecipeMaps() {
        return Arrays.asList(AHTechRecipeMaps.ElectronicsMarketRecipes);
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        final MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
        tt.addMachineType("Electronics Market")
            .addInfo("Recycles machine materials with configurable recovery rates")
            .addInfo("Circuit boards: 100% recovery")
            .addInfo("Other materials: stage base rate + voltage bonus")
            .addInfo("Stage I: 30% | Stage II: 60% | Stage III: 90%")
            .addInfo("Voltage affects speed and parallel")
            .addSeparator()
            .beginStructureBlock(7, 11, 7, false)
            .addController("Front center")
            .addCasingInfo("Casing", 54)
            .addInputBus("Any casing", 1)
            .addOutputBus("Any casing", 1)
            .addInputHatch("Any casing", 1)
            .addOutputHatch("Any casing", 1)
            .addEnergyHatch("Any casing", 1)
            .toolTipFinisher("Andgate Technology");
        return tt;
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, boolean aActive, boolean aRedstone) {
        // TODO: update with proper textures once decided
        return new ITexture[] {
            TextureFactory.of(Textures.BlockIcons.MACHINE_CASING_ROBUST_TUNGSTENSTEEL) };
    }

    // region Structure Tier Detection

    /**
     * Returns the tier for a given block/meta combination used in the structure.
     * Returns null if the block is not a valid tiered casing.
     */
    public static Integer getCasingTier(Block block, int meta) {
        // Stage I: Robust Tungstensteel Casing
        if (block == GregTechAPI.sBlockCasings2 && meta == 0) return 1;
        // Stage II: Clean Stainless Steel Casing
        if (block == GregTechAPI.sBlockCasings2 && meta == 6) return 2;
        // Stage III: Laser Safe Casing (from Intergalactic)
        if (block == IGBlocks.sBlockCasingsIG && meta == 0) return 3;
        return null;
    }

    // endregion

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        repairMachine();
        structureTier = 0;
        boolean sign = checkPiece(STRUCTURE_PIECE_MAIN, horizontalOffSet, verticalOffSet, depthOffSet);
        if (structureTier <= 0) return false;

        // Voltage-based parallel and speed
        int voltageTier = getMaxInputVoltage();
        maxParallel = (int) Math.min(Integer.MAX_VALUE, (long) structureTier * structureTier * 4 * (voltageTier + 1));
        speedBonus = 1.0F / (1.0F + 0.1F * voltageTier);
        euModifier = 1.0F;

        return sign;
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        this.buildPiece(STRUCTURE_PIECE_MAIN, stackSize, hintsOnly, horizontalOffSet, verticalOffSet, depthOffSet);
    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        if (this.mMachine) return -1;
        return this.survivalBuildPiece(
            STRUCTURE_PIECE_MAIN,
            stackSize,
            horizontalOffSet,
            verticalOffSet,
            depthOffSet,
            elementBudget,
            env,
            false,
            true);
    }

    @Override
    public IStructureDefinition<ElectronicsMarket> getStructureDefinition() {
        if (STRUCTURE_DEFINITION == null) {
            STRUCTURE_DEFINITION = StructureDefinition
                .builder()
                .addShape(STRUCTURE_PIECE_MAIN, STRUCTURE_SHAPE)
                .addElement(
                    'C',
                    withChannel(
                        "casing_tier",
                        GTStructureUtility.ofBlocksTiered(
                            ElectronicsMarket::getCasingTier,
                            ImmutableList.of(
                                Pair.of(GregTechAPI.sBlockCasings2, 0),
                                Pair.of(GregTechAPI.sBlockCasings2, 6),
                                Pair.of(IGBlocks.sBlockCasingsIG, 0)),
                            0,
                            (m, t) -> m.structureTier = t,
                            m -> m.structureTier)))
                .addElement(
                    'H',
                    HatchElementBuilder.<ElectronicsMarket>builder()
                        .atLeast(InputBus, OutputBus, InputHatch, OutputHatch, Energy.or(ExoticEnergy))
                        .adder(ElectronicsMarket::addToMachineList)
                        .dot(1)
                        .casingIndex(((BlockCasings2) GregTechAPI.sBlockCasings2).getTextureIndex(0))
                        .buildAndChain(GregTechAPI.sBlockCasings2, 0))
                .addElement('A', ofBlock(GregTechAPI.sBlockCasings2, 0))
                .build();
        }
        return STRUCTURE_DEFINITION;
    }

    public int getStructureTier() {
        return structureTier;
    }
}
```

**Important notes:**
- The `STRUCTURE_SHAPE` array and some imports (like `IGBlocks`, `GTCM_MultiMachineBase`, `ImmutableList`, `Pair`, `StructureDefinition`, etc.) need to be resolved based on actual GTNH API availability. The implementing agent should verify exact import paths against the TST project.
- The structure shape array (a 2D char map defining the multiblock layout) needs to be designed. For now use a placeholder 7x11x7 structure with characters 'A' (casing), 'C' (tiered casing), 'H' (hatch slot), and the controller character.
- The `getCasingTier()` method maps casing blocks to tiers: Stage I = Tungstensteel, Stage II = Stainless Steel, Stage III = IG Laser Safe Casing.

- [ ] **Step 2: Compile-check — resolve imports and verify against TST patterns**

Run: `./gradlew compileJava`
Expected: May have compile errors — fix import paths by referencing TST's exact import statements for `GTCM_MultiMachineBase`, `StructureDefinition`, `GTStructureUtility`, `HatchElementBuilder`, `ImmutableList`, `Pair`, `IGBlocks`, etc.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/andgatech/AHTech/common/machine/ElectronicsMarket.java
git commit -m "feat: add Electronics Market multiblock controller"
```

---

### Task 4: Register Machine and ModItemList Entry

**Files:**
- Modify: `src/main/java/com/andgatech/AHTech/common/ModItemList.java`
- Modify: `src/main/java/com/andgatech/AHTech/loader/MachineLoader.java`

- [ ] **Step 1: Add ElectronicsMarket to ModItemList enum**

Add before the semicolon in `ModItemList.java`:

```java
ElectronicsMarket,
```

- [ ] **Step 2: Register machine in MachineLoader.loadMachines()**

Add inside `loadMachines()`, gated by config:

```java
if (Config.Enable_ElectronicsMarket) {
    ModItemList.ElectronicsMarket.set(
        new ElectronicsMarket(
            35001,
            "ElectronicsMarket",
            "Electronics Market").getStackForm(1L));
}
```

- [ ] **Step 3: Compile-check**

Run: `./gradlew compileJava`

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/andgatech/AHTech/common/ModItemList.java src/main/java/com/andgatech/AHTech/loader/MachineLoader.java
git commit -m "feat: register Electronics Market machine (Meta ID 35001)"
```

---

### Task 5: Create Hardcoded Special Recipe Pool

**Files:**
- Create: `src/main/java/com/andgatech/AHTech/recipe/machineRecipe/ElectronicsMarketRecipePool.java`
- Modify: `src/main/java/com/andgatech/AHTech/loader/RecipeLoader.java`

- [ ] **Step 1: Create the recipe pool class**

```java
package com.andgatech.AHTech.recipe.machineRecipe;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import gregtech.api.enums.GTValues;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.recipe.RecipeMaps;

import com.andgatech.AHTech.recipe.recipeMap.AHTechRecipeMaps;

public class ElectronicsMarketRecipePool {

    public static void loadRecipes() {
        final IRecipeMap EM = AHTechRecipeMaps.ElectronicsMarketRecipes;

        // Cable disassembly: GT Cable -> Rubber + Wire
        // Stage I+ available (no special value gating needed, handled by machine tier check)
        // TODO: Add specific cable disassembly recipes per cable tier
        // Example for copper cable:
        // GTValues.RA.stdBuilder()
        //     .itemInputs(ItemList.Cable_Cu.getItem(1))
        //     .itemOutputs(Materials.Copper.getDust(1), Materials.Rubber.getDust(1))
        //     .eut(GTValues.RECIPE_LV)
        //     .duration(100)
        //     .addTo(EM);

        // Laser Vacuum Tube: Vanilla Glass + Iridium-Osmium Alloy Foil -> Laser Vacuum Tube
        // Stage II+ only (specialValue = 2 means stage >= 2)
        // TODO: verify exact item references for Iridium-Osmium Alloy Foil and Laser Vacuum Tube
        // GTValues.RA.stdBuilder()
        //     .itemInputs(new ItemStack(Items.glass_bottle), Materials.IridiumOsmiumAlloy.getFoil(1))
        //     .itemOutputs(ItemList.LaserVacuumTube.get(1))
        //     .specialValue(2)
        //     .eut(GTValues.RECIPE_HV)
        //     .duration(200)
        //     .addTo(EM);
    }
}
```

Note: The exact item references (cable items, Iridium-Osmium Alloy Foil, Laser Vacuum Tube) need to be verified against GT5-Unofficial's actual `ItemList` and `Materials` constants. The implementing agent should search for the correct item names.

- [ ] **Step 2: Add recipe pool loading to RecipeLoader.loadRecipes()**

Add inside `RecipeLoader.loadRecipes()`:

```java
ElectronicsMarketRecipePool.loadRecipes();
```

- [ ] **Step 3: Compile-check**

Run: `./gradlew compileJava`

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/andgatech/AHTech/recipe/machineRecipe/ElectronicsMarketRecipePool.java src/main/java/com/andgatech/AHTech/loader/RecipeLoader.java
git commit -m "feat: add Electronics Market special recipe pool (cable, vacuum tube)"
```

---

### Task 6: Create Auto-Parse Recycling Recipe Generator

**Files:**
- Create: `src/main/java/com/andgatech/AHTech/recipe/machineRecipe/RecyclingRecipeGenerator.java`
- Modify: `src/main/java/com/andgatech/AHTech/loader/RecipeLoader.java`
- Modify: `src/main/java/com/andgatech/AHTech/CommonProxy.java`

- [ ] **Step 1: Create the RecyclingRecipeGenerator class**

This class scans all GT RecipeMaps and Forge CraftingManager to generate recycling recipes.

```java
package com.andgatech.AHTech.recipe.machineRecipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;

import gregtech.api.enums.GTValues;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.recipe.recipeProperty.RecipeProperty;
import gregtech.api.util.GTRecipe;

import com.andgatech.AHTech.AndgateTechnology;
import com.andgatech.AHTech.config.Config;
import com.andgatech.AHTech.recipe.recipeMap.AHTechRecipeMaps;

public class RecyclingRecipeGenerator {

    public static void generateRecyclingRecipes() {
        AndgateTechnology.LOG.info("Generating recycling recipes for Electronics Market...");

        // Map: output ItemStack -> list of input ItemStack[] from recipes
        Map<ItemStackKey, List<ItemStack[]>> machineToIngredients = new HashMap<>();

        // Scan all GT RecipeMaps
        for (RecipeMap<?> recipeMap : getAllGTRecipeMaps()) {
            for (GTRecipe recipe : recipeMap.getAllRecipes()) {
                if (recipe.mOutputs == null || recipe.mOutputs.length == 0) continue;
                for (ItemStack output : recipe.mOutputs) {
                    if (output == null) continue;
                    ItemStackKey key = new ItemStackKey(output);
                    machineToIngredients.computeIfAbsent(key, k -> new ArrayList<>())
                        .add(recipe.mInputs.clone());
                }
            }
        }

        // Scan Forge CraftingManager
        for (IRecipe recipe : (List<IRecipe>) CraftingManager.getInstance().getRecipeList()) {
            ItemStack output = recipe.getRecipeOutput();
            if (output == null) continue;
            // Extract ingredients from crafting recipe
            ItemStack[] inputs = extractCraftingIngredients(recipe);
            if (inputs != null && inputs.length > 0) {
                ItemStackKey key = new ItemStackKey(output);
                machineToIngredients.computeIfAbsent(key, k -> new ArrayList<>()).add(inputs);
            }
        }

        // Generate recycling recipes
        int count = 0;
        for (Map.Entry<ItemStackKey, List<ItemStack[]>> entry : machineToIngredients.entrySet()) {
            ItemStack machine = entry.getKey().toStack();
            List<ItemStack[]> ingredientLists = entry.getValue();
            // Use the first found recipe's ingredients for recycling
            ItemStack[] ingredients = ingredientLists.get(0);

            ItemStack[] recycledOutputs = generateRecycledOutputs(ingredients);
            if (recycledOutputs != null && recycledOutputs.length > 0) {
                GTValues.RA.stdBuilder()
                    .itemInputs(machine)
                    .itemOutputs(recycledOutputs)
                    .eut(GTValues.RECIPE_LV)
                    .duration(200)
                    .addTo(AHTechRecipeMaps.ElectronicsMarketRecipes);
                count++;
            }
        }

        AndgateTechnology.LOG.info("Generated {} recycling recipes.", count);
    }

    private static ItemStack[] generateRecycledOutputs(ItemStack[] ingredients) {
        List<ItemStack> outputs = new ArrayList<>();
        // Use a fixed recovery rate for recipe registration; actual rate is applied at runtime
        // by the machine's ProcessingLogic based on structure tier and voltage
        for (ItemStack ingredient : ingredients) {
            if (ingredient == null) continue;
            if (isCircuitBoard(ingredient)) {
                // Circuit boards: 100% recovery
                outputs.add(ingredient.copy());
            }
            // Other materials are handled at runtime by the machine's tier-dependent logic
            // For recipe display in NEI, we register with the full output
            else {
                outputs.add(ingredient.copy());
            }
        }
        return outputs.toArray(new ItemStack[0]);
    }

    private static boolean isCircuitBoard(ItemStack stack) {
        // Check if the item is a GT circuit board
        // GT circuit boards are in ItemList with names like "Circuit_Board", "Circuit_Board_Basic", etc.
        // Also check MaterialList PCBs
        // TODO: implement proper circuit board detection using GT API
        String unlocalizedName = stack.getUnlocalizedName().toLowerCase();
        return unlocalizedName.contains("circuit_board")
            || unlocalizedName.contains("circuitboard")
            || unlocalizedName.contains("pcb")
            || unlocalizedName.contains("boardraw");
    }

    private static ItemStack[] extractCraftingIngredients(IRecipe recipe) {
        // Extract input ItemStack[] from a Forge IRecipe
        // TODO: implement proper ingredient extraction
        // This depends on the specific IRecipe implementation
        return null;
    }

    private static RecipeMap<?>[] getAllGTRecipeMaps() {
        // Return all relevant GT RecipeMaps for scanning
        return new RecipeMap<?>[] {
            RecipeMaps.assemblerRecipes,
            RecipeMaps.benderRecipes,
            RecipeMaps.pressRecipes,
            RecipeMaps.latheRecipes,
            RecipeMaps.millingRecipes,
            RecipeMaps.cuttingRecipes,
            RecipeMaps.wiremillRecipes,
            RecipeMaps.polarizerRecipes,
            RecipeMaps.chemicalBathRecipes,
            RecipeMaps.autoclaveRecipes,
            RecipeMaps.centrifugeRecipes,
            RecipeMaps.electrolyzerRecipes,
            RecipeMaps.extractorRecipes,
            RecipeMaps.mixerRecipes,
            RecipeMaps.compressorRecipes,
            RecipeMaps.vacuumFreezerRecipes,
            RecipeMaps.arcFurnaceRecipes,
            RecipeMaps.plasmaArcFurnaceRecipes,
            RecipeMaps.blastFurnaceRecipes,
            RecipeMaps.implosionCompressorRecipes,
            RecipeMaps.multiblockChemicalReactorRecipes,
            RecipeMaps.distilleryRecipes,
            RecipeMaps.crackerRecipes,
            RecipeMaps.pyrolyseOvenRecipes,
            RecipeMaps.laserEngraverRecipes,
            RecipeMaps.formingPressRecipes,
            RecipeMaps.sifterRecipes,
            RecipeMaps.cannerRecipes,
            RecipeMaps.fluidCannerRecipes,
            RecipeMaps.fluidSolidifierRecipes,
            RecipeMaps.fluidExtractorRecipes,
            RecipeMaps.breweryRecipes,
            RecipeMaps.fusionReactorRecipes,
        };
    }

    /** Helper key class for ItemStack grouping (ignoring stack size). */
    private static class ItemStackKey {
        private final net.minecraft.item.Item item;
        private final int meta;

        ItemStackKey(ItemStack stack) {
            this.item = stack.getItem();
            this.meta = stack.getItemDamage();
        }

        ItemStack toStack() {
            return new ItemStack(item, 1, meta);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ItemStackKey)) return false;
            ItemStackKey that = (ItemStackKey) o;
            return item == that.item && meta == that.meta;
        }

        @Override
        public int hashCode() {
            return 31 * item.hashCode() + meta;
        }
    }
}
```

**Important:** This is a skeleton that the implementing agent must refine. Key TODOs:
1. `extractCraftingIngredients()` — needs to handle `ShapedRecipes`, `ShapelessRecipes`, and `ShapedOreRecipe`/`ShapelessOreRecipe` from Forge
2. `isCircuitBoard()` — needs to verify against GT5-Unofficial's actual circuit board item registry
3. The actual recovery rate logic should be applied at runtime in the machine's `ProcessingLogic`, not at recipe registration time, so that the same recipe works across all tiers with different rates

- [ ] **Step 2: Add generator call to RecipeLoader.loadRecipesServerStarted()**

Add inside `RecipeLoader.loadRecipesServerStarted()`:

```java
if (Config.Enable_ElectronicsMarket) {
    RecyclingRecipeGenerator.generateRecyclingRecipes();
}
```

- [ ] **Step 3: Update CommonProxy to call serverStarted recipes**

Verify `CommonProxy.serverStarted()` calls `RecipeLoader.loadRecipesServerStarted()`. If not, add the call.

- [ ] **Step 4: Compile-check**

Run: `./gradlew compileJava`

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/andgatech/AHTech/recipe/machineRecipe/RecyclingRecipeGenerator.java src/main/java/com/andgatech/AHTech/loader/RecipeLoader.java src/main/java/com/andgatech/AHTech/CommonProxy.java
git commit -m "feat: add auto-parse recycling recipe generator"
```

---

### Task 7: Build Verification and Spotless

- [ ] **Step 1: Run spotlessApply**

Run: `./gradlew spotlessApply`

- [ ] **Step 2: Run full build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Fix any issues and commit**

If build fails, fix issues, then:

```bash
git add -A
git commit -m "fix: resolve build issues for Electronics Market"
```

---

### Task 8: Update Project Documentation

**Files:**
- Modify: `log.md`, `ToDOLIST.md`, `context.md`

- [ ] **Step 1: Update log.md with implementation work**

- [ ] **Step 2: Update ToDOLIST.md — move completed items**

- [ ] **Step 3: Update context.md — reflect implemented machines, recipes, configs**

- [ ] **Step 4: Commit**

```bash
git add log.md ToDOLIST.md context.md
git commit -m "docs: update project documentation for Electronics Market"
```
