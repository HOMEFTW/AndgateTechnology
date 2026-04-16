# 项目上下文

## 基本信息
- Mod 名称: Andgate Technology
- Mod ID: AndgateTechnology
- Mod ID（小写）: andgatetechnology
- 包名: com.andgatech.AHTech
- 目标环境: MC 1.7.10 + Forge 10.13.4.1614 + GTNH 2.8.4
- Gradle: 8.14.3, RetroFuturaGradle + GTNH Convention 1.0.50

## 2026-04-15 最新审查状态
- 已完成对 `ToDOLIST.md`、`log.md`、`context.md` 与核心代码的静态检查，并通过 `./gradlew test` 验证当前测试基线
- 这两项审查问题现已处理：
  - `ElectronicsMarket` 通过 `getModulesSubjectToMaintenance()` 显式限定维护耗电池，仅统计 AHTech 原生模块；TST 互通模块继续沿用 TST 原行为，不参与这层自定义维护耗电
  - `ExecutionCoreBase` 已补齐输出物品、输出流体、进度与耗电参数的 NBT 持久化，并抽出 `saveExecutionState(...)` / `loadExecutionState(...)` 便于复用与测试
- 新增测试已覆盖上述行为；现有测试同时覆盖 loader 配置、`ElectronicsMarket` 信息/资金行为、执行核心接线闭环、执行核心状态持久化、回收配方确定性，以及自动回收配方的多产物/流体输入边界
- `RecyclingRecipeGenerator` 的两项审查问题现已处理：
  - 输出候选去重已改为包含 `(item, damage, stackSize)`，`buildRecyclingRecipes()` 会按正向输出数量生成对应的逆向输入，不再把多产物配方压缩成“1 个成品换整套原料”
  - `processGTRecipe()` 现已同时排除 `mFluidInputs` 与 `mFluidOutputs`，不再为带流体成本的 GT 配方生成错误回收配方
- 本轮 Gradle 级验证被外部依赖阻塞：`com.gtnewhorizons.gtnhconvention` 在配置阶段从 GitHub 拉取 manifest 失败，报 `Failed to load the manifest from Github` / `java.net.ConnectException`
- `ElectronicsMarket` 新近补齐两项行为修复：
  - 货币系统现在同时约束“可执行并行数”和“最终扣款总额”：匿名 `ProcessingLogic` 会在 `createParallelHelper(...)` 中按可支付货币数压低可执行并行，`consumeCurrencyFromRecipe()` 再按真实并行数扣除总成本
  - 回收率现在使用“整数部分 + 概率补余数”的方式结算，回收为 0 的非电路板产物会被清空；单件产物不再因 `Math.max(1, ...)` 被强制 100% 回收
- `ElectronicsMarketFinancialBehaviorTest` 已扩展覆盖：并行货币总价缩放、货币限制并行上限、单件产物在命中/未命中概率时的回收结果
- 当前测试验证已恢复可用：在本环境下需要通过 `./gradlew.bat "-Pelytra.manifest.version=true" test` 强制 Elytra conventions 使用项目内 `build/elytra_conventions/2.8.4.json` 缓存，否则插件会在配置阶段尝试联网拉取 manifest
- 在上述参数下，`ElectronicsMarketFinancialBehaviorTest`、`RecyclingRecipeGeneratorBehaviorTest` 以及全量 `test` 均已重新验证通过

## 已实现内容

### 机器
| 名称 | Meta ID | 类型 | 状态 |
|------|---------|------|------|
| 美弱南电子市场 | 35001 | 多方块 | 已实现，已模块化；支持合同等级、供应商舱口与配方 metadata 联动解锁 |

#### 静态并行控制器
| 名称 | Meta ID | 并行 |
|------|---------|------|
| 静态并行控制器 T1 | 35050 | 8 |
| 静态并行控制器 T2 | 35051 | 128 |
| 静态并行控制器 T3 | 35052 | 2048 |
| 静态并行控制器 T4 | 35053 | 32768 |
| 静态并行控制器 T5 | 35054 | 524288 |
| 静态并行控制器 T6 | 35055 | 8388608 |
| 静态并行控制器 T7 | 35056 | 134217728 |
| 静态并行控制器 T8 | 35057 | MAX |

