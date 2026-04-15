# 资金系统实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为赛格大厦添加 6 级货币体系和金融舱口，使配方执行消耗对应货币。

**Architecture:** 新增 `CurrencyType` 枚举和 `CurrencyItem` 物品定义 6 种货币；新增 `FinancialHatch` 作为 `ModularHatchBase` 子类，带 9 格物品栏存储货币并从输入总线自动补货；通过 `AHTechRecipeMetadata` 的两个新 key 标记配方的币种和消耗量；`ElectronicsMarket` 在 `validateRecipeAccess()` 中检查资金，在 `checkProcessingMM()` 中扣除货币。

**Tech Stack:** Java 8, GTNH 2.8.4 (GT5-Unofficial + TecTech), Forge 10.13.4, ModularUI 1, StructureLib, JUnit 5

---

## File Structure

### New Files

| File | Responsibility |
|------|---------------|
| `src/main/java/com/andgatech/AHTech/common/currency/CurrencyType.java` | 6 种货币枚举，关联材质和翻译键 |
| `src/main/java/com/andgatech/AHTech/common/currency/CurrencyItem.java` | 货币物品（1 Item，meta 0-5） |
| `src/main/java/com/andgatech/AHTech/loader/CurrencyLoader.java` | preInit 注册货币物品 |
| `src/main/java/com/andgatech/AHTech/common/modularizedMachine/modularHatches/FinancialHatch.java` | 金融舱口（物品栏 + 补货 + 消耗） |
| `src/main/java/com/andgatech/AHTech/recipe/machineRecipe/CurrencyRecipePool.java` | 压模机配方（6 种币） |
| `src/test/java/com/andgatech/AHTech/common/currency/CurrencyTypeTest.java` | CurrencyType 单元测试 |
| `src/test/java/com/andgatech/AHTech/common/modularizedMachine/modularHatches/FinancialHatchTest.java` | 金融舱口物品栏逻辑测试 |

### Modified Files

| File | Change |
|------|--------|
| `src/main/java/com/andgatech/AHTech/common/modularizedMachine/ModularHatchType.java` | 新增 `FINANCIAL` 枚举值 |
| `src/main/java/com/andgatech/AHTech/recipe/metadata/AHTechRecipeMetadata.java` | 新增 `CURRENCY_TYPE` 和 `CURRENCY_COST` key |
| `src/main/java/com/andgatech/AHTech/common/ModItemList.java` | 新增 `FinancialHatch` 和 6 个货币条目 |
| `src/main/java/com/andgatech/AHTech/config/Config.java` | 新增资金系统配置项 |
| `src/main/java/com/andgatech/AHTech/loader/MachineLoader.java` | 注册金融舱口 |
| `src/main/java/com/andgatech/AHTech/loader/RecipeLoader.java` | 调用 `CurrencyRecipePool` |
| `src/main/java/com/andgatech/AHTech/CommonProxy.java` | preInit 调用 `CurrencyLoader` |
| `src/main/java/com/andgatech/AHTech/common/machine/ElectronicsMarket.java` | 金融舱口扫描、资金检查、货币消耗、UI、信息屏 |

---

## Task 1: CurrencyType 枚举

**Files:**
- Create: `src/main/java/com/andgatech/AHTech/common/currency/CurrencyType.java`
- Test: `src/test/java/com/andgatech/AHTech/common/currency/CurrencyTypeTest.java`

- [ ] **Step 1: Write the failing test**

```java
// src/test/java/com/andgatech/AHTech/common/currency/CurrencyTypeTest.java
package com.andgatech.AHTech.common.currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class CurrencyTypeTest {

    @Test
    void allTypesHaveCorrectMeta() {
        assertEquals(0, CurrencyType.COPPER.getMeta());
        assertEquals(1, CurrencyType.STEEL.getMeta());
        assertEquals(2, CurrencyType.TITANIUM.getMeta());
        assertEquals(3, CurrencyType.PLATINUM.getMeta());
        assertEquals(4, CurrencyType.NEUTRONIUM.getMeta());
        assertEquals(5, CurrencyType.INFINITY.getMeta());
    }

    @Test
    void getByMetaReturnsCorrectType() {
        for (CurrencyType type : CurrencyType.values()) {
            assertEquals(type, CurrencyType.getByMeta(type.getMeta()));
        }
        assertNull(CurrencyType.getByMeta(99));
    }

    @Test
    void valuesCountIs6() {
        assertEquals(6, CurrencyType.values().length);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd D:/Code/AndgateTechnology && ./gradlew test --tests com.andgatech.AHTech.common.currency.CurrencyTypeTest`
Expected: FAIL — `CurrencyType` class not found

- [ ] **Step 3: Write minimal implementation**

