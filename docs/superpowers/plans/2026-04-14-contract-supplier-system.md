# 供货协议合同系统与供应商系统实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现供货协议合同物品（4 级，控制供应商激活上限）和供应商方块（7 个，按现实领域解锁配方），替代原结构等级对配方可用性的控制。

**Architecture:** 合同物品放入 GTNH 原生数据舱（MTEHatchDataAccess），控制器在 `checkMachine()` 中读取合同等级。供应商方块继承现有 `ModularHatchBase`，作为新舱口类型注册。配方通过 GTNH 的 `RecipeMetadataKey<String>` 标记所属供应商，`validateRecipe()` 检查对应供应商是否已激活。

**Tech Stack:** Java 8, GT5-Unofficial API, ModularUI 1 (GTNH 1.7.10 版本), GTNHLib, JUnit 4

**Spec:** `docs/superpowers/specs/2026-04-14-contract-supplier-system-design.md`

---

## 文件结构

### 新建文件

| 文件 | 职责 |
|------|------|
| `common/contract/ContractTier.java` | 合同等级枚举（Lv0~Lv4） |
| `common/contract/ContractItem.java` | 合同物品基类（带 NBT tier） |
| `common/contract/ContractItemLv1.java` ~ `ContractItemLv4.java` | 4 级合同物品具体类 |
| `common/supplier/SupplierId.java` | 供应商 ID 枚举 |
| `common/supplier/SupplierInfo.java` | 供应商静态信息（名称、标语、领域、品牌色） |
| `common/supplier/SupplierHatch.java` | 供应商舱口基类（继承 ModularHatchBase） |
| `recipe/metadata/AHTechRecipeConstants.java` | 供应商 RecipeMetadataKey 定义 |
| `loader/ContractLoader.java` | 合同物品注册（preInit） |
| `loader/SupplierLoader.java` | 供应商方块注册（init，从 MachineLoader 中分离） |

### 修改文件

| 文件 | 变更内容 |
|------|---------|
| `common/machine/ElectronicsMarket.java` | 读取合同等级、扫描供应商、validateRecipe 增加供应商检查 |
| `common/modularizedMachine/ModularHatchType.java` | 新增 `SUPPLIER` 类型 |
| `common/modularizedMachine/IModularizedMachine.java` | 新增供应商相关接口方法 |
| `common/modularizedMachine/ModularizedMachineBase.java` | 实现供应商舱口收集与合同读取 |
| `common/ModItemList.java` | 新增合同物品和供应商枚举条目 |
| `config/Config.java` | 新增合同/供应商相关配置项 |
| `loader/MachineLoader.java` | 调用 SupplierLoader，添加供应商舱口 |
| `recipe/machineRecipe/ElectronicsMarketRecipePool.java` | 为配方添加 supplierId metadata |
| `CommonProxy.java` | preInit 中调用 ContractLoader |

---

## Phase A：合同物品基础

### Task 1：ContractTier 枚举

**Files:**
- Create: `src/main/java/com/andgatech/AHTech/common/contract/ContractTier.java`

- [ ] **Step 1：创建 ContractTier 枚举**

```java
package com.andgatech.AHTech.common.contract;

public enum ContractTier {
    NONE(0, "ahtech.contract.none"),
    LV1(1, "ahtech.contract.lv1"),
    LV2(2, "ahtech.contract.lv2"),
    LV3(3, "ahtech.contract.lv3"),
    LV4(4, "ahtech.contract.lv4");

    public final int tier;
    public final String translationKey;

    ContractTier(int tier, String translationKey) {
        this.tier = tier;
        this.translationKey = translationKey;
    }

    public static ContractTier fromTier(int tier) {
        for (ContractTier ct : values()) {
            if (ct.tier == tier) return ct;
        }
        return NONE;
    }
}
```

- [ ] **Step 2：编译验证**

Run: `cd D:/Code/AndgateTechnology && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3：提交**

```bash
git add src/main/java/com/andgatech/AHTech/common/contract/ContractTier.java
git commit -m "feat: add ContractTier enum for supplier contract system"
```

---

### Task 2：ContractItem 基类与 4 级物品

**Files:**
- Create: `src/main/java/com/andgatech/AHTech/common/contract/ContractItem.java`

- [ ] **Step 1：创建 ContractItem 基类**

合同物品是一个简单 Item，通过 NBT 存储等级。不需要 4 个独立子类——用一个类 + tier 参数即可。

```java
package com.andgatech.AHTech.common.contract;

import java.util.List;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import com.andgatech.AHTech.AndgateTechnology;

public class ContractItem extends Item {

    private final ContractTier contractTier;
    private IIcon icon;

    public ContractItem(ContractTier tier) {
        this.contractTier = tier;
        setUnlocalizedName("ahtech.contract." + tier.name().toLowerCase());
        setCreativeTab(CreativeTabs.tabMisc);
        setMaxStackSize(1);
        setHasSubtypes(false);
    }

    public ContractTier getContractTier() {
        return contractTier;
    }

    public static ContractTier getTierFromStack(ItemStack stack) {
        if (stack != null && stack.getItem() instanceof ContractItem) {
            return ((ContractItem) stack.getItem()).contractTier;
        }
        return ContractTier.NONE;
    }

    @Override
    public void registerIcons(IIconRegister reg) {
        icon = reg.registerIcon(
            AndgateTechnology.MODID + ":contract/" + contractTier.name().toLowerCase());
    }

    @Override
    public IIcon getIconFromDamage(int meta) {
        return icon;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv) {
        list.add(contractTier.translationKey);
        if (contractTier.tier > 1) {
            list.add("ahtech.contract.tooltip.upgrade_from_" + (contractTier.tier - 1));
        }
    }
}
```

- [ ] **Step 2：编译验证**

Run: `cd D:/Code/AndgateTechnology && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3：提交**

```bash
git add src/main/java/com/andgatech/AHTech/common/contract/ContractItem.java
git commit -m "feat: add ContractItem base class for supply agreement contracts"
```

---

### Task 3：合同物品注册（ContractLoader + ModItemList）

**Files:**
- Create: `src/main/java/com/andgatech/AHTech/loader/ContractLoader.java`
- Modify: `src/main/java/com/andgatech/AHTech/common/ModItemList.java`
- Modify: `src/main/java/com/andgatech/AHTech/CommonProxy.java`

- [ ] **Step 1：在 ModItemList 中添加合同枚举条目**

在 `ModItemList.java` 的枚举体中添加（放在 `GeneralDisassemblyModule` 后面）：

```java
// 供货协议合同
ContractLv1,
ContractLv2,
ContractLv3,
ContractLv4,
```

- [ ] **Step 2：创建 ContractLoader**

