# 模块化系统设计

> 日期：2026-04-10
> 状态：草稿

## 概述

移除美弱南电子市场中硬编码的二阶/三阶回收率系统，替换为 TST 风格的模块化舱口系统：

- 一阶：硬编码 30% 回收率，不可使用模块，仅限硬编码配方（线缆拆解、激光真空管）
- 二阶及以上：拆解能力和性能完全由安装的模块决定
- 未安装模块 = 无拆解能力（回收率 = 0）

模块分为两类：
1. **性能模块**（回收率、并行、速度、超频、功耗）——控制"拆多少"
2. **功能模块**（通用拆解、电路拆解、线缆拆解等）——控制"能拆什么"

本次实现范围：完整框架 + 回收率模块（3 级）+ 通用拆解功能模块 + 各一个 TST 标准模块用于框架验证。

---

## 1. 接口层次

```
IModularizedMachine
  ├── getSupportedModularHatchTypes() -> Collection<ModularHatchType>
  ├── resetModularStaticSettings() / applyModularStaticSettings()
  ├── getAllModularHatches() -> Collection<IModularHatch>
  ├── resetModularHatchCollections()
  │
  ├── ISupportParallelController        (性能：并行数)
  │     getStaticParallelParameterValue() / setStaticParallelParameter(int)
  │
  ├── ISupportSpeedController           (性能：速度加成)
  │     getStaticSpeedParameterValue() / setStaticSpeedParameterValue(float)
  │
  ├── ISupportOverclockController       (性能：超频类型)
  │     setOverclockType(OverclockType)
  │
  ├── ISupportPowerConsumptionController(性能：功耗)
  │     getStaticPowerConsumptionParameterValue() / setStaticPowerConsumptionParameterValue(float)
  │
  ├── ISupportRecoveryRateController    (性能：回收率) ← AHTech 新增
  │     getRecoveryRate() / setRecoveryRate(float)
  │
  ├── ISupportFunctionModule            (功能：配方类别) ← AHTech 新增
  │     getInstalledFunctionTypes() -> Set<FunctionType>
  │     addFunctionType(FunctionType)
  │
  └── ISupportAllModularHatches 聚合以上所有接口
```

## 2. 枚举类型

### ModularHatchType（模块舱口类型）

```
PARALLEL_CONTROLLER              并行控制器
SPEED_CONTROLLER                 速度控制器
OVERCLOCK_CONTROLLER             超频控制器
POWER_CONSUMPTION_CONTROLLER     功耗控制器
RECOVERY_RATE_CONTROLLER         回收率控制器（AHTech 新增）
EXECUTION_CORE                   执行核心
FUNCTION_MODULE                  功能模块（AHTech 新增）
ALL                              全部
```

### FunctionType（功能模块子类型）

```
GENERAL_DISASSEMBLY     通用拆解（本次实现）
CIRCUIT_DISASSEMBLY     电路拆解（后续）
CABLE_DISASSEMBLY       线缆拆解（后续）
COMPONENT_FACTORY       元器件工厂（后续）
CIRCUIT_BOARD_FACTORY   国产之光（后续）
```

### OverclockType（超频类型）

```
NONE(1, 1)                         无超频
NormalOverclock(2, 4)              普通超频
LowSpeedPerfectOverclock(2, 2)     低速完美超频
PerfectOverclock(4, 4)             完美超频
SingularityPerfectOverclock(8, 4)  奇点完美超频
EOHStupidOverclock(2, 8)           EOH 超频
```
参数含义：(时间缩减倍率, 功耗增加倍率)

## 3. 基类层次

```
MTEExtendedPowerMultiBlockBase<T>               (GT 基类)
  └── ModularizedMachineBase<T>                 实现 IModularizedMachine
        ├── 模块发现：addToMachineList() 中识别 ModularHatchBase
        ├── checkMachine(): 重置 → checkMachineMM → 验证 → 应用静态设置
        ├── checkProcessing(): 应用动态参数 → checkProcessingMM
        ├── abstract checkMachineMM()            子类实现结构检测
        ├── abstract canMultiplyModularHatchType()  是否允许同类多模块
        │
        └── ModularizedMachineSupportAllModuleBase<T>
              实现 ISupportAllModularHatches
              ├── 字段：staticParallel, staticSpeed, staticPower, overclockType
              ├── 字段：recoveryRate (float, 默认 0.0)
              ├── 字段：installedFunctionTypes (Set<FunctionType>)
              ├── getMaxParallelRecipes() -> int
              ├── getSpeedBonus() -> float
              ├── getEuModifier() -> float
              ├── isEnablePerfectOverclock() -> boolean
              ├── getRecoveryRate() -> float
              │     一阶返回硬编码 0.30f
              │     二/三阶返回 recoveryRate 字段（无模块 = 0.0）
              └── hasFunction(FunctionType) -> boolean
```

## 4. 模块舱口类层次

