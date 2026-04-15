# 资金系统设计

> 日期：2026-04-15
> 状态：待审核
> 关联：ToDOLIST.md 阶段四（资金系统）

---

## 1. 概述

本文档描述 AndgateTechnology（AHTech）mod 的资金系统：通过物理货币物品和金融舱口模块，为赛格大厦的配方执行增加经济消耗层。

核心设计：
- **6 级货币体系**：铜币、钢币、钛币、铂币、中子币、无尽币，均由 GT 压模机 1 锭压成 1 币
- **金融舱口**：贴墙式舱口（`ModularHatchBase` 子类），拥有 9 格物品栏存储硬币，自动从输入总线补货
- **配方级货币消耗**：每种配方通过元数据绑定所需币种和固定消耗量，执行后扣除
- **资金不足安全待机**：货币不足时配方拒绝执行，不消耗任何材料

---

## 2. 设计原则

- **融入现有模块体系**：金融舱口继承 `ModularHatchBase`，新增 `ModularHatchType.FINANCIAL`，与供应商舱口模式一致
- **物理货币沉浸感**：硬币是实体物品，可看到、可搬运、可自动化
- **配方级粒度控制**：每种配方独立绑定币种和消耗量，不与合同等级或供应商直接绑定
- **安全优先**：资金不足时与供电不足行为一致，安全待机不吞材料
- **自动化友好**：支持从输入总线自动抽取硬币，方便 AE 系统供币

---

## 3. 货币物品

### 3.1 货币等级

| Meta | 名称 | 材质来源 | 对应游戏阶段 | 注册名 |
|------|------|---------|------------|--------|
| 0 | 铜币 | 铜锭 | 蒸汽 ~ LV | `currency_copper` |
| 1 | 钢币 | 钢锭 | MV ~ HV | `currency_steel` |
| 2 | 钛币 | 钛锭 | EV ~ IV | `currency_titanium` |
| 3 | 铂币 | 铂锭 | LuV ~ ZPM | `currency_platinum` |
| 4 | 中子币 | 中子锭 | UV ~ UhV | `currency_neutronium` |
| 5 | 无尽币 | 无尽锭 | 终局 | `currency_infinity` |

### 3.2 CurrencyType 枚举

```java
public enum CurrencyType {
    COPPER(0, "copper", Materials.Copper),
    STEEL(1, "steel", Materials.Steel),
    TITANIUM(2, "titanium", Materials.Titanium),
    PLATINUM(3, "platinum", Materials.Platinum),
    NEUTRONIUM(4, "neutronium", Materials.Neutronium),
    INFINITY(5, "infinity", ...);

    private final int meta;
    private final String name;
    private final Materials material;

    // 方法
    public static CurrencyType getByMeta(int meta);
    public ItemStack getIngot();         // 对应锭物品
    public ItemStack getCoinStack(int amount);  // 硬币物品栈
    public String getTranslationKey();
}
```

### 3.3 CurrencyItem 类

```java
public class CurrencyItem extends Item {
    // 单一 Item，通过 meta 值区分 6 种币
    // maxStackSize = 64
    // getUnlocalizedName() 基于 CurrencyType 返回翻译键
}
```

### 3.4 合成配方

所有货币通过 GT 压模机（Extruder）配方：
- **1 锭 → 1 币**（无损耗）
- 模具：通用硬币模具（或无需模具，视 GT 压模机行为而定）

注册位置：`CurrencyRecipePool`（新增类），在 `RecipeLoader` 中调用。

反向配方（币→锭）不实现，避免货币循环。

---

## 4. 金融舱口

### 4.1 类定义

```
FinancialHatch extends ModularHatchBase
├── 类型: ModularHatchType.FINANCIAL
├── moduleTier: 0（无等级限制，所有阶段可用）
├── 物品栏: 9 格 IInventory，只接受 CurrencyItem
├── 无合同等级要求
└── 每个大厦可安装多个金融舱口（库存叠加检查）
```