```java
package com.andgatech.AHTech.loader;

import com.andgatech.AHTech.common.ModItemList;
import com.andgatech.AHTech.common.contract.ContractItem;
import com.andgatech.AHTech.common.contract.ContractTier;
import cpw.mods.fml.common.registry.GameRegistry;

public class ContractLoader {

    public static void loadContracts() {
        ContractItem lv1 = new ContractItem(ContractTier.LV1);
        ContractItem lv2 = new ContractItem(ContractTier.LV2);
        ContractItem lv3 = new ContractItem(ContractTier.LV3);
        ContractItem lv4 = new ContractItem(ContractTier.LV4);

        GameRegistry.registerItem(lv1, "contract_lv1");
        GameRegistry.registerItem(lv2, "contract_lv2");
        GameRegistry.registerItem(lv3, "contract_lv3");
        GameRegistry.registerItem(lv4, "contract_lv4");

        ModItemList.ContractLv1.set(new ItemStack(lv1, 1));
        ModItemList.ContractLv2.set(new ItemStack(lv2, 1));
        ModItemList.ContractLv3.set(new ItemStack(lv3, 1));
        ModItemList.ContractLv4.set(new ItemStack(lv4, 1));
    }
}
```

注意：需要在顶部添加 `import net.minecraft.item.ItemStack;`。

- [ ] **Step 3：在 CommonProxy.preInit() 中调用 ContractLoader**

在 `CommonProxy.java` 的 `preInit()` 方法中，在现有逻辑后添加：

```java
ContractLoader.loadContracts();
```

添加 import:
```java
import com.andgatech.AHTech.loader.ContractLoader;
```

- [ ] **Step 4：编译验证**

Run: `cd D:/Code/AndgateTechnology && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5：提交**

```bash
git add src/main/java/com/andgatech/AHTech/common/ModItemList.java \
        src/main/java/com/andgatech/AHTech/loader/ContractLoader.java \
        src/main/java/com/andgatech/AHTech/CommonProxy.java
git commit -m "feat: register 4-level contract items via ContractLoader"
```

---

### Task 4：合同合成配方

**Files:**
- Create: `src/main/java/com/andgatech/AHTech/recipe/machineRecipe/ContractRecipePool.java`
- Modify: `src/main/java/com/andgatech/AHTech/loader/RecipeLoader.java`

- [ ] **Step 1：创建 ContractRecipePool**

合成配方在 `loadRecipes()` 阶段注册。使用 GTNH 的 `GTValues.RA.stdBuilder()` 模式。

```java
package com.andgatech.AHTech.recipe.machineRecipe;

import static gregtech.api.enums.GTValues.RA;

import com.andgatech.AHTech.common.ModItemList;

import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.util.GTRecipeConstants;

public class ContractRecipePool {

    public static void load() {
        // Lv1: 力场发生器(LV) + 基础电路板 + 铜锭 + 锡板
        RA.stdBuilder()
            .itemInput(
                ItemList.Field_Generator_LV.get(1),
                ItemList.Circuit_Basic.get(1),
                Materials.Copper.getIngot(16),
                Materials.Tin.getPlate(8)
            )
            .itemOutput(ModItemList.ContractLv1.get(1))
            .duration(200).eut(30)
            .addTo(com.andgatech.AHTech.recipe.recipeMap.AHTechRecipeMaps.ElectronicsMarketRecipes);

        // Lv2: Lv1合同 + 力场发生器(IV) + 高级电路板 + 红合金锭
        RA.stdBuilder()
            .itemInput(
                ModItemList.ContractLv1.get(1),
                ItemList.Field_Generator_IV.get(1),
                ItemList.Circuit_Advanced.get(1),
                Materials.RedAlloy.getIngot(16)
            )
            .itemOutput(ModItemList.ContractLv2.get(1))
            .duration(400).eut(512)
            .addTo(com.andgatech.AHTech.recipe.recipeMap.AHTechRecipeMaps.ElectronicsMarketRecipes);

        // Lv3: Lv2合同 + 力场发生器(ZPM) + 精英电路板
        RA.stdBuilder()
            .itemInput(
                ModItemList.ContractLv2.get(1),
                ItemList.Field_Generator_ZPM.get(1),
                ItemList.Circuit_Elite.get(1),
                Materials.Titanium.getPlate(16)
            )
            .itemOutput(ModItemList.ContractLv3.get(1))
            .duration(800).eut(20480)
            .addTo(com.andgatech.AHTech.recipe.recipeMap.AHTechRecipeMaps.ElectronicsMarketRecipes);

        // Lv4: Lv3合同 + 力场发生器(UhV) + 大师电路板
        RA.stdBuilder()
            .itemInput(
                ModItemList.ContractLv3.get(1),
                ItemList.Field_Generator_UHV.get(1),
                ItemList.Circuit_Master.get(1),
                Materials.Osmium.getPlate(16)
            )
            .itemOutput(ModItemList.ContractLv4.get(1))
            .duration(1600).eut(524288)
            .addTo(com.andgatech.AHTech.recipe.recipeMap.AHTechRecipeMaps.ElectronicsMarketRecipes);
    }
}
```

注意：合成配方在 `completeInit` 阶段通过 `RecipeLoader.loadRecipes()` 调用。合同配方的 `specialValue` 默认为 0（通用配方），所以任何等级结构都能看到这些合成配方。但更合理的做法是将它们注册为普通合成台配方，让玩家在合成台中合成。

**替代方案**：如果合同配方应该通过普通合成台合成，改用 Forge CraftingManager：

```java
// 合成台方案（九宫格合成）
GameRegistry.addRecipe(
    ModItemList.ContractLv1.get(1),
    " F ", " C ", " T ",
    'F', ItemList.Field_Generator_LV.get(1),
    'C', ItemList.Circuit_Basic.get(1),
    'T', Materials.Copper.getIngot(1)
);
```

**决策点**：合同合成的注册方式（电子市场配方 vs 合成台配方）应在实现时确认。合成台方案更直觉，且不依赖机器本身。

- [ ] **Step 2：在 RecipeLoader.loadRecipes() 中调用**

在 `RecipeLoader.java` 的 `loadRecipes()` 方法中添加：

```java
ContractRecipePool.load();
```

- [ ] **Step 3：编译验证**

Run: `cd D:/Code/AndgateTechnology && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4：提交**

```bash
git add src/main/java/com/andgatech/AHTech/recipe/machineRecipe/ContractRecipePool.java \
        src/main/java/com/andgatech/AHTech/loader/RecipeLoader.java
git commit -m "feat: add contract crafting recipes with tiered progression"
```

---

### Task 5：控制器读取合同等级

**Files:**
- Modify: `src/main/java/com/andgatech/AHTech/common/modularizedMachine/ModularizedMachineBase.java`
- Modify: `src/main/java/com/andgatech/AHTech/common/machine/ElectronicsMarket.java`

