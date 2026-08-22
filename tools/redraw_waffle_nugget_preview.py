from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "src/main/resources/assets/candycraftmod/textures/item/waffle_nugget.png"
OUTPUT = Path.home() / "Desktop/CandyCraft-Waffle-Fragment-Preview"

PALETTE = [
    (105, 50, 4),
    (137, 70, 5),
    (171, 93, 8),
    (204, 123, 15),
    (229, 156, 30),
    (245, 187, 58),
    (253, 216, 105),
    (255, 239, 168),
]


def interpolate(t):
    t = max(0.0, min(1.0, t)) * (len(PALETTE) - 1)
    index = min(len(PALETTE) - 2, int(t))
    amount = t - index
    return tuple(round(PALETTE[index][channel] * (1.0 - amount) + PALETTE[index + 1][channel] * amount) for channel in range(3))


def luminance(pixel):
    red, green, blue, _ = pixel
    return (red * 0.2126 + green * 0.7152 + blue * 0.0722) / 255.0


def edge_distance(source, x, y):
    for radius in range(1, 4):
        for oy in range(-radius, radius + 1):
            for ox in range(-radius, radius + 1):
                tx, ty = x + ox, y + oy
                if max(abs(ox), abs(oy)) == radius and (
                    not (0 <= tx < source.width and 0 <= ty < source.height)
                    or source.getpixel((tx, ty))[3] == 0
                ):
                    return radius
    return 4


def create_fragment(source):
    source = source.convert("RGBA")
    values = [luminance(pixel) for pixel in source.getdata() if pixel[3] > 0]
    low, high = min(values), max(values)
    span = max(0.001, high - low)
    image = Image.new("RGBA", source.size)
    for y in range(source.height):
        for x in range(source.width):
            pixel = source.getpixel((x, y))
            if pixel[3] == 0:
                continue
            normalized = (luminance(pixel) - low) / span
            nx = x / max(1, source.width - 1)
            ny = y / max(1, source.height - 1)
            depth = edge_distance(source, x, y)
            light = (0.52 - nx) * 0.045 + (0.50 - ny) * 0.055
            if depth == 1 and (nx > 0.48 or ny > 0.52):
                light -= 0.065
            elif depth >= 2 and nx < 0.50 and ny < 0.50:
                light += 0.025
            color = interpolate(0.07 + normalized * 0.87 + light)
            image.putpixel((x, y), (*color, pixel[3]))
    return image


def checker(size, cell):
    image = Image.new("RGBA", size)
    draw = ImageDraw.Draw(image)
    for y in range(0, size[1], cell):
        for x in range(0, size[0], cell):
            color = (50, 55, 62, 255) if (x // cell + y // cell) % 2 == 0 else (63, 69, 77, 255)
            draw.rectangle((x, y, x + cell - 1, y + cell - 1), fill=color)
    return image


def font(size):
    for path in (Path("C:/Windows/Fonts/msyh.ttc"), Path("C:/Windows/Fonts/simhei.ttf")):
        if path.exists():
            return ImageFont.truetype(path, size)
    return ImageFont.load_default()


def main():
    OUTPUT.mkdir(parents=True, exist_ok=True)
    original = Image.open(SOURCE).convert("RGBA")
    redrawn = create_fragment(original)
    redrawn.save(OUTPUT / "waffle_nugget.png")

    canvas = Image.new("RGBA", (540, 278), (24, 27, 32, 255))
    draw = ImageDraw.Draw(canvas)
    draw.text((22, 16), "华夫饼碎片新版物品贴图", font=font(24), fill=(245, 246, 248, 255))
    draw.text((22, 48), "左：原图    右：重画", font=font(14), fill=(158, 166, 177, 255))
    old_bg = checker((176, 176), 22)
    new_bg = checker((176, 176), 22)
    old_bg.alpha_composite(original.resize((176, 176), Image.Resampling.NEAREST))
    new_bg.alpha_composite(redrawn.resize((176, 176), Image.Resampling.NEAREST))
    canvas.alpha_composite(old_bg, (52, 78))
    canvas.alpha_composite(new_bg, (312, 78))
    draw.text((120, 256), "原图", font=font(14), fill=(170, 177, 187, 255))
    draw.text((372, 256), "新版碎片", font=font(14), fill=(226, 230, 235, 255))
    canvas.convert("RGB").save(OUTPUT / "waffle_nugget_comparison.png", quality=95)


if __name__ == "__main__":
    main()
