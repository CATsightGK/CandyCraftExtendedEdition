import argparse
import base64
import json
import shutil
import uuid
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
TEXTURE_ROOT = ROOT / "src/main/resources/assets/candycraftmod/textures/entity"

VARIANTS = {
    "suguard": "sugarde.png",
    "suguard_soldier": "suguardesoldier.png",
    "mage_suguard": "suguardemage.png",
    "boss_suguard_awake": "sugardeboss.png",
    "boss_suguard_sleeping": "sugardeboss1.png",
    "boss_suguard_phase_1": "sugardeboss2.png",
    "boss_suguard_phase_2": "sugardeboss3.png",
    "boss_suguard_phase_3": "sugardeboss4.png",
}

PARTS = [
    ("leg1", [0, 0, 0], [2, 4, 2], [1, 20, -1], [0, 16], [0, 0, 0], None),
    ("leg2", [0, 0, 0], [2, 4, 2], [-3, 20, -1], [0, 16], [0, 0, 0], None),
    ("body", [0, 0, 0], [6, 6, 4], [-3, 14, -2], [0, 6], [0, 0, 0], None),
    ("head", [-1.5, 0, -1.5], [3, 3, 3], [0, 11, 0], [0, 0], [0, 0, 0], None),
    ("nose", [-0.5, 0, -2], [1, 1, 1], [0, 12, 0], [0, 22], [0, 0, 0], None),
    ("hat_brim", [-2, 0, -2], [4, 1, 4], [0, 11, 0], [12, 0], [0, 0, 0], None),
    ("ear_right", [1, 0, -0.5], [1, 2, 1], [0, 12, 0], [4, 22], [0, 0, 0], None),
    ("ear_left", [-2, 2, -0.5], [1, 2, 1], [0, 10, 0], [4, 22], [0, 0, 0], None),
    ("left_arm", [0, 0, 0], [1, 4, 2], [3, 15, 0], [20, 6], [-90, 0, 0], None),
    ("shield", [0, 0, 0], [5, 5, 1], [-2, 5, -1.5], [8, 16], [90, 0, 0], "left_arm"),
    ("right_arm", [0, 0, 0], [1, 5, 2], [-4, 15, 0], [20, 6], [-60.177, 0, 0], None),
    ("hat_top", [-1.5, 0, -1.5], [3, 1, 3], [0, 10, 0], [28, 0], [0, 0, 0], None),
]


def stable_uuid(project_name, item_name):
    return str(uuid.uuid5(uuid.NAMESPACE_URL, f"candycraft:{project_name}:{item_name}"))


def box_faces(uv, size, mirrored=False):
    u, v = uv
    width, height, depth = size
    faces = [
        ["east", [0, depth], [depth, height]],
        ["west", [depth + width, depth], [depth, height]],
        ["up", [depth + width, depth], [-width, -depth]],
        ["down", [depth + width * 2, 0], [-width, depth]],
        ["south", [depth * 2 + width, depth], [width, height]],
        ["north", [depth, depth], [width, height]],
    ]
    if mirrored:
        for face in faces:
            face[1][0] += face[2][0]
            face[2][0] *= -1
        faces[0][1:], faces[1][1:] = faces[1][1:], faces[0][1:]
    return {
        name: {
            "uv": [u + start[0], v + start[1], u + start[0] + face_size[0], v + start[1] + face_size[1]],
            "texture": 0,
        }
        for name, start, face_size in faces
    }