- [ ] **Step 1：在 ModularizedMachineBase 中添加合同读取方法**

在 `ModularizedMachineBase.java` 中添加字段和方法：

```java
import com.andgatech.AHTech.common.contract.ContractItem;
import com.andgatech.AHTech.common.contract.ContractTier;
import gregtech.api.metatileentity.implementations.MTEHatchDataAccess;
```

添加字段（在现有字段区域）：
```java
protected ContractTier contractTier = ContractTier.NONE;
```

添加方法：
```java
/**
 * 从数据舱中读取合同等级。在 checkMachine() 期间调用。
 */
protected void readContractFromDataHatches() {
    contractTier = ContractTier.NONE;
    for (MTEHatchDataAccess hatch : mDataAccessHatches) {
        // mDataAccessHatches 来自 MTEExtendedPowerMultiBlockBase
        if (hatch.isValid()) {
            for (int i = 0; i < hatch.getBaseMetaTileEntity().getSizeInventory(); i++) {
                ItemStack stack = hatch.getBaseMetaTileEntity().getStackInSlot(i);
                ContractTier tier = ContractItem.getTierFromStack(stack);
                if (tier.tier > contractTier.tier) {
                    contractTier = tier;
                }
            }
        }
    }
}

public ContractTier getContractTier() {
    return contractTier;
}
```

注意：`mDataAccessHatches` 是 `MTEExtendedPowerMultiBlockBase` 的继承字段。如果该字段不可直接访问，需要通过 `validMTEList()` 获取。实际实现时需确认字段可见性。

- [ ] **Step 2：在 ElectronicsMarket 的 checkMachineMM() 中调用合同读取**

在 `ElectronicsMarket.java` 的 `checkMachineMM()` 方法中，在结构检查通过后（`return structureTier >= TIER_I` 之前）调用：

```java
readContractFromDataHatches();
```

- [ ] **Step 3：编译验证**

Run: `cd D:/Code/AndgateTechnology && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4：提交**

```bash
git add src/main/java/com/andgatech/AHTech/common/modularizedMachine/ModularizedMachineBase.java \
        src/main/java/com/andgatech/AHTech/common/machine/ElectronicsMarket.java
git commit -m "feat: controller reads contract tier from data access hatches"
```

---

## Phase B：供应商方块系统

### Task 6：SupplierId 枚举与 SupplierInfo

**Files:**
- Create: `src/main/java/com/andgatech/AHTech/common/supplier/SupplierId.java`
- Create: `src/main/java/com/andgatech/AHTech/common/supplier/SupplierInfo.java`

- [ ] **Step 1：创建 SupplierId 枚举**

```java
package com.andgatech.AHTech.common.supplier;

import com.andgatech.AHTech.common.contract.ContractTier;

public enum SupplierId {
    SHANDONG_DEZHOU(ContractTier.LV1, "shandong_dezhou"),
    YADEPIAN(ContractTier.LV1, "yadepian"),
    ERFA(ContractTier.LV1, "erfa"),
    CHAOLA(ContractTier.LV2, "chaola"),
    HUANGWEIDA(ContractTier.LV2, "huangweida"),
    GAIGUANG(ContractTier.LV2, "gaiguang"),
    DITONG(ContractTier.LV3, "ditong");

    public final ContractTier minContractTier;
    public final String id;

    SupplierId(ContractTier minContractTier, String id) {
        this.minContractTier = minContractTier;
        this.id = id;
    }
}
```

- [ ] **Step 2：创建 SupplierInfo 数据类**

```java
package com.andgatech.AHTech.common.supplier;

import net.minecraft.util.EnumChatFormatting;

public class SupplierInfo {

    public final SupplierId supplierId;
    public final String displayNameKey;
    public final String taglineKey;
    public final String descriptionKey;
    public final EnumChatFormatting brandColor;
    public final String recipeDomain;

    public SupplierInfo(SupplierId supplierId, String displayNameKey, String taglineKey,
        String descriptionKey, EnumChatFormatting brandColor, String recipeDomain) {
        this.supplierId = supplierId;
        this.displayNameKey = displayNameKey;
        this.taglineKey = taglineKey;
        this.descriptionKey = descriptionKey;
        this.brandColor = brandColor;
        this.recipeDomain = recipeDomain;
    }

    public static SupplierInfo get(SupplierId id) {
        return SupplierInfoRegistry.get(id);
    }
}
```

- [ ] **Step 3：创建 SupplierInfoRegistry（供应商信息注册表）**

Create: `src/main/java/com/andgatech/AHTech/common/supplier/SupplierInfoRegistry.java`

```java
package com.andgatech.AHTech.common.supplier;

import java.util.EnumMap;
import net.minecraft.util.EnumChatFormatting;

public class SupplierInfoRegistry {

    private static final EnumMap<SupplierId, SupplierInfo> registry = new EnumMap<>(SupplierId.class);

    static {
        register(SupplierId.SHANDONG_DEZHOU,
            "ahtech.supplier.shandong_dezhou.name",
            "ahtech.supplier.shandong_dezhou.tagline",
            "ahtech.supplier.shandong_dezhou.desc",
            EnumChatFormatting.GOLD, "circuits_and_ic");

        register(SupplierId.YADEPIAN,
            "ahtech.supplier.yadepian.name",
            "ahtech.supplier.yadepian.tagline",
            "ahtech.supplier.yadepian.desc",
            EnumChatFormatting.DARK_BLUE, "analog_and_sensors");

        register(SupplierId.ERFA,
            "ahtech.supplier.erfa.name",
            "ahtech.supplier.erfa.tagline",
            "ahtech.supplier.erfa.desc",
            EnumChatFormatting.BLUE, "general_semiconductor");

        register(SupplierId.CHAOLA,
            "ahtech.supplier.chaola.name",
            "ahtech.supplier.chaola.tagline",
            "ahtech.supplier.chaola.desc",
            EnumChatFormatting.LIGHT_PURPLE, "fpga_and_logic");

        register(SupplierId.HUANGWEIDA,
            "ahtech.supplier.huangweida.name",
            "ahtech.supplier.huangweida.tagline",
            "ahtech.supplier.huangweida.desc",
            EnumChatFormatting.GREEN, "gpu_and_processor");

        register(SupplierId.GAIGUANG,
            "ahtech.supplier.gaiguang.name",
            "ahtech.supplier.gaiguang.tagline",
            "ahtech.supplier.gaiguang.desc",
            EnumChatFormatting.YELLOW, "optoelectronics");

        register(SupplierId.DITONG,
            "ahtech.supplier.ditong.name",
            "ahtech.supplier.ditong.tagline",
            "ahtech.supplier.ditong.desc",
            EnumChatFormatting.RED, "communication_and_rf");
    }

