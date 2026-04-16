"""
赛格大厦精细版多方块结构生成器
基于三张图片的详细分析，生成包含建筑细节的多方块结构。

关键特征：
- 裙楼：15×15 圆角方形（比塔楼宽，圆角半径3）
- 塔楼：八边形截面（11→9→9 弱退台，保持修长塔身）
- 每段之间有过渡层（阶梯式退台）
- 外立面竖向线条（边角用不同方块）
- 顶部皇冠有3-4级阶梯结构
- 最顶部有天线

用法: python tools/generate_refined.py
"""

import json
import os

OUTPUT_DIR = os.path.join(os.path.dirname(__file__))

STAGE1_H_FLOORS = {1, 2, 3, 6, 7}
STAGE2_EXTRA_H_FLOORS = {10, 11}
STAGE3_EXTRA_H_FLOORS = {30, 31, 90, 91}


# =====================================================================
# 形状辅助函数
# =====================================================================

def get_margin(z_local, width, shape_type, **kwargs):
    """获取某行的边距（每侧跳过的位置数）。
    - octagon: corner_cut 控制切角大小
    - rounded: corner_radius 控制圆角半径
    - square: 无边距
    """
    if shape_type == 'octagon':
        c = kwargs.get('corner_cut', 2)
        if z_local < c:
            return c - z_local
        elif z_local >= width - c:
            return z_local - (width - c - 1)
        return 0
    elif shape_type == 'rounded':
        r = kwargs.get('corner_radius', 3)
        if z_local < r:
            return r - z_local
        elif z_local >= width - r:
            return z_local - (width - r - 1)
        return 0
    return 0  # square


def in_shape(x_local, z_local, width, shape_type, **kwargs):
    """检查 (x_local, z_local) 是否在形状内。"""
    if not (0 <= x_local < width and 0 <= z_local < width):
        return False
    m = get_margin(z_local, width, shape_type, **kwargs)
    return m <= x_local < width - m


def on_boundary(x_local, z_local, width, shape_type, **kwargs):
    """检查 (x_local, z_local) 是否在形状边界上（至少一个邻居在形状外）。"""
    if not in_shape(x_local, z_local, width, shape_type, **kwargs):
        return False
    for dx, dz in [(1, 0), (-1, 0), (0, 1), (0, -1)]:
        nx, nz = x_local + dx, z_local + dz
        if not in_shape(nx, nz, width, shape_type, **kwargs):
            return True
    return False


# =====================================================================
# 建筑分段定义（基于图片分析手工调整的比例）
# =====================================================================

# 字符定义:
# P = 裙楼外墙 (Podium)
# p = 裙楼内墙 / 楼板
# D = 裙楼大门口
# H = Hatch 位置
# ~ = 控制器
# T = 塔楼外墙 (Tower)
# V = 竖向装饰线条 (Vertical line) - 塔楼边角和中间
# S = 退台过渡层 (Setback)
# K = 皇冠方块 (K=Krone/Crown)
# A = 天线 / 顶部桅杆 (Antenna)
# - = 空气（内部空间）
# (space) = 跳过（结构外）