#### 动态并行控制器
| 名称 | Meta ID | 并行 |
|------|---------|------|
| 动态并行控制器 T1 | 35058 | 8 |
| 动态并行控制器 T2 | 35059 | 128 |
| 动态并行控制器 T3 | 35060 | 2048 |
| 动态并行控制器 T4 | 35061 | 32768 |
| 动态并行控制器 T5 | 35062 | 524288 |
| 动态并行控制器 T6 | 35063 | 8388608 |
| 动态并行控制器 T7 | 35064 | 134217728 |
| 动态并行控制器 T8 | 35065 | MAX |

#### 静态速度控制器
| 名称 | Meta ID | 速度加成 |
|------|---------|----------|
| 静态速度控制器 T1 | 35066 | 2x |
| 静态速度控制器 T2 | 35067 | 4x |
| 静态速度控制器 T3 | 35068 | 8x |
| 静态速度控制器 T4 | 35069 | 16x |
| 静态速度控制器 T5 | 35070 | 32x |
| 静态速度控制器 T6 | 35071 | 64x |
| 静态速度控制器 T7 | 35072 | 128x |
| 静态速度控制器 T8 | 35073 | 256x |

#### 动态速度控制器
| 名称 | Meta ID | 速度加成 |
|------|---------|----------|
| 动态速度控制器 T1 | 35074 | 2x |
| 动态速度控制器 T2 | 35075 | 4x |
| 动态速度控制器 T3 | 35076 | 8x |
| 动态速度控制器 T4 | 35077 | 16x |
| 动态速度控制器 T5 | 35078 | 32x |
| 动态速度控制器 T6 | 35079 | 64x |
| 动态速度控制器 T7 | 35080 | 128x |
| 动态速度控制器 T8 | 35081 | 256x |

#### 功耗控制器
| 名称 | Meta ID | EU 倍率 |
|------|---------|---------|
| 功耗控制器 T1 | 35082 | 0.95 |
| 功耗控制器 T2 | 35083 | 0.9 |
| 功耗控制器 T3 | 35084 | 0.85 |
| 功耗控制器 T4 | 35085 | 0.8 |
| 功耗控制器 T5 | 35086 | 0.75 |
| 功耗控制器 T6 | 35087 | 0.7 |
| 功耗控制器 T7 | 35088 | 0.5 |
| 功耗控制器 T8 | 35089 | 0.25 |

#### 超频控制器
| 名称 | Meta ID | 参数 |
|------|---------|------|
| 超频控制器（低速完美） | 35090 | (2,2) |
| 超频控制器（完美） | 35091 | (4,4) |
| 超频控制器（奇点） | 35092 | (8,4) |

#### 执行核心
| 名称 | Meta ID | 能力 |
|------|---------|------|
| 执行核心（普通） | 35093 | 基础 |
| 执行核心（高级） | 35094 | 无线 EU |
| 执行核心（完美） | 35095 | 1 秒完成 |

#### 回收率模块
| 名称 | Meta ID | 回收率 |
|------|---------|--------|
| 回收率模块 Lv1 | 35096 | 50% |
| 回收率模块 Lv2 | 35097 | 70% |
| 回收率模块 Lv3 | 35098 | 90% |

#### 功能模块
| 名称 | Meta ID | 说明 |
|------|---------|------|
| 通用拆解模块 | 35099 | 启用自动回收配方 |

#### 供应商舱口
| 名称 | Meta ID | 合同要求 |
|------|---------|---------|
| 山东德州仪器 | 35100 | Lv1 |
| 亚德骗半导体 | 35101 | Lv1 |
| 二法半导体 | 35102 | Lv1 |
| 超拉半导体 | 35103 | Lv2 |
| 黄伟达 | 35104 | Lv2 |
| 钙光 | 35105 | Lv2 |
| 低通 | 35106 | Lv3 |

基类: `MTEExtendedPowerMultiBlockBase<ElectronicsMarket>`

结构等级: `ofBlocksTiered()`
- 一阶段: Tungstensteel Casing
- 二阶段: Stable Titanium Casing
- 三阶段: Prediction Casing

电压影响:
- 影响速度（基于阶段的速度加成）
- 影响并行（`tier²×4×(1+voltage/8)`）

三阶段效果:
- 启用 Perfect Overclock

UI:
- 自定义 `addUIWidgets`，显示阶段、并行、速度加成、回收率、完美超频状态、合同等级、供应商数量
- Stage / Parallel / Speed / Recovery: `TextWidget + FakeSyncWidget + synced*` 缓存字段（多人同步安全）
- Perfect OC: `DynamicTextWidget` 动态颜色（自同步，无需 `FakeSyncWidget`）
- 舱口标签使用 AHTech 自有本地化键（`AHTech.UI.Dynamic*`）