    private static void register(SupplierId id, String nameKey, String taglineKey,
        String descKey, EnumChatFormatting color, String domain) {
        registry.put(id, new SupplierInfo(id, nameKey, taglineKey, descKey, color, domain));
    }

    public static SupplierInfo get(SupplierId id) {
        return registry.get(id);
    }
}
```

- [ ] **Step 4：编译验证**

Run: `cd D:/Code/AndgateTechnology && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5：提交**

```bash
git add src/main/java/com/andgatech/AHTech/common/supplier/
git commit -m "feat: add supplier ID enum, info data class, and registry"
```

---

### Task 7：SupplierHatch 供应商舱口基类

**Files:**
- Create: `src/main/java/com/andgatech/AHTech/common/supplier/SupplierHatch.java`
- Modify: `src/main/java/com/andgatech/AHTech/common/modularizedMachine/ModularHatchType.java`

- [ ] **Step 1：在 ModularHatchType 中添加 SUPPLIER**

在 `ModularHatchType.java` 枚举中添加：

```java
SUPPLIER,
```

放在 `FUNCTION_MODULE` 之后、`ALL` 之前。

- [ ] **Step 2：创建 SupplierHatch 基类**

```java
package com.andgatech.AHTech.common.supplier;

import java.util.List;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import com.andgatech.AHTech.AndgateTechnology;
import com.andgatech.AHTech.common.contract.ContractTier;
import com.andgatech.AHTech.common.modularizedMachine.ModularHatchBase;
import com.andgatech.AHTech.common.modularizedMachine.ModularHatchType;
import com.andgatech.AHTech.common.modularizedMachine.ModularizedMachineBase;

public class SupplierHatch extends ModularHatchBase {

    protected final SupplierId supplierId;
    protected final SupplierInfo supplierInfo;
    protected boolean activated = false;
    private IIcon icon;

    public SupplierHatch(int aId, String aName, String aNameRegional, SupplierId supplierId) {
        super(aId, aName, aNameRegional, 0); // tier=0, 不使用模块等级体系
        this.supplierId = supplierId;
        this.supplierInfo = SupplierInfoRegistry.get(supplierId);
    }

    public SupplierId getSupplierId() {
        return supplierId;
    }

    public ContractTier getMinContractTier() {
        return supplierId.minContractTier;
    }

    public boolean isActivated() {
        return activated;
    }

    @Override
    public ModularHatchType getType() {
        return ModularHatchType.SUPPLIER;
    }

    @Override
    public boolean isCompatibleWithMachine(ModularizedMachineBase<?> machine) {
        // 供应商不检查结构等级，只检查合同等级
        return true;
    }

    /**
     * 由控制器调用，根据合同等级更新激活状态
     */
    public void updateActivation(ContractTier contractTier) {
        this.activated = contractTier.tier >= supplierId.minContractTier.tier;
    }

    @Override
    public void registerIcons(IIconRegister reg) {
        icon = reg.registerIcon(
            AndgateTechnology.MODID + ":supplier/" + supplierId.id);
    }

    @Override
    public IIcon getIconFromDamage(int meta) {
        return icon;
    }

    @Override
    public String[] getDescription() {
        return new String[] {
            supplierInfo.brandColor + StatCollector.translateToLocal(supplierInfo.displayNameKey),
            EnumChatFormatting.GRAY + StatCollector.translateToLocal(supplierInfo.taglineKey)
        };
    }

    @Override
    public void addAdditionalTooltipInformation(ItemStack stack, List<String> tooltip) {
        super.addAdditionalTooltipInformation(stack, tooltip);
        tooltip.add(EnumChatFormatting.AQUA + "ahtech.supplier.tooltip.contract_requirement"
            + ": " + supplierId.minContractTier.translationKey);
    }
}
```

注意：`StatCollector` 和 `EnumChatFormatting` 需要对应 import。`addAdditionalTooltipInformation` 方法名需确认是否存在于 `ModularHatchBase` 基类中——如不存在，改用 `getDescription()` 提供信息。

- [ ] **Step 3：编译验证**

Run: `cd D:/Code/AndgateTechnology && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4：提交**

```bash
git add src/main/java/com/andgatech/AHTech/common/supplier/SupplierHatch.java \
        src/main/java/com/andgatech/AHTech/common/modularizedMachine/ModularHatchType.java
git commit -m "feat: add SupplierHatch base class and SUPPLIER hatch type"
```

---

### Task 8：供应商方块注册（SupplierLoader）

**Files:**
- Create: `src/main/java/com/andgatech/AHTech/loader/SupplierLoader.java`
- Modify: `src/main/java/com/andgatech/AHTech/common/ModItemList.java`
- Modify: `src/main/java/com/andgatech/AHTech/loader/MachineLoader.java`

- [ ] **Step 1：在 ModItemList 中添加供应商枚举条目**

```java
// 供应商
SupplierShandongDezhou,
SupplierYadepian,
SupplierErfa,
SupplierChaola,
SupplierHuangweida,
SupplierGaiguang,
SupplierDitong,
```

- [ ] **Step 2：创建 SupplierLoader**

```java
package com.andgatech.AHTech.loader;

import com.andgatech.AHTech.common.ModItemList;
import com.andgatech.AHTech.common.supplier.SupplierHatch;
import com.andgatech.AHTech.common.supplier.SupplierId;

public class SupplierLoader {

    // 供应商 Meta ID 从 35100 开始
    private static int nextId = 35100;

    public static void loadSuppliers() {
        registerSupplier(SupplierId.SHANDONG_DEZHOU, ModItemList.SupplierShandongDezhou);
        registerSupplier(SupplierId.YADEPIAN, ModItemList.SupplierYadepian);
        registerSupplier(SupplierId.ERFA, ModItemList.SupplierErfa);
        registerSupplier(SupplierId.CHAOLA, ModItemList.SupplierChaola);
        registerSupplier(SupplierId.HUANGWEIDA, ModItemList.SupplierHuangweida);
        registerSupplier(SupplierId.GAIGUANG, ModItemList.SupplierGaiguang);
        registerSupplier(SupplierId.DITONG, ModItemList.SupplierDitong);
    }

    private static void registerSupplier(SupplierId sid, ModItemList itemEntry) {
        String name = "ahtech.supplier." + sid.id;
        String regional = sid.id;
        SupplierHatch hatch = new SupplierHatch(nextId++, name, regional, sid);
        itemEntry.set(hatch.getStackForm(1L));
    }
}
```

- [ ] **Step 3：在 MachineLoader.loadMachines() 中调用 SupplierLoader**

在 `MachineLoader.java` 的 `loadMachines()` 方法末尾添加：

```java
SupplierLoader.loadSuppliers();
```

添加 import:
```java
import com.andgatech.AHTech.loader.SupplierLoader;
```

- [ ] **Step 4：编译验证**

Run: `cd D:/Code/AndgateTechnology && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5：提交**

