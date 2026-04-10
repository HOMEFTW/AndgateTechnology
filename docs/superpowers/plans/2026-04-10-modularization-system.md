# 模块化系统实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**目标：** 为 AHTech 创建 TST 风格的完整模块化系统框架，重构电子市场使用模块化架构。

**架构：** 仿照 TST 的接口+基类+舱口三层架构，新增回收率控制器和功能模块接口。ElectronicsMarket 改为继承 ModularizedMachineSupportAllModuleBase，二阶以上拆解能力由模块驱动。

**技术栈：** Java 8 (Jabel 17), GTNH 2.8.4, StructureLib, ModularUI, Gradle 8.14.3

---

## 文件结构

### 新增文件

```
src/main/java/com/andgatech/AHTech/common/modularizedMachine/
├── IModularizedMachine.java                 主接口
├── ISupportParallelController.java          并行接口
├── ISupportSpeedController.java             速度接口
├── ISupportOverclockController.java         超频接口
├── ISupportPowerConsumptionController.java  功耗接口
├── ISupportRecoveryRateController.java      回收率接口（AHTech）
├── ISupportFunctionModule.java              功能模块接口（AHTech）
├── ISupportAllModularHatches.java           聚合接口
├── ModularHatchType.java                    模块类型枚举
├── FunctionType.java                        功能类型枚举
├── OverclockType.java                       超频类型枚举
├── ModularizedMachineBase.java              模块化机器基类
├── ModularizedMachineSupportAllModuleBase.java  全功能基类
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

### 修改文件

| 文件 | 修改内容 |
|------|---------|
| `config/Config.java` | 新增模块化配置，删除 Stage2/3/Voltage 配置 |
| `common/ModItemList.java` | 新增模块枚举条目 |
| `loader/MachineLoader.java` | 注册所有模块舱口 |
| `loader/RecipeLoader.java` | 添加模块合成配方占位 |
| `recipe/machineRecipe/RecyclingRecipeGenerator.java` | 为自动回收配方标记 specialValue(1) |
| `common/machine/ElectronicsMarket.java` | 继承改为 ModularizedMachineSupportAllModuleBase |

---

## Task 1: 枚举类型

**Files:**
- Create: `src/main/java/com/andgatech/AHTech/common/modularizedMachine/ModularHatchType.java`
- Create: `src/main/java/com/andgatech/AHTech/common/modularizedMachine/FunctionType.java`
- Create: `src/main/java/com/andgatech/AHTech/common/modularizedMachine/OverclockType.java`

- [ ] **Step 1: 创建 ModularHatchType 枚举**

```java
// ModularHatchType.java
package com.andgatech.AHTech.common.modularizedMachine;

public enum ModularHatchType {
    PARALLEL_CONTROLLER,
    SPEED_CONTROLLER,
    OVERCLOCK_CONTROLLER,
    POWER_CONSUMPTION_CONTROLLER,
    RECOVERY_RATE_CONTROLLER,
    EXECUTION_CORE,
    FUNCTION_MODULE,
    ALL;
}
```

- [ ] **Step 2: 创建 FunctionType 枚举**

```java
// FunctionType.java
package com.andgatech.AHTech.common.modularizedMachine;

public enum FunctionType {
    GENERAL_DISASSEMBLY,
    CIRCUIT_DISASSEMBLY,
    CABLE_DISASSEMBLY,
    COMPONENT_FACTORY,
    CIRCUIT_BOARD_FACTORY;
}
```

- [ ] **Step 3: 创建 OverclockType 枚举**

```java
// OverclockType.java
package com.andgatech.AHTech.common.modularizedMachine;

public enum OverclockType {

    NONE(1, 1),
    NormalOverclock(2, 4),
    LowSpeedPerfectOverclock(2, 2),
    PerfectOverclock(4, 4),
    SingularityPerfectOverclock(8, 4),
    EOHStupidOverclock(2, 8);

    public final int timeReduction;
    public final int powerIncrease;
    public final boolean perfectOverclock;

    OverclockType(int timeReduction, int powerIncrease) {
        this.timeReduction = timeReduction;
        this.powerIncrease = powerIncrease;
        this.perfectOverclock = timeReduction >= powerIncrease;
    }

    public boolean isPerfectOverclock() {
        return perfectOverclock;
    }

    public static OverclockType checkOverclockType(int timeReduction, int powerIncrease) {
        for (OverclockType t : values()) {
            if (t.timeReduction == timeReduction && t.powerIncrease == powerIncrease) {
                return t;
            }
        }
        return NormalOverclock;
    }

    public int getID() {
        return ordinal();
    }

    public static OverclockType getFromID(int id) {
        OverclockType[] values = values();
        return id >= 0 && id < values.length ? values[id] : NormalOverclock;
    }
}
```

- [ ] **Step 4: 构建验证**

Run: `cd D:/Code/AndgateTechnology && ./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: 提交**

```bash
git add src/main/java/com/andgatech/AHTech/common/modularizedMachine/ModularHatchType.java src/main/java/com/andgatech/AHTech/common/modularizedMachine/FunctionType.java src/main/java/com/andgatech/AHTech/common/modularizedMachine/OverclockType.java
git commit -m "feat: add modularization enums (ModularHatchType, FunctionType, OverclockType)"
```

---

## Task 2: 舱口接口

