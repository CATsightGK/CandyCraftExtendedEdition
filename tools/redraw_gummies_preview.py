from __future__ import annotations

import colorsys
import shutil
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
TEXTURE_ROOT = ROOT / "src/main/resources/assets/candycraftmod/textures/item"
OUTPUT_ROOT = Path.home() / "Desktop/CandyCraft-Gummy-Redraw-Preview"

ROUND_TEXTURES = {
    "red_gummy": TEXTURE_ROOT / "gummy/gummy_red.png",
    "orange_gummy": TEXTURE_ROOT / "gummy/gummy_orange.png",
    "yellow_gummy": TEXTURE_ROOT / "gummy/gummy_yellow.png",
    "white_gummy": TEXTURE_ROOT / "gummy/gummy_white.png",
    "green_gummy": TEXTURE_ROOT / "gummy/gummy_green.png",
}

WORM_TEXTURES = {
    "red_gummy_worm": TEXTURE_ROOT / "gummy/gummy_worm_red.png",
    "orange_gummy_worm": TEXTURE_ROOT / "gummy/gummy_worm_orange.png",
    "yellow_gummy_worm": TEXTURE_ROOT / "gummy/gummy_worm_yellow.png",
    "white_gummy_worm": TEXTURE_ROOT / "gummy/gummy_worm_white.png",
    "green_gummy_worm": TEXTURE_ROOT / "gummy/gummy_worm_green.png",
}

PALETTES = {
    "red": [
        (110, 24, 35), (139, 32, 40), (169, 39, 45), (195, 47, 51),
        (216, 56, 61), (235, 91, 92), (245, 133, 122), (255, 193, 171),
    ],
    "orange": [
        (111, 39, 17), (149, 51, 18), (187, 67, 19), (218, 86, 24),
        (239, 111, 34), (250, 141, 51), (255, 174, 79), (255, 215, 137),
    ],
    "yellow": [
        (125, 95, 28), (154, 113, 40), (180, 133, 45), (204, 154, 48),
        (223, 176, 57), (238, 205, 100), (247, 224, 132), (252, 239, 185),
    ],
    "white": [
        (123, 119, 104), (147, 141, 102), (171, 164, 111), (191, 185, 125),
        (208, 203, 178), (226, 221, 182), (240, 231, 187), (255, 252, 237),
    ],
    "green": [
        (55, 121, 22), (73, 145, 25), (88, 169, 28), (105, 190, 32),
        (119, 210, 38), (145, 225, 72), (177, 235, 125), (204, 247, 164),
    ],
    "hot": [
        (83, 32, 34), (103, 43, 43), (120, 57, 56), (140, 71, 68),
        (162, 86, 80), (188, 111, 99), (215, 143, 122), (245, 183, 158),
    ],
}

DISPLAY_NAMES = {
    "gummy": "生软糖（动画）",
    "hot_gummy": "熟软糖",
    "red_gummy": "红色软糖",
    "orange_gummy": "橙色软糖",
    "yellow_gummy": "黄色软糖",
    "white_gummy": "白色软糖",
    "green_gummy": "绿色软糖",
    "hot_gummy_worm": "熟软糖虫",
    "red_gummy_worm": "红色软糖虫",
    "orange_gummy_worm": "橙色软糖虫",
    "yellow_gummy_worm": "黄色软糖虫",
    "white_gummy_worm": "白色软糖虫",
    "green_gummy_worm": "绿色软糖虫",
}


def opaque(image: Image.Image, x: int, y: int) -> bool:
    return 0 <= x < image.width and 0 <= y < image.height and image.getpixel((x, y))[3] > 0


def edge_distance(image: Image.Image, x: int, y: int) -> int:
    for radius in range(1, 5):
        for oy in range(-radius, radius + 1):
            for ox in range(-radius, radius + 1):
                if max(abs(ox), abs(oy)) == radius and not opaque(image, x + ox, y + oy):
                    return radius
    return 5