```bash
git add src/main/java/com/andgatech/AHTech/common/ModItemList.java \
        src/main/java/com/andgatech/AHTech/loader/SupplierLoader.java \
        src/main/java/com/andgatech/AHTech/loader/MachineLoader.java
git commit -m "feat: register 7 supplier hatch blocks via SupplierLoader"
```

---

### Task 9：控制器扫描供应商并更新激活状态

**Files:**
- Modify: `src/main/java/com/andgatech/AHTech/common/modularizedMachine/ModularizedMachineBase.java`
- Modify: `src/main/java/com/andgatech/AHTech/common/modularizedMachine/IModularizedMachine.java`

- [ ] **Step 1：在 IModularizedMachine 中添加供应商相关接口方法**

```java
import com.andgatech.AHTech.common.supplier.SupplierId;
import java.util.Set;

// 在接口中添加：
Set<SupplierId> getActiveSupplierIds();
```

- [ ] **Step 2：在 ModularizedMachineBase 中实现供应商收集**

添加字段：

```java
import com.andgatech.AHTech.common.supplier.SupplierHatch;
import com.andgatech.AHTech.common.supplier.SupplierId;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

protected final Set<SupplierId> activeSupplierIds = new HashSet<>();
```

添加方法：

```java
@Override
public Set<SupplierId> getActiveSupplierIds() {
    return Collections.unmodifiableSet(activeSupplierIds);
}

/**
 * 扫描结构中的供应商舱口，根据合同等级更新激活状态。
 * 在 checkMachine() 中调用，位于 readContractFromDataHatches() 之后。
 */
protected void scanAndUpdateSuppliers() {
    activeSupplierIds.clear();
    for (IModularHatch hatch : allModularHatches) {
        if (hatch instanceof SupplierHatch) {
            SupplierHatch supplier = (SupplierHatch) hatch;
            supplier.updateActivation(contractTier);
            if (supplier.isActivated()) {
                activeSupplierIds.add(supplier.getSupplierId());
            }
        }
    }
}
```

- [ ] **Step 3：在 ElectronicsMarket.checkMachineMM() 中调用**

在 `readContractFromDataHatches()` 之后添加：

```java
scanAndUpdateSuppliers();
```

- [ ] **Step 4：编译验证**

Run: `cd D:/Code/AndgateTechnology && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5：提交**

```bash
git add src/main/java/com/andgatech/AHTech/common/modularizedMachine/IModularizedMachine.java \
        src/main/java/com/andgatech/AHTech/common/modularizedMachine/ModularizedMachineBase.java \
        src/main/java/com/andgatech/AHTech/common/machine/ElectronicsMarket.java
git commit -m "feat: controller scans supplier hatches and updates activation by contract tier"
```

---

## Phase C：配方标记与验证

### Task 10：供应商 RecipeMetadataKey

**Files:**
- Create: `src/main/java/com/andgatech/AHTech/recipe/metadata/AHTechRecipeConstants.java`

- [ ] **Step 1：创建 AHTechRecipeConstants**

GTNH 使用 `RecipeMetadataKey<T>` + `SimpleRecipeMetadataKey.create()` 定义类型化的配方元数据键。我们用 `String` 类型存储供应商 ID。

```java
package com.andgatech.AHTech.recipe.metadata;

import gregtech.api.recipe.RecipeMetadataKey;
import gregtech.api.recipe.metadata.SimpleRecipeMetadataKey;

public class AHTechRecipeConstants {

    /**
     * 配方所属的供应商 ID。值为 SupplierId.id 字符串。
     * null 或空字符串表示通用配方（不依赖供应商）。
     */
    public static final RecipeMetadataKey<String> SUPPLIER_ID =
        SimpleRecipeMetadataKey.create(String.class, "ahtech_supplier_id");
}
```

- [ ] **Step 2：编译验证**

Run: `cd D:/Code/AndgateTechnology && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3：提交**

```bash
git add src/main/java/com/andgatech/AHTech/recipe/metadata/AHTechRecipeConstants.java
git commit -m "feat: add SUPPLIER_ID RecipeMetadataKey for recipe-supplier binding"
```

---

### Task 11：validateRecipe 增加供应商检查

**Files:**
- Modify: `src/main/java/com/andgatech/AHTech/common/machine/ElectronicsMarket.java`

- [ ] **Step 1：在 validateRecipe 中添加供应商检查**

`ElectronicsMarket` 的 `validateRecipe()` 位于 `createProcessingLogic()` 内部的匿名类中。在现有的 `specialValue` 检查之后添加供应商检查：

```java
// 现有检查保持不变...

// 供应商检查
String supplierIdStr = recipe.getMetadata(AHTechRecipeConstants.SUPPLIER_ID);
if (supplierIdStr != null && !supplierIdStr.isEmpty()) {
    SupplierId requiredSupplier = SupplierId.valueOf(supplierIdStr.toUpperCase());
    if (!getActiveSupplierIds().contains(requiredSupplier)) {
        return DID_NOT_FIND_RECIPE; // 或合适的拒绝返回值
    }
}
```

需要添加 import：
```java
import com.andgatech.AHTech.recipe.metadata.AHTechRecipeConstants;
import com.andgatech.AHTech.common.supplier.SupplierId;
```

- [ ] **Step 2：编译验证**

Run: `cd D:/Code/AndgateTechnology && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3：提交**

```bash
git add src/main/java/com/andgatech/AHTech/common/machine/ElectronicsMarket.java
git commit -m "feat: validateRecipe checks supplier activation for tagged recipes"
```

---

### Task 12：为现有配方添加供应商标记

**Files:**
- Modify: `src/main/java/com/andgatech/AHTech/recipe/machineRecipe/ElectronicsMarketRecipePool.java`

- [ ] **Step 1：为线缆拆解配方添加供应商标记**

线缆拆解是基础配方，可分配给 `SHANDONG_DEZHOU`（德州仪器 = 电路板/线缆领域）。在 `ElectronicsMarketRecipePool` 的线缆拆解配方注册循环中，为每个配方添加 metadata：

```java
import com.andgatech.AHTech.recipe.metadata.AHTechRecipeConstants;

// 在 RA.stdBuilder() 链中添加：
.metadata(AHTechRecipeConstants.SUPPLIER_ID, SupplierId.SHANDONG_DEZHOU.id)
```

- [ ] **Step 2：为激光真空管配方添加供应商标记**

激光真空管是高级配方（需二阶结构），可分配给 `GAIGUANG`（钙光 = 光电器件）：

```java
.metadata(AHTechRecipeConstants.SUPPLIER_ID, SupplierId.GAIGUANG.id)
```

- [ ] **Step 3：编译验证**

Run: `cd D:/Code/AndgateTechnology && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4：提交**