BUILDING_SPEC = {
    'max_width': 18,  # 裙楼宽度
    'max_depth': 18,  # 裙楼深度
    'target_height': 220,
    'sections': [
        # ============================================================
        # 赛格大厦 220 层结构 - 按实际建筑比例分配
        # ============================================================
        # 裙楼 6层/71层 ≈ 8.5% → 20层 (含地板+入口+主体+顶)
        # 退台1: 1层
        # 塔楼一段: 59层 (约27%)
        # 退台2: 1层
        # 塔楼二段: 52层 (约24%)
        # 退台3: 1层
        # 塔楼三段: 48层 (约22%)
        # 皇冠: 14层 (约6%)
        # 天线: 24层 (约11%)
        # 合计: 20+1+59+1+52+1+48+14+24 = 220
        # ============================================================

        # ---- 裙楼 (Podium) ---- 20层, 18x18 圆角方形
        {'name': 'podium_floor',    'count': 1,  'width': 18, 'shape': 'rounded', 'corner_radius': 4,
         'chars': {'wall': 'P', 'edge': 'P'}},
        {'name': 'podium_entrance', 'count': 1,  'width': 18, 'shape': 'rounded', 'corner_radius': 4,
         'chars': {'wall': 'P', 'edge': 'P', 'special': 'entrance'}},
        {'name': 'podium_mid',      'count': 17, 'width': 18, 'shape': 'rounded', 'corner_radius': 4,
         'chars': {'wall': 'P', 'edge': 'P'}},
        {'name': 'podium_top',      'count': 1,  'width': 18, 'shape': 'rounded', 'corner_radius': 4,
         'chars': {'wall': 'P', 'edge': 'P'}},

        # ---- 退台过渡 1 (Setback 1) ---- 1层, 18→11
        {'name': 'setback_1', 'count': 1, 'width': 18, 'shape': 'rounded', 'corner_radius': 4,
         'chars': {'wall': 'S', 'edge': 'S', 'inner': 11, 'inner_shape': 'octagon', 'inner_corner_cut': 2}},

        # ---- 塔楼第一段 (Tower Section 1) ---- 59层, 11x11 八边形
        {'name': 'tower1_bot', 'count': 1,  'width': 11, 'shape': 'octagon', 'corner_cut': 2,
         'chars': {'wall': 'T', 'edge': 'V'}},
        {'name': 'tower1_mid', 'count': 57, 'width': 11, 'shape': 'octagon', 'corner_cut': 2,
         'chars': {'wall': 'T', 'edge': 'V'}},
        {'name': 'tower1_top', 'count': 1,  'width': 11, 'shape': 'octagon', 'corner_cut': 2,
         'chars': {'wall': 'T', 'edge': 'V'}},

        # ---- 退台过渡 2 (Setback 2) ---- 1层, 11→9
        {'name': 'setback_2', 'count': 1, 'width': 11, 'shape': 'octagon', 'corner_cut': 2,
         'chars': {'wall': 'S', 'edge': 'S', 'inner': 9, 'inner_shape': 'octagon', 'inner_corner_cut': 2}},

        # ---- 塔楼第二段 (Tower Section 2) ---- 52层, 9x9 八边形
        {'name': 'tower2_bot', 'count': 1,  'width': 9, 'shape': 'octagon', 'corner_cut': 2,
         'chars': {'wall': 'T', 'edge': 'V'}},
        {'name': 'tower2_mid', 'count': 50, 'width': 9, 'shape': 'octagon', 'corner_cut': 2,
         'chars': {'wall': 'T', 'edge': 'V'}},
        {'name': 'tower2_top', 'count': 1,  'width': 9, 'shape': 'octagon', 'corner_cut': 2,
         'chars': {'wall': 'T', 'edge': 'V'}},

        # ---- 退台过渡 3 (Service Belt) ---- 1层, 同宽设备带 9
        {'name': 'setback_3', 'count': 1, 'width': 9, 'shape': 'octagon', 'corner_cut': 2,
         'chars': {'wall': 'S', 'edge': 'S'}},

        # ---- 塔楼第三段 (Tower Section 3) ---- 48层, 9x9 八边形
        {'name': 'tower3_bot', 'count': 1,  'width': 9, 'shape': 'octagon', 'corner_cut': 2,
         'chars': {'wall': 'T', 'edge': 'V'}},
        {'name': 'tower3_mid', 'count': 46, 'width': 9, 'shape': 'octagon', 'corner_cut': 2,
         'chars': {'wall': 'T', 'edge': 'V'}},
        {'name': 'tower3_top', 'count': 1,  'width': 9, 'shape': 'octagon', 'corner_cut': 2,
         'chars': {'wall': 'T', 'edge': 'V'}},

        # ---- 皇冠 (Crown) ---- 14层, 更宽的设备层 + 阶梯式冠部
        {'name': 'crown_base',  'count': 4, 'width': 11, 'shape': 'octagon', 'corner_cut': 2,
         'chars': {'wall': 'K', 'edge': 'K'}},
        {'name': 'crown_step1', 'count': 3, 'width': 9, 'shape': 'octagon', 'corner_cut': 2,
         'chars': {'wall': 'K', 'edge': 'K'}},
        {'name': 'crown_step2', 'count': 3, 'width': 7, 'shape': 'octagon', 'corner_cut': 2,
         'chars': {'wall': 'K', 'edge': 'K'}},
        {'name': 'crown_step3', 'count': 2, 'width': 5, 'shape': 'octagon', 'corner_cut': 1,
         'chars': {'wall': 'K', 'edge': 'K'}},
        {'name': 'crown_step4', 'count': 1, 'width': 5, 'shape': 'octagon', 'corner_cut': 1,
         'chars': {'wall': 'K', 'edge': 'K'}},
        {'name': 'crown_top',   'count': 1, 'width': 5, 'shape': 'octagon', 'corner_cut': 1,
         'chars': {'wall': 'K', 'edge': 'K'}},

        # ---- 天线 (Antenna) ---- 24层, 顶部双桅杆
        {'name': 'antenna_base', 'count': 3,  'width': 5,
         'chars': {'wall': 'A', 'edge': 'A', 'special': 'twin_mast_base'}},
        {'name': 'antenna_mid',  'count': 20, 'width': 3,
         'chars': {'wall': 'A', 'edge': 'A', 'special': 'twin_masts'}},
        {'name': 'antenna_tip',  'count': 1,  'width': 3,
         'chars': {'wall': 'A', 'edge': 'A', 'special': 'twin_masts'}},
    ],
}


