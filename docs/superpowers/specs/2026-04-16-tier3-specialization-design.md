# 三级结构差异化设计

> 日期：2026-04-16
> 状态：已批准

## 概述

为赛格大厦（ElectronicsMarket）三级结构添加差异化处理。当前 Stage III 与 Stage II 代码逻辑完全等同，无差异化。

本次变更：
1. Tier II 添加结构级配方电压上限（UV）
2. Tier II/III 放开标准模块等级门控
3. AHTech 独占模块保留现有 structureTier 门控

## 1. 配方电压门控

### 行为

| 结构等级 | 配方电压上限 | 说明 |
|---------|------------|------|
| Tier I  | 仅 `specialValue == 0` | 保持现有逻辑 |
| Tier II | UV (等级 8, 524,288 EU/t) | 新增：拒绝超过 UV 的配方 |
| Tier III | 无限制 | 新增：可执行全部配方 |

### 实现

在 `ElectronicsMarket.validateRecipeAccess()` 中添加电压检查：

```java
// Tier II 配方电压上限
if (getStructureTier() == TIER_II && recipe.mEUt > Config.Stage2_MaxVoltageTierEUt) {
    return SimpleCheckRecipeResult.ofFailure("voltage_exceeded");
}
```

- 使用 `Config.Stage2_MaxVoltageTierEUt` 读取配置值
- 检查位置在 `specialValue` 检查之后、供应商检查之前
- 失败返回专用 `CheckRecipeResult`，便于 UI 提示

### 配置项

```java
// Config.java
@Config.LangKey("andgatetechnology.config.stage2_max_voltage_tier")
@Config.Comment("Maximum voltage tier for Tier II structure recipes. Tier III has no limit.")
@Config.RangeInt(min = 1, max = 14)
public static int Stage2_MaxVoltageTier = 8; // UV = 8

// 运行时计算对应 EU/t
public static long getStage2_MaxVoltageEUt() {
    return GregTechAPI.V[Math.min(Stage2_MaxVoltageTier, GregTechAPI.V.length - 1)];
}
```

## 2. 标准模块等级放开

### 问题

当前 `ModularHatchBase.isCompatibleWithMachine()` 使用 `structureTier >= moduleTier`，导致：
- Tier II (structureTier=2) 只能用 T1-T2 标准模块
- Tier III (structureTier=3) 只能用 T1-T3 标准模块
- 标准 TST 互通模块有 T1-T8，大部分等级无法使用

### 行为

| 模块类型 | Tier II | Tier III | 说明 |
|---------|---------|---------|------|
| 标准（并行/速度/超频/功耗/执行核心） | 全部等级 | 全部等级 | 放开限制 |
| AHTech 独占（回收率/功能模块） | structureTier >= moduleTier | structureTier >= moduleTier | 保留现有 |
| TST 原生模块 | 天然豁免 | 天然豁免 | 走独立路径，不走 isCompatibleWithMachine |

### 实现

**步骤 1：** 在 `ModularizedMachineBase` 中新增虚方法：

```java
/**
 * Returns the maximum module tier allowed for the given module type.
 * Default: structureTier. Subclasses can override for per-type gating.
 */
public int getMaxAllowedModuleTier(ModularHatchType type) {
    return getStructureTier();
}
```

**步骤 2：** 修改 `ModularHatchBase.isCompatibleWithMachine()`：

```java
public boolean isCompatibleWithMachine(ModularizedMachineBase<?> machine) {
    return machine.getMaxAllowedModuleTier(getType()) >= moduleTier;
}
```

**步骤 3：** 在 `ElectronicsMarket` 中覆写：

```java
@Override
public int getMaxAllowedModuleTier(ModularHatchType type) {
    if (getStructureTier() >= TIER_II) {
        return switch (type) {
            case PARALLEL_CONTROLLER, SPEED_CONTROLLER,
                 OVERCLOCK_CONTROLLER, POWER_CONSUMPTION_CONTROLLER,
                 EXECUTION_CORE -> Integer.MAX_VALUE;
            default -> getStructureTier();
        };
    }
    return getStructureTier(); // Tier I: 不应该到这里（Tier I 跳过模块应用）
}
```

## 3. 受影响文件

| 文件 | 变更类型 | 说明 |
|------|---------|------|
| `ElectronicsMarket.java` | 修改 | validateRecipeAccess 添加电压检查；覆写 getMaxAllowedModuleTier |
| `ModularizedMachineBase.java` | 修改 | 新增 getMaxAllowedModuleTier 虚方法 |
| `ModularHatchBase.java` | 修改 | isCompatibleWithMachine 使用 getMaxAllowedModuleTier |
| `Config.java` | 修改 | 新增 Stage2_MaxVoltageTier 配置项 |

## 4. 测试计划

- Tier II 结构拒绝 UV 以上配方
- Tier III 结构接受任意电压配方
- 标准 TST 互通模块在 Tier II 下所有等级均可用
- AHTech 回收率 T3 模块仍需 Tier III 结构
- AHTech 回收率 T2 模块在 Tier II 结构下可用
- Tier I 行为不变（不受新逻辑影响）