```bash
git add src/main/java/com/andgatech/AHTech/recipe/machineRecipe/ElectronicsMarketRecipePool.java
git commit -m "feat: tag cable and laser pipe recipes with supplier IDs"
```

---

### Task 13：更新 UI 显示合同与供应商状态

**Files:**
- Modify: `src/main/java/com/andgatech/AHTech/common/machine/ElectronicsMarket.java`

- [ ] **Step 1：在 addUIWidgets 中添加合同等级和供应商状态显示**

在现有 UI 部件（Stage、Parallel、Speed、Recovery、Perfect OC）之后，添加两行新显示：

```java
// 合同等级显示
widgetList.add(new TextWidget(new Text("ahtech.ui.contract_label"))
    .setDefaultColor(EnumChatFormatting.AQUA.getRGB())
    .setPos(startX, startY + offset));
offset += 10;
widgetList.add(TextWidget.dynamicText(() ->
    new Text(contractTier.translationKey)
        .color(contractTier.tier > 0 ? EnumChatFormatting.GREEN : EnumChatFormatting.GRAY))
    .setPos(startX + labelWidth, startY + offset - 10));

// 已激活供应商数量
widgetList.add(new TextWidget(new Text("ahtech.ui.active_suppliers_label"))
    .setDefaultColor(EnumChatFormatting.AQUA.getRGB())
    .setPos(startX, startY + offset));
offset += 10;
widgetList.add(TextWidget.dynamicText(() ->
    new Text(String.valueOf(activeSupplierIds.size()))
        .color(activeSupplierIds.isEmpty() ? EnumChatFormatting.GRAY : EnumChatFormatting.GREEN))
    .setPos(startX + labelWidth, startY + offset - 10));
```

实际 UI 位置和 widget 构建方式需参照现有 `addUIWidgets()` 模式（使用 `FakeSyncWidget` 同步）。

- [ ] **Step 2：编译验证**

Run: `cd D:/Code/AndgateTechnology && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3：提交**

```bash
git add src/main/java/com/andgatech/AHTech/common/machine/ElectronicsMarket.java
git commit -m "feat: display contract tier and active supplier count in machine UI"
```

---

## Phase D：趣味内容（供应商 Tooltip 与信息面板）

### Task 14：供应商 Tooltip 与 I18n

**Files:**
- Modify: `src/main/resources/assets/andgatetechnology/lang/en_US.lang`
- Modify: `src/main/resources/assets/andgatetechnology/lang/zh_CN.lang`

- [ ] **Step 1：添加英文翻译**

在 `en_US.lang` 中添加：

```properties
# 合同
ahtech.contract.none=None
ahtech.contract.lv1=Lv1 Supply Agreement
ahtech.contract.lv2=Lv2 Supply Agreement
ahtech.contract.lv3=Lv3 Supply Agreement
ahtech.contract.lv4=Lv4 Supply Agreement
ahtech.contract.tooltip.upgrade_from_1=Upgrade from Lv1 Contract
ahtech.contract.tooltip.upgrade_from_2=Upgrade from Lv2 Contract
ahtech.contract.tooltip.upgrade_from_3=Upgrade from Lv3 Contract

# 供应商名称
ahtech.supplier.shandong_dezhou.name=Shandong Texas Instruments
ahtech.supplier.shandong_dezhou.tagline=Every chip tastes like Dezhou braised chicken
ahtech.supplier.shandong_dezhou.desc=A legendary semiconductor manufacturer from Shandong, combining traditional flavors with cutting-edge technology.
ahtech.supplier.yadepian.name=Yadepian Semiconductor
ahtech.supplier.yadepian.tagline=Honest analog solutions, despite the name
ahtech.supplier.yadepian.desc=The name says "deception" but the products are rock-solid. Precision analog ICs you can trust.
ahtech.supplier.erfa.name=Erfa Semiconductor
ahtech.supplier.erfa.tagline=Second method, first quality
ahtech.supplier.erfa.desc=General-purpose semiconductors, microcontrollers, and MEMS sensors. The reliable second choice.
ahtech.supplier.chaola.name=Chaola Semiconductor
ahtech.supplier.chaola.tagline=Beyond your imagination
ahtech.supplier.chaola.desc=Programmable logic and FPGA solutions. Flexible, powerful, occasionally confusing.
ahtech.supplier.huangweida.name=Huangweida
ahtech.supplier.huangweida.tagline=The GPU legend
ahtech.supplier.huangweida.desc=From graphics cards to AI chips, a legend in the making. Green is not just a color, it's a way of life.
ahtech.supplier.gaiguang.name=Gaiguang Optoelectronics
ahtech.supplier.gaiguang.tagline=Calcium and light, all in one
ahtech.supplier.gaiguang.desc=Optoelectronic devices, lasers, and display modules. Making the world brighter and healthier.
ahtech.supplier.ditong.name=Ditong Communications
ahtech.supplier.ditong.tagline=Low-pass filter, high-pass quality
ahtech.supplier.ditong.desc=Communication chips and RF modules. The friendly neighbor of a certain high-pass company.
ahtech.supplier.tooltip.contract_requirement=Contract Required

# UI
ahtech.ui.contract_label=Contract:
ahtech.ui.active_suppliers_label=Active Suppliers:
```

- [ ] **Step 2：添加中文翻译**

在 `zh_CN.lang` 中添加：

```properties
# 合同
ahtech.contract.none=无
ahtech.contract.lv1=初级供货协议
ahtech.contract.lv2=中级供货协议
ahtech.contract.lv3=高级供货协议
ahtech.contract.lv4=终极供货协议
ahtech.contract.tooltip.upgrade_from_1=由初级合同升级
ahtech.contract.tooltip.upgrade_from_2=由中级合同升级
ahtech.contract.tooltip.upgrade_from_3=由高级合同升级