def generate_layer(spec, max_w, is_hollow=True):
    """
    生成单层的 Z 平面 (list of strings)。
    支持圆角方形 (rounded)、八边形 (octagon) 和正方形 (square) 截面。
    """
    w = spec['width']
    shape_type = spec.get('shape', 'square')
    shape_kwargs = {}
    if shape_type == 'octagon':
        shape_kwargs['corner_cut'] = spec.get('corner_cut', 2)
    elif shape_type == 'rounded':
        shape_kwargs['corner_radius'] = spec.get('corner_radius', 3)

    chars = spec.get('chars', {})
    is_solid = chars.get('solid', False)
    special = chars.get('special', None)
    inner_w = chars.get('inner', None)

    offset = (max_w - w) // 2
    wall_char = chars.get('wall', 'T')
    edge_char = chars.get('edge', wall_char)

    z_plane = []

    for z in range(max_w):
        row = ""
        for x in range(max_w):
            lx = x - offset
            lz = z - offset

            if not in_shape(lx, lz, w, shape_type, **shape_kwargs):
                row += " "
                continue

            # 退台层特殊处理：外层实心挑檐 + 内层塔楼墙壁
            if inner_w is not None:
                inner_shape = spec.get('inner_shape', 'square')
                inner_kwargs = {}
                if inner_shape == 'octagon':
                    inner_kwargs['corner_cut'] = spec.get('inner_corner_cut', 2)
                elif inner_shape == 'rounded':
                    inner_kwargs['corner_radius'] = spec.get('inner_corner_radius', 3)

                inner_offset = (w - inner_w) // 2
                ilx = lx - inner_offset
                ilz = lz - inner_offset

                if in_shape(ilx, ilz, inner_w, inner_shape, **inner_kwargs):
                    # 内层塔楼区域
                    if on_boundary(ilx, ilz, inner_w, inner_shape, **inner_kwargs):
                        row += 'T'  # 内层塔楼墙壁
                    else:
                        row += '-'  # 内层塔楼内部空气
                else:
                    # 外层平台（实心挑檐）
                    row += edge_char
                continue

            if is_solid:
                # 实心层（天线）
                row += wall_char
            elif on_boundary(lx, lz, w, shape_type, **shape_kwargs):
                # 边界 - 使用竖向线条字符或普通墙壁字符
                # 检查是否在角顶（边距变化的行）
                m_here = get_margin(lz, w, shape_type, **shape_kwargs)
                m_above = get_margin(lz - 1, w, shape_type, **shape_kwargs) if lz > 0 else -1
                m_below = get_margin(lz + 1, w, shape_type, **shape_kwargs) if lz < w - 1 else -1

                # 角顶：边距变化的行上的首尾位置
                at_corner_vertex = (
                    (m_here != m_above or m_here != m_below)
                    and (lx == m_here or lx == w - m_here - 1)
                )

                if at_corner_vertex:
                    row += edge_char
                elif lx % 2 == 0:
                    # 竖向线条每2格一根
                    row += edge_char
                else:
                    row += wall_char
            else:
                # 内部空气
                if is_hollow:
                    row += "-"
                else:
                    row += wall_char

        z_plane.append(row)

    # 特殊层处理
    if special == 'entrance':
        z_plane = add_entrance(z_plane, max_w, w, wall_char, shape_type, shape_kwargs)
    elif special == 'twin_mast_base':
        z_plane = build_twin_mast_layer(max_w, wall_char, with_crossbar=True)
    elif special == 'twin_masts':
        z_plane = build_twin_mast_layer(max_w, wall_char, with_crossbar=False)

    return z_plane


