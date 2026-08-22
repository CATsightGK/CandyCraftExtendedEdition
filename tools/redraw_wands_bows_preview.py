from __future__ import annotations

import colorsys
import shutil
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
TEXTURES = ROOT / "src/main/resources/assets/candycraftmod/textures/item"
OUTPUT = Path.home() / "Desktop/CandyCraft-Weapons-Redraw-Preview"

FILES = {
    "jelly_wand": TEXTURES / "jelly_wand.png",
    "jump_wand": TEXTURES / "jump_wand.png",
    "caramel_bow": TEXTURES / "caramel_bow.png",
    "caramel_bow_1": TEXTURES / "caramel_bow_1.png",
    "caramel_bow_2": TEXTURES / "caramel_bow_2.png",
    "caramel_bow_3": TEXTURES / "caramel_bow_3.png",
    "caramel_crossbow": TEXTURES / "caramel_crossbow.png",
    "caramel_crossbow_0": TEXTURES / "caramel_crossbow_0.png",
    "caramel_crossbow_1": TEXTURES / "caramel_crossbow_1.png",
    "caramel_crossbow_2": TEXTURES / "caramel_crossbow_2.png",
}

LABELS = {
    "jelly_wand": "果冻国王的权杖",
    "jump_wand": "跳跃权杖",
    "caramel_bow": "焦糖弓：普通",
    "caramel_bow_1": "焦糖弓：拉弓 I",
    "caramel_bow_2": "焦糖弓：拉弓 II",
    "caramel_bow_3": "焦糖弓：拉弓 III",
    "caramel_crossbow": "蜂蜜弩：普通",
    "caramel_crossbow_0": "蜂蜜弩：装填 I",
    "caramel_crossbow_1": "蜂蜜弩：装填 II",
    "caramel_crossbow_2": "蜂蜜弩：装填 III / 已装填",
}

PALETTES = {
    "jelly_cap": [(78, 18, 48), (119, 24, 68), (166, 40, 87), (211, 69, 112), (242, 113, 151), (255, 188, 194)],
    "jelly_stick": [(105, 43, 5), (144, 64, 6), (183, 87, 10), (218, 125, 20), (244, 170, 44), (255, 225, 112)],
    "jump_cap": [(62, 17, 76), (105, 24, 112), (153, 43, 151), (204, 76, 190), (238, 125, 216), (255, 193, 239)],
    "jump_stick": [(20, 37, 96), (30, 61, 143), (45, 91, 192), (70, 132, 232), (110, 179, 255), (194, 232, 255)],
    "wood": [(50, 24, 5), (78, 37, 6), (111, 53, 7), (146, 72, 11), (183, 99, 20), (220, 139, 42), (250, 188, 79)],
    "honey": [(91, 52, 3), (126, 73, 4), (164, 98, 7), (202, 133, 14), (231, 174, 34), (250, 213, 91), (255, 240, 159)],
    "bowstring": [(74, 20, 38), (111, 28, 55), (153, 41, 72), (198, 67, 101), (233, 112, 139), (255, 177, 190)],
    "bolt": [(104, 51, 4), (143, 75, 5), (184, 107, 10), (219, 152, 28), (246, 200, 72), (255, 235, 149)],
}


def edge_distance(image: Image.Image, x: int, y: int) -> int:
    for radius in range(1, 4):
        for oy in range(-radius, radius + 1):
            for ox in range(-radius, radius + 1):
                if max(abs(ox), abs(oy)) == radius:
                    nx, ny = x + ox, y + oy
                    if not (0 <= nx < image.width and 0 <= ny < image.height) or image.getpixel((nx, ny))[3] == 0:
                        return radius
    return 4


def material(name: str, rgb: tuple[int, int, int]) -> str:
    hue, saturation, _ = colorsys.rgb_to_hsv(*(value / 255 for value in rgb))
    degrees = hue * 360
    if name in ("jelly_wand", "jump_wand"):
        if name == "jelly_wand":
            return "jelly_cap" if degrees < 18 else "jelly_stick"
        return "jump_cap" if degrees > 290 else "jump_stick"
    if degrees >= 325 or degrees < 8:
        return "bowstring" if "bow" in name else "bolt"
    if degrees >= 48 or saturation < 0.55:
        return "honey"
    return "wood"


TARGET_HUES = {
    # The jelly wand's orb is the warm red-pink material from the source art;
    # keeping it near hue 0 avoids the purple cast of the previous preview.
    "jelly_cap": 0.012,
    "jelly_stick": 0.105,
    "jump_cap": 0.875,
    "jump_stick": 0.635,
    "wood": 0.075,
    "honey": 0.125,
    "bowstring": 0.955,
    "bolt": 0.105,
}


