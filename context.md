# Project Context

## Basic Info
- Mod Name: Andgate Technology
- Mod ID: AndgateTechnology
- Mod ID (lower): andgatetechnology
- Package: com.andgatech.AHTech
- Target: MC 1.7.10 + Forge 10.13.4.1614 + GTNH 2.8.4
- Gradle: 8.14.3, RetroFuturaGradle + GTNH Convention 1.0.50

## Implemented Content

### Machines
| Name | Meta ID | Type | Status |
|------|---------|------|--------|
| 美弱南电子市场 | 35001 | Multiblock | Implemented, placeholder structure |

Base class: `MTEExtendedPowerMultiBlockBase<ElectronicsMarket>`
Structure tier: `ofBlocksTiered()` — Tier I: Tungstensteel Casing, Tier II: Stable Titanium Casing, Tier III: Prediction Casing
Voltage: affects speed (tier-based bonus) and parallel (tier²×4×(1+voltage/8))
Stage III: enables Perfect Overclock
UI: custom `addUIWidgets` override displaying stage, parallel, speed bonus, perfect OC status (synced via FakeSyncWidget)

### Items
| Name | Registration | Description |
|------|-------------|-------------|
_(none yet)_

### Blocks
| Name | Registration | Description |
|------|-------------|-------------|
_(none yet)_

### Materials
_(none yet)_

### Recipe Maps
| RecipeMap | I/O | Description |
|-----------|-----|-------------|
| AHTechRecipeMaps.ElectronicsMarketRecipes | 9in/9out/4fin/4fout | Custom map for Electronics Market |

### Recipe Pools
| Recipe Pool | Type | Description |
|-------------|------|-------------|
| ElectronicsMarketRecipePool | Hardcoded | Cable disassembly (36 materials × 6 sizes), Laser Vacuum Pipe (TecTech, Stage II+) |
| RecyclingRecipeGenerator | Auto-parsed | Scans GT RecipeMaps + Forge CraftingManager at serverStarted |
| ShapedCraftRecipePool | Crafting | Empty placeholder |

### Config Options
| Key | Default | Description |
|-----|---------|-------------|
| MAX_PARALLEL_LIMIT | 256 | Max parallel for all machines |
| DEFAULT_BATCH_MODE | false | Default batch mode for machines |
| Enable_ElectronicsMarket | true | Enable/disable Electronics Market |
| Stage1_BaseRecoveryRate | 0.30 | Stage I base recycling rate |
| Stage2_BaseRecoveryRate | 0.60 | Stage II base recycling rate |
| Stage3_BaseRecoveryRate | 0.90 | Stage III base recycling rate |
| VoltageBonusPerTier | 0.02 | Recovery rate bonus per voltage tier |

### Mixins
_(none yet)_

## Key Classes
- `com.andgatech.AHTech.common.machine.ElectronicsMarket` — Multiblock controller
- `com.andgatech.AHTech.recipe.recipeMap.AHTechRecipeMaps` — Custom RecipeMap definitions
- `com.andgatech.AHTech.recipe.machineRecipe.ElectronicsMarketRecipePool` — Hardcoded recipes
- `com.andgatech.AHTech.recipe.machineRecipe.RecyclingRecipeGenerator` — Auto-parse generator

## Dependencies
- GT5-Unofficial (GregTech + merged TecTech)
- GTNHLib
- NewHorizonsCoreMod
- IC2 (industrialcraft-2:2.2.828-experimental)

## Architecture Notes
- Lifecycle: preInit → Config + Materials; init → Machines; completeInit → Recipes; serverStarted → Recycling recipes
- I18n: `//#tr key value` comments auto-generate lang files via addon.gradle
- Mixin package: `com.andgatech.AHTech.mixin`
- Resource namespace: `assets/andgatetechnology/`
- Circuit boards detected by unlocalizedName patterns, marked for 100% recovery
- Recycling rate applied at runtime by ProcessingLogic, not at recipe registration