def shade_level(mask: Image.Image, x: int, y: int, kind: str) -> int:
    bbox = mask.getbbox()
    if bbox is None:
        return 0
    left, top, right, bottom = bbox
    nx = (x - left) / max(1, right - left - 1)
    ny = (y - top) / max(1, bottom - top - 1)
    depth = edge_distance(mask, x, y)

    # Minecraft-style directional lighting: compact upper-left shine, colored
    # lower-right rim, and only a few intentional value steps.
    value = 2.75 + min(depth, 3) * 0.62
    value += (0.58 - nx) * 0.82 + (0.55 - ny) * 0.94

    if depth == 1:
        value -= 1.35 if (nx > 0.46 or ny > 0.54) else 0.55
    if nx > 0.72 and ny > 0.48:
        value -= 0.55
    if ny > 0.78:
        value -= 0.45

    if kind == "round":
        gloss = ((nx - 0.30) / 0.20) ** 2 + ((ny - 0.25) / 0.22) ** 2
        if gloss <= 0.38 and depth >= 2:
            value = max(value, 7.0)
        elif gloss <= 1.0:
            value = max(value, 5.7)
    else:
        # The narrow worm gets small highlights along each bend instead of a
        # broad stripe that would make it look metallic.
        if depth >= 2 and ((x + 2 * y) % 7 in (0, 1)):
            value += 1.15
        if depth == 1 and nx < 0.44 and ny < 0.55:
            value += 0.45

    return max(0, min(7, round(value)))


def recolor(source: Image.Image, palette: list[tuple[int, int, int]], kind: str) -> Image.Image:
    source = source.convert("RGBA")
    result = Image.new("RGBA", source.size)
    for y in range(source.height):
        for x in range(source.width):
            original = source.getpixel((x, y))
            if original[3] == 0:
                continue
            color = palette[shade_level(source, x, y, kind)]
            result.putpixel((x, y), (*color, original[3]))
    return result


def rainbow_palette(hue: float) -> list[tuple[int, int, int]]:
    palette = []
    levels = [(0.39, 0.42), (0.50, 0.55), (0.62, 0.67), (0.72, 0.77),
              (0.80, 0.86), (0.86, 0.94), (0.72, 1.00), (0.46, 1.00)]
    for saturation, value in levels:
        # Shadows lean a little toward violet, highlights toward warm light.
        shifted_hue = (hue - (0.012 if value < 0.70 else 0.0)) % 1.0
        rgb = colorsys.hsv_to_rgb(shifted_hue, saturation, value)
        palette.append(tuple(round(channel * 255) for channel in rgb))
    return palette


def average_hue(frame: Image.Image) -> float:
    pixels = [pixel for pixel in frame.convert("RGBA").getdata() if pixel[3] > 0]
    if not pixels:
        return 0.0
    x_total = y_total = 0.0
    for red, green, blue, _ in pixels:
        hue, saturation, _ = colorsys.rgb_to_hsv(red / 255, green / 255, blue / 255)
        x_total += saturation * __import__("math").cos(hue * __import__("math").tau)
        y_total += saturation * __import__("math").sin(hue * __import__("math").tau)
    return (__import__("math").atan2(y_total, x_total) / __import__("math").tau) % 1.0


