import json
from pathlib import Path


STAIRS = {
    "marshmallow_stairs": "0",
    "dark_marshmallow_stairs": "1",
    "light_marshmallow_stairs": "2",
}
DIRECTION_Y = {"east": 0, "south": 90, "west": 180, "north": 270}
LEFT_Y = {"east": 270, "south": 0, "west": 90, "north": 180}
TOP_RIGHT_Y = {"east": 90, "south": 180, "west": 270, "north": 0}


def variant(model, x=0, y=0):
    result = {"model": model}
    if x:
        result["x"] = x
    if y:
        result["y"] = y
    if x or y:
        result["uvlock"] = True
    return result


def stairs_blockstate(straight_name, legacy_index):
    variants = {}
    for facing in ("east", "north", "south", "west"):
        for half in ("bottom", "top"):
            for shape in ("straight", "inner_left", "inner_right", "outer_left", "outer_right"):
                if shape == "straight":
                    model_name = straight_name
                    y = DIRECTION_Y[facing]
                elif shape.startswith("inner"):
                    model_name = f"marshmallow_inner_stairs.{legacy_index}"
                    if half == "bottom":
                        y = LEFT_Y[facing] if shape.endswith("left") else DIRECTION_Y[facing]
                    else:
                        y = DIRECTION_Y[facing] if shape.endswith("left") else TOP_RIGHT_Y[facing]
                else:
                    model_name = f"marshmallow_outer_stairs.{legacy_index}"
                    if half == "bottom":
                        y = LEFT_Y[facing] if shape.endswith("left") else DIRECTION_Y[facing]
                    else:
                        y = DIRECTION_Y[facing] if shape.endswith("left") else TOP_RIGHT_Y[facing]
                x = 180 if half == "top" else 0
                variants[f"facing={facing},half={half},shape={shape}"] = variant(
                    f"candycraftmod:block/{model_name}", x, y
                )
    return {"variants": variants}


def main():
    resources = Path(__file__).resolve().parents[1] / "src/main/resources"
    directories = [
        resources / "assets/candycraftmod/blockstates",
        resources / "resourcepacks/candycraft_classic/assets/candycraftmod/blockstates",
    ]
    for directory in directories:
        directory.mkdir(parents=True, exist_ok=True)
        for stairs_name, legacy_index in STAIRS.items():
            output = directory / f"{stairs_name}.json"
            output.write_text(
                json.dumps(stairs_blockstate(stairs_name, legacy_index), indent=2) + "\n",
                encoding="utf-8",
            )


if __name__ == "__main__":
    main()