### 4.2 物品栏管理

- `isItemValidForSlot(int slot, ItemStack stack)`：仅接受 `stack.getItem() instanceof CurrencyItem`
- 硬币自动按类型堆叠（每堆最多 64 个）
- 提供 `countCurrency(CurrencyType type)` 方法：返回舱口中指定币种的总数
- 提供 `consumeCurrency(CurrencyType type, int amount)` 方法：从舱口中扣除指定数量的货币（可能跨多个堆叠位）
- **多舱口叠加**：当安装多个金融舱口时，资金检查和消耗均跨所有舱口汇总。检查时求和所有舱口的 `countCurrency()`；消耗时按舱口顺序依次扣除

### 4.3 自动补货

`autoRefillFromInputBus(Collection<MTEHatchInput> inputBuses)` 方法：

```
1. 遍历大厦所有输入总线
2. 查找输入总线中的 CurrencyItem 物品栈
3. 将硬币转移到金融舱口的空位或已有堆叠
4. 不影响非货币物品（输入总线中其他物品不被移动）
5. 每次 checkMachine() 调用时触发一次
```

### 4.4 金融舱口 GUI

右键点击金融舱口打开自定义 GUI：

```
┌─────────────────────────────┐
│  资金舱室                    │
│  ─────────────────────────  │
│  [铜币]×128  [钢币]×64      │
│  [钛币]×32   [铂币]×0       │
│  [中子币]×0  [无尽币]×0     │
│  ─────────────────────────  │
│  状态: 自动补货已启用        │
└─────────────────────────────┘
```

使用 ModularUI 构建，布局参照供应商舱口的 `addUIWidgets()` 模式。

---

## 5. 配方标记与资金消耗

### 5.1 配方元数据扩展

在 `AHTechRecipeMetadata` 中新增：

```java
// 货币类型
public static final RecipeMetadataKey<CurrencyType> CURRENCY_TYPE =
    SimpleRecipeMetadataKey.create(CurrencyType.class, "ahtech_currency_type");

// 货币消耗量（固定值，每次执行消耗）
public static final RecipeMetadataKey<Integer> CURRENCY_COST =
    SimpleRecipeMetadataKey.create(Integer.class, "ahtech_currency_cost");
```

### 5.2 配方注册示例

```java
// 线缆拆解：每次消耗 2 铜币
.stdBuilder()
  .metadata(AHTechRecipeMetadata.SUPPLIER_ID, "shandong_dezhou")
  .metadata(AHTechRecipeMetadata.CURRENCY_TYPE, CurrencyType.COPPER)
  .metadata(AHTechRecipeMetadata.CURRENCY_COST, 2)
  ...

// 高级 FPGA 拆解：每次消耗 3 铂币
.stdBuilder()
  .metadata(AHTechRecipeMetadata.SUPPLIER_ID, "chaola")
  .metadata(AHTechRecipeMetadata.CURRENCY_TYPE, CurrencyType.PLATINUM)
  .metadata(AHTechRecipeMetadata.CURRENCY_COST, 3)
  ...
```

### 5.3 无需货币的配方

当 `CURRENCY_TYPE` 元数据为 `null` 时，配方不消耗货币。这确保向后兼容：
- 现有配方无需修改即可正常运行
- 后续可逐步为配方添加货币消耗

### 5.4 资金检查流程

在 `ElectronicsMarket.validateRecipeAccess()` 末尾追加：

```
// 资金检查
currencyType = recipe.getMetadata(CURRENCY_TYPE)
currencyCost = recipe.getMetadata(CURRENCY_COST)

if currencyType == null:
    通过（无需货币，向后兼容）

financialHatches = getModularHatchesByType(FINANCIAL)
if financialHatches 为空:
    拒绝（需要安装资金舱室）

totalCount = financialHatches 所有舱口的 countCurrency(currencyType) 之和
if totalCount < currencyCost:
    拒绝（资金不足）

通过
```

