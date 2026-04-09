# Development Log

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
