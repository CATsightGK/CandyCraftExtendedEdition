from __future__ import annotations

import colorsys
import math
import shutil
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
TEXTURES = ROOT / "src/main/resources/assets/candycraftmod/textures/item"
OUTPUT = Path.home() / "Desktop/CandyCraft-Material-Items-Redraw-Preview"

FILES = {
    "pez": TEXTURES / "pez.png",
    "licorice": TEXTURES / "licorice.png",
    "honey_shard": TEXTURES / "honey_shard.png",
    "honeycomb": TEXTURES / "honeycomb.png",
}

LABELS = {
    "pez": "PEZ糖",
    "licorice": "甘草",
    "honey_shard": "蜜蜡片",
    "honeycomb": "糖果世界蜂巢",
}

RAMPS = {
    "pez": [
        (82, 82, 79), (108, 108, 104), (137, 137, 132), (166, 166, 160),
        (193, 193, 187), (217, 217, 210), (239, 239, 232), (255, 253, 245),
    ],
    "licorice": [
        (5, 4, 8), (11, 7, 14), (19, 11, 22), (29, 16, 31),
        (42, 23, 40), (57, 32, 50), (73, 43, 61), (91, 57, 73),
    ],
    "honey_shard": [
        (126, 69, 4), (158, 91, 6), (191, 117, 11), (220, 148, 24),
        (240, 177, 48), (250, 205, 91), (255, 226, 143), (255, 244, 199),
    ],
    "honeycomb": [
        (79, 40, 0), (111, 60, 1), (145, 84, 3), (178, 111, 8),
        (210, 145, 18), (234, 179, 42), (248, 210, 83), (255, 235, 145),
    ],
}


def opaque(image: Image.Image, x: int, y: int) -> bool:
    return 0 <= x < image.width and 0 <= y < image.height and image.getpixel((x, y))[3] > 0


def edge_distance(image: Image.Image, x: int, y: int) -> int:
    for radius in range(1, 4):
        for oy in range(-radius, radius + 1):
            for ox in range(-radius, radius + 1):
                if max(abs(ox), abs(oy)) == radius and not opaque(image, x + ox, y + oy):
                    return radius
    return 4


def interpolate(ramp: list[tuple[int, int, int]], t: float) -> tuple[int, int, int]:
    t = max(0.0, min(1.0, t)) * (len(ramp) - 1)
    index = min(len(ramp) - 2, int(t))
    amount = t - index
    return tuple(round(ramp[index][channel] * (1.0 - amount) + ramp[index + 1][channel] * amount) for channel in range(3))


def source_luminance(pixel: tuple[int, int, int, int]) -> float:
    red, green, blue, _ = pixel
    return (red * 0.2126 + green * 0.7152 + blue * 0.0722) / 255.0


def redraw(name: str, source: Image.Image) -> Image.Image:
    source = source.convert("RGBA")
    luminances = [source_luminance(pixel) for pixel in source.getdata() if pixel[3] > 0]
    low, high = min(luminances), max(luminances)
    span = max(0.001, high - low)
    result = Image.new("RGBA", source.size)
    for y in range(source.height):
        for x in range(source.width):
            pixel = source.getpixel((x, y))
            if pixel[3] == 0:
                continue
            normalized = (source_luminance(pixel) - low) / span
            nx = x / max(1, source.width - 1)
            ny = y / max(1, source.height - 1)
            depth = edge_distance(source, x, y)
            light = (0.52 - nx) * 0.045 + (0.50 - ny) * 0.055
            if depth == 1 and (nx > 0.48 or ny > 0.52):
                light -= 0.038 if name == "honey_shard" else 0.075
            elif depth >= 2 and nx < 0.48 and ny < 0.48:
                light += 0.025

            if name == "pez":
                # Retain the original embossed tablet details; use a cool metal
                # shadow and a slightly warm top highlight.
                t = 0.10 + normalized * 0.84 + light
            elif name == "licorice":
                # The source lives almost entirely below RGB 35. Stretch its
                # internal spiral/detail without turning black licorice gray.
                t = normalized ** 0.82 * 0.82 + light * 0.45
            elif name == "honey_shard":
                t = 0.10 + normalized * 0.78 + light * 1.25
                if depth == 2 and ny > 0.48:
                    t -= 0.022
                if depth >= 2 and 0.20 < nx < 0.58 and 0.20 < ny < 0.55:
                    t += 0.08
            else:
                # Preserve each hex cell, deepen recessed centers, and sharpen
                # the tiny wall-to-hole transitions without flattening them.
                nearby_luminances = []
                for oy in (-1, 0, 1):
                    for ox in (-1, 0, 1):
                        tx, ty = x + ox, y + oy
                        if 0 <= tx < source.width and 0 <= ty < source.height:
                            neighbor = source.getpixel((tx, ty))
                            if neighbor[3] > 0:
                                nearby_luminances.append(source_luminance(neighbor))
                nearby = sum(nearby_luminances) / len(nearby_luminances)
                micro_detail = normalized - (nearby - low) / span
                lower_right_shade = max(0.0, nx + ny - 0.82) * 0.075
                t = 0.08 + normalized * 0.82 + light * 0.72 + micro_detail * 0.42 - lower_right_shade

            color = interpolate(RAMPS[name], t)
            result.putpixel((x, y), (*color, pixel[3]))
    return result