def checker(size: tuple[int, int], cell: int = 8) -> Image.Image:
    image = Image.new("RGBA", size, (38, 42, 48, 255))
    draw = ImageDraw.Draw(image)
    colors = ((50, 55, 62, 255), (62, 68, 76, 255))
    for y in range(0, size[1], cell):
        for x in range(0, size[0], cell):
            draw.rectangle((x, y, x + cell - 1, y + cell - 1), fill=colors[(x // cell + y // cell) % 2])
    return image


def load_font(size: int) -> ImageFont.FreeTypeFont | ImageFont.ImageFont:
    for path in [Path("C:/Windows/Fonts/msyh.ttc"), Path("C:/Windows/Fonts/simhei.ttf")]:
        if path.exists():
            return ImageFont.truetype(path, size)
    return ImageFont.load_default()


def create_comparison(originals: dict[str, Image.Image], redraws: dict[str, Image.Image]) -> None:
    card_w, card_h = 390, 230
    columns = 3
    rows = (len(redraws) + columns - 1) // columns
    sheet = Image.new("RGBA", (columns * card_w, rows * card_h + 66), (24, 27, 32, 255))
    draw = ImageDraw.Draw(sheet)
    title_font = load_font(25)
    label_font = load_font(17)
    small_font = load_font(13)
    draw.text((24, 17), "CandyCraft 软糖新版像素风格预览", font=title_font, fill=(245, 246, 248, 255))
    draw.text((24, 48), "左：原图    右：重画（严格保留轮廓与透明像素）", font=small_font, fill=(158, 166, 177, 255))

    for index, name in enumerate(redraws):
        col, row = index % columns, index // columns
        x0, y0 = col * card_w, 66 + row * card_h
        draw.rounded_rectangle((x0 + 9, y0 + 9, x0 + card_w - 9, y0 + card_h - 9), radius=7,
                               fill=(31, 35, 41, 255), outline=(67, 73, 83, 255), width=1)
        draw.text((x0 + 22, y0 + 18), DISPLAY_NAMES[name], font=label_font, fill=(235, 238, 242, 255))

        old = originals[name]
        new = redraws[name]
        old_bg = checker((128, 128), 16)
        new_bg = checker((160, 160), 20)
        old_bg.alpha_composite(old.resize((128, 128), Image.Resampling.NEAREST))
        new_bg.alpha_composite(new.resize((160, 160), Image.Resampling.NEAREST))
        sheet.alpha_composite(old_bg, (x0 + 22, y0 + 65))
        sheet.alpha_composite(new_bg, (x0 + 190, y0 + 49))
        draw.text((x0 + 64, y0 + 197), "原图", font=small_font, fill=(144, 151, 161, 255))
        draw.text((x0 + 248, y0 + 213), "重画", font=small_font, fill=(203, 209, 218, 255))

    sheet.convert("RGB").save(OUTPUT_ROOT / "all_gummies_comparison.png", quality=95)


def main() -> None:
    if OUTPUT_ROOT.exists():
        shutil.rmtree(OUTPUT_ROOT)
    OUTPUT_ROOT.mkdir(parents=True)

    originals: dict[str, Image.Image] = {}
    redraws: dict[str, Image.Image] = {}

    for name, path in ROUND_TEXTURES.items():
        color_name = name.removesuffix("_gummy")
        source = Image.open(path).convert("RGBA")
        originals[name] = source
        redraws[name] = recolor(source, PALETTES[color_name], "round")

    hot_source = Image.open(TEXTURE_ROOT / "hot_gummy.png").convert("RGBA")
    originals["hot_gummy"] = hot_source
    redraws["hot_gummy"] = recolor(hot_source, PALETTES["hot"], "round")

    order = ["red_gummy", "orange_gummy", "yellow_gummy", "white_gummy", "green_gummy", "hot_gummy"]
    originals = {name: originals[name] for name in order}
    redraws = {name: redraws[name] for name in order}

    for name, image in redraws.items():
        output_name = "hot_gummy.png" if name == "hot_gummy" else f"gummy_{name.removesuffix('_gummy')}.png"
        image.save(OUTPUT_ROOT / output_name)
    create_comparison(originals, redraws)

    # A clean new-only sheet is useful for judging the family as one set.
    columns, cell = 5, 210
    rows = (len(redraws) + columns - 1) // columns
    gallery = Image.new("RGBA", (columns * cell, rows * cell + 58), (24, 27, 32, 255))
    draw = ImageDraw.Draw(gallery)
    draw.text((20, 15), "重画后的软糖全套", font=load_font(24), fill=(245, 246, 248, 255))
    for index, (name, image) in enumerate(redraws.items()):
        col, row = index % columns, index // columns
        x, y = col * cell, 58 + row * cell
        bg = checker((176, 176), 22)
        bg.alpha_composite(image.resize((176, 176), Image.Resampling.NEAREST))
        gallery.alpha_composite(bg, (x + 17, y + 3))
        label = DISPLAY_NAMES[name]
        box = draw.textbbox((0, 0), label, font=load_font(14))
        draw.text((x + (cell - (box[2] - box[0])) // 2, y + 183), label, font=load_font(14), fill=(220, 224, 230, 255))
    gallery.convert("RGB").save(OUTPUT_ROOT / "all_gummies_redrawn.png", quality=95)


if __name__ == "__main__":
    main()