**Files:**
- Create: `src/main/java/com/andgatech/AHTech/common/modularizedMachine/modularHatches/IModularHatch.java`
- Create: `src/main/java/com/andgatech/AHTech/common/modularizedMachine/modularHatches/IStaticModularHatch.java`
- Create: `src/main/java/com/andgatech/AHTech/common/modularizedMachine/modularHatches/IDynamicModularHatch.java`

- [ ] **Step 1: 创建三个舱口接口**

`IModularHatch.java`:
```java
package com.andgatech.AHTech.common.modularizedMachine.modularHatches;

import com.andgatech.AHTech.common.modularizedMachine.ModularHatchType;

public interface IModularHatch {
    ModularHatchType getType();
}
```

`IStaticModularHatch.java`:
```java
package com.andgatech.AHTech.common.modularizedMachine.modularHatches;

import com.andgatech.AHTech.common.modularizedMachine.ModularizedMachineBase;

public interface IStaticModularHatch extends IModularHatch {
    void onCheckMachine(ModularizedMachineBase<?> machine);
}
```

`IDynamicModularHatch.java`:
```java
package com.andgatech.AHTech.common.modularizedMachine.modularHatches;

import com.andgatech.AHTech.common.modularizedMachine.ModularizedMachineBase;

public interface IDynamicModularHatch extends IModularHatch {
    void onCheckProcessing(ModularizedMachineBase<?> machine);
}
```

- [ ] **Step 2: 构建验证**

Run: `cd D:/Code/AndgateTechnology && ./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/andgatech/AHTech/common/modularizedMachine/modularHatches/
git commit -m "feat: add modular hatch interfaces (IModularHatch, IStatic, IDynamic)"
```

---

## Task 3: IModularizedMachine 接口族

**Files:**
- Create: `src/main/java/com/andgatech/AHTech/common/modularizedMachine/IModularizedMachine.java`
- Create: `src/main/java/com/andgatech/AHTech/common/modularizedMachine/ISupportParallelController.java`
- Create: `src/main/java/com/andgatech/AHTech/common/modularizedMachine/ISupportSpeedController.java`
- Create: `src/main/java/com/andgatech/AHTech/common/modularizedMachine/ISupportOverclockController.java`
- Create: `src/main/java/com/andgatech/AHTech/common/modularizedMachine/ISupportPowerConsumptionController.java`
- Create: `src/main/java/com/andgatech/AHTech/common/modularizedMachine/ISupportRecoveryRateController.java`
- Create: `src/main/java/com/andgatech/AHTech/common/modularizedMachine/ISupportFunctionModule.java`
- Create: `src/main/java/com/andgatech/AHTech/common/modularizedMachine/ISupportAllModularHatches.java`

- [ ] **Step 1: 创建主接口 IModularizedMachine**

```java
// IModularizedMachine.java
package com.andgatech.AHTech.common.modularizedMachine;

import java.util.Collection;

import com.andgatech.AHTech.common.modularizedMachine.modularHatches.IModularHatch;

public interface IModularizedMachine {

    Collection<ModularHatchType> getSupportedModularHatchTypes();

    void resetModularStaticSettings();

    void applyModularStaticSettings();

    default void checkModularStaticSettings() {
        resetModularStaticSettings();
        applyModularStaticSettings();
    }

    void resetModularDynamicParameters();

    void applyModularDynamicParameters();

    default void checkModularDynamicParameters() {
        resetModularDynamicParameters();
        applyModularDynamicParameters();
    }

    Collection<IModularHatch> getAllModularHatches();

    void resetModularHatchCollections();
}
```

- [ ] **Step 2: 创建五个性能子接口**

`ISupportParallelController.java`:
```java
package com.andgatech.AHTech.common.modularizedMachine;

public interface ISupportParallelController extends IModularizedMachine {
    int getStaticParallelParameterValue();
    void setStaticParallelParameter(int value);
    int getDynamicParallelParameterValue();
    void setDynamicParallelParameter(int value);
}
```

`ISupportSpeedController.java`:
```java
package com.andgatech.AHTech.common.modularizedMachine;

public interface ISupportSpeedController extends IModularizedMachine {
    float getStaticSpeedParameterValue();
    void setStaticSpeedParameterValue(float value);
    float getDynamicSpeedParameterValue();
    void setDynamicSpeedParameterValue(float value);
}
```

`ISupportOverclockController.java`:
```java
package com.andgatech.AHTech.common.modularizedMachine;

public interface ISupportOverclockController extends IModularizedMachine {
    void setOverclockType(OverclockType type);
    OverclockType getOverclockType();
}
```

`ISupportPowerConsumptionController.java`:
```java
package com.andgatech.AHTech.common.modularizedMachine;

public interface ISupportPowerConsumptionController extends IModularizedMachine {
    float getStaticPowerConsumptionParameterValue();
    void setStaticPowerConsumptionParameterValue(float value);
}
```

`ISupportRecoveryRateController.java`:
```java
package com.andgatech.AHTech.common.modularizedMachine;

public interface ISupportRecoveryRateController extends IModularizedMachine {
    float getRecoveryRate();
    void setRecoveryRate(float value);
}
```

- [ ] **Step 3: 创建 ISupportFunctionModule**