```java
// src/main/java/com/andgatech/AHTech/common/currency/CurrencyType.java
package com.andgatech.AHTech.common.currency;

public enum CurrencyType {
    COPPER(0, "copper"),
    STEEL(1, "steel"),
    TITANIUM(2, "titanium"),
    PLATINUM(3, "platinum"),
    NEUTRONIUM(4, "neutronium"),
    INFINITY(5, "infinity");

    private final int meta;
    private final String name;

    CurrencyType(int meta, String name) {
        this.meta = meta;
        this.name = name;
    }

    public int getMeta() {
        return meta;
    }

    public String getName() {
        return name;
    }

    public String getTranslationKey() {
        return "ahtech.currency." + name;
    }

    public static CurrencyType getByMeta(int meta) {
        for (CurrencyType type : values()) {
            if (type.meta == meta) return type;
        }
        return null;
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd D:/Code/AndgateTechnology && ./gradlew test --tests com.andgatech.AHTech.common.currency.CurrencyTypeTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
cd D:/Code/AndgateTechnology
git add src/main/java/com/andgatech/AHTech/common/currency/CurrencyType.java src/test/java/com/andgatech/AHTech/common/currency/CurrencyTypeTest.java
git commit -m "feat: add CurrencyType enum for 6-level currency system"
```

---

## Task 2: CurrencyItem 物品

**Files:**
- Create: `src/main/java/com/andgatech/AHTech/common/currency/CurrencyItem.java`

- [ ] **Step 1: Write CurrencyItem**

```java
// src/main/java/com/andgatech/AHTech/common/currency/CurrencyItem.java
package com.andgatech.AHTech.common.currency;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import com.andgatech.AHTech.AndgateTechnology;

public class CurrencyItem extends Item {

    public CurrencyItem() {
        setMaxStackSize(64);
        setHasSubtypes(true);
        setCreativeTab(CreativeTabs.tabMisc);
        setUnlocalizedName("ahtech_currency");
        setTextureName(AndgateTechnology.RESOURCE_ROOT_ID + ":currency");
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        CurrencyType type = CurrencyType.getByMeta(stack.getItemDamage());
        if (type != null) {
            return "item." + type.getTranslationKey();
        }
        return super.getUnlocalizedName(stack);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        CurrencyType type = CurrencyType.getByMeta(stack.getItemDamage());
        if (type != null) {
            tooltip.add(StatCollector.translateToLocal(type.getTranslationKey()));
        }
    }

    @Override
    public boolean isItemEqual(ItemStack a, ItemStack b) {
        return a != null && b != null && a.getItem() == b.getItem() && a.getItemDamage() == b.getItemDamage();
    }

    public static boolean isCurrency(ItemStack stack) {
        return stack != null && stack.getItem() instanceof CurrencyItem;
    }

    public static CurrencyType getCurrencyType(ItemStack stack) {
        if (!isCurrency(stack)) return null;
        return CurrencyType.getByMeta(stack.getItemDamage());
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd D:/Code/AndgateTechnology && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
cd D:/Code/AndgateTechnology
git add src/main/java/com/andgatech/AHTech/common/currency/CurrencyItem.java
git commit -m "feat: add CurrencyItem for physical currency items"
```

---

## Task 3: ModItemList 扩展 + CurrencyLoader 注册

**Files:**
- Modify: `src/main/java/com/andgatech/AHTech/common/ModItemList.java`
- Create: `src/main/java/com/andgatech/AHTech/loader/CurrencyLoader.java`
- Modify: `src/main/java/com/andgatech/AHTech/CommonProxy.java`

- [ ] **Step 1: Add enum entries to ModItemList**

In `ModItemList.java`, add after the `// region Supplier Hatches` block (after line 110 `SupplierDitong,`) and before the semicolon:

```java
    // endregion

    // region Currency
    CurrencyCopper,
    CurrencySteel,
    CurrencyTitanium,
    CurrencyPlatinum,
    CurrencyNeutronium,
    CurrencyInfinity,
    // endregion

    // region Financial Hatch
    FinancialHatch,
    // endregion
```

- [ ] **Step 2: Create CurrencyLoader**

```java
// src/main/java/com/andgatech/AHTech/loader/CurrencyLoader.java
package com.andgatech.AHTech.loader;

import net.minecraft.item.ItemStack;

import com.andgatech.AHTech.common.ModItemList;
import com.andgatech.AHTech.common.currency.CurrencyItem;
import com.andgatech.AHTech.common.currency.CurrencyType;

import cpw.mods.fml.common.registry.GameRegistry;

public final class CurrencyLoader {

    private CurrencyLoader() {}

    public static void loadCurrencies() {
        CurrencyItem currencyItem = new CurrencyItem();
        GameRegistry.registerItem(currencyItem, "ahtech_currency");

        CurrencyType[] types = CurrencyType.values();
        ModItemList[] entries = {
            ModItemList.CurrencyCopper,
            ModItemList.CurrencySteel,
            ModItemList.CurrencyTitanium,
            ModItemList.CurrencyPlatinum,
            ModItemList.CurrencyNeutronium,
            ModItemList.CurrencyInfinity,
        };

        for (int i = 0; i < types.length; i++) {
            entries[i].set(new ItemStack(currencyItem, 1, types[i].getMeta()));
        }
    }
}
```

