# 待办清单

## 赛格大厦重构（大方向）

核心概念：将美弱南电子市场从单台机器重构为模块化赛格大厦建筑系统。

### 阶段一：建筑与外观
- [ ] 设计赛格大厦三级外观模型：一级=底部卖场、二级=中间扩充、三级=完整大厦
- [ ] 实现大厦承重墙系统（承重墙=机器本体，外观可自由修改）
- [ ] 添加默认赛格大厦投影外观

### 阶段二：模块化系统（二级结构及以上可用）

#### 已完成
- [x] 设计模块化接口：接口/基类/舱口三层架构完整（IModularizedMachine + 8 子接口、ModularizedMachineBase → ModularizedMachineSupportAllModuleBase、ModularHatchBase），共注册 58 个模块（标准 46 + AHTech 独占 12）
- [x] 模块化系统基础运行：模块可安装、效果可应用、TST 互通正常
- [x] 模块适配规则（等级门控）：全部 9 类模块的 `onCheckMachine()` / `onCheckProcessing()` 均调用 `isCompatibleWithMachine()`，高等级模块安装到低等级结构上静默不生效
- [x] 一级结构限制：Stage I 通过 `checkModularStaticSettings()` 覆写显式跳过模块应用，配方门禁 + 模块禁装双重限制
  - [x] 配方门禁：Stage I 只能执行 `specialValue == 0` 的线缆拆解配方
  - [x] 模块禁装：Stage I 不应用模块效果（硬编码 parallel=3，回收率=30%）
- [x] 二级结构解锁模块化：通过 `checkModularStaticSettings()` 覆写实现显式分支，Stage II/III 走 `super` 正常应用模块
  - [x] 配方解锁：`specialValue == 1` 的回收配方和 `specialValue == 2` 的高级配方在 Stage II 被放行
  - [x] 回收率切换：Stage I 固定 30%，Stage II/III 改为模块驱动
  - [x] 显式模块化开关：`checkModularStaticSettings()` 按 structureTier 分支处理
- [x] 供电系统：模块维护耗电 + 供电不足吞材料
  - [x] 基础供电：能源仓放置于 'H' 位置，支持 PowerConsumption 模块调节耗能
  - [x] 无线 EU：高级/完美执行核心使用 `WirelessNetworkManager`
  - [x] 模块维护耗电：`ModularHatchBase.getMaintenanceEUt()` 按 tier 缩放，tier=0 无成本
  - [x] 供电不足吞材料：存储 EU 不足以覆盖模块维护耗电时，产出清零但输入照扣（`Config.EnablePowerInsufficientMaterialLoss` 控制）
- [x] 模块数量限制：通过模块维护耗电间接约束——模块越多维护成本越高，供电不足则吞材料

#### 未开始
- [ ] 三级结构特殊处理：当前 Stage III 与 Stage II 代码逻辑完全等同，无差异化处理
- [ ] 输入输出仓跟随模块放置：所有仓室固定在 Layer 0 的 16 个 'H' 位置，不跟随模块位置变化

### 阶段三：供货协议合同系统（配方解锁）
- [x] 设计[供货协议合同]物品（配方等级解锁道具；当前已作为供应商配方解锁条件接入）
- [x] 一级合同 → 解锁一级供应商，需对应等级力场发生器+材料合成
- [x] 二级合同 → 需一级合同 + 更高等级材料（套娃合成，不可跳级）
- [x] 三级合同 → 需二级合同 + 更高等级材料
- [x] 扩展实现四级合同 → 需三级合同 + 更高等级材料
- [x] 供应商舱口与合同联动解锁配方：Data Access Hatch 中的最高合同等级 + 已安装供应商共同决定可用配方
- [ ] 八仓可升压，但仅影响配方速度；配方等级由合同决定



### 阶段五：锁科技与趣味内容
- [ ] 设计锁科技模块（无具体功能，纯科技锁定）
- [x] 实现供应商模块列表（已注册 7 个供应商舱口，并提供名称/标语本地化）：
  - [x] [山东德州仪器]
  - [x] [亚德骗半导体]
  - [x] [超拉半导体]
  - [x] [黄伟达]
  - [x] [钙光]
  - [x] [低通]
  - [x] [二法半导体]
  - [ ] …更多供应商


## 其他未来想法
- [ ] 添加更多特殊配方到 ElectronicsMarketRecipePool
- [ ] 添加 Mixin（如有需要）
- [ ] 配置 CI/CD 构建
- [ ] **矿物类型分配器**（新多方块机器构想）：输入仓放入矿石相关物品，按类型自动分配到六个输出仓
  - [ ] 输出仓1：矿石（OrePrefixes.ore）和粗矿石（OrePrefixes.crushedOre）
  - [ ] 输出仓2：粉碎的矿石（OrePrefixes.crushed）
  - [ ] 输出仓3：洗净的矿石（OrePrefixes.crushedPurified）
  - [ ] 输出仓4：离心矿石（OrePrefixes.crushedCentrifuged）
  - [ ] 输出仓5：含杂矿石粉（OrePrefixes.dustImpure）
  - [ ] 输出仓6：矿石粉（OrePrefixes.dust）
  - [ ] 输出仓7：不匹配以上六种类型的物品（兜底输出）