工业信息屏集成:
- `getInfoData()`: 为标准 GT 传感卡提供稳定的 8 行输出
- `reportMetrics()`: 为高级传感卡 / Metrics Transmitter / 工业信息屏提供稳定输出
- 导出字段: 机器状态、阶段/合同、供应商/并行、速度加成、回收率/完美超频、已安装功能模块、资金摘要

### 物品
| 名称 | 注册 | 说明 |
|------|------|------|
| 供货协议合同 Lv1 | `contract_lv1` | 基础合同物品，可解锁 Lv1 供应商 |
| 供货协议合同 Lv2 | `contract_lv2` | 进阶合同物品，需要 Lv1 合同参与合成 |
| 供货协议合同 Lv3 | `contract_lv3` | 高级合同物品，需要 Lv2 合同参与合成 |
| 供货协议合同 Lv4 | `contract_lv4` | 顶级合同物品，需要 Lv3 合同参与合成 |

### 方块
_(暂无)_

### 材料
_(暂无)_

### 配方映射
| 配方映射 | I/O | 说明 |
|----------|-----|------|
| AHTechRecipeMaps.ElectronicsMarketRecipes | 9in/9out/4fin/4fout | 美弱南电子市场自定义配方映射 |

### 配方池
| 配方池 | 类型 | 说明 |
|--------|------|------|
| ElectronicsMarketRecipePool | 硬编码 | 线缆拆解（36 材质 × 6 尺寸，`shandong_dezhou`）、Laser Vacuum Pipe（TecTech，`gaiguang`） |
| RecyclingRecipeGenerator | 自动解析 | 在 `serverStarted` 扫描 GT RecipeMaps 与 Forge CraftingManager |
| ShapedCraftRecipePool | 合成 | 合同 Lv1-Lv4 与 7 个供应商舱口的工作台配方 |

### 配置项
| 键名 | 默认值 | 说明 |
|------|--------|------|
| MAX_PARALLEL_LIMIT | 256 | 所有机器的最大并行上限 |
| DEFAULT_BATCH_MODE | false | 机器默认批处理模式 |
| Enable_ElectronicsMarket | true | 启用/禁用美弱南电子市场 |
| Stage1_BaseRecoveryRate | 0.30 | 一阶段基础回收率 |
| EnableModularizedMachineSystem | true | 启用/禁用模块化系统 |
| RecoveryModuleLv1Rate | 0.50 | Lv1 回收率模块数值 |
| RecoveryModuleLv2Rate | 0.70 | Lv2 回收率模块数值 |
| RecoveryModuleLv3Rate | 0.90 | Lv3 回收率模块数值 |
| TST_StandardModules | (array) | TST 标准模块配置数组（并行/速度/超频/功耗/执行核心的 tier 与参数） |

### Mixins
_(暂无)_

## 关键类
- `com.andgatech.AHTech.common.machine.ElectronicsMarket`：多方块控制器
- `com.andgatech.AHTech.common.contract.ContractTier`：合同等级枚举与本地化键
- `com.andgatech.AHTech.common.contract.ContractItem`：合同物品定义与最高合同等级检索
- `com.andgatech.AHTech.common.supplier.SupplierId`：供应商枚举、最低合同要求与本地化键
- `com.andgatech.AHTech.common.supplier.SupplierHatch`：供应商舱口定义
- `com.andgatech.AHTech.loader.ContractLoader`：合同物品注册入口
- `com.andgatech.AHTech.recipe.recipeMap.AHTechRecipeMaps`：自定义配方映射定义
- `com.andgatech.AHTech.recipe.machineRecipe.ElectronicsMarketRecipePool`：硬编码配方
- `com.andgatech.AHTech.recipe.machineRecipe.RecyclingRecipeGenerator`：自动解析回收配方生成器
- `com.andgatech.AHTech.recipe.metadata.AHTechRecipeMetadata`：AHTech 自定义配方 metadata key

## 关键类：模块化系统
- `com.andgatech.AHTech.common.modularizedMachine.ModularizedMachineBase`：感知模块的多方块基类
- `com.andgatech.AHTech.common.modularizedMachine.ModularizedMachineSupportAllModuleBase`：支持完整参数集的模块化机器基类
- `com.andgatech.AHTech.common.modularizedMachine.modularHatches.ModularHatchBase`：模块舱口基类
- `com.andgatech.AHTech.common.modularizedMachine.modularHatches.RecoveryRateModule`：回收率控制模块
- `com.andgatech.AHTech.common.modularizedMachine.modularHatches.GeneralDisassemblyModule`：功能模块