### 继承关系

```
ModularHatchBase extends MTEHatch implements IModularHatch
  ├── getType() -> ModularHatchType
  ├── getModuleTier() -> int (1-3 或 1-8，取决于模块类型)
  ├── isCompatibleWithMachine() -> boolean
  │     检查 machine.structureTier >= this.moduleTier
  │
  ├── 性能模块（均实现 IStaticModularHatch）
  │   ├── ParallelControllerModule      并行控制器，1-8 级
  │   ├── SpeedControllerModule         速度控制器，1-8 级
  │   ├── OverclockControllerModule     超频控制器，1-3 级
  │   ├── PowerConsumptionModule        功耗控制器，1-8 级
  │   └── RecoveryRateModule            回收率模块，1-3 级
  │
  ├── 执行核心（实现 IStaticModularHatch）
  │   ├── ExecutionCoreBase             执行核心基类
  │   ├── ExecutionCore                 普通核心（共享机器 EU）
  │   ├── AdvExecutionCore              高级核心（无线 EU）
  │   └── PerfectExecutionCore          完美核心（无线 EU，无限并行，1 秒完成）
  │
  └── 功能模块（实现 IStaticModularHatch）
        └── FunctionModuleBase
              ├── getFunctionType() -> FunctionType
              └── GeneralDisassemblyModule  通用拆解模块（激活自动回收配方）
```

### 回收率模块

| 等级 | 回收率 | 电路板回收率 | 结构要求 |
|------|--------|------------|---------|
| I | 50% | 100% | 二阶以上 |
| II | 70% | 100% | 二阶以上 |
| III | 90% | 100% | 三阶 |

所有数值通过 Config 可配置。

### 通用拆解功能模块

- 激活 `RecyclingRecipeGenerator` 生成的自动回收配方
- 不提供回收率——回收率由 `RecoveryRateModule` 决定
- 功能模块控制"能不能拆"，性能模块控制"拆多少"

## 5. 数据流

### 模块发现流程（checkMachine）

```
1. resetModularHatchCollections()     清空所有模块集合
2. checkMachineMM()                   子类执行结构检测，模块舱口被发现并加入集合
3. canMultiplyModularHatchType()      验证同类模块数量约束
4. checkModularStaticSettings():
     a. 重置: parallel=0, speed=1.0, power=1.0, recoveryRate=0.0, overclock=Normal, functions=空
     b. 应用: 遍历所有 IStaticModularHatch，调用 onCheckMachine(this)
        - 每个模块通过 instanceof 检查机器是否支持对应接口
        - 模块等级兼容性在 onCheckMachine 中检查
```

### 配方处理流程（checkProcessing）

```
1. applyModularDynamicParameters()    预留动态模块
2. checkProcessingMM():
     a. ProcessingLogic.validateRecipe(recipe):
          - 线缆拆解 / 激光真空管配方 → 始终通过
          - 自动回收配方 → 检查 hasFunction(GENERAL_DISASSEMBLY)
          - 未来其他配方类型 → 检查对应 FunctionType
          - 一阶：跳过模块检查，使用硬编码 30% 回收率
          - 二/三阶无模块：回收率=0，validateRecipe 返回失败
     b. ProcessingLogic.process():
          - 从模块设置 euModifier, speedBonus, overclock
          - 从模块设置 maxParallel
          - 执行配方
          - 对输出物品应用回收率：
              output.stackSize = Math.max(1, round(original * getRecoveryRate()))
              例外：isCircuitBoard() 的输出始终 100%
```

### 回收率应用机制

- 配方注册时仍按 100% 输出注册（保持现状不变）
- 回收率在运行时输出阶段应用
- 电路板通过 `isCircuitBoard()` 检测（现有逻辑在 RecyclingRecipeGenerator 中）→ 始终 100%
- 更换模块后回收率即时生效

### 配方分类标记

- 自动回收配方（`RecyclingRecipeGenerator` 生成）通过 `specialValue` 标记类型：
  - `specialValue(0)` 或默认：硬编码配方（线缆/激光管），不需功能模块
  - `specialValue(1)`：通用拆解配方，需要 `GENERAL_DISASSEMBLY` 功能模块
  - `specialValue(2)`：激光真空管配方，需要二阶以上（已存在的逻辑）
- `validateRecipe` 读取 `recipe.mSpecialValue` 判断配方类别

## 6. 阶段行为重新设计

| 属性 | 一阶 | 二阶 | 三阶 |
|------|------|------|------|
| 基础回收率 | 30%（硬编码） | 0%（无模块=不能拆解） | 0%（无模块=不能拆解） |
| 模块系统 | 不可用 | 可用，限 1-2 级模块 | 可用，1-3 级模块 |
| 配方类别 | 仅硬编码配方 | 由安装的功能模块决定 | 由安装的功能模块决定 |
| 性能参数 | 基础并行 4，无速度加成 | 由安装的性能模块决定 | 由安装的性能模块决定 |