# 供应商名称
ahtech.supplier.shandong_dezhou.name=山东德州仪器
ahtech.supplier.shandong_dezhou.tagline=每颗芯片都有扒鸡味
ahtech.supplier.shandong_dezhou.desc=来自山东的传奇半导体制造商，传统风味与尖端科技的完美结合。
ahtech.supplier.yadepian.name=亚德骗半导体
ahtech.supplier.yadepian.tagline=绝不骗人的模拟方案
ahtech.supplier.yadepian.desc=名字带"骗"但产品靠谱，精确模拟芯片，值得信赖。
ahtech.supplier.erfa.name=二法半导体
ahtech.supplier.erfa.tagline=第二种方法，第一品质
ahtech.supplier.erfa.desc=通用半导体、微控制器和 MEMS 传感器，可靠的第二种选择。
ahtech.supplier.chaola.name=超拉半导体
ahtech.supplier.chaola.tagline=超越你的想象力
ahtech.supplier.chaola.desc=可编程逻辑与 FPGA 解决方案，灵活、强大、偶尔让人困惑。
ahtech.supplier.huangweida.name=黄伟达
ahtech.supplier.huangweida.tagline=显卡界的传奇人物
ahtech.supplier.huangweida.desc=从游戏显卡到 AI 芯片，传奇正在书写。绿色不仅是颜色，更是一种信仰。
ahtech.supplier.gaiguang.name=钙光光电
ahtech.supplier.gaiguang.tagline=补钙又补光
ahtech.supplier.gaiguang.desc=光电器件、激光器与显示模块，让世界更亮更健康。
ahtech.supplier.ditong.name=低通通信
ahtech.supplier.ditong.tagline=低通滤波，高通品质
ahtech.supplier.ditong.desc=通信芯片与射频模块，某高通公司的友好邻居。
ahtech.supplier.tooltip.contract_requirement=需要合同

# UI
ahtech.ui.contract_label=合同等级：
ahtech.ui.active_suppliers_label=已激活供应商：
```

- [ ] **Step 3：提交**

```bash
git add src/main/resources/assets/andgatetechnology/lang/en_US.lang \
        src/main/resources/assets/andgatetechnology/lang/zh_CN.lang
git commit -m "feat: add I18n translations for contracts and suppliers (EN/ZH)"
```

---

### Task 15：供应商信息面板 GUI（右键打开）

**Files:**
- Modify: `src/main/java/com/andgatech/AHTech/common/supplier/SupplierHatch.java`

此任务实现右键点击供应商方块打开信息面板。需要使用 ModularUI 1（GTNH 1.7.10 版本）的 GUI 模式。

- [ ] **Step 1：在 SupplierHatch 中实现右键 GUI**

GTNH 1.7.10 的舱口 GUI 通常通过 `onRightclick()` + `getServerGUI()`/`getClientGUI()` 实现（ModularUI 2 是更高版本的模式）。需要确认当前项目使用的 ModularUI 版本。

基于 GTNH 1.7.10 的典型模式：

```java
@Override
public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
    // 打开信息面板 GUI
    // 使用 GTNHLib 的 GUI 工具或直接发送聊天消息
    if (aBaseMetaTileEntity.isServerSide()) {
        sendSupplierInfo(aPlayer);
    }
    return true;
}

private void sendSupplierInfo(EntityPlayer player) {
    SupplierInfo info = supplierInfo;
    player.addChatMessage(new ChatComponentText(""));
    player.addChatMessage(new ChatComponentText(
        info.brandColor + "═══ " +
        StatCollector.translateToLocal(info.displayNameKey) +
        info.brandColor + " ═══"));
    player.addChatMessage(new ChatComponentText(
        EnumChatFormatting.ITALIC + EnumChatFormatting.GRAY.toString() +
        "\"" + StatCollector.translateToLocal(info.taglineKey) + "\""));
    player.addChatMessage(new ChatComponentText(
        EnumChatFormatting.WHITE +
        StatCollector.translateToLocal(info.descriptionKey)));
    player.addChatMessage(new ChatComponentText(
        EnumChatFormatting.AQUA + "ahtech.supplier.tooltip.contract_requirement: " +
        EnumChatFormatting.YELLOW + supplierId.minContractTier.translationKey));
    player.addChatMessage(new ChatComponentText(
        EnumChatFormatting.GREEN + "Status: " +
        (activated ? "✅ ahtech.supplier.status.active" : "❌ ahtech.supplier.status.inactive")));
    player.addChatMessage(new ChatComponentText(""));
}
```

注意：`ChatComponentText` 是 1.7.10 的聊天组件类。如果 GUI 库支持，可改用真正的 GUI 面板。

- [ ] **Step 2：添加状态翻译键**

```properties
# en_US.lang
ahtech.supplier.status.active=Active
ahtech.supplier.status.inactive=Inactive (Contract level insufficient)

# zh_CN.lang
ahtech.supplier.status.active=已激活
ahtech.supplier.status.inactive=未激活（合同等级不足）
```

- [ ] **Step 3：编译验证**

Run: `cd D:/Code/AndgateTechnology && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4：提交**

```bash
git add src/main/java/com/andgatech/AHTech/common/supplier/SupplierHatch.java \
        src/main/resources/assets/andgatetechnology/lang/en_US.lang \
        src/main/resources/assets/andgatetechnology/lang/zh_CN.lang
git commit -m "feat: supplier info panel on right-click with brand-colored chat display"
```

---

## Phase E：GTNH 任务书集成（可选，低优先级）

### Task 16：任务书集成配置与 API

**Files:**
- Modify: `src/main/java/com/andgatech/AHTech/config/Config.java`

- [ ] **Step 1：在 Config 中添加任务书集成配置项**

```java
// 任务书集成
public static boolean EnableQuestBookIntegration = false;
```

在 `synchronizeConfiguration()` 中添加：

```java
EnableQuestBookIntegration = configuration.getBoolean(
    "EnableQuestBookIntegration", "Integration", false,
    "Enable optional GTNH Quest Book integration hooks");
```

- [ ] **Step 2：提交**

```bash
git add src/main/java/com/andgatech/AHTech/config/Config.java
git commit -m "feat: add quest book integration config option"
```

---

### Task 17：供应商合成配方

**Files:**
- Create: `src/main/java/com/andgatech/AHTech/recipe/machineRecipe/SupplierRecipePool.java`

- [ ] **Step 1：为每个供应商创建合成配方**

供应商方块需要合成配方才能在游戏中获得。使用合成台配方：