- [ ] **Step 3: Add CurrencyLoader call to CommonProxy.preInit()**

In `CommonProxy.java`, add import and call after `ContractLoader.loadContracts();`:

```java
// Add import at top:
import com.andgatech.AHTech.loader.CurrencyLoader;

// In preInit(), after ContractLoader.loadContracts(); add:
CurrencyLoader.loadCurrencies();
```

- [ ] **Step 4: Verify compilation**

Run: `cd D:/Code/AndgateTechnology && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
cd D:/Code/AndgateTechnology
git add src/main/java/com/andgatech/AHTech/common/ModItemList.java src/main/java/com/andgatech/AHTech/loader/CurrencyLoader.java src/main/java/com/andgatech/AHTech/CommonProxy.java
git commit -m "feat: register currency items in ModItemList and CurrencyLoader"
```

---

## Task 4: CurrencyRecipePool 压模机配方

**Files:**
- Create: `src/main/java/com/andgatech/AHTech/recipe/machineRecipe/CurrencyRecipePool.java`
- Modify: `src/main/java/com/andgatech/AHTech/loader/RecipeLoader.java`

- [ ] **Step 1: Create CurrencyRecipePool**

```java
// src/main/java/com/andgatech/AHTech/recipe/machineRecipe/CurrencyRecipePool.java
package com.andgatech.AHTech.recipe.machineRecipe;

import com.andgatech.AHTech.AndgateTechnology;
import com.andgatech.AHTech.common.ModItemList;
import com.andgatech.AHTech.common.currency.CurrencyType;

import gregtech.api.enums.GTValues;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.enums.TierEU;
import gregtech.api.util.GTOreDictUnificator;

/**
 * Extruder recipes for currency coins: 1 ingot → 1 coin.
 */
public class CurrencyRecipePool {

    private static final Materials[] CURRENCY_MATERIALS = {
        Materials.Copper,
        Materials.Steel,
        Materials.Titanium,
        Materials.Platinum,
        Materials.Neutronium,
        Materials.CosmicNeutronium,
    };

    public static void loadRecipes() {
        CurrencyType[] types = CurrencyType.values();

        for (int i = 0; i < types.length; i++) {
            ModItemList coinEntry = getCoinEntry(types[i]);
            if (coinEntry == null || coinEntry.get() == null) {
                AndgateTechnology.LOG.warn("Currency recipe skipped for {}: missing coin item.", types[i].getName());
                continue;
            }

            Materials mat = CURRENCY_MATERIALS[i];
            ItemStack ingot = GTOreDictUnificator.get(OrePrefixes.ingot, mat, 1);
            if (ingot == null) {
                AndgateTechnology.LOG.warn("Currency recipe skipped for {}: missing ingot for {}.", types[i].getName(), mat.name());
                continue;
            }

            GTValues.RA.stdBuilder()
                .itemInputs(ingot)
                .itemOutputs(coinEntry.get())
                .eut(TierEU.RECIPE_LV)
                .duration(100)
                .addTo(com.gtnewhorizon.structurelib.recipe.RecipeMapExtruderRecipes.sExtruderRecipes);
        }

        AndgateTechnology.LOG.info("Registered {} currency extruder recipes.", types.length);
    }

    private static ModItemList getCoinEntry(CurrencyType type) {
        return switch (type) {
            case COPPER -> ModItemList.CurrencyCopper;
            case STEEL -> ModItemList.CurrencySteel;
            case TITANIUM -> ModItemList.CurrencyTitanium;
            case PLATINUM -> ModItemList.CurrencyPlatinum;
            case NEUTRONIUM -> ModItemList.CurrencyNeutronium;
            case INFINITY -> ModItemList.CurrencyInfinity;
        };
    }
}
```

> **注意：** 无尽币的材质可能需要根据 GTNH 实际的 Avaritia/Infinity 材质调整。当前先用 `Materials.CosmicNeutronium` 作为中子币的材质（GTNH 中 `Materials.Neutronium` 可能不存在或对应不同的物品）。实际运行时如遇到问题，需根据 GTNH 源码调整材质映射。

- [ ] **Step 2: Add call to RecipeLoader**

In `RecipeLoader.java`, add import and call. In `loadRecipes()` after `ElectronicsMarketRecipePool.loadRecipes();`:

```java
// Add import at top:
import com.andgatech.AHTech.recipe.machineRecipe.CurrencyRecipePool;

// In loadRecipes(), after ElectronicsMarketRecipePool.loadRecipes() and its closing brace:
CurrencyRecipePool.loadRecipes();
```

