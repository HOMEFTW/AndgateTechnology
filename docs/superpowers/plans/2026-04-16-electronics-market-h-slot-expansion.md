# Electronics Market H-Slot Expansion Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rework `ElectronicsMarket` H-slot generation so the building uses a fixed, stage-progressive hatch skeleton with podium-heavy capacity, tower-side advanced expansion, and regression tests that lock both H-slot counts and floor distribution.

**Architecture:** Keep `H` as the single universal interface character in `ElectronicsMarket.java`, but move all H-slot placement policy into explicit stage-aware generator rules in `tools/generate_refined.py`. Prevent layer-object aliasing during structure generation, regenerate `ElectronicsMarketShapes.java`, and upgrade structure tests from loose minimum-count assertions to range, floor-whitelist, and stage-superset assertions.

**Tech Stack:** Java 21-compatible GTNH addon code, Python generator script, JUnit 5, Gradle `test`, StructureLib 1.4.28 shape pipeline

---

## File Map

- Modify: `tools/generate_refined.py`
  Responsibility: generate independent building layers and write stage-specific universal H-slot distributions.
- Modify: `src/main/java/com/andgatech/AHTech/common/machine/ElectronicsMarketShapes.java`
  Responsibility: generated shape constants for Stage I/II/III.
- Modify: `src/test/java/com/andgatech/AHTech/common/machine/ElectronicsMarketStructureBehaviorTest.java`
  Responsibility: regression coverage for H-slot totals, floor distribution, and stage superset relationships.
- Read-only context: `src/main/java/com/andgatech/AHTech/common/machine/ElectronicsMarket.java`
  Responsibility: keeps universal `H` semantics unchanged; no new hatch characters.
- Modify: `log.md`
  Responsibility: record implementation and verification in Chinese.
- Modify: `ToDOLIST.md`
  Responsibility: move H-slot expansion work from pending toward completed in Chinese.
- Modify: `context.md`
  Responsibility: summarize the finalized H-slot architecture and verification status in Chinese.

---

### Task 1: Lock the New H-Slot Rules in Tests

**Files:**
- Modify: `src/test/java/com/andgatech/AHTech/common/machine/ElectronicsMarketStructureBehaviorTest.java`
- Test: `src/test/java/com/andgatech/AHTech/common/machine/ElectronicsMarketStructureBehaviorTest.java`

- [ ] **Step 1: Write failing distribution assertions**

Add helper methods and replace the loose `>= 8` checks with stage-aware assertions.

```java
@Test
void stageShapesKeepHSlotsInsideExpectedFloorsAndCounts() {
    assertShapeDistribution(ElectronicsMarketShapes.STAGE1_SHAPE, 12, 24, setOf(1, 2, 3, 6, 7));
    assertShapeDistribution(ElectronicsMarketShapes.STAGE2_SHAPE, 24, 48, setOf(1, 2, 3, 6, 7, 10, 11));
    assertShapeDistribution(ElectronicsMarketShapes.STAGE3_SHAPE, 32, 64, setOf(1, 2, 3, 6, 7, 10, 11, 30, 31, 90, 91));
}

@Test
void laterStagesAreStrictSupersetsOfEarlierHSlotPositions() {
    Set<String> stage1 = collectHPositions(ElectronicsMarketShapes.STAGE1_SHAPE);
    Set<String> stage2 = collectHPositions(ElectronicsMarketShapes.STAGE2_SHAPE);
    Set<String> stage3 = collectHPositions(ElectronicsMarketShapes.STAGE3_SHAPE);

    assertTrue(stage2.containsAll(stage1));
    assertTrue(stage3.containsAll(stage2));
}
```

- [ ] **Step 2: Add test helpers for count, floor whitelist, and position sets**

Add helper methods in the same test file so the assertions are easy to read and maintain.

