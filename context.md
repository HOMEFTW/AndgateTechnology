# Project Context

## Basic Info
- Mod Name: Andgate Technology
- Mod ID: AndgateTechnology
- Mod ID (lower): andgatetechnology
- Package: com.andgatech.AHTech
- Target: MC 1.7.10 + Forge 10.13.4.1614 + GTNH 2.8.4
- Gradle: 8.14.3, RetroFuturaGradle + GTNH Convention 1.0.50

## Implemented Content

### Machines
| Name | Meta ID | Type | Status |
|------|---------|------|--------|
_(none yet)_

### Items
| Name | Registration | Description |
|------|-------------|-------------|
_(none yet)_

### Blocks
| Name | Registration | Description |
|------|-------------|-------------|
_(none yet)_

### Materials
_(none yet)_

### Recipes
| Recipe Pool | Type | Count |
|-------------|------|-------|
| ShapedCraftRecipePool | Crafting | 0 |

### Config Options
| Key | Default | Description |
|-----|---------|-------------|
| MAX_PARALLEL_LIMIT | 256 | Max parallel for all machines |
| DEFAULT_BATCH_MODE | false | Default batch mode for machines |

### Mixins
_(none yet)_

## Dependencies
- GT5-Unofficial (GregTech)
- GTNHLib
- NewHorizonsCoreMod
- IC2 (industrialcraft-2:2.2.828-experimental)

## Architecture Notes
- Lifecycle: preInit → Config + Materials; init → Machines; completeInit → Recipes
- I18n: `//#tr key value` comments auto-generate lang files via addon.gradle
- Mixin package: `com.andgatech.AHTech.mixin`
- Resource namespace: `assets/andgatetechnology/`
