import argparse
import json
import math
import shutil
from pathlib import Path

from PIL import Image, ImageDraw


DIRECTION_VECTORS = {
    "north": (0, 0, -1),
    "south": (0, 0, 1),
    "west": (-1, 0, 0),
    "east": (1, 0, 0),
    "up": (0, 1, 0),
    "down": (0, -1, 0),
}
VECTOR_DIRECTIONS = {value: key for key, value in DIRECTION_VECTORS.items()}
QUADRANTS_BY_CUT = {
    1: {"north_west", "north_east", "south_west"},
    2: {"north_west", "south_west"},
    3: {"north_west"},
}
SLIME_ALPHA = 180
INNER_INSET = (1.0, 0.75, 1.0)


def rotate_y(point, origin, angle_degrees):
    angle = math.radians(angle_degrees)
    cosine = round(math.cos(angle))
    sine = round(math.sin(angle))
    x = point[0] - origin[0]
    z = point[2] - origin[2]
    return [
        origin[0] + cosine * x + sine * z,
        point[1],
        origin[2] - sine * x + cosine * z,
    ]


def rotate_direction(direction, angle_degrees):
    vector = DIRECTION_VECTORS[direction]
    rotated = rotate_y(vector, (0, 0, 0), angle_degrees)
    return VECTOR_DIRECTIONS[tuple(int(round(value)) for value in rotated)]


def convert_face(face, direction, angle_degrees, uv_scale):
    if face.get("texture") is None:
        return None
    converted = {"uv": [value * uv_scale for value in face["uv"]], "texture": "#0"}
    if "cullface" in face:
        converted["cullface"] = rotate_direction(face["cullface"], angle_degrees)
    if "tintindex" in face:
        converted["tintindex"] = face["tintindex"]
    existing_rotation = int(face.get("rotation", 0))
    if direction in ("up", "down") and angle_degrees:
        converted["rotation"] = (existing_rotation + int(angle_degrees)) % 360
    elif existing_rotation:
        converted["rotation"] = existing_rotation
    return converted


def bake_element(element, uv_scale):
    angle = element.get("rotation", [0, 0, 0])[1]
    origin = element.get("origin", [8, 8, 8])
    corners = []
    for x in (element["from"][0], element["to"][0]):
        for y in (element["from"][1], element["to"][1]):
            for z in (element["from"][2], element["to"][2]):
                corners.append(rotate_y((x, y, z), origin, angle))
    baked_from = [min(point[index] for point in corners) for index in range(3)]
    baked_to = [max(point[index] for point in corners) for index in range(3)]
    faces = {}
    for direction, face in element.get("faces", {}).items():
        converted = convert_face(face, direction, angle, uv_scale)
        if converted is not None:
            faces[rotate_direction(direction, angle)] = converted
    return {
        "name": element.get("name", "cube"),
        "from": baked_from,
        "to": baked_to,
        "shade": element.get("shade", True),
        "faces": faces,
    }


def quadrant_for(x_index, z_index):
    if x_index == 0 and z_index == 0:
        return "north_west"
    if x_index == 1 and z_index == 0:
        return "north_east"
    if x_index == 0 and z_index == 1:
        return "south_west"
    return "south_east"


def crop_axis(uv_start, uv_end, old_start, old_end, new_start, new_end):
    if old_end == old_start:
        return uv_start, uv_end
    first = (new_start - old_start) / (old_end - old_start)
    second = (new_end - old_start) / (old_end - old_start)
    return (
        uv_start + (uv_end - uv_start) * first,
        uv_start + (uv_end - uv_start) * second,
    )


def crop_faces(element, new_from, new_to):
    old_from = element["from"]
    old_to = element["to"]
    cropped = {}
    for direction, face in element["faces"].items():
        copied = dict(face)
        u1, v1, u2, v2 = face["uv"]
        if direction in ("north", "south"):
            u1, u2 = crop_axis(u1, u2, old_from[0], old_to[0], new_from[0], new_to[0])
            v1, v2 = crop_axis(v1, v2, old_from[1], old_to[1], new_from[1], new_to[1])
        elif direction in ("east", "west"):
            u1, u2 = crop_axis(u1, u2, old_from[2], old_to[2], new_from[2], new_to[2])
            v1, v2 = crop_axis(v1, v2, old_from[1], old_to[1], new_from[1], new_to[1])
        else:
            u1, u2 = crop_axis(u1, u2, old_from[0], old_to[0], new_from[0], new_to[0])
            v1, v2 = crop_axis(v1, v2, old_from[2], old_to[2], new_from[2], new_to[2])
        copied["uv"] = [round(u1, 5), round(v1, 5), round(u2, 5), round(v2, 5)]
        cropped[direction] = copied
    return cropped