## 2026-04-15 审查待修
- [x] 按 TST 原行为显式化维护耗电池：`ElectronicsMarket.getModulesSubjectToMaintenance()` 只统计 AHTech 原生模块，TST 互通模块继续不参与 AHTech 自定义维护耗电
- [x] 修复执行核心持久化缺口：为 `ExecutionCoreBase` 补齐在途任务输出缓存、进度与耗电参数的 NBT 持久化；主机关联继续通过结构重检后的 `setup(...)` 恢复
- [x] 为上述两项补充测试：覆盖 TST 互通模块维护耗电边界，以及执行核心运行状态持久化
- [x] 修复 `RecyclingRecipeGenerator` 的多产物逆向失真：输出候选去重现已包含 `stackSize`，`buildRecyclingRecipes()` 会按正向输出数量注册逆向输入
- [x] 修复 `RecyclingRecipeGenerator` 的流体输入遗漏：`processGTRecipe()` 现已同时排除 `mFluidInputs` 与 `mFluidOutputs`
- [x] 为自动回收配方补充回归测试：新增 `RecyclingRecipeGeneratorBehaviorTest`，覆盖“同物品不同输出数量”与“GT 配方含流体输入”两类场景
- [x] 修复 `ElectronicsMarket` 并行货币白嫖：并行上限现已受可支付货币数量约束，`consumeCurrencyFromRecipe()` 也会按真实并行数扣除总成本
- [x] 修复 `ElectronicsMarket` 单件产物回收率失真：`applyRecoveryRate()` 现已改为“整数部分 + 余数概率补 1 个”，单件非电路板产物不再被无条件保底
- [x] 为上述两项补充回归测试：`ElectronicsMarketFinancialBehaviorTest` 已覆盖“并行货币乘数 / 货币限制并行 / 单件产物概率回收”三类断言

## 已完成
- [x] 合同与供应商系统首版：Lv1-Lv4 合同物品、7 个供应商舱口、Data Access Hatch 合同读取、供应商 metadata 门禁、UI/工业信息显示与合成配方
- [x] 将 `log.md`、`context.md`、`ToDOLIST.md` 统一整理为中文，并补充 `gtnh-dev-logging` 的中文写入规则
- [x] 美弱南电子市场核心主方块已通过 `getInfoData()` 与 `reportMetrics()` 提供稳定的工业信息屏数据
- [x] 项目脚手架生成与构建验证
- [x] gtnh-addon-generator skill 模板修复
- [x] gtnh-dev-logging skill 创建
- [x] 强化 gtnh-dev-logging：每完成用户请求后强制更新三文件
- [x] 美弱南电子市场设计文档
- [x] 美弱南电子市场实现（控制器 + 配方系统 + 构建验证）
- [x] 模块化系统框架（接口/基类/舱口注册/结构检测）
- [x] 回收率模块（3 级：50%/70%/90%）
- [x] 通用拆解功能模块
- [x] TST 标准模块各一级（并行/速度/超频/功耗）框架验证
- [x] 执行核心（普通版，框架验证）
- [x] ElectronicsMarket 重构为模块化架构
- [x] 移植 TST 完整模块系统（35 个标准模块）
- [x] 实现 TST 模块互通（检测+识别+效果应用）
- [x] UI 同步修复（FakeSyncWidget setter / DynamicTextWidget / 翻译键）
- [x] gtnh-multiblock-ui skill 增强（从 TST 提取 7 个新模式）
- [x] 版本 0.0.2-pre 推送至 GitHub

## 已拒绝 / 已延期

## 2026-04-15 更新
- [x] 资金系统首版实现：6 级货币、FinancialHatch、资金 metadata、ElectronicsMarket 资金校验与扣款
- [x] 重写 `src/main/resources/assets/andgatetechnology/lang/zh_CN.lang`，修复旧文件乱码、多 key 串行与中文本地化缺失问题
- [x] 修复 `ElectronicsMarket` 审查回归问题：工业信息屏恢复固定 8 行输出、模块维护耗电边界判定改为“相等即充足”、配方校验前触发资金舱自动补币
- [x] 修复配置耦合与回收配方确定性问题：供应商/资金加载不再被 `EnableModularizedMachineSystem` 隐式禁用、TST 检测改为 `Loader.isModLoaded`、自动回收配方改为稳定候选选择
- [x] 重新打通测试验证通道：在当前环境下使用 `./gradlew.bat "-Pelytra.manifest.version=true" test` 强制 Elytra conventions 读取本地 manifest 缓存，定向与全量测试均已恢复可用
- [ ] 为更多 `ElectronicsMarket` 配方逐步补充 `CURRENCY_TYPE` / `CURRENCY_COST`
- [ ] 视需要补充“资金不足”专用提示文案或自定义 `CheckRecipeResult`
- [ ] 如需彻底消除语言预处理阶段的 `comments` 警告，后续可单独检查 `addon.gradle` 的 lang 解析逻辑
- [ ] 如果后续继续维护本地化，统一使用 UTF-8，并在命令行检查时显式指定 `-Encoding UTF8`
## 2026-04-15 审查补充
- [x] 继续审查模块化系统，确认 ModularizedMachineBase 仍有旧的 TST 检测路径，且域内执行核心尚未接入主机生产逻辑
- [x] 修复 ModularizedMachineBase.isTSTLoaded()，将模块化基类的 TST 检测与 MachineLoader 统一到 Loader.isModLoaded("TwistSpaceTechnology")
- [x] 补全执行核心的主机集成：分配配方、设置 hasBeenSetup、推进进度、回收输出送回主机