def add_entrance(z_plane, max_w, section_w, wall_char, shape_type, shape_kwargs):
    """在裙楼正面添加入口效果"""
    center = max_w // 2

    # 找到正面（Z 最小的有实际内容的行）
    # 对于圆角/八边形，第一行可能有边距
    for z in range(max_w):
        if center < len(z_plane[z]) and z_plane[z][center] != ' ':
            row = list(z_plane[z])
            for x in range(center - 2, center + 3):
                if 0 <= x < max_w and row[x] != ' ':
                    row[x] = 'D'  # Door
            z_plane[z] = "".join(row)
            break

    return z_plane


def build_twin_mast_layer(max_w, wall_char, with_crossbar=False):
    """生成顶部双桅杆截面。"""
    z_plane = [" " * max_w for _ in range(max_w)]
    center = max_w // 2
    mast_columns = [center - 1, center + 1]
    mast_rows = [center - 1, center, center + 1] if with_crossbar else [center]

    for z in mast_rows:
        row = list(z_plane[z])
        for x in mast_columns:
            row[x] = wall_char
        if with_crossbar and z == center + 1:
            for x in range(mast_columns[0], mast_columns[1] + 1):
                row[x] = wall_char
        z_plane[z] = "".join(row)

    return z_plane


def copy_layer(z_plane):
    """复制单层，避免多个楼层共享同一个 list 对象。"""
    return list(z_plane)


def clone_layers(layers):
    """复制层列表，避免阶段切片互相污染。"""
    return [copy_layer(layer) for layer in layers]


def find_front_row_index(z_plane):
    """找到当前层最前方可见的一行。"""
    if not z_plane:
        return None

    center = len(z_plane[0]) // 2
    for z, row in enumerate(z_plane):
        if center < len(row) and row[center] != ' ':
            return z
    return None


def replace_row_chars(z_plane, z, updates, protected=None):
    """在指定行上按坐标写入字符。"""
    if z is None or not (0 <= z < len(z_plane)):
        return z_plane

    protected = protected or set()
    row = list(z_plane[z])
    for x, ch in updates.items():
        if 0 <= x < len(row) and row[x] not in protected and row[x] != ' ':
            row[x] = ch
    z_plane[z] = "".join(row)
    return z_plane


def add_controller(layers, max_w):
    """只放置控制器，不在这里写入 H 位。"""
    controller_y = 1

    if not (0 <= controller_y < len(layers)):
        return layers

    center = max_w // 2
    z = find_front_row_index(layers[controller_y])
    replace_row_chars(layers[controller_y], z, {center: '~'}, protected={' '})

    return layers


def mark_hatches_on_front_row(z_plane, xs):
    """在最前排立面按显式坐标写入 H。"""
    z = find_front_row_index(z_plane)
    return replace_row_chars(z_plane, z, {x: 'H' for x in xs}, protected={' ', '~', 'D'})


def apply_hatch_rules(layers, floor_rules):
    """将楼层 -> 前排 X 坐标规则写入到结构中。"""
    for y, xs in floor_rules.items():
        if 0 <= y < len(layers):
            layers[y] = mark_hatches_on_front_row(copy_layer(layers[y]), xs)
    return layers


def merge_hatch_rules(*rule_sets):
    """合并多个阶段规则，后者可以在同层追加位置。"""
    merged = {}
    for rules in rule_sets:
        for y, xs in rules.items():
            merged.setdefault(y, [])
            for x in xs:
                if x not in merged[y]:
                    merged[y].append(x)
    return merged


def podium_entrance_hatch_positions():
    """Stage I 控制器层：保留 D~D 中轴，只开放基础 podium H 位。"""
    return [4, 5, 12, 13]


def podium_service_hatch_positions():
    """Stage I / II podium 设备层：小规模、对称的前脸 H 位。"""
    return [4, 5, 12, 13]


def tower11_equipment_hatch_positions():
    """Stage III 塔楼第一段设备层：比 podium 更克制，但仍保留 4 个设备位。"""
    return [5, 6, 10, 11]