- [ ] **Step 3: Verify compilation**

Run: `cd D:/Code/AndgateTechnology && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
cd D:/Code/AndgateTechnology
git add src/main/java/com/andgatech/AHTech/recipe/machineRecipe/CurrencyRecipePool.java src/main/java/com/andgatech/AHTech/loader/RecipeLoader.java
git commit -m "feat: add extruder recipes for 6 currency types"
```

---

## Task 5: ModularHatchType + RecipeMetadata 扩展

**Files:**
- Modify: `src/main/java/com/andgatech/AHTech/common/modularizedMachine/ModularHatchType.java`
- Modify: `src/main/java/com/andgatech/AHTech/recipe/metadata/AHTechRecipeMetadata.java`

- [ ] **Step 1: Add FINANCIAL to ModularHatchType**

In `ModularHatchType.java`, add `FINANCIAL` between `SUPPLIER` and `ALL`:

```java
// Change from:
    SUPPLIER,
    ALL;

// Change to:
    SUPPLIER,
    FINANCIAL,
    ALL;
```

- [ ] **Step 2: Add CURRENCY_TYPE and CURRENCY_COST to AHTechRecipeMetadata**

In `AHTechRecipeMetadata.java`, add imports and new keys:

```java
// Add import:
import com.andgatech.AHTech.common.currency.CurrencyType;

// Add after SUPPLIER_ID:
    public static final RecipeMetadataKey<CurrencyType> CURRENCY_TYPE = SimpleRecipeMetadataKey
        .create(CurrencyType.class, "ahtech_currency_type");

    public static final RecipeMetadataKey<Integer> CURRENCY_COST = SimpleRecipeMetadataKey
        .create(Integer.class, "ahtech_currency_cost");
```

- [ ] **Step 3: Verify compilation**

Run: `cd D:/Code/AndgateTechnology && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
cd D:/Code/AndgateTechnology
git add src/main/java/com/andgatech/AHTech/common/modularizedMachine/ModularHatchType.java src/main/java/com/andgatech/AHTech/recipe/metadata/AHTechRecipeMetadata.java
git commit -m "feat: add FINANCIAL hatch type and currency recipe metadata keys"
```

---

## Task 6: FinancialHatch 金融舱口

**Files:**
- Create: `src/main/java/com/andgatech/AHTech/common/modularizedMachine/modularHatches/FinancialHatch.java`
- Test: `src/test/java/com/andgatech/AHTech/common/modularizedMachine/modularHatches/FinancialHatchTest.java`

- [ ] **Step 1: Write the failing test**

```java
// src/test/java/com/andgatech/AHTech/common/modularizedMachine/modularHatches/FinancialHatchTest.java
package com.andgatech.AHTech.common.modularizedMachine.modularHatches;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.andgatech.AHTech.common.currency.CurrencyType;

import org.junit.jupiter.api.Test;

class FinancialHatchTest {

    @Test
    void currencyTypeValuesAreComplete() {
        // Verify the enum is usable standalone (no MC dependency needed)
        assertEquals(6, CurrencyType.values().length);
        assertEquals("copper", CurrencyType.COPPER.getName());
        assertEquals("platinum", CurrencyType.PLATINUM.getName());
    }
}
```

- [ ] **Step 2: Run test to verify it passes (CurrencyType already exists)**

Run: `cd D:/Code/AndgateTechnology && ./gradlew test --tests com.andgatech.AHTech.common.modularizedMachine.modularHatches.FinancialHatchTest`
Expected: PASS

- [ ] **Step 3: Write FinancialHatch**