`structureTier` 仍然存在，用途：
1. 门控模块可用性（一阶不能使用模块）
2. 门控模块等级上限（二阶不能插三级模块）
3. 决定一阶基础回收率

## 7. Meta ID 分配

| Meta ID | 类 | 说明 |
|---------|-----|------|
| 35001 | ElectronicsMarket | 控制器（已占用） |
| 35050 | ParallelControllerModule | 并行 Lv1（框架验证） |
| 35051 | SpeedControllerModule | 速度 Lv1（框架验证） |
| 35052 | OverclockControllerModule | 超频 Lv1（框架验证） |
| 35053 | PowerConsumptionModule | 功耗 Lv1（框架验证） |
| 35054 | RecoveryRateModule Lv1 | 回收率 50% |
| 35055 | RecoveryRateModule Lv2 | 回收率 70% |
| 35056 | RecoveryRateModule Lv3 | 回收率 90% |
| 35060 | ExecutionCore | 普通执行核心（框架验证） |
| 35070 | GeneralDisassemblyModule | 通用拆解功能模块 |

## 8. 配置项

```java
// General 类别
boolean EnableModularizedMachineSystem = true;  // 模块化系统总开关

// ModuleDefaults 类别 — 回收率模块
float RecoveryModuleLv1Rate = 0.50f;  // 可配置
float RecoveryModuleLv2Rate = 0.70f;  // 可配置
float RecoveryModuleLv3Rate = 0.90f;  // 可配置

// 保留
float Stage1BaseRecoveryRate = 0.30f;  // 一阶基础回收率

// 删除
// Stage2BaseRecoveryRate, Stage3BaseRecoveryRate, VoltageBonusPerTier
```

## 9. 文件清单

### 新增文件（约 25 个）

```
src/main/java/com/andgatech/AHTech/common/modularizedMachine/
├── IModularizedMachine.java                 主接口
├── ISupportParallelController.java          并行控制器接口
├── ISupportSpeedController.java             速度控制器接口
├── ISupportOverclockController.java         超频控制器接口
├── ISupportPowerConsumptionController.java  功耗控制器接口
├── ISupportRecoveryRateController.java      回收率接口（AHTech 新增）
├── ISupportFunctionModule.java              功能模块接口（AHTech 新增）
├── ISupportAllModularHatches.java           聚合接口
├── ModularHatchType.java                    模块类型枚举
├── FunctionType.java                        功能类型枚举
├── OverclockType.java                       超频类型枚举
├── ModularizedMachineBase.java              模块化机器基类
├── ModularizedMachineSupportAllModuleBase.java  全功能模块化机器基类
├── ModularHatchElement.java                 StructureLib 集成
└── modularHatches/
    ├── IModularHatch.java                   模块舱口接口
    ├── IStaticModularHatch.java             静态模块接口
    ├── IDynamicModularHatch.java            动态模块接口（预留）
    ├── ModularHatchBase.java                舱口基类
    ├── ParallelControllerModule.java        并行控制器
    ├── SpeedControllerModule.java           速度控制器
    ├── OverclockControllerModule.java       超频控制器
    ├── PowerConsumptionModule.java          功耗控制器
    ├── RecoveryRateModule.java              回收率模块
    ├── ExecutionCoreBase.java               执行核心基类
    ├── ExecutionCore.java                   普通执行核心
    ├── FunctionModuleBase.java              功能模块基类
    └── GeneralDisassemblyModule.java        通用拆解模块
```

### 修改文件（7 个）

| 文件 | 修改内容 |
|------|---------|
| `ElectronicsMarket.java` | 继承改为 `ModularizedMachineSupportAllModuleBase`，移除阶段相关性能参数硬编码，`validateRecipe` 加功能模块检查，`process()` 加回收率应用 |
| `Config.java` | 新增 `EnableModularizedMachineSystem`、回收率模块配置；删除 Stage2/3/Voltage 配置 |
| `MachineLoader.java` | 注册所有模块舱口 |
| `ModItemList.java` | 新增模块枚举条目 |
| `RecipeLoader.java` | 添加模块合成配方 |
| `RecyclingRecipeGenerator.java` | 为自动回收配方标记 specialValue |
| `ElectronicsMarketRecipePool.java` | 确保 hardcode 配方不被标记为功能门控 |

## 10. 实现范围

**本次迭代：**
- 完整模块化框架（接口、基类、注册、结构检测）
- 各一个 TST 标准模块等级（并行/速度/超频/功耗/执行核心）用于框架验证
- 回收率模块 1-3 级
- 通用拆解功能模块
- 美弱南电子市场重构为使用新系统

**后续迭代：**
- TST 标准模块的更多等级（2-8 级）
- 高级执行核心、完美执行核心
- 电路拆解模块
- 线缆拆解模块
- 元器件工厂模块
- 国产之光模块
- 动态模块变体