def tower9_facade_band_hatch_positions():
    """Stage III 塔楼第二段立面带：仅保留少量 facade-band H 位。"""
    return [6, 10]


def build_stage1_hatch_rules():
    """Stage I 只开放基础 podium H 位和少量底部设备区 H 位。"""
    rules = {}
    for floor in sorted(STAGE1_H_FLOORS):
        if floor == 1:
            rules[floor] = podium_entrance_hatch_positions()
        else:
            rules[floor] = podium_service_hatch_positions()
    return rules


def build_stage2_podium_hatch_rules():
    """Stage II 在 Stage I 基础上追加 podium 主设备层 H 位。"""
    return {floor: podium_service_hatch_positions() for floor in sorted(STAGE2_EXTRA_H_FLOORS)}


def build_stage3_tower_hatch_rules():
    """Stage III 在 Stage II 基础上追加少量 tower 设备层 / 立面带 H 位。"""
    return {
        30: tower11_equipment_hatch_positions(),
        31: tower11_equipment_hatch_positions(),
        90: tower9_facade_band_hatch_positions(),
        91: tower9_facade_band_hatch_positions(),
    }


def add_floor_slabs(layers, max_w, section_widths):
    """不修改结构，仅标记"""
    return layers


def generate_full_structure():
    """生成完整的赛格大厦结构"""
    spec = BUILDING_SPEC
    max_w = spec['max_width']
    sections = spec['sections']

    layers = []
    section_widths = []

    for section in sections:
        count = section['count']
        template_layer = generate_layer(section, max_w)
        for _ in range(count):
            layers.append(copy_layer(template_layer))
            section_widths.append(section['width'])

    layers = add_controller(layers, max_w)

    return layers, section_widths, max_w


def identify_refined_sections(section_widths, spec):
    """根据 spec 标记每层的区段"""
    labels = []
    for section in spec['sections']:
        count = section['count']
        name = section['name']
        for _ in range(count):
            if name.startswith('podium'):
                labels.append('裙楼')
            elif name.startswith('setback'):
                labels.append('退台')
            elif name.startswith('tower'):
                labels.append('塔楼')
            elif name.startswith('crown'):
                labels.append('皇冠')
            elif name.startswith('antenna'):
                labels.append('天线')
            else:
                labels.append('其他')
    return labels


def generate_ascii_preview(layers, section_widths, max_w, labels):
    """生成精细的 ASCII 预览（压缩版，只显示宽度变化的层）"""
    lines = []
    n = len(layers)

    lines.append("=" * (max_w + 24))
    lines.append("赛格大厦 - 220层精细版多方块结构预览")
    lines.append("=" * (max_w + 24))
    lines.append("裙楼: 15x15 圆角方形 (corner_radius=3)")
    lines.append("塔楼: 八边形截面 (11/9/7 递减退台)")
    lines.append("")

    prev_w = None
    prev_label = None
    skip_count = 0

    for y in range(n - 1, -1, -1):
        w = section_widths[y]
        label = labels[y]
        is_section_boundary = (label != prev_label) if prev_label is not None else False
        width_changed = (w != prev_w) if prev_w is not None else False

        if is_section_boundary or width_changed or y == n - 1 or y == 0:
            if skip_count > 0:
                lines.append(f"     ... ({skip_count}层省略, W={prev_w}) ...")
                skip_count = 0
            # 显示实际截面
            z_plane = layers[y]
            # 取中间行显示宽度
            mid_z = max_w // 2
            actual_width = len(z_plane[mid_z].replace(' ', ''))
            line = z_plane[mid_z].replace(' ', '.')
            lines.append(f"Y={y:3d} [{label}] |{line}| W={w}")
        else:
            skip_count += 1

        prev_w = w
        prev_label = label

    if skip_count > 0:
        lines.append(f"     ... ({skip_count}层省略, W={prev_w}) ...")

    lines.append("")
    lines.append(f"总尺寸: {max_w}W x {n}H x {max_w}D")

    # 统计各区段
    section_stats = {}
    for label, w in zip(labels, section_widths):
        if label not in section_stats:
            section_stats[label] = {'layers': 0, 'widths': set()}
        section_stats[label]['layers'] += 1
        section_stats[label]['widths'].add(w)

    for name, info in section_stats.items():
        widths_str = ','.join(str(w) for w in sorted(info['widths']))
        lines.append(f"  {name}: {info['layers']}层, 宽度={widths_str}")

    return "\n".join(lines)