## 依赖
- GT5-Unofficial（GregTech + 已合并的 TecTech）
- GTNHLib
- NewHorizonsCoreMod
- IC2（`industrialcraft-2:2.2.828-experimental`）
- TST（可选，`compileOnly` 存根）

## 架构说明
- 生命周期: `preInit → 配置 + 材料`，`init → 机器`，`completeInit → 配方`，`serverStarted → 回收配方`
- 合同在 `preInit` 由 `ContractLoader.loadContracts()` 注册；供应商舱口在 `init` 只要 `Enable_ElectronicsMarket` 开启就注册，不再依赖 `EnableModularizedMachineSystem`
- I18n: `//#tr key value` 注释通过 `addon.gradle` 自动生成语言文件
- Mixin 包路径: `com.andgatech.AHTech.mixin`
- 资源命名空间: `assets/andgatetechnology/`
- 电路板通过 `unlocalizedName` 模式识别，始终按 100% 回收
- 回收率在运行时由 `ProcessingLogic` 应用，而不是在配方注册时写死
- 项目记录文档（`log.md`、`context.md`、`ToDOLIST.md`）现按“中文优先、技术标识保留原文”的规则维护
- `ElectronicsMarket` 会从 `MTEHatchDataAccess` 读取其中物品，提取最高 `ContractTier` 作为当前合同等级
- 供应商配方通过 `AHTechRecipeMetadata.SUPPLIER_ID` 标记，机器在原有 `specialValue` 校验之后追加供应商门禁
- 货币物品与压币配方只在 `Enable_ElectronicsMarket && EnableFinancialSystem` 时加载，避免出现“有货币/配方但无资金舱”的死状态

## 架构说明：模块化系统
- 基类层次: `ModularizedMachineSupportAllModuleBase<T> extends ModularizedMachineBase<T> extends MTEExtendedPowerMultiBlockBase<T>`
- 模块发现: `addToMachineList()` 在结构扫描时识别 `ModularHatchBase`
- `ModularHatchType.ALL` 视为通配类型，支持 `ModularizedMachineSupportAllModuleBase` 正确识别新增的 `SUPPLIER` 舱口
- 静态模块: 在 `checkMachine()` 期间通过 `onCheckMachine()` 应用
- 动态模块: 在 `checkProcessing()` 期间通过 `onCheckProcessing()` 应用（预留给未来）
- 一阶段: 固定 30% 回收率、硬编码 4 并行、不应用模块化系统（`checkModularStaticSettings()` 覆写跳过模块应用）、仅允许硬编码基础配方
- 二阶段及以上: 回收率与性能由已安装模块决定；功能模块决定可用配方类别
- 所有模块（包括并行/速度/超频/功耗/执行核心）均在 `onCheckMachine()` / `onCheckProcessing()` 中通过 `isCompatibleWithMachine()` 检查等级兼容性；高等级模块安装到低等级结构上静默不生效
- 配方分类: `specialValue(0)=硬编码`，`specialValue(1)=通用拆解`，`specialValue(2)=需要二阶段及以上`
- 供应商模块属于独立的模块类型 `ModularHatchType.SUPPLIER`，激活列表受合同等级限制
- 供应商舱口与 `FinancialHatch` 的注册不再依赖标准模块开关；即使关闭 `EnableModularizedMachineSystem`，只要对应功能开启，它们仍保持可用
- 回收率在运行时应用到输出物品；电路板始终 100% 回收
- TST 模块互通: 当 TST 已安装时，AHTech 不再注册重复的标准模块（并行/速度/超频/功耗/执行核心）
- AHTech 多方块结构可识别 TST 的模块舱口并应用其效果
- 互通通过编译时存根类实现，运行时通过 `Loader.isModLoaded("TwistSpaceTechnology")` 检测 TST，避免被本地存根类误判
- AHTech 专属模块（回收率、功能模块）始终注册，不受 TST 影响
- 工业信息屏输出刻意保持固定顺序，避免高级传感卡的按行过滤在机器重启或更换模块后失效
- 自动回收配方生成对同一输出采用稳定候选规则：优先更少输入数，其次更少总输入量，最后按规范化签名字典序打破平手