```java
private static void assertShapeDistribution(String[][] shape, int minH, int maxH, Set<Integer> allowedFloors) {
    int total = countChar(shape, 'H');
    assertTrue(total >= minH, "expected at least " + minH + " H slots, got " + total);
    assertTrue(total <= maxH, "expected at most " + maxH + " H slots, got " + total);

    Set<Integer> floors = collectHFloors(shape);
    assertEquals(allowedFloors, floors);
}

private static Set<Integer> collectHFloors(String[][] shape) {
    Set<Integer> floors = new LinkedHashSet<>();
    for (int y = 0; y < shape.length; y++) {
        for (String row : shape[y]) {
            if (row.indexOf('H') >= 0) {
                floors.add(y);
                break;
            }
        }
    }
    return floors;
}

private static Set<String> collectHPositions(String[][] shape) {
    Set<String> positions = new LinkedHashSet<>();
    for (int y = 0; y < shape.length; y++) {
        for (int z = 0; z < shape[y].length; z++) {
            String row = shape[y][z];
            for (int x = 0; x < row.length(); x++) {
                if (row.charAt(x) == 'H') {
                    positions.add(y + ":" + z + ":" + x);
                }
            }
        }
    }
    return positions;
}
```

- [ ] **Step 3: Run the targeted test to confirm it fails against the current polluted shapes**

Run:

```powershell
./gradlew.bat "-Pelytra.manifest.version=true" test --tests com.andgatech.AHTech.common.machine.ElectronicsMarketStructureBehaviorTest
```

Expected: `FAIL` because the current generated shapes contain 88 `H` slots across too many podium floors.

- [ ] **Step 4: Commit the red test**

```bash
git add src/test/java/com/andgatech/AHTech/common/machine/ElectronicsMarketStructureBehaviorTest.java
git commit -m "test: lock electronics market h-slot distribution"
```

---

### Task 2: Remove Layer Aliasing and Encode Stage-Specific H-Slot Policy

**Files:**
- Modify: `tools/generate_refined.py`
- Test via generation: `tools/generate_refined.py`

- [ ] **Step 1: Stop reusing the same layer object for repeated floors**

Replace shared `layer` appends with per-floor copies.

```python
for section in sections:
    count = section["count"]
    template_layer = generate_layer(section, max_w)
    for _ in range(count):
        layers.append(list(template_layer))
        section_widths.append(section["width"])
```

- [ ] **Step 2: Introduce explicit H-slot floor groups**

Define named floor groups instead of mutating “whatever layer happens to exist next”.

```python
STAGE1_H_FLOORS = {1, 2, 3, 6, 7}
STAGE2_EXTRA_H_FLOORS = {10, 11}
STAGE3_EXTRA_H_FLOORS = {30, 31, 90, 91}

PODIUM_FRONT_PATTERN = {
    1: (center - 5, center - 4, center + 4, center + 5),
    2: (center - 5, center - 4, center - 3, center + 3, center + 4, center + 5),
}
```

- [ ] **Step 3: Write stage-aware hatch placement helpers**

Add explicit helper functions that place universal `H` slots into allowed floors only.

```python
def mark_hatches_on_front_row(z_plane, xs):
    center = len(z_plane[0]) // 2
    for z in range(len(z_plane)):
        if center < len(z_plane[z]) and z_plane[z][center] != " ":
            row = list(z_plane[z])
            for x in xs:
                if 0 <= x < len(row) and row[x] != " " and row[x] != "~":
                    row[x] = "H"
            z_plane[z] = "".join(row)
            return z_plane
    return z_plane

def apply_stage_hatches(layers, floor_rules):
    for y, xs in floor_rules.items():
        layers[y] = mark_hatches_on_front_row(layers[y], xs)
    return layers
```

- [ ] **Step 4: Compose Stage I/II/III hatch rules as supersets**

Generate cumulative rule maps so later stages strictly include earlier positions.

```python
stage1_rules = build_stage1_hatch_rules(max_w)
stage2_rules = merge_hatch_rules(stage1_rules, build_stage2_podium_hatch_rules(max_w))
stage3_rules = merge_hatch_rules(stage2_rules, build_stage3_tower_hatch_rules(max_w))
```

- [ ] **Step 5: Run the generator script and inspect its summary**

Run:

```powershell
python tools/generate_refined.py
```