def generate_3d_preview(layers, section_widths, max_w, labels):
    """生成全部 220 层的完整 3D 截面预览"""
    lines = []
    n = len(layers)

    lines.append("=" * 60)
    lines.append("赛格大厦 220层 - 完整逐层截面预览")
    lines.append("裙楼: 15x15 圆角 | 塔楼: 八边形 11/9/7")
    lines.append("=" * 60)

    lines.append("")
    lines.append("图例:")
    lines.append("  P=裙楼  T=塔楼  V=竖向线条  S=退台过渡")
    lines.append("  K=皇冠  A=天线  H=Hatch  ~=控制器")
    lines.append("  D=大门  -=空气(内部)  .=跳过(外部)")
    lines.append("")

    for y in range(n - 1, -1, -1):
        z_plane = layers[y]
        lines.append(f"--- Y={y:3d} [{labels[y]}] W={section_widths[y]} ---")
        for z, row in enumerate(z_plane):
            display = row.replace(' ', '.')
            if display.strip('.'):
                lines.append(f"  Z{z:2d}: |{display}|")
        lines.append("")

    # 找控制器位置
    for y, z_plane in enumerate(layers):
        for z, row in enumerate(z_plane):
            if '~' in row:
                x = row.index('~')
                lines.append(f"控制器位置: X={x}, Y={y}, Z={z}")
                lines.append(f"偏移量: horizontalOff={x}, verticalOff={y}, depthOff={z}")

    return "\n".join(lines)


def generate_java_code(layers, section_widths, max_w):
    """生成完整的 Java String[][] 代码"""
    lines = []
    lines.append('// 赛格大厦精细版结构形状数组')
    lines.append(f'// 尺寸: {max_w}W x {len(layers)}H x {max_w}D')
    lines.append(f'// 裙楼: 15x15 圆角 | 塔楼: 八边形 11/9/7')
    lines.append('')
    lines.append(f'private static final String[][] SEG_PLAZA_SHAPE = transpose(new String[][]{{')

    for y, z_plane in enumerate(layers):
        lines.append(f'    // Y={y} ({section_widths[y]}x{section_widths[y]})')
        lines.append('    {')
        for z, row in enumerate(z_plane):
            lines.append(f'        "{row}",')
        lines.append('    },')

    lines.append('});')
    return '\n'.join(lines)


def generate_java_class(layers, section_widths, max_w):
    """生成完整的 Java 类代码（仅供参考，不直接使用）"""
    h_off = v_off = d_off = 0
    for y, z_plane in enumerate(layers):
        for z, row in enumerate(z_plane):
            if '~' in row:
                h_off = row.index('~')
                v_off = y
                d_off = z

    shape_lines = []
    for y, z_plane in enumerate(layers):
        shape_lines.append(f'        // Y={y} ({section_widths[y]}x{section_widths[y]})')
        shape_lines.append('        {')
        for z, row in enumerate(z_plane):
            shape_lines.append(f'            "{row}",')
        shape_lines.append('        },')

    shape_code = '\n'.join(shape_lines)

    code = f'''// 赛格大厦 (SEG Plaza) 精细版多方块机器
// 裙楼: 15x15 圆角 | 塔楼: 八边形 11/9/7
// 总尺寸: {max_w}W x {len(layers)}H x {max_w}D

package com.andgatech.AHTech.common.machine;

public class TST_SegPlaza {{
    private static final int horizontalOff = {h_off};
    private static final int verticalOff = {v_off};
    private static final int depthOff = {d_off};
}}
'''
    return code