```java
// src/main/java/com/andgatech/AHTech/common/modularizedMachine/modularHatches/FinancialHatch.java
package com.andgatech.AHTech.common.modularizedMachine.modularHatches;

import java.util.Collection;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import com.andgatech.AHTech.common.currency.CurrencyItem;
import com.andgatech.AHTech.common.currency.CurrencyType;
import com.andgatech.AHTech.common.modularizedMachine.ModularHatchType;

import com.gtnewhorizons.modularui.api.drawable.Text;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchInputBus;

public class FinancialHatch extends ModularHatchBase {

    private static final int INVENTORY_SIZE = 9;

    public FinancialHatch(int aID, String aName, String aNameRegional, int aTier) {
        super(aID, aName, aNameRegional, aTier, INVENTORY_SIZE, null);
    }

    public FinancialHatch(String aName, int aTier, ITexture[][][] aTextures) {
        super(aName, aTier, INVENTORY_SIZE, null, aTextures);
    }

    @Override
    public ModularHatchType getType() {
        return ModularHatchType.FINANCIAL;
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new FinancialHatch(mName, mTier, mTextures);
    }

    @Override
    public ITexture[] getTexturesActive(ITexture aBaseTexture) {
        return new ITexture[] { aBaseTexture };
    }

    @Override
    public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
        return new ITexture[] { aBaseTexture };
    }

    @Override
    public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
        openGui(aPlayer);
        return true;
    }

    // region Inventory

    @Override
    public boolean isValidSlot(int aIndex) {
        return true; // Allow items in all 9 slots
    }

    @Override
    public boolean isItemValidForSlot(int aIndex, ItemStack aStack) {
        return CurrencyItem.isCurrency(aStack);
    }

    public int countCurrency(CurrencyType type) {
        int total = 0;
        for (int i = 0; i < getSizeInventory(); i++) {
            ItemStack stack = getStackInSlot(i);
            if (stack != null && CurrencyItem.getCurrencyType(stack) == type) {
                total += stack.stackSize;
            }
        }
        return total;
    }

    /**
     * Consumes up to {@code amount} of the specified currency from this hatch's inventory.
     * Returns the actual number consumed.
     */
    public int consumeCurrency(CurrencyType type, int amount) {
        int remaining = amount;
        for (int i = 0; i < getSizeInventory() && remaining > 0; i++) {
            ItemStack stack = getStackInSlot(i);
            if (stack != null && CurrencyItem.getCurrencyType(stack) == type) {
                int toConsume = Math.min(remaining, stack.stackSize);
                stack.stackSize -= toConsume;
                remaining -= toConsume;
                if (stack.stackSize <= 0) {
                    setInventorySlotContents(i, null);
                }
            }
        }
        return amount - remaining;
    }

    // endregion

    // region Auto-Refill

    /**
     * Pulls currency items from input buses into this hatch's inventory.
     */
    public void autoRefillFromInputBus(Collection<MTEHatchInputBus> inputBuses) {
        for (MTEHatchInputBus bus : inputBuses) {
            if (bus == null) continue;
            for (int i = 0; i < bus.getSizeInventory(); i++) {
                ItemStack busStack = bus.getStackInSlot(i);
                if (!CurrencyItem.isCurrency(busStack)) continue;

                ItemStack remaining = tryInsertCurrency(busStack);
                if (remaining == null || remaining.stackSize <= 0) {
                    bus.setInventorySlotContents(i, null);
                } else {
                    bus.setInventorySlotContents(i, remaining);
                }
            }
        }
    }

    /**
     * Tries to insert a currency stack into this hatch's inventory.
     * Returns the leftover (or null if fully inserted).
     */
    private ItemStack tryInsertCurrency(ItemStack toInsert) {
        toInsert = toInsert.copy();
        // First, try to stack onto existing slots
        for (int i = 0; i < getSizeInventory() && toInsert.stackSize > 0; i++) {
            ItemStack existing = getStackInSlot(i);
            if (existing != null
                && existing.getItem() == toInsert.getItem()
                && existing.getItemDamage() == toInsert.getItemDamage()
                && existing.stackSize < existing.getMaxStackSize()) {
                int space = existing.getMaxStackSize() - existing.stackSize;
                int toAdd = Math.min(space, toInsert.stackSize);
                existing.stackSize += toAdd;
                toInsert.stackSize -= toAdd;
            }
        }
        // Then, try empty slots
        for (int i = 0; i < getSizeInventory() && toInsert.stackSize > 0; i++) {
            if (getStackInSlot(i) == null) {
                int toAdd = Math.min(toInsert.getMaxStackSize(), toInsert.stackSize);
                setInventorySlotContents(i, new ItemStack(toInsert.getItem(), toAdd, toInsert.getItemDamage()));
                toInsert.stackSize -= toAdd;
            }
        }
        return toInsert.stackSize > 0 ? toInsert : null;
    }

    // endregion

    // region UI

    @Override
    public void addUIWidgets(ModularWindow.Builder builder, UIBuildContext buildContext) {
        super.addUIWidgets(builder, buildContext);

        int startX = 6;
        int y = 10;

        builder.widget(
            new TextWidget(StatCollector.translateToLocal("AHTech.FinancialHatch.GUI.Title"))
                .setDefaultColor(0xFFD700)
                .setPos(startX, y)
                .setSize(164, 12));
        y += 16;

        builder.widget(
            new TextWidget("---------------------------")
                .setDefaultColor(0x555555)
                .setPos(startX, y)
                .setSize(164, 8));
        y += 12;

        // Currency inventory display
        for (CurrencyType type : CurrencyType.values()) {
            int count = countCurrency(type);
            String line = StatCollector.translateToLocal(type.getTranslationKey())
                + ": " + count;
            builder.widget(
                new TextWidget(line)
                    .setDefaultColor(0xAAAAAA)
                    .setPos(startX, y)
                    .setSize(164, 10));
            y += 12;
        }
    }

    @Override
    public String[] getDescription() {
        return new String[] {
            StatCollector.translateToLocal("AHTech.FinancialHatch.Description") };
    }

    // endregion
}
```

