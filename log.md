# Development Log

## 2026-04-10: TST 模块移植与互通

### Completed
- 将 TST 完整模块系统移植到 AHTech（35 个标准模块）
  - 静态并行控制器 T1-T8：8/128/2048/32768/524288/8388608/134217728/MAX
  - 动态并行控制器 T1-T8：同上参数
  - 静态速度控制器 T1-T8：2x/4x/8x/16x/32x/64x/128x/256x
  - 动态速度控制器 T1-T8：同上参数
  - 功耗控制器 T1-T8：0.95/0.9/0.85/0.8/0.75/0.7/0.5/0.25
  - 超频控制器：低速完美(2,2) / 完美(4,4) / 奇点(8,4)
  - 执行核心：普通 / 高级(无线EU) / 完美(1秒完成)
- 实现 TST 模块互通
  - ModularizedMachineBase 可识别 TST 的 ModularHatchBase
  - 通过 instanceof 检测 TST 模块类型，应用效果到 AHTech 机器
  - MachineLoader 检测 TST 是否安装，避免重复注册
- 使用编译时存根类实现互通（不依赖 TST JAR）
- BUILD SUCCESSFUL 验证通过

### Decisions Made
- 使用存根类而非 Maven 依赖（TST 未发布到 GTNH Maven）
- TST 存根类从输出 JAR 中排除，避免类冲突
- TST 安装时跳过标准模块注册，AHTech 专属模块始终注册
- 执行核心暂仅做发现/记录，深度集成留待后续

---

## 2026-04-10: 模块化系统实现

### Completed
- 实现完整的 TST 风格模块化系统框架
  - 接口层次：IModularizedMachine + 8 个子接口（并行/速度/超频/功耗/回收率/功能模块/聚合）
  - 基类层次：ModularizedMachineBase → ModularizedMachineSupportAllModuleBase
  - 舱口基类：ModularHatchBase（含 moduleTier 兼容性检查）
- 实现 TST 标准模块各一个（并行/速度/超频/功耗）用于框架验证
- 实现回收率模块 3 级（50%/70%/90%）
- 实现执行核心（普通版，框架验证）
- 实现功能模块系统 + 通用拆解模块
- 重构 ElectronicsMarket 使用模块化架构
  - 移除 stage 硬编码性能参数
  - 一阶保留 30% 基础回收率
  - 二阶以上由模块驱动
  - validateRecipe 根据 specialValue 检查功能模块
  - 输出应用回收率（电路板 100%）
- 更新 Config（新增模块化配置，删除 Stage2/3/Voltage）
- 更新 ModItemList（新增 9 个模块条目）
- 更新 MachineLoader（注册模块舱口，Meta 35050-35070）
- 更新 RecyclingRecipeGenerator（自动回收配方标记 specialValue(1)）
- BUILD SUCCESSFUL 验证通过

### Decisions Made
- 模块化系统完整仿照 TST 的接口+基类+舱口三层架构
- 新增 ISupportRecoveryRateController 和 ISupportFunctionModule 接口
- 功能模块控制"能拆什么"，性能模块控制"拆多少"
- 配方通过 specialValue 分类：0=硬编码, 1=通用拆解, 2=需二阶
- 一阶无模块功能，二阶开始才能插入模块
- 回收率取所有安装模块的最大值
- 允许同类多模块叠加效果

---

## 2026-04-09: 自定义 UI 与激光真空管修复

### Completed
- 确认 TecTech 已合并到 GT5-Unofficial，找到 Laser Vacuum Pipe（Meta ID 15465，`CustomItemList.LASERpipe`）
- 更新激光真空管配方输出为 `CustomItemList.LASERpipe`
- 参考 TST 的 `addUIWidgets` 模式，为美弱南电子市场添加自定义 UI
- UI 显示：阶段（Stage I/II/III）、并行数、速度加成、完美超频开关
- 所有 UI 数据通过 `FakeSyncWidget` 同步服务端→客户端
- 添加 I18n 翻译 key 到 `en_US.lang`
- BUILD SUCCESSFUL 验证通过

### Issues Encountered
- **TextWidget.setTextColor 不存在** → 应使用 `setDefaultColor`
- **FakeSyncWidget.FloatSyncer 不存在** → 改用 `DoubleSyncer` + `val.floatValue()` 转换

### Decisions Made
- UI 使用纯 TextWidget 显示运行时信息，暂不自定义纹理
- UI 位置在默认 GT 多方块 UI 下方（y=73/83/93/103）

---

## 2026-04-09: 整理赛格大厦愿景到 ToDOLIST

### Completed
- 读取 chat.txt 中的设计愿景
- 拆分为 6 个阶段、约 20 个具体小任务，写入 ToDOLIST.md

### Decisions Made
- 核心方向：从单台机器重构为模块化赛格大厦建筑系统
- 配方解锁由[供货协议合同]决定，不再由结构方块决定
- 模块用闪存绑定主方块，不限制固定位置
- 供电统一到大夏，模块从大厦取电，不足吞材料

---

## 2026-04-09: 激光真空管配方修复