```java
package com.andgatech.AHTech.common.modularizedMachine;

import java.util.Set;

public interface ISupportFunctionModule extends IModularizedMachine {
    Set<FunctionType> getInstalledFunctionTypes();
    void addFunctionType(FunctionType type);
    boolean hasFunction(FunctionType type);
}
```

- [ ] **Step 4: 创建聚合接口 ISupportAllModularHatches**

```java
package com.andgatech.AHTech.common.modularizedMachine;

public interface ISupportAllModularHatches extends ISupportParallelController,
    ISupportSpeedController, ISupportOverclockController,
    ISupportPowerConsumptionController, ISupportRecoveryRateController,
    ISupportFunctionModule {
}
```

- [ ] **Step 5: 构建验证**

Run: `cd D:/Code/AndgateTechnology && ./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: 提交**

```bash
git add src/main/java/com/andgatech/AHTech/common/modularizedMachine/IModularizedMachine.java src/main/java/com/andgatech/AHTech/common/modularizedMachine/ISupport*.java
git commit -m "feat: add IModularizedMachine interface hierarchy with all sub-interfaces"
```

---

## Task 4: ModularHatchBase 舱口基类

**Files:**
- Create: `src/main/java/com/andgatech/AHTech/common/modularizedMachine/modularHatches/ModularHatchBase.java`

- [ ] **Step 1: 创建 ModularHatchBase**

继承 `MTEHatch`，提供模块等级和兼容性检查。参考 TST 的 `ModularHatchBase`，但增加 `moduleTier` 字段。

```java
package com.andgatech.AHTech.common.modularizedMachine.modularHatches;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import com.andgatech.AHTech.common.modularizedMachine.ModularizedMachineBase;

import gregtech.api.interfaces.ITexture;
import gregtech.api.metatileentity.implementations.MTEHatch;

public abstract class ModularHatchBase extends MTEHatch implements IModularHatch {

    protected final int moduleTier;

    public ModularHatchBase(int aID, String aName, String aNameRegional, int aTier, int aInvSlotCount,
        String[] aDescription, ITexture... aTextures) {
        super(aID, aName, aNameRegional, aTier, aInvSlotCount, aDescription, aTextures);
        this.moduleTier = aTier;
    }

    public ModularHatchBase(String aName, int aTier, int aInvSlotCount, String[] aDescription,
        ITexture[][][] aTextures) {
        super(aName, aTier, aInvSlotCount, aDescription, aTextures);
        this.moduleTier = aTier;
    }

    public int getModuleTier() {
        return moduleTier;
    }

    public boolean isCompatibleWithMachine(ModularizedMachineBase<?> machine) {
        return machine.getStructureTier() >= moduleTier;
    }

    @Override
    public boolean willExplodeInRain() {
        return false;
    }

    @Override
    public boolean isFacingValid(ForgeDirection facing) {
        return true;
    }

    @Override
    public boolean isAccessAllowed(EntityPlayer aPlayer) {
        return true;
    }

    @Override
    public boolean isValidSlot(int aIndex) {
        return false;
    }

    @Override
    public boolean isLiquidInput(ForgeDirection side) {
        return false;
    }