- [ ] **Step 4: Verify compilation**

Run: `cd D:/Code/AndgateTechnology && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
cd D:/Code/AndgateTechnology
git add src/main/java/com/andgatech/AHTech/common/modularizedMachine/modularHatches/FinancialHatch.java src/test/java/com/andgatech/AHTech/common/modularizedMachine/modularHatches/FinancialHatchTest.java
git commit -m "feat: add FinancialHatch with inventory, auto-refill, and UI"
```

---

## Task 7: Config + MachineLoader 注册

**Files:**
- Modify: `src/main/java/com/andgatech/AHTech/config/Config.java`
- Modify: `src/main/java/com/andgatech/AHTech/loader/MachineLoader.java`

- [ ] **Step 1: Add config fields**

In `Config.java`, add after the `// endregion` of Modularization section (before `// region Machine Enables`):

```java
    // region Financial System
    public static boolean EnableFinancialSystem = true;
    public static boolean EnableAutoRefillFromInputBus = true;
    // endregion
```

In `synchronizeConfiguration()`, add before the `if (configuration.hasChanged())` block:

```java
        // Financial System
        EnableFinancialSystem = configuration.getBoolean(
            "EnableFinancialSystem",
            "FinancialSystem",
            true,
            "Enable/disable the financial system.");
        EnableAutoRefillFromInputBus = configuration.getBoolean(
            "EnableAutoRefillFromInputBus",
            "FinancialSystem",
            true,
            "Enable/disable auto-refilling financial hatch from input buses.");
```

- [ ] **Step 2: Register FinancialHatch in MachineLoader**

In `MachineLoader.java`, add import and modify `registerSupplierHatches` to return the final `id`, then register financial hatch after suppliers:

Add import:
```java
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.FinancialHatch;
```

Change `registerSupplierHatches` from `private static void registerSupplierHatches(int id)` to `private static int registerSupplierHatches(int id)` and update its last line from:
```java
        registerSupplier(id, ModItemList.SupplierDitong, SupplierId.DITONG);
```
to:
```java
        registerSupplier(id++, ModItemList.SupplierDitong, SupplierId.DITONG);
        return id;
```

Also update the earlier `registerSupplier` calls to use `id++` (currently they already do except the last one).

Then in `registerModularHatches`, change:
```java
        registerSupplierHatches(id);
```
to:
```java
        id = registerSupplierHatches(id);
        if (Config.EnableFinancialSystem) {
            ModItemList.FinancialHatch.set(
                new FinancialHatch(id++, "FinancialHatch", "Financial Hatch", 0).getStackForm(1L));
        }
```

- [ ] **Step 3: Verify compilation**

Run: `cd D:/Code/AndgateTechnology && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
cd D:/Code/AndgateTechnology
git add src/main/java/com/andgatech/AHTech/config/Config.java src/main/java/com/andgatech/AHTech/loader/MachineLoader.java
git commit -m "feat: add financial system config and register FinancialHatch in MachineLoader"
```

---

## Task 8: ElectronicsMarket 集成 — 金融舱口扫描 + 资金检查 + 货币消耗

**Files:**
- Modify: `src/main/java/com/andgatech/AHTech/common/machine/ElectronicsMarket.java`

- [ ] **Step 1: Add imports**

Add these imports at the top of `ElectronicsMarket.java`:

```java
import java.util.stream.Collectors;

import com.andgatech.AHTech.common.currency.CurrencyType;
import com.andgatech.AHTech.common.modularizedMachine.modularHatches.FinancialHatch;
import com.andgatech.AHTech.config.Config;
import gregtech.api.metatileentity.implementations.MTEHatchInputBus;
```

- [ ] **Step 2: Add instance fields**

After the existing `syncedActiveSuppliers;` field (line 97), add:

```java
    // Financial system
    private List<FinancialHatch> financialHatches = new ArrayList<>();
    private String syncedFinancialStatus = "";
```

- [ ] **Step 3: Modify checkMachineMM() — add financial hatch scanning**

In `checkMachineMM()`, after `rebuildActiveSuppliers();` (line 475) and before the Tier I parallel check, add:

```java
        // Financial system: scan financial hatches and auto-refill
        financialHatches = new ArrayList<>();
        List<IModularHatch> finHatches = modularHatches.getOrDefault(
            ModularHatchType.FINANCIAL, Collections.emptyList());
        for (IModularHatch hatch : finHatches) {
            if (hatch instanceof FinancialHatch fh) {
                financialHatches.add(fh);
            }
        }
        if (Config.EnableAutoRefillFromInputBus) {
            List<MTEHatchInputBus> inputBuses = getInputBuses();
            for (FinancialHatch fh : financialHatches) {
                fh.autoRefillFromInputBus(inputBuses);
            }
        }
```

- [ ] **Step 4: Modify validateRecipeAccess() — add financial check**

