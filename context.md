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

#### 静态并行控制器
| Name | Meta ID | Parallel |
|------|---------|----------|
| 静态并行控制器 T1 | 35050 | 8 |
| 静态并行控制器 T2 | 35051 | 128 |
| 静态并行控制器 T3 | 35052 | 2048 |
| 静态并行控制器 T4 | 35053 | 32768 |
| 静态并行控制器 T5 | 35054 | 524288 |
| 静态并行控制器 T6 | 35055 | 8388608 |
| 静态并行控制器 T7 | 35056 | 134217728 |
| 静态并行控制器 T8 | 35057 | MAX |

#### 动态并行控制器
| Name | Meta ID | Parallel |
|------|---------|----------|
| 动态并行控制器 T1 | 35058 | 8 |
| 动态并行控制器 T2 | 35059 | 128 |
| 动态并行控制器 T3 | 35060 | 2048 |
| 动态并行控制器 T4 | 35061 | 32768 |
| 动态并行控制器 T5 | 35062 | 524288 |
| 动态并行控制器 T6 | 35063 | 8388608 |
| 动态并行控制器 T7 | 35064 | 134217728 |
| 动态并行控制器 T8 | 35065 | MAX |

#### 静态速度控制器
| Name | Meta ID | Speed Bonus |
|------|---------|-------------|
| 静态速度控制器 T1 | 35066 | 2x |
| 静态速度控制器 T2 | 35067 | 4x |
| 静态速度控制器 T3 | 35068 | 8x |
| 静态速度控制器 T4 | 35069 | 16x |
| 静态速度控制器 T5 | 35070 | 32x |
| 静态速度控制器 T6 | 35071 | 64x |
| 静态速度控制器 T7 | 35072 | 128x |
| 静态速度控制器 T8 | 35073 | 256x |

#### 动态速度控制器
| Name | Meta ID | Speed Bonus |
|------|---------|-------------|
| 动态速度控制器 T1 | 35074 | 2x |
| 动态速度控制器 T2 | 35075 | 4x |
| 动态速度控制器 T3 | 35076 | 8x |
| 动态速度控制器 T4 | 35077 | 16x |
| 动态速度控制器 T5 | 35078 | 32x |
| 动态速度控制器 T6 | 35079 | 64x |
| 动态速度控制器 T7 | 35080 | 128x |
| 动态速度控制器 T8 | 35081 | 256x |

#### 功耗控制器
| Name | Meta ID | EU Multiplier |
|------|---------|---------------|
| 功耗控制器 T1 | 35082 | 0.95 |
| 功耗控制器 T2 | 35083 | 0.9 |
| 功耗控制器 T3 | 35084 | 0.85 |
| 功耗控制器 T4 | 35085 | 0.8 |
| 功耗控制器 T5 | 35086 | 0.75 |
| 功耗控制器 T6 | 35087 | 0.7 |
| 功耗控制器 T7 | 35088 | 0.5 |
| 功耗控制器 T8 | 35089 | 0.25 |

#### 超频控制器
| Name | Meta ID | Parameters |
|------|---------|------------|
| 超频控制器（低速完美） | 35090 | (2,2) |
| 超频控制器（完美） | 35091 | (4,4) |
| 超频控制器（奇点） | 35092 | (8,4) |

#### 执行核心
| Name | Meta ID | Ability |
|------|---------|---------|
| 执行核心（普通） | 35093 | Base |
| 执行核心（高级） | 35094 | Wireless EU |
| 执行核心（完美） | 35095 | 1-second completion |

#### 回收率模块
| Name | Meta ID | Recovery Rate |
|------|---------|---------------|
| 回收率模块 Lv1 | 35096 | 50% |
| 回收率模块 Lv2 | 35097 | 70% |
| 回收率模块 Lv3 | 35098 | 90% |

#### 功能模块
| Name | Meta ID | Description |
|------|---------|-------------|
| 通用拆解模块 | 35099 | Activates auto-recycling recipes |

Base class: `MTEExtendedPowerMultiBlockBase<ElectronicsMarket>`
Structure tier: `ofBlocksTiered()` — Tier I: Tungstensteel Casing, Tier II: Stable Titanium Casing, Tier III: Prediction Casing
Voltage: affects speed (tier-based bonus) and parallel (tier²×4×(1+voltage/8))
Stage III: enables Perfect Overclock
UI: custom `addUIWidgets` override displaying stage, parallel, speed bonus, recovery rate, perfect OC status
  - Stage/Parallel/Speed/Recovery: TextWidget + FakeSyncWidget + synced* cached fields (multiplayer-safe)
  - Perfect OC: DynamicTextWidget with dynamic color (self-syncing, no FakeSyncWidget needed)
  - Hatch labels: AHTech-owned localization keys (AHTech.UI.Dynamic*)

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
| TST_StandardModules | (array) | TST 标准模块配置数组（并行/速度/超频/功耗/执行核心 tiers & parameters） |

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
- TST（可选，compileOnly 存根）

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
- TST 模块互通：当 TST 已安装时，AHTech 不注册重复的标准模块（并行/速度/超频/功耗/执行核心）
- AHTech 多方块结构可识别 TST 的模块舱口并应用其效果
- 互通通过编译时存根类实现，运行时通过 Class.forName() 检测 TST
- AHTech 专属模块（回收率、功能模块）始终注册，不受 TST 影响