    @Override
    public boolean isFluidInputAllowed(FluidStack aFluid) {
        return false;
    }
}
```

- [ ] **Step 2: 构建验证**

Run: `cd D:/Code/AndgateTechnology && ./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/andgatech/AHTech/common/modularizedMachine/modularHatches/ModularHatchBase.java
git commit -m "feat: add ModularHatchBase with module tier compatibility"
```

---

## Task 5: ModularizedMachineBase 模块化机器基类

**Files:**
- Create: `src/main/java/com/andgatech/AHTech/common/modularizedMachine/ModularizedMachineBase.java`

- [ ] **Step 1: 创建 ModularizedMachineBase**

继承 `MTEExtendedPowerMultiBlockBase`，实现 `IModularizedMachine`。管理模块发现、结构检测、参数应用。参考 TST 的 `ModularizedMachineBase`，但不需要 `GTCM_MultiMachineBase` 中间层。

此类包含模块集合管理（`modularHatches`/`staticModularHatches`/`dynamicModularHatches`/`allModularHatches`）、`addToMachineList` 中的模块发现、`checkMachine` 中的模块参数应用、`checkProcessing` 中的动态参数应用。

关键方法：
- `addToMachineList()`: 同时处理普通 hatch 和模块 hatch
- `addAnyModularHatchToMachineList()`: 识别 `ModularHatchBase`，检查类型是否支持，加入集合
- `checkMachine()`: 重置集合 → checkMachineMM → 验证模块数量 → 应用静态设置
- `checkProcessing()`: 应用动态参数 → checkProcessingMM
- `checkSingleModularHatch()`: 默认每种类型只允许一个（除非 `canMultiplyModularHatchType()` 返回 true）
- `getStructureTier()`: 子类实现，返回结构等级（1/2/3）

继承层次：`MTEExtendedPowerMultiBlockBase<T>` → `ModularizedMachineBase<T>`

需要 `abstract checkMachineMM()` 和 `abstract canMultiplyModularHatchType()` 供子类实现。

注意：AHTech 的 `ModularizedMachineBase` 直接继承 GT 的 `MTEExtendedPowerMultiBlockBase`，不经过 TST 的 `GTCM_MultiMachineBase`。

- [ ] **Step 2: 构建验证**

Run: `cd D:/Code/AndgateTechnology && ./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/andgatech/AHTech/common/modularizedMachine/ModularizedMachineBase.java
git commit -m "feat: add ModularizedMachineBase with module discovery and lifecycle management"
```

---

## Task 6: ModularizedMachineSupportAllModuleBase 全功能基类

**Files:**
- Create: `src/main/java/com/andgatech/AHTech/common/modularizedMachine/ModularizedMachineSupportAllModuleBase.java`

- [ ] **Step 1: 创建全功能基类**

继承 `ModularizedMachineBase`，实现 `ISupportAllModularHatches`。

包含字段：`staticParallel`, `dynamicParallel`, `staticSpeedBonus`, `dynamicSpeedBonus`, `staticPowerConsumptionMultiplier`, `overclockType`, `recoveryRate`, `installedFunctionTypes`。

重置逻辑：
- `resetModularStaticSettings()`: parallel=0, speed=1, power=1, overclock=Normal, recoveryRate=0, functions=空
- `resetModularDynamicParameters()`: dynamicParallel=0, dynamicSpeed=1

GT 框架集成方法：
- `getMaxParallelRecipes()` → dynamicParallel + staticParallel + 1
- `getSpeedBonus()` → staticSpeedBonus * dynamicSpeedBonus
- `getEuModifier()` → staticPowerConsumptionMultiplier
- `isEnablePerfectOverclock()` → overclockType.isPerfectOverclock()

回收率特化：
- `getRecoveryRate()`: 一阶返回 `Config.Stage1_BaseRecoveryRate`，二/三阶返回 `recoveryRate` 字段

功能模块：
- `hasFunction()` → installedFunctionTypes.contains(type)
- `addFunctionType()` → installedFunctionTypes.add(type)

NBT 保存/加载所有字段。

`getSupportedModularHatchTypes()` 返回 `[ModularHatchType.ALL]`。

`getStructureTier()` 为 abstract，由子类（如 ElectronicsMarket）实现。

- [ ] **Step 2: 构建验证**

Run: `cd D:/Code/AndgateTechnology && ./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/andgatech/AHTech/common/modularizedMachine/ModularizedMachineSupportAllModuleBase.java
git commit -m "feat: add ModularizedMachineSupportAllModuleBase with all parameter fields"
```

---

## Task 7: TST 标准性能模块（并行/速度/超频/功耗）

**Files:**
- Create: `src/main/java/com/andgatech/AHTech/common/modularizedMachine/modularHatches/ParallelControllerModule.java`
- Create: `src/main/java/com/andgatech/AHTech/common/modularizedMachine/modularHatches/SpeedControllerModule.java`
- Create: `src/main/java/com/andgatech/AHTech/common/modularizedMachine/modularHatches/OverclockControllerModule.java`
- Create: `src/main/java/com/andgatech/AHTech/common/modularizedMachine/modularHatches/PowerConsumptionModule.java`

- [ ] **Step 1: 创建 ParallelControllerModule**

继承 `ModularHatchBase`，实现 `IStaticModularHatch`。构造函数接收 `parallel` 参数。`onCheckMachine()` 中通过 `instanceof ISupportParallelController` 推入并行数。`getType()` 返回 `PARALLEL_CONTROLLER`。`newMetaEntity()` 返回新实例。

本次只创建 Lv1（如 parallel=4）用于框架验证。

- [ ] **Step 2: 创建 SpeedControllerModule**

同样模式。`onCheckMachine()` 中 `instanceof ISupportSpeedController`，推入速度乘数（如 `1.0f / 2` 表示 2 倍速）。`getType()` 返回 `SPEED_CONTROLLER`。本次 Lv1 speedMultiplier=2。

- [ ] **Step 3: 创建 OverclockControllerModule**

`onCheckMachine()` 中 `instanceof ISupportOverclockController`，设置超频类型。本次 Lv1 使用 `NormalOverclock(2,4)` 用于框架验证。`getType()` 返回 `OVERCLOCK_CONTROLLER`。

- [ ] **Step 4: 创建 PowerConsumptionModule**

`onCheckMachine()` 中 `instanceof ISupportPowerConsumptionController`，乘入功耗倍率。本次 Lv1 multiplier=0.8（省电 20%）。`getType()` 返回 `POWER_CONSUMPTION_CONTROLLER`。

- [ ] **Step 5: 构建验证**

Run: `cd D:/Code/AndgateTechnology && ./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: 提交**

```bash
git add src/main/java/com/andgatech/AHTech/common/modularizedMachine/modularHatches/ParallelControllerModule.java src/main/java/com/andgatech/AHTech/common/modularizedMachine/modularHatches/SpeedControllerModule.java src/main/java/com/andgatech/AHTech/common/modularizedMachine/modularHatches/OverclockControllerModule.java src/main/java/com/andgatech/AHTech/common/modularizedMachine/modularHatches/PowerConsumptionModule.java
git commit -m "feat: add TST-standard performance modules (parallel, speed, overclock, power)"
```

---

## Task 8: 回收率模块（3 级）

**Files:**
- Create: `src/main/java/com/andgatech/AHTech/common/modularizedMachine/modularHatches/RecoveryRateModule.java`

- [ ] **Step 1: 创建 RecoveryRateModule**