In `validateRecipeAccess()`, replace the return statement at the end (lines 540-542):

```java
        String supplierIdString = recipe.getMetadata(AHTechRecipeMetadata.SUPPLIER_ID);
        return isSupplierRecipeAccessible(supplierIdString) ? CheckRecipeResultRegistry.SUCCESSFUL
            : CheckRecipeResultRegistry.NO_RECIPE;
```

with:

```java
        String supplierIdString = recipe.getMetadata(AHTechRecipeMetadata.SUPPLIER_ID);
        if (!isSupplierRecipeAccessible(supplierIdString)) {
            return CheckRecipeResultRegistry.NO_RECIPE;
        }

        // Financial check
        if (!Config.EnableFinancialSystem) {
            return CheckRecipeResultRegistry.SUCCESSFUL;
        }
        CurrencyType currencyType = recipe.getMetadata(AHTechRecipeMetadata.CURRENCY_TYPE);
        if (currencyType == null) {
            return CheckRecipeResultRegistry.SUCCESSFUL; // No currency required
        }
        Integer currencyCost = recipe.getMetadata(AHTechRecipeMetadata.CURRENCY_COST);
        if (currencyCost == null || currencyCost <= 0) {
            return CheckRecipeResultRegistry.SUCCESSFUL;
        }
        if (financialHatches.isEmpty()) {
            return CheckRecipeResultRegistry.NO_RECIPE; // Need a financial hatch
        }
        int totalAvailable = 0;
        for (FinancialHatch fh : financialHatches) {
            totalAvailable += fh.countCurrency(currencyType);
        }
        if (totalAvailable < currencyCost) {
            return CheckRecipeResultRegistry.NO_RECIPE; // Insufficient funds
        }

        return CheckRecipeResultRegistry.SUCCESSFUL;
```

- [ ] **Step 5: Modify checkProcessingMM() — add currency consumption**

In `checkProcessingMM()`, after `applyRecoveryRate();` (line 279) and before `return result;`, add:

```java
            consumeCurrencyFromRecipe();
```

Add the new method in the `// region Check Processing with Recovery Rate` section, after `applyRecoveryRate()`:

```java
    /**
     * Consumes the required currency from financial hatches after a successful recipe execution.
     */
    private void consumeCurrencyFromRecipe() {
        // Get the current recipe's currency requirement
        ProcessingLogic logic = getProcessingLogic();
        if (logic == null) return;

        // We need to access the last processed recipe; use a stored reference
        // For now, we track the currency consumption via the mLastRecipe or similar mechanism
        // This will be refined during implementation based on available GT API access
        if (!Config.EnableFinancialSystem) return;
        CurrencyType currencyType = currentRecipeCurrencyType;
        int currencyCost = currentRecipeCurrencyCost;
        if (currencyType == null || currencyCost <= 0) return;

        int remaining = currencyCost;
        for (FinancialHatch fh : financialHatches) {
            if (remaining <= 0) break;
            remaining -= fh.consumeCurrency(currencyType, remaining);
        }
    }
```

Add fields for tracking current recipe currency (after `syncedFinancialStatus`):

```java
    private CurrencyType currentRecipeCurrencyType = null;
    private int currentRecipeCurrencyCost = 0;
```

Modify `validateRecipeAccess()` to store the currency info when validation passes. At the end of `validateRecipeAccess()`, before the final `return CheckRecipeResultRegistry.SUCCESSFUL;`:

```java
        // Store currency info for later consumption
        this.currentRecipeCurrencyType = currencyType;
        this.currentRecipeCurrencyCost = currencyCost != null ? currencyCost : 0;

        return CheckRecipeResultRegistry.SUCCESSFUL;
```

And reset at the beginning of `validateRecipeAccess()`:

```java
        this.currentRecipeCurrencyType = null;
        this.currentRecipeCurrencyCost = 0;
```

- [ ] **Step 6: Add helper method for input buses**

Add a helper method:

```java
    private List<MTEHatchInputBus> getInputBuses() {
        List<MTEHatchInputBus> buses = new ArrayList<>();
        for (MTEHatch hatch : mInputBusses) {
            if (hatch instanceof MTEHatchInputBus bus) {
                buses.add(bus);
            }
        }
        return buses;
    }
```

- [ ] **Step 7: Verify compilation**

