from pathlib import Path
from PIL import Image, ImageDraw, ImageFont


OUTPUT = Path(r"C:\Users\10424\Desktop\CandyCraft_GUI_Texture_Previews\workbench_gui_previews")
OUTPUT.mkdir(parents=True, exist_ok=True)

THEMES = {
    "milk_chocolate": (0x6F3F22, 0xC98952, 0x3B2114),
    "white_chocolate": (0xEAD9B6, 0xFFF5D8, 0x9A7B4A),
    "dark_chocolate": (0x2D1710, 0x6B3B28, 0x160A07),
    "white_candy_cane": (0xF2F2E7, 0xFFFFFF, 0xB8A48E),
    "red_candy_cane": (0xC92F38, 0xFFE8E8, 0x801E28),
    "green_candy_cane": (0x45A845, 0xECFFE8, 0x266C29),
    "white_red_candy_cane": (0xE7DDD5, 0xD9454D, 0x8D2530),
    "white_green_candy_cane": (0xE7EBDD, 0x58B85B, 0x2E742F),
    "red_green_candy_cane": (0xC9363E, 0x4CA84F, 0x752327),
    "red_gummy": (0xF04A48, 0xFF9B9B, 0x8E1E25),
    "orange_gummy": (0xFF8A36, 0xFFC07A, 0x9E481E),
    "yellow_gummy": (0xFFD94A, 0xFFF2A0, 0xA87614),
    "white_gummy": (0xFFF1C6, 0xFFFFFF, 0xB69B6A),
    "green_gummy": (0x7CD943, 0xC7FF9A, 0x3E7F20),
}


def rgba(rgb, alpha=255):
    if isinstance(rgb, tuple):
        return (*rgb, alpha)
    return *((rgb >> 16 & 255, rgb >> 8 & 255, rgb & 255)), alpha


def tint(rgb, factor):
    return tuple(min(255, round(((rgb >> shift) & 255) * factor)) for shift in (16, 8, 0))


def blend(first, second, second_weight):
    first_weight = 1.0 - second_weight
    return tuple(round(((first >> shift) & 255) * first_weight + ((second >> shift) & 255) * second_weight)
                   for shift in (16, 8, 0))


def draw_slot(draw, x, y, light, dark, fill):
    draw.rectangle((x, y, x + 17, y + 17), fill=rgba(dark))
    draw.rectangle((x + 1, y + 1, x + 17, y + 2), fill=(0, 0, 0, 170))
    draw.rectangle((x + 1, y + 1, x + 2, y + 17), fill=(0, 0, 0, 170))
    draw.rectangle((x + 2, y + 2, x + 16, y + 16), fill=(*fill, 255))
    draw.rectangle((x + 2, y + 2, x + 16, y + 3), fill=rgba(light))
    draw.rectangle((x + 2, y + 2, x + 3, y + 16), fill=rgba(light))


def render(name, colors):
    base, light, dark = colors
    slot_fill = blend(base, light, 0.35)
    image = Image.new("RGBA", (176, 166), rgba(dark))
    draw = ImageDraw.Draw(image)
    draw.rectangle((2, 2, 173, 163), fill=rgba(base))
    draw.rectangle((5, 5, 170, 160), fill=rgba(tint(base, 0.88)))
    draw.rectangle((7, 7, 168, 17), fill=rgba(tint(light, 0.95)))
    draw.rectangle((7, 81, 168, 159), fill=rgba(tint(light, 0.82)))

    # Small pixel ribbons make the preview visibly patterned while retaining the game's flat pixel language.
    for y in range(20, 79, 8):
        for x in range(8, 168, 16):
            if ((x // 16) + (y // 8)) % 2 == 0:
                draw.rectangle((x, y, min(x + 6, 167), y + 1), fill=rgba(tint(light, 0.78)))
            else:
                draw.rectangle((x + 4, y + 2, min(x + 11, 167), y + 3), fill=rgba(tint(dark, 0.9)))
    for x in range(10, 168, 18):
        draw.rectangle((x, 84, x + 1, 157), fill=rgba(tint(light, 0.72)))

    for row in range(3):
        for column in range(3):
            draw_slot(draw, 29 + column * 18, 16 + row * 18, light, dark, slot_fill)
    draw.rectangle((88, 41, 109, 45), fill=rgba(dark))
    draw.rectangle((89, 42, 107, 44), fill=rgba(light))
    draw.rectangle((104, 37, 108, 49), fill=rgba(dark))
    draw.rectangle((108, 40, 111, 46), fill=rgba(dark))
    draw.rectangle((105, 39, 106, 47), fill=rgba(light))
    draw.rectangle((108, 42, 109, 44), fill=rgba(light))
    draw_slot(draw, 123, 34, light, dark, slot_fill)
    for row in range(3):
        for column in range(9):
            draw_slot(draw, 7 + column * 18, 83 + row * 18, light, dark, slot_fill)
    for column in range(9):
        draw_slot(draw, 7 + column * 18, 141, light, dark, slot_fill)
    path = OUTPUT / f"{name}_gui.png"
    image.save(path)
    return image


font = ImageFont.load_default()
sheet = Image.new("RGBA", (4 * 360, 4 * 365), (35, 35, 35, 255))
sheet_draw = ImageDraw.Draw(sheet)
for index, (name, colors) in enumerate(THEMES.items()):
    preview = render(name, colors).resize((352, 332), Image.Resampling.NEAREST)
    x = (index % 4) * 360 + 4
    y = (index // 4) * 365 + 24
    sheet.alpha_composite(preview, (x, y))
    sheet_draw.text((x, y - 18), name, fill=(255, 255, 255, 255), font=font)
sheet.save(OUTPUT / "all_non_marshmallow_workbench_guis.png")
