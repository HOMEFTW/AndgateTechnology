"""
分析赛格大厦图片，提取建筑轮廓和分段信息。
输出每层的归一化宽度，用于后续生成 GTNH 多方块结构。

用法: python tools/analyze_building.py
"""

from PIL import Image
import numpy as np
import os
import json

IMG_PATH = os.path.join(os.path.dirname(__file__), '..', '赛格大厦.jpeg')
OUTPUT_PATH = os.path.join(os.path.dirname(__file__), 'building_profile.json')
PREVIEW_PATH = os.path.join(os.path.dirname(__file__), 'building_preview.txt')

# === 参数 ===
TARGET_MAX_WIDTH = 11     # Minecraft 中最大宽度（方块数，奇数）
TARGET_MAX_HEIGHT = 40    # Minecraft 中最大高度（方块数）
MIN_BUILDING_RATIO = 0.3  # 建筑占图片高度的最小比例


def analyze(path):
    img = Image.open(path).convert('RGB')
    W, H = img.size
    print(f"图片尺寸: {W}x{H}")

    arr = np.array(img, dtype=np.float64)  # shape (H, W, 3)

    # --- Step 1: 找到建筑的水平范围 ---
    # 对每一列计算垂直方向的梯度（色调变化），建筑区域梯度较大
    gray = np.mean(arr, axis=2)  # (H, W)

    # 用 Sobel 近似做垂直梯度
    grad_y = np.abs(np.diff(gray, axis=0, n=1))  # (H-1, W)
    col_activity = np.sum(grad_y, axis=0)  # (W,)

    # 找到活动最集中的列范围（建筑所在列）
    threshold_col = np.max(col_activity) * 0.15
    active_cols = np.where(col_activity > threshold_col)[0]
    if len(active_cols) == 0:
        print("未能检测到建筑列范围，使用中心区域")
        building_left = W // 4
        building_right = 3 * W // 4
    else:
        # 找最大连续段
        building_left = active_cols[0]
        building_right = active_cols[-1]

    print(f"建筑水平范围: X={building_left}~{building_right} (宽度 {building_right - building_left}px)")

    # --- Step 2: 找到建筑的垂直范围 ---
    row_activity = np.sum(grad_y, axis=1)  # (H-1,)
    threshold_row = np.max(row_activity) * 0.10
    active_rows = np.where(row_activity > threshold_row)[0]
    if len(active_rows) == 0:
        building_top = 0
        building_bottom = H - 1
    else:
        building_top = active_rows[0]
        building_bottom = active_rows[-1]

    print(f"建筑垂直范围: Y={building_top}~{building_bottom} (高度 {building_bottom - building_top}px)")

    # --- Step 3: 在建筑范围内，逐行检测边缘 ---
    # 裁剪到建筑区域
    crop_gray = gray[building_top:building_bottom+1, building_left:building_right+1]
    crop_h, crop_w = crop_gray.shape

    # 对每行计算水平梯度
    grad_x = np.abs(np.diff(crop_gray, axis=1, n=1))  # (crop_h, crop_w-1)

    layer_widths_px = []
    layer_centers_px = []

    # 采样步长：将建筑高度映射到 TARGET_MAX_HEIGHT 层
    sample_step = max(1, crop_h / TARGET_MAX_HEIGHT)
    num_samples = min(TARGET_MAX_HEIGHT, crop_h)

    for i in range(num_samples):
        row_idx = int(i * sample_step)
        if row_idx >= crop_h:
            row_idx = crop_h - 1

        row_grad = grad_x[row_idx]

        # 找梯度较大的区域（建筑边缘）
        grad_threshold = np.max(row_grad) * 0.2
        edge_cols = np.where(row_grad > grad_threshold)[0]

        if len(edge_cols) < 2:
            # 没有明显边缘，用上一层的宽度
            if layer_widths_px:
                layer_widths_px.append(layer_widths_px[-1])
                layer_centers_px.append(layer_centers_px[-1])
            else:
                layer_widths_px.append(crop_w // 2)
                layer_centers_px.append(crop_w // 2)
            continue

        left_edge = edge_cols[0]
        right_edge = edge_cols[-1]
        width = right_edge - left_edge
        center = (left_edge + right_edge) // 2

        layer_widths_px.append(width)
        layer_centers_px.append(center)

    if not layer_widths_px:
        print("错误: 无法提取建筑轮廓")
        return None

    # --- Step 4: 归一化到 Minecraft 方块尺寸 ---
    # 图片中 Y=0 是顶部（建筑顶部），需要翻转使 Y=0 = 底座
    layer_widths_px.reverse()

    max_px_width = max(layer_widths_px)
    scale = (TARGET_MAX_WIDTH - 2) / max(max_px_width, 1)

    widths = [max(1, round(w * scale)) for w in layer_widths_px]

    # 确保所有宽度为奇数（对称）
    widths = [w if w % 2 == 1 else w + 1 for w in widths]

    # 平滑处理（移动平均）
    smoothed = list(widths)
    for _ in range(3):
        tmp = list(smoothed)
        for i in range(1, len(tmp) - 1):
            tmp[i] = round((smoothed[i-1] + smoothed[i] * 2 + smoothed[i+1]) / 4)
            tmp[i] = max(1, tmp[i])
        # 重新确保奇数
        smoothed = [w if w % 2 == 1 else w + 1 for w in tmp]

    # 调整层数到目标高度
    if len(smoothed) > TARGET_MAX_HEIGHT:
        step = len(smoothed) / TARGET_MAX_HEIGHT
        result = [smoothed[int(i * step)] for i in range(TARGET_MAX_HEIGHT)]
        smoothed = result
    elif len(smoothed) < TARGET_MAX_HEIGHT:
        # 补齐到目标高度（在顶部补1宽度的尖顶）
        while len(smoothed) < TARGET_MAX_HEIGHT:
            smoothed.append(1)

    return smoothed


def identify_sections(widths):
    """识别建筑的三个区段：底座、塔楼、皇冠"""
    n = len(widths)
    max_w = max(widths)

    # --- 底座检测 ---
    # 底座特征：从底部开始，宽度最大且较稳定的连续区域
    base_end = 0
    for i in range(min(n, n // 3)):  # 只看底部1/3
        if widths[i] >= max_w * 0.8:
            base_end = i
        else:
            break

    # 如果底座只有1层，尝试放宽标准
    if base_end == 0:
        for i in range(min(n, n // 4)):
            if widths[i] >= max_w * 0.6:
                base_end = i
            else:
                break

    # --- 皇冠检测 ---
    # 皇冠特征：顶部区域，宽度变化频繁
    crown_start = n - 1
    # 从顶部往下找，找到第一个宽度稳定的层
    stable_run = 0
    for i in range(n - 1, base_end + 2, -1):
        if i > 0 and abs(widths[i] - widths[i-1]) <= 1:
            stable_run += 1
            if stable_run >= 3:
                crown_start = i + stable_run - 1
                break
        else:
            stable_run = 0

    # 皇冠至少3层
    if n - 1 - crown_start < 3:
        crown_start = n - 3

    # --- 塔楼 ---
    tower_start = base_end + 1
    tower_end = crown_start - 1

    # 找塔楼标准层宽度
    tower_widths = widths[tower_start:tower_end+1] if tower_start <= tower_end else []
    standard_width = None
    if tower_widths:
        from collections import Counter
        wc = Counter(tower_widths)
        standard_width = wc.most_common(1)[0][0]

    return {
        'base': {'start': 0, 'end': base_end, 'layers': widths[:base_end+1]},
        'tower': {'start': tower_start, 'end': tower_end,
                  'layers': tower_widths, 'standard_width': standard_width},
        'crown': {'start': crown_start, 'end': n-1, 'layers': widths[crown_start:]}
    }


def generate_ascii_preview(widths, sections):
    """生成 ASCII 预览"""
    lines = []
    max_w = max(widths)
    labels = {'base': '底座', 'tower': '塔楼', 'crown': '皇冠'}

    lines.append("=" * (max_w + 20))
    lines.append("赛格大厦 - 多方块结构轮廓预览")
    lines.append("=" * (max_w + 20))
    lines.append("")

    for i in range(len(widths) - 1, -1, -1):
        w = widths[i]
        if i <= sections['base']['end']:
            section = "底座"
        elif i >= sections['crown']['start']:
            section = "皇冠"
        else:
            section = "塔楼"

        padding = (max_w - w) // 2
        line = " " * padding + "#" * w + " " * (max_w - w - padding)
        lines.append(f"Y={i:2d} [{section}] |{line}| W={w}")

    lines.append("")
    lines.append(f"总尺寸: {max_w}W x {len(widths)}H")
    lines.append(f"底座: Y=0~{sections['base']['end']} ({len(sections['base']['layers'])}层)")
    if sections['tower']['layers']:
        lines.append(f"塔楼: Y={sections['tower']['start']}~{sections['tower']['end']} ({len(sections['tower']['layers'])}层, 标准宽度={sections['tower']['standard_width']})")
    lines.append(f"皇冠: Y={sections['crown']['start']}~{len(widths)-1} ({len(sections['crown']['layers'])}层)")
    lines.append("")

    return "\n".join(lines)


def main():
    print("正在分析赛格大厦图片...")
    print()

    widths = analyze(IMG_PATH)
    if widths is None:
        return

    sections = identify_sections(widths)

    preview = generate_ascii_preview(widths, sections)
    print(preview)

    output = {
        'widths': widths,
        'max_width': max(widths),
        'height': len(widths),
        'sections': {
            'base': sections['base'],
            'tower': sections['tower'],
            'crown': sections['crown']
        },
        'parameters': {
            'target_max_width': TARGET_MAX_WIDTH,
            'target_max_height': TARGET_MAX_HEIGHT,
        }
    }

    with open(OUTPUT_PATH, 'w', encoding='utf-8') as f:
        json.dump(output, f, indent=2, ensure_ascii=False)
    print(f"\n数据已保存到: {OUTPUT_PATH}")

    with open(PREVIEW_PATH, 'w', encoding='utf-8') as f:
        f.write(preview)
    print(f"预览已保存到: {PREVIEW_PATH}")


if __name__ == '__main__':
    main()
