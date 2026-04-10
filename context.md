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
| 美弱南电子市场 | 35001 | Multiblock | Implemented, modularized (Stage I hardcoded, Stage II+ module-driven) |
| 并行控制器 Lv1 | 35050 | Modular Hatch | Framework validation |
| 速度控制器 Lv1 | 35051 | Modular Hatch | Framework validation |
| 超频控制器 Lv1 | 35052 | Modular Hatch | Framework validation |
| 功耗控制器 Lv1 | 35053 | Modular Hatch | Framework validation |
| 回收率模块 Lv1 | 35054 | Modular Hatch | Recovery 50% |
| 回收率模块 Lv2 | 35055 | Modular Hatch | Recovery 70% |
| 回收率模块 Lv3 | 35056 | Modular Hatch | Recovery 90% |
| 执行核心 | 35060 | Modular Hatch | Framework validation |
| 通用拆解模块 | 35070 | Function Module | Activates auto-recycling recipes |

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
| EnableModularizedMachineSystem | true | Enable/disable modularization system |
| RecoveryModuleLv1Rate | 0.50 | Recovery rate for Lv1 module |
| RecoveryModuleLv2Rate | 0.70 | Recovery rate for Lv2 module |
| RecoveryModuleLv3Rate | 0.90 | Recovery rate for Lv3 module |

### Mixins
_(none yet)_

## Key Classes
- `com.andgatech.AHTech.common.machine.ElectronicsMarket` — Multiblock controller
- `com.andgatech.AHTech.recipe.recipeMap.AHTechRecipeMaps` — Custom RecipeMap definitions
- `com.andgatech.AHTech.recipe.machineRecipe.ElectronicsMarketRecipePool` — Hardcoded recipes
- `com.andgatech.AHTech.recipe.machineRecipe.RecyclingRecipeGenerator` — Auto-parse generator

## Key Classes — Modularization
- `com.andgatech.AHTech.common.modularizedMachine.ModularizedMachineBase` — Module-aware multiblock base
- `com.andgatech.AHTech.common.modularizedMachine.ModularizedMachineSupportAllModuleBase` — Full parameter support
- `com.andgatech.AHTech.common.modularizedMachine.modularHatches.ModularHatchBase` — Module hatch base class
- `com.andgatech.AHTech.common.modularizedMachine.modularHatches.RecoveryRateModule` — Recovery rate controller
- `com.andgatech.AHTech.common.modularizedMachine.modularHatches.GeneralDisassemblyModule` — Function module

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

## Architecture Notes — Modularization System
- Base class: ModularizedMachineSupportAllModuleBase<T> extends ModularizedMachineBase<T> extends MTEExtendedPowerMultiBlockBase<T>
- Module discovery: addToMachineList() detects ModularHatchBase in structure
- Static modules: applied during checkMachine() via onCheckMachine()
- Dynamic modules: applied during checkProcessing() via onCheckProcessing() (reserved for future)
- Tier I: hardcoded 30% recovery, no modules, hardcoded recipes only
- Tier II+: recovery/performance determined by installed modules; function modules gate recipe categories
- Recipe classification: specialValue(0)=hardcoded, specialValue(1)=GENERAL_DISASSEMBLY, specialValue(2)=Stage II+ required
- Recovery rate applied at runtime to output items; circuit boards always 100%