def create_project(project_name, texture_path):
    elements = []
    groups = {}
    absolute_pivots = {}

    for color, (name, cube_from, size, position, uv, java_rotation, parent) in enumerate(PARTS):
        parent_pivot = absolute_pivots[parent] if parent else [0, 0, 0]
        pivot = [parent_pivot[i] + position[i] for i in range(3)]
        absolute_pivots[name] = pivot
        origin = [-pivot[0], 24 - pivot[1], pivot[2]]
        start = [
            origin[0] - cube_from[0] - size[0],
            origin[1] - cube_from[1] - size[1],
            origin[2] + cube_from[2],
        ]
        end = [start[i] + size[i] for i in range(3)]
        rotation = [-java_rotation[0], -java_rotation[1], java_rotation[2]]
        element_uuid = stable_uuid(project_name, f"cube:{name}")
        group_uuid = stable_uuid(project_name, f"group:{name}")
        elements.append({
            "name": name,
            "box_uv": True,
            "rescale": False,
            "locked": False,
            "light_emission": 0,
            "render_order": "default",
            "allow_mirror_modeling": True,
            "from": start,
            "to": end,
            "autouv": 0,
            "color": color % 8,
            "origin": origin,
            "uv_offset": uv,
            "mirror_uv": False,
            "faces": box_faces(uv, size),
            "type": "cube",
            "uuid": element_uuid,
        })
        group = {
            "name": name,
            "origin": origin,
            "color": color % 8,
            "uuid": group_uuid,
            "export": True,
            "isOpen": True,
            "locked": False,
            "visibility": True,
            "autouv": 0,
            "children": [element_uuid],
        }
        if rotation != [0, 0, 0]:
            group["rotation"] = rotation
        groups[name] = (group, parent)

    outliner = []
    for name, (group, parent) in groups.items():
        if parent is None:
            outliner.append(group)
        else:
            groups[parent][0]["children"].append(group)

    texture_bytes = texture_path.read_bytes()
    texture_uuid = stable_uuid(project_name, f"texture:{texture_path.name}")
    texture = {
        "path": texture_path.name,
        "name": texture_path.name,
        "folder": "",
        "namespace": "candycraftmod",
        "id": "0",
        "particle": False,
        "render_mode": "default",
        "visible": True,
        "mode": "bitmap",
        "saved": True,
        "uuid": texture_uuid,
        "source": "data:image/png;base64," + base64.b64encode(texture_bytes).decode("ascii"),
    }

    return {
        "meta": {
            "format_version": "4.10",
            "model_format": "modded_entity",
            "box_uv": True,
        },
        "name": project_name,
        "modded_entity_flip_y": True,
        "model_identifier": f"candycraftmod:{project_name}",
        "credit": "CandyCraft Extended Edition - exported from SuguardModel",
        "visible_box": [1, 1, 0],
        "variable_placeholders": "",
        "variable_placeholder_buttons": [],
        "timeline_setups": [],
        "resolution": {"width": 64, "height": 32},
        "elements": elements,
        "outliner": outliner,
        "textures": [texture],
        "animations": [],
    }


def main():
    parser = argparse.ArgumentParser(description="Export CandyCraft Suguard variants as Blockbench projects.")
    parser.add_argument("--output", type=Path, default=ROOT / "output/blockbench/suguard")
    args = parser.parse_args()
    args.output.mkdir(parents=True, exist_ok=True)

    for project_name, texture_name in VARIANTS.items():
        texture_path = TEXTURE_ROOT / texture_name
        if not texture_path.is_file():
            raise FileNotFoundError(texture_path)
        shutil.copy2(texture_path, args.output / texture_name)
        project = create_project(project_name, texture_path)
        project_path = args.output / f"{project_name}.bbmodel"
        project_path.write_text(json.dumps(project, indent=2, ensure_ascii=True) + "\n", encoding="utf-8")
        print(project_path)

    readme = """CandyCraft Suguard Blockbench exports

Each .bbmodel contains the shared SuguardModel geometry, Box UV mapping, pivots, and an embedded 64x32 texture.
The matching PNG is also included beside each project for direct editing or replacement.

suguard.bbmodel                 -> sugarde.png
suguard_soldier.bbmodel         -> suguardesoldier.png
mage_suguard.bbmodel            -> suguardemage.png
boss_suguard_awake.bbmodel      -> sugardeboss.png
boss_suguard_sleeping.bbmodel   -> sugardeboss1.png
boss_suguard_phase_1.bbmodel    -> sugardeboss2.png
boss_suguard_phase_2.bbmodel    -> sugardeboss3.png
boss_suguard_phase_3.bbmodel    -> sugardeboss4.png

The boss uses the same model geometry and is scaled to 2x by SuguardRenderer in game.
"""
    (args.output / "README.txt").write_text(readme, encoding="utf-8")


if __name__ == "__main__":
    main()