```java
package com.andgatech.AHTech.recipe.machineRecipe;

import com.andgatech.AHTech.common.ModItemList;
import cpw.mods.fml.common.registry.GameRegistry;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class SupplierRecipePool {

    public static void load() {
        // 每个供应商方块 = 机器外壳 + 对应领域材料 + 合同物品
        // Lv1 供应商（需要 Lv1 合同）
        GameRegistry.addRecipe(
            ModItemList.SupplierShandongDezhou.get(1),
            " C ", "MBM", " S ",
            'C', ModItemList.ContractLv1.get(1),
            'M', Materials.Iron.getPlate(4),
            'B', ItemList.Machine_Box.get(1),
            'S', Materials.Copper.getIngot(4)
        );
        GameRegistry.addRecipe(
            ModItemList.SupplierYadepian.get(1),
            " C ", "MBM", " S ",
            'C', ModItemList.ContractLv1.get(1),
            'M', Materials.Iron.getPlate(4),
            'B', ItemList.Machine_Box.get(1),
            'S', Materials.RedAlloy.getIngot(4)
        );
        GameRegistry.addRecipe(
            ModItemList.SupplierErfa.get(1),
            " C ", "MBM", " S ",
            'C', ModItemList.ContractLv1.get(1),
            'M', Materials.Iron.getPlate(4),
            'B', ItemList.Machine_Box.get(1),
            'S', Materials.Silicon.getIngot(4)
        );
        // Lv2 供应商（需要 Lv2 合同）
        GameRegistry.addRecipe(
            ModItemList.SupplierChaola.get(1),
            " C ", "MBM", " S ",
            'C', ModItemList.ContractLv2.get(1),
            'M', Materials.StainlessSteel.getPlate(4),
            'B', ItemList.Machine_Box.get(1),
            'S', Materials.Titanium.getIngot(4)
        );
        GameRegistry.addRecipe(
            ModItemList.SupplierHuangweida.get(1),
            " C ", "MBM", " S ",
            'C', ModItemList.ContractLv2.get(1),
            'M', Materials.StainlessSteel.getPlate(4),
            'B', ItemList.Machine_Box.get(1),
            'S', Materials.Aluminium.getIngot(4)
        );
        GameRegistry.addRecipe(
            ModItemList.SupplierGaiguang.get(1),
            " C ", "MBM", " S ",
            'C', ModItemList.ContractLv2.get(1),
            'M', Materials.StainlessSteel.getPlate(4),
            'B', ItemList.Machine_Box.get(1),
            'S', Materials.Glass.getIngot(4)
        );
        // Lv3 供应商（需要 Lv3 合同）
        GameRegistry.addRecipe(
            ModItemList.SupplierDitong.get(1),
            " C ", "MBM", " S ",
            'C', ModItemList.ContractLv3.get(1),
            'M', Materials.TungstenSteel.getPlate(4),
            'B', ItemList.Machine_Box.get(1),
            'S', Materials.Platinum.getIngot(4)
        );
    }
}
```

- [ ] **Step 2：在 RecipeLoader.loadRecipes() 中调用**

```java
SupplierRecipePool.load();
```

- [ ] **Step 3：编译验证与提交**

```bash
git add src/main/java/com/andgatech/AHTech/recipe/machineRecipe/SupplierRecipePool.java \
        src/main/java/com/andgatech/AHTech/loader/RecipeLoader.java
git commit -m "feat: add crafting recipes for supplier blocks"
```

---

## Phase F：集成测试与最终构建

### Task 18：集成测试

**Files:**
- Create: `src/test/java/com/andgatech/AHTech/contract/ContractTierTest.java`
- Create: `src/test/java/com/andgatech/AHTech/supplier/SupplierActivationTest.java`

- [ ] **Step 1：编写 ContractTier 测试**

```java
package com.andgatech.AHTech.contract;

import static org.junit.Assert.*;
import org.junit.Test;
import com.andgatech.AHTech.common.contract.ContractTier;

public class ContractTierTest {

    @Test
    public void testFromTier() {
        assertEquals(ContractTier.NONE, ContractTier.fromTier(0));
        assertEquals(ContractTier.LV1, ContractTier.fromTier(1));
        assertEquals(ContractTier.LV2, ContractTier.fromTier(2));
        assertEquals(ContractTier.LV3, ContractTier.fromTier(3));
        assertEquals(ContractTier.LV4, ContractTier.fromTier(4));
    }

    @Test
    public void testFromTierInvalid() {
        assertEquals(ContractTier.NONE, ContractTier.fromTier(-1));
        assertEquals(ContractTier.NONE, ContractTier.fromTier(99));
    }

    @Test
    public void testTierOrdering() {
        assertTrue(ContractTier.LV1.tier < ContractTier.LV2.tier);
        assertTrue(ContractTier.LV2.tier < ContractTier.LV3.tier);
        assertTrue(ContractTier.LV3.tier < ContractTier.LV4.tier);
    }
}
```

- [ ] **Step 2：编写供应商激活逻辑测试**

```java
package com.andgatech.AHTech.supplier;

import static org.junit.Assert.*;
import org.junit.Test;
import com.andgatech.AHTech.common.contract.ContractTier;
import com.andgatech.AHTech.common.supplier.SupplierId;

public class SupplierActivationTest {

    @Test
    public void testLv1SuppliersRequireLv1Contract() {
        assertEquals(ContractTier.LV1, SupplierId.SHANDONG_DEZHOU.minContractTier);
        assertEquals(ContractTier.LV1, SupplierId.YADEPIAN.minContractTier);
        assertEquals(ContractTier.LV1, SupplierId.ERFA.minContractTier);
    }

    @Test
    public void testLv2SuppliersRequireLv2Contract() {
        assertEquals(ContractTier.LV2, SupplierId.CHAOLA.minContractTier);
        assertEquals(ContractTier.LV2, SupplierId.HUANGWEIDA.minContractTier);
        assertEquals(ContractTier.LV2, SupplierId.GAIGUANG.minContractTier);
    }

    @Test
    public void testLv3SupplierRequiresLv3Contract() {
        assertEquals(ContractTier.LV3, SupplierId.DITONG.minContractTier);
    }

    @Test
    public void testContractTierSufficientForSupplier() {
        // Lv2 合同应该能激活 Lv1 供应商
        assertTrue(ContractTier.LV2.tier >= SupplierId.SHANDONG_DEZHOU.minContractTier.tier);
        // Lv1 合同不应能激活 Lv2 供应商
        assertFalse(ContractTier.LV1.tier >= SupplierId.CHAOLA.minContractTier.tier);
    }
}
```

- [ ] **Step 3：运行测试**

Run: `cd D:/Code/AndgateTechnology && ./gradlew test`
Expected: All tests pass

- [ ] **Step 4：提交**

```bash
git add src/test/
git commit -m "test: add contract tier and supplier activation tests"
```

---

### Task 19：最终构建验证与文档更新

**Files:**
- Modify: `context.md`
- Modify: `log.md`
- Modify: `ToDOLIST.md`

- [ ] **Step 1：运行完整构建**

Run: `cd D:/Code/AndgateTechnology && ./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2：更新 context.md**

在"物品"部分添加合同物品条目。在"关键类"部分添加新的类路径。

- [ ] **Step 3：更新 log.md**

记录本次实现的详细信息。

- [ ] **Step 4：更新 ToDOLIST.md**

将已完成的合同系统和供应商系统任务标记为 `[x]`。

- [ ] **Step 5：最终提交**

```bash
git add context.md log.md ToDOLIST.md
git commit -m "docs: update project docs for contract and supplier system implementation"
```
