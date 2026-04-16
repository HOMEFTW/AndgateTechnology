"""
赛格大厦多方块结构 - 交互式预览
用法: python tools/preview_shape.py
"""

layers = [['BBBBBBBBB', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'BBBBBBBBB'], ['BHBB~BBHB', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'BBBBBBBBB'], ['BBBBBBBBB', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'BBBBBBBBB'], ['BBBBBBBBB', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'BBBBBBBBB'], ['BBBBBBBBB', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'BBBBBBBBB'], ['BBBBBBBBB', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'BBBBBBBBB'], ['BBBBBBBBB', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'BBBBBBBBB'], ['BBBBBBBBB', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'BBBBBBBBB'], ['BBBBBBBBB', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'BBBBBBBBB'], ['BBBBBBBBB', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'BBBBBBBBB'], ['BBBBBBBBB', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'BBBBBBBBB'], ['BBBBBBBBB', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'BBBBBBBBB'], ['BBBBBBBBB', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'B-------B', 'BBBBBBBBB'], ['         ', ' TTTTTTT ', ' T-----T ', ' T-----T ', ' T-----T ', ' T-----T ', ' T-----T ', ' TTTTTTT ', '         '], ['         ', ' TTTTTTT ', ' T-----T ', ' T-----T ', ' T-----T ', ' T-----T ', ' T-----T ', ' TTTTTTT ', '         '], ['         ', '         ', '  TTTTT  ', '  T---T  ', '  T---T  ', '  T---T  ', '  TTTTT  ', '         ', '         '], ['         ', '         ', '  TTTTT  ', '  T---T  ', '  T---T  ', '  T---T  ', '  TTTTT  ', '         ', '         '], ['         ', ' TTTTTTT ', ' T-----T ', ' T-----T ', ' T-----T ', ' T-----T ', ' T-----T ', ' TTTTTTT ', '         '], ['         ', ' TTTTTTT ', ' T-----T ', ' T-----T ', ' T-----T ', ' T-----T ', ' T-----T ', ' TTTTTTT ', '         '], ['         ', ' TTTTTTT ', ' T-----T ', ' T-----T ', ' T-----T ', ' T-----T ', ' T-----T ', ' TTTTTTT ', '         '], ['         ', ' TTTTTTT ', ' T-----T ', ' T-----T ', ' T-----T ', ' T-----T ', ' T-----T ', ' TTTTTTT ', '         '], ['         ', ' TTTTTTT ', ' T-----T ', ' T-----T ', ' T-----T ', ' T-----T ', ' T-----T ', ' TTTTTTT ', '         '], ['TTTTTTTTT', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'TTTTTTTTT'], ['TTTTTTTTT', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'TTTTTTTTT'], ['TTTTTTTTT', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'TTTTTTTTT'], ['TTTTTTTTT', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'TTTTTTTTT'], ['TTTTTTTTT', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'TTTTTTTTT'], ['TTTTTTTTT', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'TTTTTTTTT'], ['TTTTTTTTT', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'TTTTTTTTT'], ['TTTTTTTTT', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'TTTTTTTTT'], ['TTTTTTTTT', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'TTTTTTTTT'], ['TTTTTTTTT', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'TTTTTTTTT'], ['TTTTTTTTT', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'T-------T', 'TTTTTTTTT'], ['CCCCCCCCC', 'C-------C', 'C-------C', 'C-------C', 'C-------C', 'C-------C', 'C-------C', 'C-------C', 'CCCCCCCCC'], ['         ', ' CCCCCCC ', ' C-----C ', ' C-----C ', ' C-----C ', ' C-----C ', ' C-----C ', ' CCCCCCC ', '         '], ['         ', ' CCCCCCC ', ' C-----C ', ' C-----C ', ' C-----C ', ' C-----C ', ' C-----C ', ' CCCCCCC ', '         '], ['         ', '         ', '  CCCCC  ', '  C---C  ', '  C---C  ', '  C---C  ', '  CCCCC  ', '         ', '         '], ['         ', '         ', '  CCCCC  ', '  C---C  ', '  C---C  ', '  C---C  ', '  CCCCC  ', '         ', '         '], ['         ', ' CCCCCCC ', ' C-----C ', ' C-----C ', ' C-----C ', ' C-----C ', ' C-----C ', ' CCCCCCC ', '         '], ['CCCCCCCCC', 'C-------C', 'C-------C', 'C-------C', 'C-------C', 'C-------C', 'C-------C', 'C-------C', 'CCCCCCCCC']]

legend = {
    'B': '底座方块 (sBlockCasings2:15)',
    'T': '塔楼方块 (sBlockCasings4:1)',
    'C': '皇冠方块 (sBlockCasings3:2)',
    'H': 'Hatch 位置',
    '~': '控制器',
    '-': '空气 (内部)',
    ' ': '跳过 (外部)',
}

w = len(layers[0][0]) if layers else 0
d = len(layers[0]) if layers else 0
h = len(layers)

print(f"赛格大厦结构: {w}W x {h}H x {d}D")
print(f"图例:")
for k, v in legend.items():
    print(f"  {k} = {v}")

print("\n=== 各层截面 ===")
for y in range(h - 1, -1, -1):
    print(f"\nY={y}:")
    for z, row in enumerate(layers[y]):
        print(f"  Z{z}: |{row.replace(' ', '.')}|")

# 找控制器
for y, z_plane in enumerate(layers):
    for z, row in enumerate(z_plane):
        if '~' in row:
            x = row.index('~')
            print(f"\n控制器偏移: horizontalOff={x}, verticalOff={y}, depthOff={z}")
