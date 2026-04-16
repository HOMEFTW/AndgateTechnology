"""
根据赛格大厦轮廓数据，生成 GTNH 多方块结构。
输出: String[][] 形状数组 + Java 代码骨架 + 3D 可视化预览

用法: python tools/generate_multiblock.py
"""

import json
import os

PROFILE_PATH = os.path.join(os.path.dirname(__file__), 'building_profile.json')
OUTPUT_DIR = os.path.join(os.path.dirname(__file__))


def load_profile():
    with open(PROFILE_PATH, 'r', encoding='utf-8') as f:
        return json.load(f)


def generate_shape_array(profile):
    """
    根据轮廓数据生成 String[][] 形状数组。
    每层是正方形截面 (width x width)，用不同字符表示不同区段。
    控制器 ~ 放在底座的正面中央。
    """
    widths = profile['widths']
    sections = profile['sections']

    # 字符映射：
    # B = 底座方块 (Base)
    # T = 塔楼方块 (Tower)
    # C = 皇冠方块 (Crown)
    # H = Hatch 位置 (放在底座)
    # ~ = 控制器 (Controller)
    # - = 空气 (Air，内部空间)
    # 空格 = 跳过

    char_map = {
        'base': 'B',
        'tower': 'T',
        'crown': 'C'
    }

    layers = []  # String[Y][Z] - 每层是一个 String[]

    controller_placed = False

    for y in range(len(widths)):
        w = widths[y]

        # 确定区段
        if y <= sections['base']['end']:
            section = 'base'
        elif y >= sections['crown']['start']:
            section = 'crown'
        else:
            section = 'tower'

        outer_char = char_map[section]
        inner_char = '-'  # 空气（内部空间）

        # 构建该层的 Z 平面
        z_plane = []
        for z in range(w):
            row = ""
            for x in range(w):
                is_edge = (x == 0 or x == w - 1 or z == 0 or z == w - 1)

                if is_edge:
                    # 底座正面中央放置控制器和 Hatch
                    if (section == 'base' and not controller_placed
                            and y == 1 and z == 0 and x == w // 2):
                        row += '~'
                        controller_placed = True
                    elif (section == 'base' and y == 1 and z == 0
                          and (x == 1 or x == w - 2)):
                        # Hatch 位置
                        row += 'H'
                    else:
                        row += outer_char
                else:
                    row += inner_char

            z_plane.append(row)

        layers.append(z_plane)

    # 用空格填充使所有层的维度一致（结构需要矩形）
    # 找到最大宽度和深度
    max_w = max(widths)

    # 填充每层使其成为 max_w x max_w
    padded_layers = []
    for y in range(len(layers)):
        w = widths[y]
        offset = (max_w - w) // 2
        z_plane = layers[y]

        padded_z = []
        for z in range(max_w):
            if z < offset or z >= offset + w:
                # 这一行超出了该层宽度，用空格
                padded_z.append(" " * max_w)
            else:
                row = z_plane[z - offset]
                # 行内也需要左右填充空格
                padded_row = " " * offset + row + " " * (max_w - offset - len(row))
                padded_z.append(padded_row)

        padded_layers.append(padded_z)

    return padded_layers, widths, max_w


def generate_shape_array_sectioned(profile):
    """
    生成简化版的形状数组，按区段分 piece。
    使用动态重复段落模式：base + middle(repeatable) + top。
    """
    widths = profile['widths']
    sections = profile['sections']

    base_end = sections['base']['end']
    tower_start = sections['tower']['start']
    tower_end = sections['tower']['end']
    crown_start = sections['crown']['start']

    # === Base piece ===
    base_layers = generate_piece_layers(widths[:base_end + 1], 'base',
                                         place_controller=True)

    # === Tower standard layer (one repeating layer) ===
    tower_widths = widths[tower_start:tower_end + 1]
    # 找最常见的宽度作为标准层
    from collections import Counter
    if tower_widths:
        std_w = Counter(tower_widths).most_common(1)[0][0]
    else:
        std_w = 7

    tower_single_layer = generate_single_layer(std_w, 'T', with_air=True)

    # === Crown piece ===
    crown_layers = generate_piece_layers(widths[crown_start:], 'crown',
                                          place_controller=False)

    return {
        'base': base_layers,
        'tower_layer': tower_single_layer,
        'tower_width': std_w,
        'tower_count': len(tower_widths),
        'crown': crown_layers,
        'total_height': len(widths),
        'max_width': max(widths)
    }


def generate_piece_layers(widths_sub, section, place_controller=False):
    """为一个区段生成填充后的层"""
    char = 'B' if section == 'base' else 'C'
    max_w = max(widths_sub) if widths_sub else 3

    # 为了让所有区段在同一个大结构中对齐，使用统一最大宽度
    # 但作为独立 piece，每个 piece 用自己的最大宽度
    layers = []
    controller_done = False

    for y, w in enumerate(widths_sub):
        offset = (max_w - w) // 2
        z_plane = []
        for z in range(max_w):
            if z < offset or z >= offset + w:
                z_plane.append(" " * max_w)
            else:
                row = ""
                for x in range(max_w):
                    if x < offset or x >= offset + w:
                        row += " "
                    elif x == offset or x == offset + w - 1 or z == offset or z == offset + w - 1:
                        # 边缘
                        if (place_controller and not controller_done
                                and y == 1 and z == offset and x == max_w // 2):
                            row += '~'
                            controller_done = True
                        elif (place_controller and y == 1 and z == offset
                              and (x == offset + 1 or x == offset + w - 2)):
                            row += 'H'
                        else:
                            row += char
                    else:
                        row += '-'
                z_plane.append(row)
        layers.append(z_plane)

    return layers


def generate_single_layer(w, char, with_air=True):
    """生成单个正方形层"""
    z_plane = []
    for z in range(w):
        row = ""
        for x in range(w):
            if x == 0 or x == w - 1 or z == 0 or z == w - 1:
                row += char
            else:
                row += '-' if with_air else char
        z_plane.append(row)
    return z_plane


def array_to_java_string(layers, var_name="STRUCTURE_SHAPE"):
    """将层列表转为 Java String[][] 代码"""
    lines = [f'// 自动生成的赛格大厦结构形状数组',
             f'// 尺寸: {len(layers[0][0])}W x {len(layers)}H x {len(layers[0])}D',
             f'private static final String[][] {var_name} = transpose(new String[][]{{{"}"}']

    for y, z_plane in enumerate(layers):
        lines.append(f'    // Y={y}')
        lines.append('    {')
        for z, row in enumerate(z_plane):
            lines.append(f'        "{row}",')
        lines.append('    },')

    lines.append('});')
    return '\n'.join(lines)


def generate_visualization(layers):
    """生成3D层叠可视化"""
    lines = []
    max_w = len(layers[0][0]) if layers else 0
    n = len(layers)

    lines.append("=" * 60)
    lines.append("赛格大厦 - 多方块结构 3D 预览")
    lines.append("=" * 60)
    lines.append("")

    for y in range(n - 1, -1, -1):
        z_plane = layers[y]
        lines.append(f"--- Y={y} (从上往下看) ---")
        for z, row in enumerate(z_plane):
            display = row.replace(' ', '.').replace('-', '·')
            lines.append(f"  Z{z}: |{display}|")
        lines.append("")

    # 找到控制器位置
    for y, z_plane in enumerate(layers):
        for z, row in enumerate(z_plane):
            if '~' in row:
                x = row.index('~')
                lines.append(f"控制器位置: X={x}, Y={y}, Z={z}")
                lines.append(f"偏移量: horizontalOff={x}, verticalOff={y}, depthOff={z}")

    return '\n'.join(lines)


def generate_java_skeleton(sectioned):
    """生成 Java 代码骨架"""
    base = sectioned['base']
    tower_w = sectioned['tower_width']
    tower_count = sectioned['tower_count']
    crown = sectioned['crown']
    total_h = sectioned['total_height']

    # 计算控制器偏移（在 base piece 中找 ~）
    h_off = v_off = d_off = 0
    for y, z_plane in enumerate(base):
        for z, row in enumerate(z_plane):
            if '~' in row:
                h_off = row.index('~')
                v_off = y
                d_off = z

    code = f'''// 赛格大厦多方块机器 - 自动生成的代码骨架
// 总尺寸约: {tower_w}W x {total_h}H x {tower_w}D
// 结构: 底座({len(base)}层) + 塔楼({tower_count}层x{tower_w}x{tower_w}) + 皇冠({len(crown)}层)

package com.andgatech.AHTech.common.machine;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.*;
import static gregtech.api.enums.HatchElement.*;
import static gregtech.api.util.GTStructureUtility.*;

import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.metatileentity.IGregTechTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTETieredMachine;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.util.HatchElementBuilder;
import net.minecraft.item.ItemStack;

public class TST_SegPlaza extends GTCM_MultiMachineBase<TST_SegPlaza> {{

    private static final String STRUCTURE_PIECE_BASE = "base";
    private static final String STRUCTURE_PIECE_TOWER = "tower";
    private static final String STRUCTURE_PIECE_CROWN = "crown";

    // 控制器偏移量（在 base piece 中的位置）
    private static final int horizontalOff = {h_off};
    private static final int verticalOff = {v_off};
    private static final int depthOff = {d_off};

    // 塔楼重复层数（可通过 stackSize 调节）
    private static final int TOWER_MIN_LAYERS = 5;
    private static final int TOWER_MAX_LAYERS = {tower_count};
    private int mTowerLayers = 0;

    // -- Constructors --
    public TST_SegPlaza(int aID, String aName, String aNameRegional) {{
        super(aID, aName, aNameRegional);
    }}

    public TST_SegPlaza(String aName) {{ super(aName); }}

    @Override
    public IMetaTileEntity newMetaEntity() {{ return new TST_SegPlaza(mName); }}

    // -- Structure Definition --
    private static IStructureDefinition<TST_SegPlaza> STRUCTURE_DEF = null;

    @Override
    public IStructureDefinition<TST_SegPlaza> getStructureDefinition() {{
        if (STRUCTURE_DEF == null) {{
            STRUCTURE_DEF = StructureDefinition.<TST_SegPlaza>builder()
                .addShape(STRUCTURE_PIECE_BASE, transpose(new String[][]{{
                    // TODO: 粘贴 base 层数组
                    // { "         ", "  BBBBB  ", ... },
                }}))
                .addShape(STRUCTURE_PIECE_TOWER, transpose(new String[][]{{
                    // 单层塔楼 (重复使用)
                    // { "TTTTT", "T---T", "T---T", "T---T", "TTTTT" },
                }}))
                .addShape(STRUCTURE_PIECE_CROWN, transpose(new String[][]{{
                    // TODO: 粘贴 crown 层数组
                }}))
                .addElement('B', ofBlock(GregTechAPI.sBlockCasings2, 15))  // 底座方块
                .addElement('T', ofBlock(GregTechAPI.sBlockCasings4, 1))   // 塔楼方块
                .addElement('C', ofBlock(GregTechAPI.sBlockCasings3, 2))   // 皇冠方块
                .addElement('H',
                    HatchElementBuilder.<TST_SegPlaza>builder()
                        .atLeast(InputBus, OutputBus, InputHatch, OutputHatch, Energy.or(ExoticEnergy))
                        .adder(TST_SegPlaza::addToMachineList)
                        .dot(1)
                        .casingIndex(48)
                        .buildAndChain(GregTechAPI.sBlockCasings2, 15))
                .build();
        }}
        return STRUCTURE_DEF;
    }}

    // -- 动态结构检查 --
    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {{
        repairMachine();
        mTowerLayers = 0;
        if (!checkPiece(STRUCTURE_PIECE_BASE, horizontalOff, verticalOff, depthOff))
            return false;
        // 向上检查塔楼层
        while (mTowerLayers < TOWER_MAX_LAYERS) {{
            int towerY = verticalOff + {len(base)} + mTowerLayers;
            if (!checkPiece(STRUCTURE_PIECE_TOWER,
                    horizontalOff - ({tower_w} - {max(sectioned['base'][0][0] if sectioned['base'] else '', key=len)}) // 2,
                    towerY,
                    depthOff - ({tower_w} - {max(sectioned['base'][0][0] if sectioned['base'] else '', key=len)}) // 2))
                break;
            mTowerLayers++;
        }}
        if (mTowerLayers < TOWER_MIN_LAYERS) return false;
        // 检查皇冠
        int crownY = verticalOff + {len(base)} + mTowerLayers;
        return checkPiece(STRUCTURE_PIECE_CROWN,
            horizontalOff, crownY, depthOff);
    }}

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {{
        buildPiece(STRUCTURE_PIECE_BASE, stackSize, hintsOnly, horizontalOff, verticalOff, depthOff);
        int layers = Math.min(TOWER_MAX_LAYERS,
            Math.max(TOWER_MIN_LAYERS, stackSize != null ? stackSize.stackSize : TOWER_MAX_LAYERS));
        for (int i = 0; i < layers; i++) {{
            int towerY = verticalOff + {len(base)} + i;
            buildPiece(STRUCTURE_PIECE_TOWER, stackSize, hintsOnly,
                horizontalOff, towerY, depthOff);
        }}
        int crownY = verticalOff + {len(base)} + layers;
        buildPiece(STRUCTURE_PIECE_CROWN, stackSize, hintsOnly,
            horizontalOff, crownY, depthOff);
    }}

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget,
            ISurvivalBuildEnvironment env) {{
        if (this.mMachine) return -1;
        return survivalBuildPiece(STRUCTURE_PIECE_BASE, stackSize,
            horizontalOff, verticalOff, depthOff, elementBudget, env, false, true);
    }}

    // -- Recipe Map --
    @Override
    public RecipeMap<?> getRecipeMap() {{
        return null; // TODO: 指定配方表
    }}
}}
'''
    return code


def main():
    print("正在生成赛格大厦多方块结构...\n")

    profile = load_profile()
    print(f"轮廓数据: {profile['height']}层, 最大宽度{profile['max_width']}")

    # 生成单件式形状数组
    layers, widths, max_w = generate_shape_array(profile)

    # 生成 Java String[][] 代码
    java_code = array_to_java_string(layers, "SEG_PLAZA_SHAPE")
    code_path = os.path.join(OUTPUT_DIR, 'seg_plaza_shape.java.txt')
    with open(code_path, 'w', encoding='utf-8') as f:
        f.write(java_code)
    print(f"Java 形状数组已保存到: {code_path}")

    # 生成3D可视化
    viz = generate_visualization(layers)
    viz_path = os.path.join(OUTPUT_DIR, 'seg_plaza_3d_preview.txt')
    with open(viz_path, 'w', encoding='utf-8') as f:
        f.write(viz)
    print(f"3D 预览已保存到: {viz_path}")

    # 打印可视化摘要
    print("\n" + "=" * 50)
    print("结构摘要:")
    print(f"  总尺寸: {max_w}W x {len(layers)}H x {max_w}D")
    print(f"  总方块数: {sum(widths[y] * widths[y] for y in range(len(widths)))}")

    # 计算不同字符的数量
    char_counts = {}
    for y, z_plane in enumerate(layers):
        for row in z_plane:
            for ch in row:
                if ch != ' ':
                    char_counts[ch] = char_counts.get(ch, 0) + 1
    print("  字符统计:", char_counts)

    # 打印部分预览
    print("\n底座 Y=1 (含控制器和 Hatch):")
    for row in layers[1]:
        print(f"  |{row.replace(' ', '.')}|")

    print("\n塔楼中部 Y=20:")
    for row in layers[20]:
        print(f"  |{row.replace(' ', '.')}|")

    print(f"\n皇冠顶部 Y={len(layers)-1}:")
    for row in layers[-1]:
        print(f"  |{row.replace(' ', '.')}|")

    # 生成区段式设计
    sectioned = generate_shape_array_sectioned(profile)
    print(f"\n区段式设计:")
    print(f"  底座: {len(sectioned['base'])}层")
    print(f"  塔楼: {sectioned['tower_count']}层, 标准宽度={sectioned['tower_width']}")
    print(f"  皇冠: {len(sectioned['crown'])}层")

    # 塔楼标准层
    print(f"\n塔楼标准层 ({sectioned['tower_width']}x{sectioned['tower_width']}):")
    for row in sectioned['tower_layer']:
        print(f"  |{row}|")

    # 保存区段式结构
    section_path = os.path.join(OUTPUT_DIR, 'seg_plaza_sectioned.java.txt')
    java_skeleton = generate_java_skeleton(sectioned)
    with open(section_path, 'w', encoding='utf-8') as f:
        f.write(java_skeleton)
    print(f"\nJava 代码骨架已保存到: {section_path}")

    # 可视化 Python 脚本（用于后续调试）
    viz_script = generate_viz_script(layers, widths)
    viz_script_path = os.path.join(OUTPUT_DIR, 'preview_shape.py')
    with open(viz_script_path, 'w', encoding='utf-8') as f:
        f.write(viz_script)
    print(f"可视化脚本已保存到: {viz_script_path}")


def generate_viz_script(layers, widths):
    """生成可独立运行的可视化脚本"""
    # 将 layers 序列化为 Python 字面量
    layers_repr = repr(layers)

    return f'''"""
赛格大厦多方块结构 - 交互式预览
用法: python tools/preview_shape.py
"""

layers = {layers_repr}

legend = {{
    'B': '底座方块 (sBlockCasings2:15)',
    'T': '塔楼方块 (sBlockCasings4:1)',
    'C': '皇冠方块 (sBlockCasings3:2)',
    'H': 'Hatch 位置',
    '~': '控制器',
    '-': '空气 (内部)',
    ' ': '跳过 (外部)',
}}

w = len(layers[0][0]) if layers else 0
d = len(layers[0]) if layers else 0
h = len(layers)

print(f"赛格大厦结构: {{w}}W x {{h}}H x {{d}}D")
print(f"图例:")
for k, v in legend.items():
    print(f"  {{k}} = {{v}}")

print("\\n=== 各层截面 ===")
for y in range(h - 1, -1, -1):
    print(f"\\nY={{y}}:")
    for z, row in enumerate(layers[y]):
        print(f"  Z{{z}}: |{{row.replace(' ', '.')}}|")

# 找控制器
for y, z_plane in enumerate(layers):
    for z, row in enumerate(z_plane):
        if '~' in row:
            x = row.index('~')
            print(f"\\n控制器偏移: horizontalOff={{x}}, verticalOff={{y}}, depthOff={{z}}")
'''


if __name__ == '__main__':
    main()