def split_element(element, retained_quadrants):
    x_ranges = [(element["from"][0], min(element["to"][0], 8))]
    if element["to"][0] > 8:
        x_ranges.append((max(element["from"][0], 8), element["to"][0]))
    z_ranges = [(element["from"][2], min(element["to"][2], 8))]
    if element["to"][2] > 8:
        z_ranges.append((max(element["from"][2], 8), element["to"][2]))

    pieces = []
    for x_index, x_range in enumerate(x_ranges):
        for z_index, z_range in enumerate(z_ranges):
            if x_range[1] <= x_range[0] or z_range[1] <= z_range[0]:
                continue
            quadrant = quadrant_for(x_index, z_index)
            if quadrant not in retained_quadrants:
                continue
            new_from = [x_range[0], element["from"][1], z_range[0]]
            new_to = [x_range[1], element["to"][1], z_range[1]]
            pieces.append({
                "name": f'{element["name"]}_{quadrant}',
                "from": new_from,
                "to": new_to,
                "shade": element["shade"],
                "faces": crop_faces(element, new_from, new_to),
                "quadrant": quadrant,
            })

    by_quadrant = {piece["quadrant"]: piece for piece in pieces}
    adjacent = (
        ("north_west", "north_east", "east", "west"),
        ("south_west", "south_east", "east", "west"),
        ("north_west", "south_west", "south", "north"),
        ("north_east", "south_east", "south", "north"),
    )
    for first, second, first_face, second_face in adjacent:
        if first in by_quadrant and second in by_quadrant:
            by_quadrant[first]["faces"].pop(first_face, None)
            by_quadrant[second]["faces"].pop(second_face, None)
    for piece in pieces:
        piece.pop("quadrant")
    return pieces


def inset_element(element):
    new_from = [
        element["from"][axis] + INNER_INSET[axis]
        for axis in range(3)
    ]
    new_to = [
        element["to"][axis] - INNER_INSET[axis]
        for axis in range(3)
    ]
    faces = crop_faces(element, new_from, new_to)
    for face in faces.values():
        face["texture"] = "#inner"
    return {
        "name": f'{element["name"]}_inner',
        "from": new_from,
        "to": new_to,
        "shade": element["shade"],
        "faces": faces,
    }


def jelly_state(outer_elements, inner_element, cuts):
    if cuts == 0:
        return [inner_element, *outer_elements]

    retained_quadrants = QUADRANTS_BY_CUT[cuts]
    inner_pieces = split_element(inner_element, retained_quadrants)
    outer_pieces = []
    for element in outer_elements:
        outer_pieces.extend(split_element(element, retained_quadrants))
    return [*inner_pieces, *outer_pieces]


def minecraft_element(element):
    result = {
        "from": [round(value, 5) for value in element["from"]],
        "to": [round(value, 5) for value in element["to"]],
        "faces": element["faces"],
    }
    if not element["shade"]:
        result["shade"] = False
    return result


def model_part(elements, render_type, texture_key, texture_path):
    return {
        "ambientocclusion": False,
        "render_type": render_type,
        "textures": {
            texture_key: texture_path,
            "particle": texture_path,
        },
        "elements": [minecraft_element(element) for element in elements],
    }


def model_json(elements, texture_name):
    inner_elements = [
        element for element in elements
        if any(face["texture"] == "#inner" for face in element["faces"].values())
    ]
    outer_elements = [
        element for element in elements
        if not any(face["texture"] == "#inner" for face in element["faces"].values())
    ]
    return {
        "credit": "Converted from the provided Blockbench model",
        "loader": "forge:composite",
        "ambientocclusion": False,
        "textures": {
            "particle": f"candycraftmod:block/{texture_name}_inner",
        },
        "children": {
            "inner": model_part(
                inner_elements,
                "minecraft:cutout",
                "inner",
                f"candycraftmod:block/{texture_name}_inner",
            ),
            "outer": model_part(
                outer_elements,
                "minecraft:translucent",
                "0",
                f"candycraftmod:block/{texture_name}",
            ),
        },
        "item_render_order": ["inner", "outer"],
    }


def project(point, center_x, center_y, scale):
    x, y, z = point
    return (
        center_x + (x - z) * scale,
        center_y + (x + z) * scale * 0.48 - y * scale,
    )