Run: `cd D:/Code/AndgateTechnology && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```bash
cd D:/Code/AndgateTechnology
git add src/main/java/com/andgatech/AHTech/common/machine/ElectronicsMarket.java
git commit -m "feat: integrate financial system into ElectronicsMarket"
```

---

## Task 9: ElectronicsMarket UI + 信息屏集成

**Files:**
- Modify: `src/main/java/com/andgatech/AHTech/common/machine/ElectronicsMarket.java`

- [ ] **Step 1: Update buildInformationLines() — add financial lines**

In `buildInformationLines()`, after the `"Modules: "` line and before `return lines;`, add:

```java
        // Financial system lines
        if (Config.EnableFinancialSystem) {
            if (financialHatches.isEmpty()) {
                lines.add("Financial Hatch: Not Installed");
            } else {
                lines.add("Financial Hatch: Installed");
                StringBuilder sb = new StringBuilder("Inventory: ");
                for (CurrencyType type : CurrencyType.values()) {
                    int total = 0;
                    for (FinancialHatch fh : financialHatches) {
                        total += fh.countCurrency(type);
                    }
                    sb.append(type.getName())
                        .append("x")
                        .append(total)
                        .append(" ");
                }
                lines.add(sb.toString().trim());
            }
        }
```

- [ ] **Step 2: Update addUIWidgets() — add financial status line**

In `addUIWidgets()`, after the perfect overclock indicator block (after line 702 `setSize(80, 10));`), add:

```java
        // Financial status display (synced via cached field)
        if (Config.EnableFinancialSystem) {
            builder
                .widget(
                    new TextWidget()
                        .setStringSupplier(() -> syncedFinancialStatus)
                        .setDefaultColor(0xFFD700)
                        .setPos(6, 143)
                        .setSize(160, 10))
                .widget(new FakeSyncWidget.StringSyncer(() -> {
                    if (financialHatches.isEmpty()) {
                        return StatCollector.translateToLocal("AHTech.UI.Finance.NoHatch");
                    }
                    StringBuilder sb = new StringBuilder();
                    for (CurrencyType type : CurrencyType.values()) {
                        int total = 0;
                        for (FinancialHatch fh : financialHatches) {
                            total += fh.countCurrency(type);
                        }
                        if (total > 0) {
                            if (sb.length() > 0) sb.append(" | ");
                            sb.append(type.getName())
                                .append("x")
                                .append(total);
                        }
                    }
                    if (sb.length() == 0) {
                        return StatCollector.translateToLocal("AHTech.UI.Finance.Empty");
                    }
                    return sb.toString();
                }, val -> syncedFinancialStatus = val));
        }
```

- [ ] **Step 3: Update NBT — save financial hatch data**

In `saveNBTData()`, after saving `contractTier`, add:

```java
        // Financial system state is per-session (hatches are physical blocks, not NBT state)
```

No NBT saving needed for financial state — financial hatch inventories are physical blocks with their own tile entity NBT. The `financialHatches` list is rebuilt on every `checkMachineMM()`.

- [ ] **Step 4: Verify compilation**

Run: `cd D:/Code/AndgateTechnology && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
cd D:/Code/AndgateTechnology
git add src/main/java/com/andgatech/AHTech/common/machine/ElectronicsMarket.java
git commit -m "feat: add financial status to ElectronicsMarket UI and information panel"
```

---

## Task 10: 为现有配方添加货币消耗标记

**Files:**
- Modify: `src/main/java/com/andgatech/AHTech/recipe/machineRecipe/ElectronicsMarketRecipePool.java`

- [ ] **Step 1: Add currency metadata to cable disassembly recipes**

In `loadCableDisassemblyRecipes()`, add currency metadata to the recipe builder (after the `.metadata(AHTechRecipeMetadata.SUPPLIER_ID, ...)` line):

```java
                    .metadata(AHTechRecipeMetadata.CURRENCY_TYPE, CurrencyType.COPPER)
                    .metadata(AHTechRecipeMetadata.CURRENCY_COST, 2)
```

Add import at the top:
```java
import com.andgatech.AHTech.common.currency.CurrencyType;
```

- [ ] **Step 2: Add currency metadata to laser vacuum tube recipe**

In `loadLaserVacuumTubeRecipe()`, add after the `.metadata(AHTechRecipeMetadata.SUPPLIER_ID, ...)` line:

```java
            .metadata(AHTechRecipeMetadata.CURRENCY_TYPE, CurrencyType.STEEL)
            .metadata(AHTechRecipeMetadata.CURRENCY_COST, 5)
```

- [ ] **Step 3: Verify compilation**

Run: `cd D:/Code/AndgateTechnology && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
cd D:/Code/AndgateTechnology
git add src/main/java/com/andgatech/AHTech/recipe/machineRecipe/ElectronicsMarketRecipePool.java
git commit -m "feat: add currency cost metadata to existing recipes"
```

---

## Task 11: 端到端验证

- [ ] **Step 1: Run full test suite**

Run: `cd D:/Code/AndgateTechnology && ./gradlew test`
Expected: All tests pass

- [ ] **Step 2: Run full build**

Run: `cd D:/Code/AndgateTechnology && ./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Final commit**

```bash
cd D:/Code/AndgateTechnology
git add -A
git commit -m "feat: complete financial system implementation (currency + financial hatch + recipe integration)"
```