继承 `ModularHatchBase`，实现 `IStaticModularHatch`。

构造函数：`(int aID, String aName, String aNameRegional, int aTier, float recoveryRate)` — 注册三种实例：Lv1(50%), Lv2(70%), Lv3(90%)，数值从 Config 读取。

`getType()` 返回 `RECOVERY_RATE_CONTROLLER`。

`onCheckMachine()` 中：
```java
if (machine instanceof ISupportRecoveryRateController ctrl) {
    if (isCompatibleWithMachine(machine)) {
        // 取已设值和当前值的最大值（多个回收率模块取最好的）
        ctrl.setRecoveryRate(Math.max(ctrl.getRecoveryRate(), this.recoveryRate));
    }
}
```

- [ ] **Step 2: 构建验证**

Run: `cd D:/Code/AndgateTechnology && ./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/andgatech/AHTech/common/modularizedMachine/modularHatches/RecoveryRateModule.java
git commit -m "feat: add RecoveryRateModule (3 tiers: 50%/70%/90%)"
```

---

## Task 9: 执行核心

**Files:**
- Create: `src/main/java/com/andgatech/AHTech/common/modularizedMachine/modularHatches/ExecutionCoreBase.java`
- Create: `src/main/java/com/andgatech/AHTech/common/modularizedMachine/modularHatches/ExecutionCore.java`

- [ ] **Step 1: 创建 ExecutionCoreBase**

继承 `ModularHatchBase`，实现 `IStaticModularHatch`。参考 TST 的 `ExecutionCoreBase`，包含进度管理（`maxProgressingTime`, `progressedTime`, `eut`, `outputItems`, `outputFluids`）、tick 处理（`onPostTick` 中的 `runExecutionCoreTick`）、NBT 保存/加载。`getType()` 返回 `EXECUTION_CORE`。

`onCheckMachine()` 默认空实现。

抽象方法：`done()`, `useMainMachinePower()`。

本次为框架验证版，简化 TST 的实现：包含基本的进度推进和输出合并逻辑。

- [ ] **Step 2: 创建 ExecutionCore**

继承 `ExecutionCoreBase`。`done()` 设置活跃状态返回 true。`useMainMachinePower()` 返回 true（共享机器 EU）。`newMetaEntity()` 返回新实例。

- [ ] **Step 3: 构建验证**

Run: `cd D:/Code/AndgateTechnology && ./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/andgatech/AHTech/common/modularizedMachine/modularHatches/ExecutionCoreBase.java src/main/java/com/andgatech/AHTech/common/modularizedMachine/modularHatches/ExecutionCore.java
git commit -m "feat: add ExecutionCore base and normal core for framework validation"
```

---

## Task 10: 功能模块

**Files:**
- Create: `src/main/java/com/andgatech/AHTech/common/modularizedMachine/modularHatches/FunctionModuleBase.java`
- Create: `src/main/java/com/andgatech/AHTech/common/modularizedMachine/modularHatches/GeneralDisassemblyModule.java`

- [ ] **Step 1: 创建 FunctionModuleBase**

继承 `ModularHatchBase`，实现 `IStaticModularHatch`。抽象方法 `getFunctionType()`。

`onCheckMachine()` 中：
```java
if (machine instanceof ISupportFunctionModule fm) {
    if (isCompatibleWithMachine(machine)) {
        fm.addFunctionType(getFunctionType());
    }
}
```

`getType()` 返回 `ModularHatchType.FUNCTION_MODULE`。

- [ ] **Step 2: 创建 GeneralDisassemblyModule**

继承 `FunctionModuleBase`。`getFunctionType()` 返回 `FunctionType.GENERAL_DISASSEMBLY`。`newMetaEntity()` 返回新实例。moduleTier=1（所有阶段都能用）。

- [ ] **Step 3: 构建验证**

Run: `cd D:/Code/AndgateTechnology && ./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/andgatech/AHTech/common/modularizedMachine/modularHatches/FunctionModuleBase.java src/main/java/com/andgatech/AHTech/common/modularizedMachine/modularHatches/GeneralDisassemblyModule.java
git commit -m "feat: add function module system with GeneralDisassemblyModule"
```

---

## Task 11: Config 和 ModItemList 修改

**Files:**
- Modify: `src/main/java/com/andgatech/AHTech/config/Config.java`
- Modify: `src/main/java/com/andgatech/AHTech/common/ModItemList.java`

- [ ] **Step 1: 更新 Config.java**

新增字段：
```java
// region Modularization
public static boolean EnableModularizedMachineSystem = true;
public static float RecoveryModuleLv1Rate = 0.50f;
public static float RecoveryModuleLv2Rate = 0.70f;
public static float RecoveryModuleLv3Rate = 0.90f;
// endregion
```

在 `synchronizeConfiguration()` 中添加配置读取。

删除 `Stage2_BaseRecoveryRate`, `Stage3_BaseRecoveryRate`, `VoltageBonusPerTier` 及其配置读取代码。

保留 `Stage1_BaseRecoveryRate`。

- [ ] **Step 2: 更新 ModItemList.java**