def recolor(name: str, source: Image.Image) -> Image.Image:
    """Recolor by preserving every source pixel's value variation.

    The former palette-index approach reduced detailed source art to broad
    bands. This keeps the source's hue/value texture, then applies a restrained
    hue shift and directional lighting so the original pixel detail survives.
    """
    source = source.convert("RGBA")
    result = Image.new("RGBA", source.size)
    for y in range(source.height):
        for x in range(source.width):
            red, green, blue, alpha = source.getpixel((x, y))
            if alpha == 0:
                continue
            hue, saturation, value = colorsys.rgb_to_hsv(red / 255, green / 255, blue / 255)
            current_material = material(name, (red, green, blue))
            target = TARGET_HUES[current_material]
            # Keep small hue differences from the original pixels. Those
            # differences are the fine grain that was lost in the old preview.
            shifted_hue = (target + (hue - target) * 0.18) % 1.0
            shifted_saturation = min(1.0, saturation * 1.07 + 0.025)
            nx = x / max(1, source.width - 1)
            ny = y / max(1, source.height - 1)
            lighting = (0.54 - nx) * 0.075 + (0.54 - ny) * 0.09
            depth = edge_distance(source, x, y)
            if depth == 1 and (nx > 0.55 or ny > 0.55):
                lighting -= 0.075
            elif depth >= 2 and nx < 0.52 and ny < 0.52:
                lighting += 0.035
            if name == "jump_wand" and current_material == "jump_stick":
                # The source stick is only three pixels wide. A small local
                # value blend removes the artificial hard band while keeping
                # the original pixel texture and silhouette intact.
                nearby_values = []
                for oy in (-1, 0, 1):
                    for ox in (-1, 0, 1):
                        if ox == 0 and oy == 0:
                            continue
                        tx, ty = x + ox, y + oy
                        if 0 <= tx < source.width and 0 <= ty < source.height:
                            neighbor = source.getpixel((tx, ty))
                            if neighbor[3] and material(name, neighbor[:3]) == current_material:
                                nearby_values.append(colorsys.rgb_to_hsv(*(channel / 255 for channel in neighbor[:3]))[2])
                if nearby_values:
                    value = value * 0.64 + sum(nearby_values) / len(nearby_values) * 0.36
                shifted_saturation = min(1.0, saturation * 0.97 + 0.018)
                lighting *= 0.45
            contrasted_value = max(0.06, min(1.0, 0.055 + value * 0.92 + lighting))
            new_rgb = colorsys.hsv_to_rgb(shifted_hue, shifted_saturation, contrasted_value)
            result.putpixel((x, y), tuple(round(channel * 255) for channel in new_rgb) + (alpha,))
    return result


def font(size: int):
    for path in (Path("C:/Windows/Fonts/msyh.ttc"), Path("C:/Windows/Fonts/simhei.ttf")):
        if path.exists():
            return ImageFont.truetype(path, size)
    return ImageFont.load_default()


def checker(size: tuple[int, int], cell: int = 8) -> Image.Image:
    image = Image.new("RGBA", size, (38, 42, 48, 255))
    draw = ImageDraw.Draw(image)
    for y in range(0, size[1], cell):
        for x in range(0, size[0], cell):
            color = (50, 55, 62, 255) if (x // cell + y // cell) % 2 == 0 else (63, 69, 77, 255)
            draw.rectangle((x, y, x + cell - 1, y + cell - 1), fill=color)
    return image


def main() -> None:
    if OUTPUT.exists():
        shutil.rmtree(OUTPUT)
    OUTPUT.mkdir(parents=True)
    originals = {name: Image.open(path).convert("RGBA") for name, path in FILES.items()}
    redraws = {name: recolor(name, image) for name, image in originals.items()}
    for name, image in redraws.items():
        image.save(OUTPUT / f"{name}.png")

    columns, card_w, card_h = 3, 390, 245
    rows = (len(FILES) + columns - 1) // columns
    sheet = Image.new("RGBA", (columns * card_w, rows * card_h + 68), (24, 27, 32, 255))
    draw = ImageDraw.Draw(sheet)
    draw.text((22, 15), "CandyCraft 法杖 / 弓 / 弩新版像素风格预览", font=font(24), fill=(245, 246, 248, 255))
    draw.text((22, 45), "左：原图    右：重画（保留轮廓与所有阶段）", font=font(14), fill=(158, 166, 177, 255))
    for index, name in enumerate(FILES):
        col, row = index % columns, index // columns
        x0, y0 = col * card_w, 68 + row * card_h
        draw.rounded_rectangle((x0 + 9, y0 + 8, x0 + card_w - 9, y0 + card_h - 9), radius=7,
                               fill=(31, 35, 41, 255), outline=(67, 73, 83, 255), width=1)
        draw.text((x0 + 20, y0 + 16), LABELS[name], font=font(16), fill=(235, 238, 242, 255))
        old_bg = checker((128, 128), 16)
        new_bg = checker((160, 160), 20)
        old_bg.alpha_composite(originals[name].resize((128, 128), Image.Resampling.NEAREST))
        new_bg.alpha_composite(redraws[name].resize((160, 160), Image.Resampling.NEAREST))
        sheet.alpha_composite(old_bg, (x0 + 20, y0 + 58))
        sheet.alpha_composite(new_bg, (x0 + 190, y0 + 42))
        draw.text((x0 + 62, y0 + 193), "原图", font=font(13), fill=(144, 151, 161, 255))
        draw.text((x0 + 248, y0 + 209), "重画", font=font(13), fill=(203, 209, 218, 255))
    sheet.convert("RGB").save(OUTPUT / "weapons_comparison.png", quality=95)

    cell, columns = 210, 5
    rows = (len(FILES) + columns - 1) // columns
    gallery = Image.new("RGBA", (columns * cell, rows * cell + 58), (24, 27, 32, 255))
    draw = ImageDraw.Draw(gallery)
    draw.text((20, 15), "重画后的法杖、焦糖弓与蜂蜜弩", font=font(24), fill=(245, 246, 248, 255))
    for index, (name, image) in enumerate(redraws.items()):
        col, row = index % columns, index // columns
        x, y = col * cell, 58 + row * cell
        bg = checker((176, 176), 22)
        bg.alpha_composite(image.resize((176, 176), Image.Resampling.NEAREST))
        gallery.alpha_composite(bg, (x + 17, y + 2))
        label = LABELS[name]
        box = draw.textbbox((0, 0), label, font=font(13))
        draw.text((x + (cell - box[2]) // 2, y + 183), label, font=font(13), fill=(220, 224, 230, 255))
    gallery.convert("RGB").save(OUTPUT / "weapons_redrawn.png", quality=95)


if __name__ == "__main__":
    main()