### Completed
- 确认 TecTech 已合并到 GT5-Unofficial
- 在 GT5-Unofficial 源码中找到 Laser Vacuum Pipe（Meta ID 15465，`tectech.thing.CustomItemList.LASERpipe`）
- 更新 `ElectronicsMarketRecipePool.java`：将 `ItemList.Circuit_Parts_Vacuum_Tube` 替换为 `CustomItemList.LASERpipe`
- 移除无用的 `ItemList` import
- BUILD SUCCESSFUL 验证通过

### Decisions Made
- 激光真空管配方输出改为 TecTech 的 Laser Vacuum Pipe（`CustomItemList.LASERpipe`），不再使用占位符

---

## 2026-04-09: 美弱南电子市场实现

### Completed
- 实现了完整的 8 步开发计划
- Task 1: Config 配置项（Enable、回收率、电压加成）
- Task 2: 自定义 RecipeMap `AHTechRecipeMaps.ElectronicsMarketRecipes`（9in/9out/4fin/4fout）
- Task 3: 多方块控制器 `ElectronicsMarket`（继承 MTEExtendedPowerMultiBlockBase）
- Task 4: 注册机器 Meta ID 35001 + ModItemList 条目
- Task 5: 硬编码特殊配方（线缆拆解 36 材质×6 尺寸、激光真空管 Stage II+）
- Task 6: 自动解析回收配方生成器（扫描 GT RecipeMaps + Forge CraftingManager）
- Task 7: BUILD SUCCESSFUL 验证通过
- 修复 `.gitignore` 遗漏 `bin/` 和 `chat.txt` 的问题

### Issues Encountered
- **TST 依赖问题**: ElectronicsMarket 初始引用了 TST 的 GTCM_MultiMachineBase 等类 → 改为直接继承 GT 的 MTEExtendedPowerMultiBlockBase
- **RecipeMapBackend 导入路径错误**: `gregtech.api.recipe.backend.RecipeMapBackend` → `gregtech.api.recipe.RecipeMapBackend`
- **TierEU 常量位置**: `GTValues.RECIPE_LV` 不存在 → 应使用 `TierEU.RECIPE_LV`
- **Materials 命名**: `Tungstensteel` → `TungstenSteel`，`SuperconductorUXV` 不存在
- **网络不稳定**: GitHub manifest 加载偶尔失败，重试后恢复

### Decisions Made
- 电压影响：speedBonus 按 tier 递减（1x/0.5x/0.25x），maxParallel 按 tier²×4×(1+voltage/8)
- Stage III 启用 Perfect Overclock
- 回收率在运行时由 ProcessingLogic 根据阶段和电压动态计算，不在配方注册时固定
- 结构方块：Tier I=Tungstensteel Casing, Tier II=Stable Titanium Casing, Tier III=Prediction Casing

---

## 2026-04-09: 美弱南电子市场设计

### Completed
- 完成美弱南电子市场多方块机器的设计文档
- 文档路径: `docs/superpowers/specs/2026-04-09-electronics-market-design.md`
- 修复 `.gitignore` 遗漏 `bin/` 目录的问题

### Decisions Made
- 单控制器方块 + 多方块结构组合判定阶段（而非三台独立机器）
- Meta ID: 35001
- 混合配方系统：硬编码特殊配方 + 自动解析回收配方
- 配方来源覆盖 GT RecipeMaps + Forge CraftingManager（跨 mod 兼容）
- 回收率 = 阶段基础率 + 电压加成，全部可配置
- 电路板 100% 回收，不受阶段和电压影响

---

## 2026-04-09: 强化 gtnh-dev-logging skill

### Completed
- 为 `gtnh-dev-logging` skill 添加强制规则：每完成一个用户请求后必须更新全部三个文件
- 新增 Red Flags 列表，防止跳过文档更新的常见借口
- 将规则 1 提升为 MANDATORY 级别

---

## 2026-04-09: 项目初始化与 Skill 改进

### Completed
- 使用 `gtnh-addon-generator` skill 从模板生成完整项目骨架
- 创建标准目录结构（loader / common / config / recipe / client / mixin 等）
- 从 TST 项目复制 Gradle wrapper（gradlew / gradlew.bat / gradle-wrapper.jar）
- 构建修复并验证通过（BUILD SUCCESSFUL，生成 dev / 普通 / sources 三个 jar）
- 更新 `gtnh-addon-generator` skill 模板（修复 5 个构建问题）
- 创建 `gtnh-dev-logging` skill（项目文档维护规范）

### Issues Encountered
- **Git 无提交记录**: GTNH Gradle 版本插件需要 git HEAD → 先 commit 再 build
- **Mixin 包路径错误**: GTNH Convention 插件将 `mixinsPackage` 解析为相对 `modGroup` 的路径 → mixin 目录放在 `{PACKAGE_PATH}/mixin/`
- **Spotless 格式检查失败**: `@SidedProxy` 多行注解 + LF 换行 → 改单行 + `spotlessApply`

### Decisions Made
- Mod ID: `AndgateTechnology`，资源命名空间: `andgatetechnology`
- 包名: `com.andgatech.AHTech`（用户指定）
- Meta ID 范围: 未分配，待后续添加机器时确定（避开 TST 使用的 18791-19080）
- Git 用户临时设为 `developer@andgate.tech`，需用户后续更新

---