def render_preview(states, texture_path, output_path):
    texture = Image.open(texture_path).convert("RGBA")
    visible = [pixel for pixel in texture.get_flattened_data() if pixel[3] > 0]
    base = tuple(sum(pixel[index] for pixel in visible) // len(visible) for index in range(3))
    canvas = Image.new("RGBA", (800, 260), (38, 38, 38, 255))
    draw = ImageDraw.Draw(canvas, "RGBA")
    labels = ["full", "3/4", "1/2", "1/4"]
    for state_index, elements in enumerate(states):
        center_x = 100 + state_index * 200
        center_y = 160
        scale = 5.0
        ordered = sorted(elements, key=lambda item: sum(item["from"]) + sum(item["to"]))
        for element in ordered:
            x1, y1, z1 = element["from"]
            x2, y2, z2 = element["to"]
            top = [
                project((x1, y2, z1), center_x, center_y, scale),
                project((x2, y2, z1), center_x, center_y, scale),
                project((x2, y2, z2), center_x, center_y, scale),
                project((x1, y2, z2), center_x, center_y, scale),
            ]
            east = [
                project((x2, y1, z1), center_x, center_y, scale),
                project((x2, y2, z1), center_x, center_y, scale),
                project((x2, y2, z2), center_x, center_y, scale),
                project((x2, y1, z2), center_x, center_y, scale),
            ]
            south = [
                project((x1, y1, z2), center_x, center_y, scale),
                project((x1, y2, z2), center_x, center_y, scale),
                project((x2, y2, z2), center_x, center_y, scale),
                project((x2, y1, z2), center_x, center_y, scale),
            ]
            draw.polygon(east, fill=(*[max(0, value - 48) for value in base], 150))
            draw.polygon(south, fill=(*[max(0, value - 25) for value in base], 155))
            draw.polygon(top, fill=(*base, 175))
        draw.text((center_x - 14, 225), labels[state_index], fill=(245, 245, 245, 255))
    output_path.parent.mkdir(parents=True, exist_ok=True)
    canvas.save(output_path)


def write_slime_texture(texture, output_path):
    pixels = [
        (red, green, blue, SLIME_ALPHA if alpha > 0 else 0)
        for red, green, blue, alpha in texture.get_flattened_data()
    ]
    texture.putdata(pixels)
    texture.save(output_path)


def luminance(color):
    red, green, blue = color
    return 0.2126 * red + 0.7152 * green + 0.0722 * blue


def recolor_from_reference(source, reference_path):
    reference = Image.open(reference_path).convert("RGBA")
    source_colors = sorted(
        {(red, green, blue) for red, green, blue, alpha in source.get_flattened_data() if alpha > 0},
        key=luminance,
    )
    reference_colors = sorted(
        [(red, green, blue) for red, green, blue, alpha in reference.get_flattened_data() if alpha > 0],
        key=luminance,
    )
    mapping = {}
    for index, color in enumerate(source_colors):
        quantile = index / max(1, len(source_colors) - 1)
        mapping[color] = reference_colors[round(quantile * (len(reference_colors) - 1))]
    recolored = Image.new("RGBA", source.size)
    recolored.putdata([
        (*mapping[(red, green, blue)], alpha) if alpha > 0 else (0, 0, 0, 0)
        for red, green, blue, alpha in source.get_flattened_data()
    ])
    return recolored


def write_fragment_texture(output_path):
    texture = Image.new("RGBA", (8, 8), (255, 255, 255, 0))
    pixels = []
    for y in range(8):
        for x in range(8):
            edge = min(x, y, 7 - x, 7 - y)
            alpha = 92 if edge == 0 else 164 if edge == 1 else 218
            pixels.append((255, 255, 255, alpha))
    texture.putdata(pixels)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    texture.save(output_path)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("model", type=Path)
    parser.add_argument("texture", type=Path)
    parser.add_argument("resources", type=Path)
    parser.add_argument("preview", type=Path)
    args = parser.parse_args()

    source = json.loads(args.model.read_text(encoding="utf-8"))
    uv_scale = 16.0 / source.get("resolution", {}).get("width", 16)
    baked = [bake_element(element, uv_scale) for element in source["elements"] if element.get("export", True)]
    inner = inset_element(baked[0])
    states = [jelly_state(baked, inner, cuts) for cuts in range(4)]

    models_dir = args.resources / "assets/candycraftmod/models/block"
    blockstates_dir = args.resources / "assets/candycraftmod/blockstates"
    textures_dir = args.resources / "assets/candycraftmod/textures/block"
    models_dir.mkdir(parents=True, exist_ok=True)
    blockstates_dir.mkdir(parents=True, exist_ok=True)
    textures_dir.mkdir(parents=True, exist_ok=True)

    strawberry_texture = Image.open(args.texture).convert("RGBA")
    item_textures_dir = args.resources / "assets/candycraftmod/textures/item"
    textures = {
        "strawberry_jelly_block": strawberry_texture,
        "caramel_jelly_block": recolor_from_reference(strawberry_texture, item_textures_dir / "caramel_jelly.png"),
        "royal_rations_block": recolor_from_reference(strawberry_texture, item_textures_dir / "royal_rations.png"),
    }
    for texture_name, texture in textures.items():
        names = [texture_name, *[f"{texture_name}_cut_{cuts}" for cuts in range(1, 4)]]
        for name, elements in zip(names, states):
            (models_dir / f"{name}.json").write_text(
                json.dumps(model_json(elements, texture_name), indent=2) + "\n",
                encoding="utf-8",
            )
        variants = {f"cuts={index}": {"model": f"candycraftmod:block/{name}"} for index, name in enumerate(names)}
        (blockstates_dir / f"{texture_name}.json").write_text(
            json.dumps({"variants": variants}, indent=2) + "\n", encoding="utf-8"
        )
        write_slime_texture(texture.copy(), textures_dir / f"{texture_name}.png")
        if texture_name == "strawberry_jelly_block":
            shutil.copyfile(args.texture, textures_dir / f"{texture_name}_inner.png")
        else:
            texture.save(textures_dir / f"{texture_name}_inner.png")

    write_fragment_texture(args.resources / "assets/candycraftmod/textures/particle/jelly_fragment.png")
    render_preview(states, args.texture, args.preview)


if __name__ == "__main__":
    main()