新增枚举条目（在 `ElectronicsMarket` 之后）：
```java
// region Modular Hatches - Performance
ParallelControllerLv1,
SpeedControllerLv1,
OverclockControllerLv1,
PowerConsumptionLv1,
RecoveryRateLv1,
RecoveryRateLv2,
RecoveryRateLv3,
// endregion

// region Modular Hatches - Execution Cores
ExecutionCoreNormal,
// endregion

// region Modular Hatches - Function Modules
GeneralDisassemblyModule,
// endregion
```

- [ ] **Step 3: 构建验证**

Run: `cd D:/Code/AndgateTechnology && ./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/andgatech/AHTech/config/Config.java src/main/java/com/andgatech/AHTech/common/ModItemList.java
git commit -m "feat: update Config and ModItemList for modularization system"
```

---

## Task 12: MachineLoader 注册模块

**Files:**
- Modify: `src/main/java/com/andgatech/AHTech/loader/MachineLoader.java`

- [ ] **Step 1: 在 MachineLoader.loadMachines() 中注册所有模块**

在 `ElectronicsMarket` 注册之后，由 `Config.EnableModularizedMachineSystem` 门控：

```java
if (Config.EnableModularizedMachineSystem) {
    // Performance modules - Lv1 for framework validation
    ModItemList.ParallelControllerLv1.set(
        new ParallelControllerModule(35050, "ParallelControllerLv1", "Parallel Controller Lv1", 1, 4)
            .getStackForm(1L));
    ModItemList.SpeedControllerLv1.set(
        new SpeedControllerModule(35051, "SpeedControllerLv1", "Speed Controller Lv1", 1, 2)
            .getStackForm(1L));
    ModItemList.OverclockControllerLv1.set(
        new OverclockControllerModule(35052, "OverclockControllerLv1", "Overclock Controller Lv1", 1, 2, 4)
            .getStackForm(1L));
    ModItemList.PowerConsumptionLv1.set(
        new PowerConsumptionModule(35053, "PowerConsumptionLv1", "Power Consumption Lv1", 1, 0.8f)
            .getStackForm(1L));

    // Recovery rate modules
    ModItemList.RecoveryRateLv1.set(
        new RecoveryRateModule(35054, "RecoveryRateLv1", "Recovery Rate Lv1", 1, Config.RecoveryModuleLv1Rate)
            .getStackForm(1L));
    ModItemList.RecoveryRateLv2.set(
        new RecoveryRateModule(35055, "RecoveryRateLv2", "Recovery Rate Lv2", 2, Config.RecoveryModuleLv2Rate)
            .getStackForm(1L));
    ModItemList.RecoveryRateLv3.set(
        new RecoveryRateModule(35056, "RecoveryRateLv3", "Recovery Rate Lv3", 3, Config.RecoveryModuleLv3Rate)
            .getStackForm(1L));

    // Execution cores
    ModItemList.ExecutionCoreNormal.set(
        new ExecutionCore(35060, "ExecutionCoreNormal", "Execution Core", 1)
            .getStackForm(1L));

    // Function modules
    ModItemList.GeneralDisassemblyModule.set(
        new GeneralDisassemblyModule(35070, "GeneralDisassemblyModule", "General Disassembly Module", 1)
            .getStackForm(1L));
}
```

需要添加对应 import 语句。

- [ ] **Step 2: 构建验证**

Run: `cd D:/Code/AndgateTechnology && ./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/andgatech/AHTech/loader/MachineLoader.java
git commit -m "feat: register all modular hatches in MachineLoader"
```

---

## Task 13: RecyclingRecipeGenerator 标记配方

**Files:**
- Modify: `src/main/java/com/andgatech/AHTech/recipe/machineRecipe/RecyclingRecipeGenerator.java`

- [ ] **Step 1: 为自动回收配方添加 specialValue(1)**

在 `buildRecyclingRecipes()` 方法的 `GTValues.RA.stdBuilder()` 链中，在 `.duration(200)` 之后、`.addTo(...)` 之前，添加 `.specialValue(1)`：

```java
// specialValue(1) = GENERAL_DISASSEMBLY, needs function module to process
GTValues.RA.stdBuilder()
    .itemInputs(GTUtility.copyAmountUnsafe(1, inputStack))
    .itemOutputs(outputs)
    .specialValue(1)  // 标记为通用拆解配方
    .eut(TierEU.RECIPE_LV)
    .duration(200)
    .addTo(AHTechRecipeMaps.ElectronicsMarketRecipes);
```

硬编码配方（线缆拆解、激光真空管）不添加此标记：
- 线缆拆解配方：无 specialValue（默认 0），始终可用
- 激光真空管配方：已有 `specialValue(2)`，表示需要二阶以上

- [ ] **Step 2: 构建验证**

Run: `cd D:/Code/AndgateTechnology && ./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/andgatech/AHTech/recipe/machineRecipe/RecyclingRecipeGenerator.java
git commit -m "feat: tag auto-recycling recipes with specialValue(1) for function module gating"
```

---

## Task 14: 重构 ElectronicsMarket

**Files:**
- Modify: `src/main/java/com/andgatech/AHTech/common/machine/ElectronicsMarket.java`

这是最复杂的修改。将 `ElectronicsMarket` 从直接继承 `MTEExtendedPowerMultiBlockBase` 改为继承 `ModularizedMachineSupportAllModuleBase`。

- [ ] **Step 1: 修改类声明和继承**