def main():
    print("=" * 60)
    print("赛格大厦精细版多方块结构生成器")
    print("裙楼: 15x15 圆角 | 塔楼: 八边形 11/9/7")
    print("=" * 60)
    print()

    # 生成结构
    layers, section_widths, max_w = generate_full_structure()
    n = len(layers)

    # 标记区段
    labels = identify_refined_sections(section_widths, BUILDING_SPEC)

    print(f"总层数: {n}")
    print(f"最大宽度: {max_w}")
    print()

    # --- ASCII 轮廓预览 ---
    preview = generate_ascii_preview(layers, section_widths, max_w, labels)
    print(preview)

    preview_path = os.path.join(OUTPUT_DIR, 'refined_preview.txt')
    with open(preview_path, 'w', encoding='utf-8') as f:
        f.write(preview)

    # --- 3D 逐层预览 ---
    viz = generate_3d_preview(layers, section_widths, max_w, labels)
    viz_path = os.path.join(OUTPUT_DIR, 'refined_3d_preview.txt')
    with open(viz_path, 'w', encoding='utf-8') as f:
        f.write(viz)
    print(f"\n3D 预览已保存到: {viz_path}")

    # --- Java 形状数组 ---
    java_shape = generate_java_code(layers, section_widths, max_w)
    shape_path = os.path.join(OUTPUT_DIR, 'refined_shape.java.txt')
    with open(shape_path, 'w', encoding='utf-8') as f:
        f.write(java_shape)
    print(f"Java 形状数组已保存到: {shape_path}")

    # --- 三级形状常量类 ---
    shapes_class = generate_shapes_class()
    shapes_path = os.path.join(OUTPUT_DIR, '..', 'src', 'main', 'java',
                               'com', 'andgatech', 'AHTech', 'common', 'machine',
                               'ElectronicsMarketShapes.java')
    with open(shapes_path, 'w', encoding='utf-8') as f:
        f.write(shapes_class)
    print(f"\n三级形状常量类已保存到: {shapes_path}")

    # --- 打印关键层截面 ---
    print("\n" + "=" * 60)
    print("关键层截面预览:")
    print("=" * 60)

    # Y 索引: 0~19=裙楼(20层), 20=退台1, 21~79=塔楼1(59层),
    # 80=退台2, 81~132=塔楼2(52层), 133=退台3, 134~181=塔楼3(48层),
    # 182~195=皇冠(14层), 196~219=天线(24层)
    key_layers = [
        (0, "裙楼底层 (圆角15x15)"),
        (1, "裙楼入口层(含控制器)"),
        (19, "裙楼顶层"),
        (20, "退台过渡1 (15→11)"),
        (21, "塔楼第一段底(11x11八边形)"),
        (50, "塔楼第一段中(11x11八边形)"),
        (79, "塔楼第一段顶(11x11八边形)"),
        (80, "退台过渡2 (11→9)"),
        (81, "塔楼第二段底(9x9八边形)"),
        (106, "塔楼第二段中(9x9八边形)"),
        (132, "塔楼第二段顶(9x9八边形)"),
        (133, "退台过渡3 (9→7)"),
        (134, "塔楼第三段底(7x7八边形)"),
        (157, "塔楼第三段中(7x7八边形)"),
        (181, "塔楼第三段顶(7x7八边形)"),
        (182, "皇冠底座(W=9八边形)"),
        (187, "皇冠阶梯2(W=9八边形)"),
        (195, "皇冠顶(W=3)"),
        (196, "天线底座(W=3)"),
        (205, "天线中段(W=1)"),
        (219, "天线尖(W=1)"),
    ]

    for y, desc in key_layers:
        if y < n:
            print(f"\n  Y={y} [{desc}] W={section_widths[y]}:")
            z_plane = layers[y]
            for z, row in enumerate(z_plane):
                display = row.replace(' ', '.')
                if display.strip('.'):
                    print(f"    Z{z:2d}: |{display}|")

    # --- 字符统计 ---
    char_counts = {}
    for z_plane in layers:
        for row in z_plane:
            for ch in row:
                if ch != ' ':
                    char_counts[ch] = char_counts.get(ch, 0) + 1

    print(f"\n字符统计:")
    char_desc = {
        'P': '裙楼外墙', 'D': '大门', 'T': '塔楼外墙',
        'V': '竖向线条', 'S': '退台过渡', 'K': '皇冠',
        'A': '天线', 'H': 'Hatch', '~': '控制器', '-': '空气(内部)'
    }
    for ch in sorted(char_counts.keys()):
        desc = char_desc.get(ch, '未知')
        print(f"  {ch}: {char_counts[ch]:4d} ({desc})")

    print(f"\n总方块数(非空气): {sum(v for k, v in char_counts.items() if k != '-')}")

    # --- 控制器位置 ---
    for y, z_plane in enumerate(layers):
        for z, row in enumerate(z_plane):
            if '~' in row:
                x = row.index('~')
                print(f"\n控制器位置: X={x}, Y={y}, Z={z}")
                print(f"偏移量: horizontalOff={x}, verticalOff={y}, depthOff={z}")


