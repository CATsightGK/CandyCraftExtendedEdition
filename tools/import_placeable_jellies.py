import argparse
import json
from pathlib import Path

from PIL import Image


FACE_Y_180 = {
    "north": "south",
    "south": "north",
    "east": "west",
    "west": "east",
    "up": "up",
    "down": "down",
}

LEMON_PALETTE = [
    (203, 207, 81),
    (206, 208, 102),
    (213, 216, 107),
    (223, 223, 114),
    (223, 225, 119),
    (231, 235, 111),
    (235, 240, 113),
]

def luminance(color):
    red, green, blue = color
    return 0.2126 * red + 0.7152 * green + 0.0722 * blue


def bake_element(element, texture_key):
    start = list(element["from"])
    end = list(element["to"])
    faces = element["faces"]
    rotation = element.get("rotation", [0, 0, 0])
    rotated = abs(rotation[1]) == 180
    if rotated:
        origin = element["origin"]
        start, end = (
            [2 * origin[0] - end[0], start[1], 2 * origin[2] - end[2]],
            [2 * origin[0] - start[0], end[1], 2 * origin[2] - start[2]],
        )

    exported_faces = {}
    for source_direction, face in faces.items():
        if face.get("texture") is None:
            continue
        direction = FACE_Y_180[source_direction] if rotated else source_direction
        exported = {
            "uv": [round(value / 2.0, 4) for value in face["uv"]],
            "texture": texture_key,
        }
        source_rotation = face.get("rotation", 0)
        if rotated and direction in ("up", "down"):
            source_rotation = (source_rotation + 180) % 360
        if source_rotation:
            exported["rotation"] = source_rotation
        if direction == "down":
            exported["cullface"] = "down"
        exported_faces[direction] = exported

    return {
        "from": start,
        "to": end,
        "faces": exported_faces,
    }


def inner_element(first_element):
    element = bake_element(first_element, "#inner")
    element["from"] = [4.5, 0.5, 4.5]
    element["to"] = [11.5, 3.5, 11.5]
    return element


def model_json(elements, name):
    return {
        "credit": "Converted from mintl.bbmodel",
        "loader": "forge:composite",
        "ambientocclusion": False,
        "textures": {"particle": f"candycraftmod:block/{name}_inner"},
        "children": {
            "inner": {
                "ambientocclusion": False,
                "render_type": "minecraft:cutout",
                "textures": {
                    "inner": f"candycraftmod:block/{name}_inner",
                    "particle": f"candycraftmod:block/{name}_inner",
                },
                "elements": [inner_element(elements[0])],
            },
            "outer": {
                "ambientocclusion": False,
                "render_type": "minecraft:translucent",
                "textures": {
                    "outer": f"candycraftmod:block/{name}",
                    "particle": f"candycraftmod:block/{name}",
                },
                "elements": [bake_element(element, "#outer") for element in elements],
            },
        },
    }


def recolor_with_palette(image, palette):
    source_colors = sorted(
        {pixel[:3] for pixel in image.getdata() if pixel[3] > 0},
        key=luminance,
    )
    if len(source_colors) != len(palette):
        raise ValueError(f"Expected {len(palette)} mint shades, found {len(source_colors)}")
    mapping = dict(zip(source_colors, palette))
    result = Image.new("RGBA", image.size)
    result.putdata([
        (*mapping[pixel[:3]], pixel[3]) if pixel[3] else pixel
        for pixel in image.getdata()
    ])
    return result


def recolor_lemon(image):
    return recolor_with_palette(image, LEMON_PALETTE)


def outer_texture(image):
    result = image.copy()
    result.putdata([
        (red, green, blue, 180) if alpha else (red, green, blue, 0)
        for red, green, blue, alpha in image.getdata()
    ])
    return result


def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--model", required=True, type=Path)
    parser.add_argument("--texture", required=True, type=Path)
    parser.add_argument("--mint-item-texture", required=True, type=Path)
    parser.add_argument("--lemon-item-texture", required=True, type=Path)
    parser.add_argument("--resources", required=True, type=Path)
    args = parser.parse_args()

    project = json.loads(args.model.read_text(encoding="utf-8"))
    if project.get("resolution") != {"width": 32, "height": 32}:
        raise ValueError("Expected a 32x32 Blockbench texture resolution")
    elements = project["elements"]
    if len(elements) != 10:
        raise ValueError(f"Expected 10 model elements, found {len(elements)}")

    mint = Image.open(args.texture).convert("RGBA")
    if mint.size != (32, 32):
        raise ValueError(f"Expected a 32x32 texture, found {mint.size}")
    lemon = recolor_lemon(mint)
    mint_item = Image.open(args.mint_item_texture).convert("RGBA")
    if mint_item.size != (16, 16):
        raise ValueError(f"Expected a 16x16 item texture, found {mint_item.size}")
    lemon_item = Image.open(args.lemon_item_texture).convert("RGBA")
    if lemon_item.size != (16, 16):
        raise ValueError(f"Expected a 16x16 lemon item texture, found {lemon_item.size}")

    block_textures = args.resources / "assets/candycraftmod/textures/block"
    block_models = args.resources / "assets/candycraftmod/models/block"
    item_models = args.resources / "assets/candycraftmod/models/item"
    item_textures = args.resources / "assets/candycraftmod/textures/item"
    blockstates = args.resources / "assets/candycraftmod/blockstates"
    block_textures.mkdir(parents=True, exist_ok=True)
    item_textures.mkdir(parents=True, exist_ok=True)

    for name, image in (("mint_jelly_food", mint), ("lemon_jelly_food", lemon)):
        image.save(block_textures / f"{name}_inner.png")
        outer_texture(image).save(block_textures / f"{name}.png")
        write_json(block_models / f"{name}.json", model_json(elements, name))
        write_json(blockstates / f"{name}.json", {
            "variants": {
                "facing=north": {"model": f"candycraftmod:block/{name}"},
                "facing=east": {"model": f"candycraftmod:block/{name}", "y": 90},
                "facing=south": {"model": f"candycraftmod:block/{name}", "y": 180},
                "facing=west": {"model": f"candycraftmod:block/{name}", "y": 270},
            }
        })

    for name, image in (("mint_jelly_slice", mint_item), ("lemon_jelly_slice", lemon_item)):
        image.save(item_textures / f"{name}.png")
        write_json(item_models / f"{name}.json", {
            "parent": "minecraft:item/generated",
            "textures": {"layer0": f"candycraftmod:item/{name}"},
        })


if __name__ == "__main__":
    main()