```java
// 从：
public class ElectronicsMarket extends MTEExtendedPowerMultiBlockBase<ElectronicsMarket>
    implements IConstructable, ISurvivalConstructable {

// 改为：
public class ElectronicsMarket extends ModularizedMachineSupportAllModuleBase<ElectronicsMarket>
    implements IConstructable, ISurvivalConstructable {
```

- [ ] **Step 2: 删除旧的性能字段**

删除实例字段：`enablePerfectOverclock`, `maxParallel`, `euModifier`, `speedBonus`。这些现在由基类 `ModularizedMachineSupportAllModuleBase` 管理。

保留 `structureTier` 字段（基类需要通过 `getStructureTier()` 暴露）。

- [ ] **Step 3: 实现 getStructureTier()**

```java
@Override
public int getStructureTier() {
    return structureTier;
}
```

- [ ] **Step 4: 实现 canMultiplyModularHatchType()**

```java
@Override
protected boolean canMultiplyModularHatchType() {
    // 允许同类多个模块（如多个并行控制器叠加效果）
    return true;
}
```

- [ ] **Step 5: 实现 checkMachineMM()**

将原 `checkMachine()` 中的结构检测逻辑移到 `checkMachineMM()`。移除性能参数计算（speedBonus/maxParallel/enablePerfectOverclock/euModifier 的硬编码），只保留结构检测和 `structureTier` 赋值。

一阶的基础并行需要在这里设置：
```java
@Override
public boolean checkMachineMM(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
    structureTier = TIER_NONE;
    boolean sign = checkPiece(STRUCTURE_PIECE_MAIN, horizontalOffSet, verticalOffSet, depthOffSet);
    if (!sign || structureTier < TIER_I) {
        return false;
    }
    // 一阶基础：并行 4，无速度加成
    // 二/三阶：由模块决定（resetModularStaticSettings 已将 parallel 清零）
    if (structureTier == TIER_I) {
        setStaticParallelParameter(3); // +1 base = 4 total
    }
    return true;
}
```

- [ ] **Step 6: 修改结构定义，加入模块舱口位**

在 `getStructureDefinition()` 的 HatchElementBuilder 中，将模块舱口加入 `atLeast()`：

```java
.addElement(
    'H',
    HatchElementBuilder.<ElectronicsMarket>builder()
        .atLeast(InputBus, OutputBus, InputHatch, OutputHatch, Energy.or(ExoticEnergy))
        // 模块舱口不在 'H' 位，需要单独的元素
        .adder(ElectronicsMarket::addToMachineList)
        .dot(1)
        .casingIndex(...)
        .buildAndChain(GregTechAPI.sBlockCasings2, 0))
```

结构需要增加模块舱口位（使用新字符如 `'M'`）或让现有 `'H'` 位也接受模块。推荐方案：让 `'C'` 位的 tiered casing 旁边增加模块位，或直接在 `'H'` 位接受模块。

实际做法：在 HatchElementBuilder 的 `.atLeast()` 中不添加模块类型，而是新增一个结构元素 `'M'` 用于模块位。或者更简单的方式——将模块舱口也放在 `'H'` 位：

```java
.addElement(
    'H',
    HatchElementBuilder.<ElectronicsMarket>builder()
        .atLeast(InputBus, OutputBus, InputHatch, OutputHatch, Energy.or(ExoticEnergy))
        .adder(ElectronicsMarket::addNormalHatchToMachineList)
        .dot(1)
        .casingIndex(...)
        .buildAndChain(GregTechAPI.sBlockCasings2, 0))
```

然后为模块位添加单独的 `'M'` 元素（可放在结构边缘的 'A' 位中替换一些）：

在 shape 中将部分 `'A'` 替换为 `'M'`，或新增一层 hatch。简单起见，本次将前面两层的边缘 `'A'` 中部分位置替换为 `'M'`。

- [ ] **Step 7: 修改 ProcessingLogic**

更新 `createProcessingLogic()`：

`validateRecipe()` 中：
- 一阶：仅允许 specialValue==0 的配方（硬编码配方）
- 二阶以上：检查 specialValue → 对应功能模块
  - specialValue==0: 始终通过（线缆拆解）
  - specialValue==1: 需要 `hasFunction(GENERAL_DISASSEMBLY)`
  - specialValue==2: 需要二阶以上

`process()` 中：
- 调用 `super.process()` 后，对输出物品应用回收率

```java
@Override
protected ProcessingLogic createProcessingLogic() {
    return new ProcessingLogic() {
        @NotNull
        @Override
        protected CheckRecipeResult validateRecipe(@NotNull GTRecipe recipe) {
            if (getStructureTier() < TIER_I) {
                return CheckRecipeResultRegistry.insufficientMachineTier(getStructureTier());
            }
            // 一阶只能用硬编码配方（specialValue==0）
            if (getStructureTier() == TIER_I && recipe.mSpecialValue > 0) {
                return CheckRecipeResultRegistry.insufficientMachineTier(getStructureTier());
            }
            // specialValue==1 需要 GENERAL_DISASSEMBLY 功能模块
            if (recipe.mSpecialValue == 1 && !hasFunction(FunctionType.GENERAL_DISASSEMBLY)) {
                return CheckRecipeResultRegistry.NO_RECIPE;
            }
            // specialValue==2 需要二阶以上
            if (recipe.mSpecialValue == 2 && getStructureTier() < TIER_II) {
                return CheckRecipeResultRegistry.insufficientMachineTier(getStructureTier());
            }
            return CheckRecipeResultRegistry.SUCCESSFUL;
        }

        @NotNull
        @Override
        public CheckRecipeResult process() {
            setEuModifier(getEuModifier());
            setSpeedBonus(getSpeedBonus());
            setOverclock(isEnablePerfectOverclock() ? 4 : 2, 4);
            return super.process();
        }
    }.setMaxParallelSupplier(this::getMaxParallelRecipes);
}
```