def generate_three_stage_shapes():
    """生成三个阶段的独立 Java 形状数组文件。"""
    layers, section_widths, max_w = generate_full_structure()
    stage1_rules = build_stage1_hatch_rules()
    stage2_rules = merge_hatch_rules(stage1_rules, build_stage2_podium_hatch_rules())
    stage3_rules = merge_hatch_rules(stage2_rules, build_stage3_tower_hatch_rules())

    # === Stage I: 裙楼 20 层 ===
    stage1_layers = apply_hatch_rules(clone_layers(layers[0:20]), stage1_rules)
    stage1_widths = section_widths[0:20]

    # === Stage II: 裙楼(20) + 退台1(1) + 4层塔楼(4) + 封顶(1) = 26 层 ===
    # layers[0:20] = 裙楼
    # layers[20]   = 退台1 (15→11)
    # layers[21]   = 塔楼一段底 (11x11 八边形)
    # layers[22]   = 塔楼一段第2层
    # layers[23]   = 塔楼一段第3层
    # layers[24]   = 塔楼一段第4层 (封顶前最后一层)
    # + 1层封顶: 实心板
    stage2_layers = clone_layers(layers[0:25])

    # 封顶层: 复制 layers[24] (11x11 八边形), 内部空气替换为实心
    cap_layer = []
    for row in layers[24]:
        cap_row = row.replace('-', 'V')
        cap_layer.append(cap_row)
    stage2_layers.append(cap_layer)
    stage2_layers = apply_hatch_rules(stage2_layers, stage2_rules)

    stage2_widths = list(section_widths[0:25]) + [section_widths[24]]

    # === Stage III: 全部 220 层 ===
    stage3_layers = apply_hatch_rules(clone_layers(layers), stage3_rules)
    stage3_widths = section_widths

    print("\n三级形状切片:")
    print(f"  Stage I:  {len(stage1_layers)} 层 (裙楼 15x15 圆角)")
    print(f"  Stage II: {len(stage2_layers)} 层 (裙楼+退台+4层八边形塔楼+封顶)")
    print(f"  Stage III: {len(stage3_layers)} 层 (完整大厦)")

    return stage1_layers, stage1_widths, stage2_layers, stage2_widths, stage3_layers, stage3_widths, max_w


def layers_to_java_field(layers, field_name):
    """将层列表转为 Java static final String[][] 字段代码。"""
    lines = []
    lines.append(f'    // spotless:off')
    lines.append(f'    public static final String[][] {field_name} = new String[][]{{')
    for y, z_plane in enumerate(layers):
        lines.append(f'        // Y={y}')
        lines.append('        {')
        for row in z_plane:
            lines.append(f'            "{row}",')
        lines.append('        },')
    lines.append('    };')
    lines.append(f'    // spotless:on')
    return '\n'.join(lines)


def generate_shapes_class():
    """生成 ElectronicsMarketShapes.java 完整文件。"""
    (stage1_layers, stage1_widths,
     stage2_layers, stage2_widths,
     stage3_layers, stage3_widths, max_w) = generate_three_stage_shapes()

    s1_code = layers_to_java_field(stage1_layers, 'STAGE1_SHAPE')
    s2_code = layers_to_java_field(stage2_layers, 'STAGE2_SHAPE')
    s3_code = layers_to_java_field(stage3_layers, 'STAGE3_SHAPE')

    code = f'''// 赛格大厦三级形状数组 - 由 tools/generate_refined.py 自动生成
// Stage I:  {len(stage1_layers)}层 (裙楼 15x15 圆角)
// Stage II: {len(stage2_layers)}层 (裙楼+退台+4层八边形塔楼+封顶)
// Stage III: {len(stage3_layers)}层 (完整大厦 裙楼15x15圆角 + 八边形塔楼11/9/7)
// 注意: 使用前需用 StructureUtility.transpose() 包裹

package com.andgatech.AHTech.common.machine;

public class ElectronicsMarketShapes {{
    private ElectronicsMarketShapes() {{}}

{s1_code}

{s2_code}

{s3_code}
}}
'''
    return code


if __name__ == '__main__':
    main()
