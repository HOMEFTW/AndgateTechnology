# Development Log

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