def redraw_licorice_stick() -> Image.Image:
    """Create a new twisted licorice-stick silhouette at native resolution."""
    image = Image.new("RGBA", (16, 16))
    start = (2.8, 12.6)
    end = (12.4, 3.0)
    dx, dy = end[0] - start[0], end[1] - start[1]
    length_sq = dx * dx + dy * dy
    radius = 2.15
    ramp = RAMPS["licorice"]

    for y in range(16):
        for x in range(16):
            px, py = x + 0.5, y + 0.5
            projection = ((px - start[0]) * dx + (py - start[1]) * dy) / length_sq
            t = max(0.0, min(1.0, projection))
            nearest_x = start[0] + dx * t
            nearest_y = start[1] + dy * t
            distance = math.hypot(px - nearest_x, py - nearest_y)
            if distance > radius:
                continue

            # Signed cross-section distance controls the rounded body lighting.
            signed = ((px - nearest_x) * -dy + (py - nearest_y) * dx) / math.sqrt(length_sq)
            rounded_light = 1.0 - abs(signed) / radius
            # Alternating diagonal ridges make the stick read as twisted candy,
            # with narrow bright seams rather than broad flat bands.
            ridge_wave = math.sin(t * math.tau * 5.0 + signed * 1.35)
            value = 0.16 + rounded_light * 0.48 + ridge_wave * 0.13
            value += (0.5 - x / 15.0) * 0.05 + (0.5 - y / 15.0) * 0.06
            if distance > radius - 0.55:
                value -= 0.17
            if 0.16 < t < 0.90 and -0.75 < signed < -0.15 and ridge_wave > 0.70:
                value += 0.10
            color = interpolate(ramp, value)
            image.putpixel((x, y), (*color, 255))
    return image


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
    redraws = {name: redraw(name, image) for name, image in originals.items()}
    redraws["licorice"] = redraw_licorice_stick()
    for name, image in redraws.items():
        image.save(OUTPUT / f"{name}.png")

    card_w, card_h = 390, 245
    sheet = Image.new("RGBA", (card_w * 2, card_h * 2 + 68), (24, 27, 32, 255))
    draw = ImageDraw.Draw(sheet)
    draw.text((22, 15), "CandyCraft 材料物品新版像素风格预览", font=font(24), fill=(245, 246, 248, 255))
    draw.text((22, 45), "左：原图    右：重画（轮廓与细碎纹理保持）", font=font(14), fill=(158, 166, 177, 255))
    for index, name in enumerate(FILES):
        col, row = index % 2, index // 2
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
    sheet.convert("RGB").save(OUTPUT / "materials_comparison.png", quality=95)

    gallery = Image.new("RGBA", (880, 250), (24, 27, 32, 255))
    draw = ImageDraw.Draw(gallery)
    draw.text((20, 15), "重画后的 PEZ糖、甘草、蜜蜡片与蜂巢", font=font(24), fill=(245, 246, 248, 255))
    for index, (name, image) in enumerate(redraws.items()):
        x = index * 220 + 22
        bg = checker((176, 176), 22)
        bg.alpha_composite(image.resize((176, 176), Image.Resampling.NEAREST))
        gallery.alpha_composite(bg, (x, 56))
        label = LABELS[name]
        box = draw.textbbox((0, 0), label, font=font(14))
        draw.text((x + (176 - (box[2] - box[0])) // 2, 232), label, font=font(14), fill=(220, 224, 230, 255))
    gallery.convert("RGB").save(OUTPUT / "materials_redrawn.png", quality=95)


if __name__ == "__main__":
    main()
