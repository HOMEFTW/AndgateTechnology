# 美弱南电子市场 — 多方块机器设计文档

## 概述

一台多功能材料回收多方块机器，支持三阶段结构升级。通过自动解析 GT 和 Forge 配方表实现机器材料回收，同时提供硬编码的特殊配方（线缆拆解、激光真空管制造）。

## 机器定义

| 属性 | 值 |
|------|-----|
| 名称 | 美弱南电子市场 |
| Meta ID | 35001 |
| 类型 | 多方块（继承 `GTCM_MultiMachineBase<T>`） |
| 电压 | 任意 GT 电压等级，影响速度和并行数 |
| 阶段数 | 3（通过多方块结构组合判定） |

## 阶段系统

### 阶段判定

多方块结构中的方块组合共同决定当前阶段等级。`checkStructure()` 时检测结构中的方块类型并计算阶段。

**阶段 I**：基础方块组成（具体方块待多方块结构设计时确定）
**阶段 II**：中级方块组成
**阶段 III**：高级方块组成

### 回收率

```
实际回收率 = 阶段基础率 + 电压等级 × 电压加成
```

| 阶段 | 基础回收率（默认） | 解锁配方范围 |
|------|-------------------|-------------|
| I | 30% | 基础配方 + 简单回收 |
| II | 60% | 全部通用配方 + 中级回收 |
| III | 85% | 全部配方 + 高级回收 |

电路板始终 100% 回收，不受阶段和电压影响。

## 配方系统

### 混合方式：硬编码特殊配方 + 自动解析

### 硬编码特殊配方

1. **线缆拆解**：GT 线缆 → 橡胶 + GT 导线（所有阶段可用）
2. **激光真空管制造**：原版玻璃 + 1×铱锇合金箔 → 激光真空管（所有阶段可用）

### 自动解析回收配方

**解析时机**：`serverStarted` 阶段（所有 mod 的配方已注册完毕）

**配方来源**：
- `gregtech.api.recipe.RecipeMaps.*` — 所有 GT 配方表
- `net.minecraft.item.crafting.CraftingManager` — Forge 合成配方（覆盖其他 mod 的合成配方）
- `net.minecraft.item.crafting.FurnaceRecipes` — 熔炉配方

**解析流程**：
```
1. 遍历所有配方来源
2. 对每个配方，提取输出物品
3. 如果输出物品是机器/可回收物品，记录其输入材料
4. 生成回收配方：
   - 电路板材料 → 100% 产出
   - 其他材料 → 按回收率概率产出
5. 按材料阶段要求将回收配方分组
6. 注册到自定义 RecipeMap
```

## 配置项

```java
// 阶段基础回收率
Stage1_BaseRecoveryRate = 0.30
Stage2_BaseRecoveryRate = 0.60
Stage3_BaseRecoveryRate = 0.85

// 电压加成（每级增加的回收率）
VoltageBonusPerTier = 0.02

// 机器启用开关
Enable_ElectronicsMarket = true
```

## 类结构

```
com.andgatech.AHTech/
├── common/
│   ├── machine/
│   │   └── ElectronicsMarket.java        ← 多方块主控制器
│   └── ModItemList.java                  ← 新增 ElectronicsMachine 枚举项
├── recipe/
│   ├── machineRecipe/
│   │   ├── ElectronicsMarketRecipePool.java   ← 硬编码特殊配方
│   │   └── RecyclingRecipeGenerator.java      ← 自动解析配方生成器
│   └── RecyclingRecipeMap.java                ← 自定义 RecipeMap
├── loader/
│   ├── MachineLoader.java                     ← 注册机器 Meta ID 35001
│   └── RecipeLoader.java                      ← 调用配方注册
└── config/
    └── Config.java                            ← 新增回收率配置项
```

### 关键类说明

**`ElectronicsMarket`**：继承 `GTCM_MultiMachineBase<ElectronicsMarket>`
- `checkStructure()` 中检测方块组合，判定阶段 I/II/III
- 根据阶段限制可用配方范围
- 电压决定速度和并行数

**`RecyclingRecipeGenerator`**：
- `serverStarted` 时扫描所有配方来源
- 为可回收物品生成回收配方
- 区分电路板（100%）和普通材料（按回收率）

**`RecyclingRecipeMap`**：自定义 RecipeMap，兼容 NEI 显示

## 跨 Mod 兼容性

- 配方解析不依赖 GT 内部 API，使用 Forge 标准 `CraftingManager` 接口
- 对未知 mod 的物品也能生成回收配方（只要能找到其合成配方）
- 不会崩溃：解析失败时跳过该配方并记录 warn 日志

## Meta ID 范围

- 35001: 美弱南电子市场
- 35002-35099: 预留给后续机器