### 5.5 资金消耗时机

在 `ElectronicsMarket.checkProcessingMM()` 中，配方执行成功后：

```
checkProcessingMM():
    super.checkProcessingMM()     // 执行配方
    applyRecoveryRate()           // 应用回收率
    consumeCurrency(currentRecipe) // 扣除货币 ← 新增
```

`consumeCurrency(recipe)` 逻辑：
```
currencyType = recipe.getMetadata(CURRENCY_TYPE)
currencyCost = recipe.getMetadata(CURRENCY_COST)
if currencyType == null: return

// 从所有金融舱口中扣除（优先从第一个舱口扣）
for hatch in financialHatches:
    if currencyCost <= 0: break
    consumed = hatch.consumeCurrency(currencyType, currencyCost)
    currencyCost -= consumed
```

### 5.6 资金不足时的行为

- **验证阶段**：`validateRecipeAccess()` 返回 `null`，配方不执行
- **不消耗材料**：与供电不足一致，安全待机
- **UI 反馈**：主控制器 UI 显示资金状态

---

## 6. 系统集成

### 6.1 ModularHatchType 扩展

新增枚举值 `FINANCIAL`（在 `SUPPLIER` 之后）。

### 6.2 ElectronicsMarket 控制器修改

#### 新增字段

```java
// 资金系统
private List<FinancialHatch> financialHatches;          // 金融舱口列表
private String syncedFinancialStatus = "";               // 客户端同步：资金状态文本
```

#### 修改 checkMachineMM()

```
1. 现有逻辑保持不变
2. 新增：从 modularHatches 中提取 FINANCIAL 类型 → financialHatches
3. 新增：触发所有金融舱口的 autoRefillFromInputBus()
```

#### 修改 validateRecipeAccess()

在现有供应商检查之后追加资金检查（如第 5.4 节所述）。

#### 修改 checkProcessingMM()

在现有回收率应用之后追加货币扣除（如第 5.5 节所述）。

#### 修改 addUIWidgets()

新增金融状态行（y 位置在现有 UI 下方）：
```
资金: 铜币×128 | 状态: ✅ 充足
```
使用 `FakeSyncWidget.StringSyncer` + `syncedFinancialStatus` 同步到客户端。

#### 修改 getInfoData() / reportMetrics()

新增输出行：
```
资金舱室: 已安装/未安装
库存: 铜币×N 钢币×N 钛币×N 铂币×N 中子币×N 无尽币×N
```

### 6.3 注册与加载

#### MachineLoader

新增注册：
```java
// 金融舱口（Meta ID 待分配）
ModItemList.FinancialHatch.set(
    new FinancialHatch(id++, "financial_hatch", "Financial Hatch", 0).getStackForm(1L)
);
```

#### ModItemList

新增枚举条目：`FinancialHatch`

#### CurrencyLoader（新增类）

在 `preInit` 阶段注册 `CurrencyItem`：
```java
public class CurrencyLoader {
    public static void loadCurrencies() {
        CurrencyItem currencyItem = new CurrencyItem();
        GameRegistry.registerItem(currencyItem, "currency");
        // 将各 meta 的 ItemStack 存入 ModItemList
        for (CurrencyType type : CurrencyType.values()) {
            ModItemList.CurrencyEntries[type.ordinal()].set(
                new ItemStack(currencyItem, 1, type.getMeta())
            );
        }
    }
}
```

#### Config

新增配置项：
```java
// FinancialSystem 类别
boolean EnableFinancialSystem = true;   // 资金系统总开关
boolean EnableAutoRefillFromInputBus = true;  // 输入总线自动补货开关
```

---

## 7. Meta ID 分配

| Meta ID | 类 | 说明 |
|---------|-----|------|
| 35107 | FinancialHatch | 金融舱口（当前供应商舱口结束于 35106） |
| 不适用 | CurrencyItem | 货币物品（Item，非 MetaTileEntity，使用 meta 0-5） |