- [ ] **Step 8: 应用回收率到输出**

在 `checkProcessingMM()` 的结果处理中，对 `mOutputItems` 应用回收率。覆写基类的输出处理：

```java
@Override
public CheckRecipeResult checkProcessingMM() {
    CheckRecipeResult result = super.checkProcessingMM();
    if (result.wasSuccessful()) {
        applyRecoveryRate();
    }
    return result;
}

private void applyRecoveryRate() {
    float rate = getRecoveryRate();
    if (rate >= 1.0f || mOutputItems == null) return;

    for (int i = 0; i < mOutputItems.length; i++) {
        if (mOutputItems[i] == null) continue;
        if (RecyclingRecipeGenerator.isCircuitBoard(mOutputItems[i])) continue; // 电路板 100%
        int original = mOutputItems[i].stackSize;
        if (original <= 0) continue;
        int recovered = Math.max(1, Math.round(original * rate));
        mOutputItems[i].stackSize = recovered;
    }
}
```

- [ ] **Step 9: 更新 NBT**

移除旧的性能字段保存/加载（`enablePerfectOverclock`, `maxParallel`, `euModifier`, `speedBonus`）。保留 `structureTier` 的保存/加载。调用 `super.saveNBTData()` / `super.loadNBTData()` 以处理基类字段。

- [ ] **Step 10: 更新 Tooltip**

更新 `createTooltip()` 说明模块化系统。

- [ ] **Step 11: 更新 UI**

在 `addUIWidgets()` 中更新回收率显示，替换旧的固定阶段显示。

- [ ] **Step 12: 构建验证**

Run: `cd D:/Code/AndgateTechnology && ./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 13: 提交**

```bash
git add src/main/java/com/andgatech/AHTech/common/machine/ElectronicsMarket.java
git commit -m "feat: refactor ElectronicsMarket to use modularization system"
```

---

## Task 15: 更新文档

**Files:**
- Modify: `context.md`
- Modify: `log.md`
- Modify: `ToDOLIST.md`

- [ ] **Step 1: 更新 context.md**

在 Implemented Content 中添加：
- Modularization 系统架构描述
- 新增的机器列表（模块舱口）
- 新增的配置项
- 架构变更说明（基类变更）

- [ ] **Step 2: 更新 log.md**

添加今天的开发日志条目，记录模块化系统实现。

- [ ] **Step 3: 更新 ToDOLIST.md**

- 将"模块化系统框架"相关条目标记为完成
- 将"回收率模块"标记为完成
- 将"通用拆解功能模块"标记为完成
- 更新赛格大厦阶段二的剩余任务

- [ ] **Step 4: 提交**

```bash
git add context.md log.md ToDOLIST.md
git commit -m "docs: update project documentation for modularization system"
```

---

## 自查清单

### 规格覆盖

| 规格要求 | 对应 Task |
|---------|-----------|
| 完整接口层次 | Task 2, 3 |
| ModularHatchType/FunctionType/OverclockType 枚举 | Task 1 |
| ModularizedMachineBase 基类 | Task 5 |
| ModularizedMachineSupportAllModuleBase 全功能基类 | Task 6 |
| TST 标准模块（并行/速度/超频/功耗）各一级 | Task 7 |
| RecoveryRateModule 3 级 | Task 8 |
| ExecutionCore | Task 9 |
| FunctionModule + GeneralDisassemblyModule | Task 10 |
| Config 更新 | Task 11 |
| ModItemList 更新 | Task 11 |
| MachineLoader 注册 | Task 12 |
| RecyclingRecipeGenerator 配方标记 | Task 13 |
| ElectronicsMarket 重构 | Task 14 |
| 一阶硬编码 30%、二/三阶由模块驱动 | Task 6, 14 |
| specialValue 配方分类 | Task 13, 14 |
| 回收率运行时应用 + 电路板 100% | Task 14 |
| 模块等级 ≤ 结构等级 | Task 4 (ModularHatchBase.isCompatibleWithMachine) |
| Meta ID 分配 | Task 12 |

### 占位符检查

无 TBD/TODO。所有步骤包含具体代码或明确指令。

### 类型一致性

- `getStructureTier()` 在 Task 6 声明为 abstract，Task 14 实现
- `getRecoveryRate()` 在 `ISupportRecoveryRateController` 中声明，在 `ModularizedMachineSupportAllModuleBase` 中实现
- `hasFunction()` 在 `ISupportFunctionModule` 中声明，在 `ModularizedMachineSupportAllModuleBase` 中实现
- `onCheckMachine()` 在 `IStaticModularHatch` 中接收 `ModularizedMachineBase<?>` 参数
- `addToMachineList()` 在 Task 5 的 `ModularizedMachineBase` 中覆写