Expected: script completes successfully and the printed H-slot summary no longer reports 88 total slots on all three stages.

- [ ] **Step 6: Commit the generator refactor**

```bash
git add tools/generate_refined.py src/main/java/com/andgatech/AHTech/common/machine/ElectronicsMarketShapes.java
git commit -m "feat: generate staged electronics market h-slot skeleton"
```

---

### Task 3: Verify the Generated Shapes Match the Design

**Files:**
- Modify: `src/main/java/com/andgatech/AHTech/common/machine/ElectronicsMarketShapes.java`
- Read-only context: `docs/superpowers/specs/2026-04-16-electronics-market-h-slot-expansion-design.md`

- [ ] **Step 1: Regenerate the Java shape constants from the updated generator**

Run:

```powershell
python tools/generate_refined.py
```

Expected: `src/main/java/com/andgatech/AHTech/common/machine/ElectronicsMarketShapes.java` is updated in-place.

- [ ] **Step 2: Manually verify representative floors in the generated Java file**

Check that Stage I has only podium H floors, Stage II adds more podium capacity, and Stage III adds tower floors.

```java
// Representative expectations after regeneration:
// Stage I: H on a small set of podium floors only
// Stage II: Stage I floors plus 1-2 podium equipment floors
// Stage III: Stage II floors plus a few tower equipment / facade-band floors
```

- [ ] **Step 3: If the generated file includes unstable formatting noise, run code formatting**

Run:

```powershell
./gradlew.bat "-Pelytra.manifest.version=true" spotlessApply
```

Expected: formatting completes without altering H-slot semantics.

- [ ] **Step 4: Commit the regenerated structure file**

```bash
git add src/main/java/com/andgatech/AHTech/common/machine/ElectronicsMarketShapes.java
git commit -m "chore: regenerate electronics market shapes"
```

---

### Task 4: Turn the Structure Green and Close the Loop

**Files:**
- Modify: `src/test/java/com/andgatech/AHTech/common/machine/ElectronicsMarketStructureBehaviorTest.java`
- Modify: `log.md`
- Modify: `ToDOLIST.md`
- Modify: `context.md`

- [ ] **Step 1: Run the targeted structure test until it passes**

Run:

```powershell
./gradlew.bat "-Pelytra.manifest.version=true" test --tests com.andgatech.AHTech.common.machine.ElectronicsMarketStructureBehaviorTest
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 2: Run full regression tests**

Run:

```powershell
./gradlew.bat "-Pelytra.manifest.version=true" test
```

Expected: `BUILD SUCCESSFUL`, with only previously known non-blocking warnings such as the existing `comments` translation warning.

- [ ] **Step 3: Update project records in Chinese**

Record the completed implementation, remaining risks, and verification results.

```markdown
## log.md
- 修复 H 位楼层对象复用与阶段化分布规则
- 升级结构回归测试为数量 + 分布 + 超集断言

## ToDOLIST.md
- [x] 修复 `tools/generate_refined.py` 的楼层对象复用
- [x] 收紧 `ElectronicsMarketStructureBehaviorTest`

## context.md
- H 位现已采用固定接口骨架 + 分阶段启用
- 裙楼主容量，塔楼承担进阶扩展
```

- [ ] **Step 4: Commit the verification and docs sync**

```bash
git add src/test/java/com/andgatech/AHTech/common/machine/ElectronicsMarketStructureBehaviorTest.java log.md ToDOLIST.md context.md
git commit -m "test: verify staged electronics market h-slot layout"
```

---

## Self-Review

- Spec coverage:
  - Fixed interface skeleton: Task 2
  - Stage-progressive supersets: Task 2 + Task 1
  - Podium-heavy, tower-light distribution: Task 2 + Task 3
  - Universal `H` semantics unchanged: File Map + Task 2
  - Regression coverage for count and distribution: Task 1 + Task 4
- Placeholder scan:
  - No `TODO` / `TBD` / “implement later” placeholders remain.
- Type consistency:
  - Uses existing `ElectronicsMarketShapes` and `ElectronicsMarketStructureBehaviorTest` names consistently.

