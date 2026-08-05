from pathlib import Path
from PIL import Image


ROOT = Path(r"C:\Users\10424\Documents\Codex\2026-05-26\1-8-9forge-1-20-1forge\CandyCraftExtendedEdition-clean")
MAIN = ROOT / "src/main/resources/assets/candycraftmod/textures/block"
CLASSIC = ROOT / "src/main/resources/resourcepacks/candycraft_classic/assets/candycraftmod/textures/block"
DESKTOP = Path(r"C:\Users\10424\Desktop\CandyCraft_GUI_Texture_Previews\marshmallow_workbench_sides")

SIDE_SOURCE = DESKTOP / "marshmallow_workbench_dark_side.png"
PLANKS = MAIN / "marshmallow_planks.0.png"


def luminance(color):
    red, green, blue = color[:3]
    return 0.2126 * red + 0.7152 * green + 0.0722 * blue


def recolor(image):
    source_pixels = [pixel[:3] for pixel in image.getdata()]
    source_luma = [luminance(pixel) for pixel in source_pixels]
    target = Image.open(PLANKS).convert("RGBA")
    target_palette = sorted({pixel[:3] for pixel in target.getdata()}, key=luminance)
    source_min, source_max = min(source_luma), max(source_luma)
    target_min, target_max = luminance(target_palette[0]), luminance(target_palette[-1])

    result = Image.new("RGBA", image.size)
    output = []
    for pixel in image.getdata():
        if pixel[3] == 0:
            output.append(pixel)
            continue
        normalized = (luminance(pixel) - source_min) / max(1.0, source_max - source_min)
        desired = target_min + max(0.0, min(1.0, normalized)) * (target_max - target_min)
        color = min(target_palette, key=lambda candidate: abs(luminance(candidate) - desired))
        output.append((*color, pixel[3]))
    result.putdata(output)
    return result


side = Image.open(SIDE_SOURCE).convert("RGBA")
side_result = recolor(side)

for directory in (MAIN, CLASSIC):
    side_result.save(directory / "marshmallow_workbench_side.png")

side_result.save(DESKTOP / "marshmallow_workbench_normal_side.png")