## 2026-04-15 阶段二补全
- 模块等级门控已全部生效：所有 9 类模块的 `onCheckMachine()` / `onCheckProcessing()` 均调用 `isCompatibleWithMachine()`
- Stage I 通过 `ElectronicsMarket.checkModularStaticSettings()` 覆写显式跳过模块应用，仅使用硬编码参数
- 模块维护耗电：`ModularHatchBase.getMaintenanceEUt()` 按 tier 缩放（T7=1024, T8=2048...），tier=0 无成本
- 供电不足吞材料：`ElectronicsMarket.checkProcessingMM()` 在配方成功后检查存储 EU 是否覆盖模块维护耗电；存储 EU 与维护耗电相等时视为充足，不足时产出清零
- 配置项 `Config.EnablePowerInsufficientMaterialLoss` 控制吞材料开关（默认开启）

## 2026-04-15 最新补充
- `ElectronicsMarket` 现已支持资金系统：扫描 `FinancialHatch`、汇总余额、在配方资金校验前触发自动补币、校验配方币种/消耗、成功处理后扣款，并在信息输出中展示资金摘要
- 已存在的资金系统核心文件：
  - `src/main/java/com/andgatech/AHTech/common/currency/CurrencyType.java`
  - `src/main/java/com/andgatech/AHTech/common/currency/CurrencyItem.java`
  - `src/main/java/com/andgatech/AHTech/common/modularizedMachine/modularHatches/FinancialHatch.java`
  - `src/main/java/com/andgatech/AHTech/recipe/machineRecipe/CurrencyRecipePool.java`
- 本轮回归修复新增 `prepareFinancialStateForRecipeCheck()` 与 `hasEnoughStoredPower(...)`，分别用于配方前自动补币与模块维护耗电边界判定
- `src/main/resources/assets/andgatetechnology/lang/zh_CN.lang` 已整体重写为正常 UTF-8 中文文件；在 PowerShell 中检查该文件时应优先使用 `Get-Content -Encoding UTF8`
- 资金系统相关中文 key 已覆盖 `ElectronicsMarket` 资金状态、`FinancialHatch` 描述/自动补币以及 6 种货币名称
- 最近一次验证结果：
  - `./gradlew test --tests com.andgatech.AHTech.loader.LoaderConfigBehaviorTest --tests com.andgatech.AHTech.recipe.machineRecipe.RecyclingRecipeGeneratorDeterminismTest` 通过
  - `./gradlew test --tests com.andgatech.AHTech.common.machine.ElectronicsMarketInformationTest --tests com.andgatech.AHTech.common.machine.ElectronicsMarketFinancialBehaviorTest` 通过
  - `./gradlew compileJava` 通过
  - `./gradlew test` 通过

## 2026-04-15 审查记录（已修复）
- 当时发现 ModularizedMachineBase 仍使用 Class.forName("com.Nxer.TwistSpaceTechnology.TwistSpaceTechnology") 进行 TST 加载判断，现已改为与 MachineLoader 一致的 Loader.isModLoaded(...)
- 当时发现 ExecutionCoreBase 仅定义了状态字段和 EXECUTION_CORE hatch 类型；现已补齐 hasBeenSetup 绑定路径与主机关联
- 当时发现 ModularizedMachineSupportAllModuleBase.checkProcessingMM() 只走 GT processingLogic / checkRecipe；现已在 ModularizedMachineBase.checkProcessing() 成功后补上向执行核心的任务转交
## 2026-04-15 模块化执行核心修复
- ModularizedMachineBase.isTSTLoaded() 已改为委托 Loader.isModLoaded("TwistSpaceTechnology")，并提供 isTSTLoaded(Predicate<String>) 便于测试
- ExecutionCoreBase 现已具备主机绑定能力：setup(ModularizedMachineBase<?>) 会在结构检查阶段绑定主机，完成工作后通过主机侧 mergeOutputItems(...) / mergeOutputFluids(...) 回流产物
- ModularizedMachineBase.checkProcessing() 在已有 processingLogic 成功匹配配方后，会将当前配方结果优先转交给空闲执行核心；若执行核心接管成功，主机自身进度与输出字段会被清空
- 使用主机供电的执行核心现在会在 ModularizedMachineBase.onPostTick() 中按 tick 消耗主机能源；掉电时会统一关闭执行核心并触发 POWER_LOSS
- 新增测试：
  - src/test/java/com/andgatech/AHTech/common/modularizedMachine/ExecutionCoreIntegrationTest.java
  - src/test/java/com/andgatech/AHTech/loader/LoaderConfigBehaviorTest.java 新增模块化基类的 TST 检测覆盖