Meta ID 计算路径：`MODULAR_BASE(35050) + 46(标准模块) + 4(AHTech 专属) + 7(供应商) = 35107`。

---

## 8. 文件清单

### 新增文件

```
src/main/java/com/andgatech/AHTech/
├── common/
│   ├── currency/
│   │   ├── CurrencyType.java        货币类型枚举（6 种）
│   │   └── CurrencyItem.java        货币物品类（1 Item，meta 区分）
│   └── modularizedMachine/
│       └── modularHatches/
│           └── FinancialHatch.java   金融舱口（含物品栏、补货、消耗逻辑）
├── loader/
│   └── CurrencyLoader.java          货币物品注册入口
└── recipe/
    └── machineRecipe/
        └── CurrencyRecipePool.java   压模机配方（6 种币）
```

### 修改文件

| 文件 | 修改内容 |
|------|---------|
| `AHTechRecipeMetadata.java` | 新增 `CURRENCY_TYPE` 和 `CURRENCY_COST` 元数据 key |
| `ModularHatchType.java` | 新增 `FINANCIAL` 枚举值 |
| `ElectronicsMarket.java` | 新增金融舱口扫描、资金检查、货币消耗、UI 显示、信息屏输出 |
| `MachineLoader.java` | 注册金融舱口 |
| `ModItemList.java` | 新增 `FinancialHatch` 和货币条目 |
| `Config.java` | 新增资金系统配置项 |
| `RecipeLoader.java` | 调用 `CurrencyRecipePool` |

---

## 9. 与现有系统的交互

### 9.1 资金系统 × 合同系统

两个维度独立运作：
- **合同等级**（Lv0~Lv4）：决定供应商激活
- **资金系统**：决定配方能否执行（有足够货币）

交叉规则：
- 合同等级足够 + 资金充足 → 配方可执行
- 合同等级足够 + 资金不足 → 配方拒绝执行（安全待机）
- 合同等级不足 → 供应商未激活，资金检查不触发（配方已被供应商门禁拦截）

### 9.2 资金系统 × 模块系统

- 金融舱口与现有模块舱口互不干扰
- 模块决定"拆多少/多快"，资金决定"能不能启动"
- 金融舱口不需要特定结构等级（所有阶段可用）

### 9.3 资金系统 × 供应商系统

- 供应商解锁"能拆什么"，资金系统提供"执行成本"
- 两者可独立实现，配方同时携带供应商标记和货币标记

---

## 10. 实现范围与优先级

### 阶段 A：货币物品（前置）
1. 定义 `CurrencyType` 枚举（6 种币）
2. 实现 `CurrencyItem` 类（1 Item，meta 0-5）
3. 实现 `CurrencyLoader` 注册入口
4. 实现 `CurrencyRecipePool` 压模机配方

### 阶段 B：金融舱口
1. 新增 `ModularHatchType.FINANCIAL` 枚举值
2. 实现 `FinancialHatch`（含 9 格物品栏、物品过滤、货币计数/消耗）
3. 实现自动补货（从输入总线抽取硬币）
4. 金融舱口 GUI

### 阶段 C：配方标记与资金消耗
1. 新增 `CURRENCY_TYPE` 和 `CURRENCY_COST` 元数据 key
2. 修改 `ElectronicsMarket.validateRecipeAccess()` 添加资金检查
3. 修改 `ElectronicsMarket.checkProcessingMM()` 添加货币扣除
4. 为现有配方添加货币消耗标记（逐步）

### 阶段 D：UI 与信息屏集成
1. 主控制器 UI 显示金融状态
2. `getInfoData()` / `reportMetrics()` 新增金融输出行
3. I18n 翻译

### 阶段 E：配置与注册
1. 新增配置项
2. 更新 MachineLoader、ModItemList
3. 端到端验证
