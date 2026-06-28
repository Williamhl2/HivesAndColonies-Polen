from __future__ import annotations

import gzip
import io
import json
import struct
from dataclasses import dataclass
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
TARGET_ROOT = ROOT / "src" / "main" / "resources"
BIOMES = ("plains", "desert", "savanna", "snowy", "taiga")
DATA_VERSION = 3955


TAG_END = 0
TAG_BYTE = 1
TAG_SHORT = 2
TAG_INT = 3
TAG_LONG = 4
TAG_FLOAT = 5
TAG_DOUBLE = 6
TAG_BYTE_ARRAY = 7
TAG_STRING = 8
TAG_LIST = 9
TAG_COMPOUND = 10
TAG_INT_ARRAY = 11
TAG_LONG_ARRAY = 12


@dataclass(frozen=True)
class BlockPlacement:
    x: int
    y: int
    z: int
    state: dict
    nbt: dict | None = None


def write_named_tag(buffer: io.BytesIO, tag_type: int, name: str, payload) -> None:
    buffer.write(struct.pack(">b", tag_type))
    encoded = name.encode("utf-8")
    buffer.write(struct.pack(">H", len(encoded)))
    buffer.write(encoded)
    write_payload(buffer, tag_type, payload)


def write_payload(buffer: io.BytesIO, tag_type: int, payload) -> None:
    if tag_type == TAG_BYTE:
        buffer.write(struct.pack(">b", payload))
    elif tag_type == TAG_SHORT:
        buffer.write(struct.pack(">h", payload))
    elif tag_type == TAG_INT:
        buffer.write(struct.pack(">i", payload))
    elif tag_type == TAG_LONG:
        buffer.write(struct.pack(">q", payload))
    elif tag_type == TAG_STRING:
        encoded = payload.encode("utf-8")
        buffer.write(struct.pack(">H", len(encoded)))
        buffer.write(encoded)
    elif tag_type == TAG_LIST:
        if isinstance(payload, tuple):
            child_type, items = payload
        else:
            items = payload
            child_type = infer_list_child_type(items)
        buffer.write(struct.pack(">b", child_type))
        buffer.write(struct.pack(">i", len(items)))
        for item in items:
            write_payload(buffer, child_type, item)
    elif tag_type == TAG_COMPOUND:
        for key, value in payload.items():
            child_type = infer_tag_type(value)
            write_named_tag(buffer, child_type, key, value)
        buffer.write(struct.pack(">b", TAG_END))
    elif tag_type == TAG_INT_ARRAY:
        buffer.write(struct.pack(">i", len(payload)))
        for item in payload:
            buffer.write(struct.pack(">i", item))
    else:
        raise ValueError(f"Unsupported tag type {tag_type}")


def infer_tag_type(value) -> int:
    if isinstance(value, str):
        return TAG_STRING
    if isinstance(value, dict):
        return TAG_COMPOUND
    if isinstance(value, tuple):
        return TAG_LIST
    if isinstance(value, list):
        if value and all(isinstance(item, int) for item in value):
            return TAG_INT_ARRAY
        return TAG_LIST
    if isinstance(value, int):
        return TAG_INT
    raise TypeError(f"Unsupported payload type: {type(value)!r}")


def infer_list_child_type(items: list) -> int:
    if not items:
        return TAG_END
    return infer_tag_type(items[0])


def make_jigsaw_state(orientation: str) -> dict:
    return {"Name": "minecraft:jigsaw", "Properties": {"orientation": orientation}}


def build_tavern_structure(biome: str) -> dict:
    size_x, size_y, size_z = 13, 11, 11
    blocks: dict[tuple[int, int, int], tuple[dict, dict | None]] = {}

    def place(x: int, y: int, z: int, name: str, properties: dict | None = None, nbt: dict | None = None) -> None:
        state = {"Name": name}
        if properties:
            state["Properties"] = properties
        blocks[(x, y, z)] = (state, nbt)

    def fill_air_column(x: int, z: int, from_y: int, to_y: int) -> None:
        for y in range(from_y, to_y + 1):
            place(x, y, z, "minecraft:air")

    def fill_box(x1: int, y1: int, z1: int, x2: int, y2: int, z2: int, name: str, properties: dict | None = None) -> None:
        for x in range(x1, x2 + 1):
            for y in range(y1, y2 + 1):
                for z in range(z1, z2 + 1):
                    place(x, y, z, name, properties)

    wall = "domum_ornamentum:paper_extra"
    frame = "domum_ornamentum:framed"
    frame_props = {"facing": "up"}
    base = "domum_ornamentum:dark_brick"
    floor = "domum_ornamentum:cream_bricks"
    sill = "domum_ornamentum:sand_stone_bricks"
    beam = "domum_ornamentum:cream_stone_bricks"
    chimney = "domum_ornamentum:roan_bricks"
    barrel = "domum_ornamentum:blockbarreldeco_standing"
    laying_barrel = "domum_ornamentum:blockbarreldeco_onside"
    board = "bountiful:bountyboard"
    counter = "domum_ornamentum:plain"
    counter_props = {"facing": "north"}
    roof_steep = "domum_ornamentum:shingle_steep"
    roof_slab = "domum_ornamentum:shingle_slab"

    if biome in ("desert", "savanna"):
        door = "minecraft:acacia_door"
        step = "minecraft:acacia_stairs"
    elif biome in ("snowy", "taiga"):
        door = "minecraft:spruce_door"
        step = "minecraft:spruce_stairs"
    else:
        door = "minecraft:dark_oak_door"
        step = "minecraft:dark_oak_stairs"

    def frame_column(x: int, z: int, from_y: int = 1, to_y: int = 5) -> None:
        for y in range(from_y, to_y + 1):
            place(x, y, z, frame, frame_props)

    for x in range(size_x):
        for z in range(size_z):
            border = x in (0, size_x - 1) or z in (0, size_z - 1)
            place(x, 0, z, base if border else floor)
            fill_air_column(x, z, 1, 10)

    fill_box(0, 0, 4, 1, 0, 6, floor)
    fill_box(1, 0, 3, 2, 0, 7, floor)

    for x, z in ((1, 1), (1, 9), (11, 1), (11, 9), (1, 4), (1, 6), (11, 5), (4, 1), (8, 1), (4, 9), (8, 9)):
        frame_column(x, z)
    frame_column(0, 4, 1, 3)
    frame_column(0, 6, 1, 3)

    for z in range(1, 10):
        if z != 5:
            place(1, 1, z, sill)
        place(11, 1, z, sill)
    for x in range(2, 11):
        place(x, 1, 1, sill)
        place(x, 1, 9, sill)

    for y in range(2, 5):
        for z in range(1, 10):
            if (1, z) in {(1, 1), (1, 4), (1, 6), (1, 9)} or z == 5:
                continue
            if z in (2, 8) and y in (2, 3):
                place(1, y, z, "minecraft:glass_pane")
            else:
                place(1, y, z, wall)

        for z in range(1, 10):
            if (11, z) in {(11, 1), (11, 5), (11, 9)}:
                continue
            if z in (3, 7) and y in (2, 3):
                place(11, y, z, "minecraft:glass_pane")
            else:
                place(11, y, z, wall)

        for x in range(2, 11):
            if x in (4, 8):
                continue
            if x in (3, 6, 9) and y in (2, 3):
                place(x, y, 1, "minecraft:glass_pane")
                place(x, y, 9, "minecraft:glass_pane")
            else:
                place(x, y, 1, wall)
                place(x, y, 9, wall)

    for x in range(1, 12):
        place(x, 5, 1, beam)
        place(x, 5, 9, beam)
    for z in range(1, 10):
        place(1, 5, z, beam)
        place(11, 5, z, beam)

    for x in (1, 11):
        for y, z1, z2 in ((5, 2, 8), (6, 3, 7), (7, 4, 6), (8, 5, 5)):
            for z in range(z1, z2 + 1):
                if z == 5:
                    place(x, y, z, frame, frame_props)
                else:
                    place(x, y, z, wall)

    place(1, 1, 5, door, {"facing": "west", "half": "lower", "hinge": "left", "open": "false", "powered": "false"})
    place(1, 2, 5, door, {"facing": "west", "half": "upper", "hinge": "left", "open": "false", "powered": "false"})
    place(0, 0, 5, step, {"facing": "west", "half": "bottom", "shape": "straight", "waterlogged": "false"})
    place(0, 0, 4, floor)
    place(0, 0, 6, floor)

    for x in range(size_x):
        place(x, 5, 0, roof_steep, {"facing": "north", "half": "bottom", "shape": "straight"})
        place(x, 5, 10, roof_steep, {"facing": "south", "half": "bottom", "shape": "straight"})
        place(x, 6, 1, roof_steep, {"facing": "north", "half": "bottom", "shape": "straight"})
        place(x, 6, 9, roof_steep, {"facing": "south", "half": "bottom", "shape": "straight"})
        place(x, 7, 2, roof_steep, {"facing": "north", "half": "bottom", "shape": "straight"})
        place(x, 7, 8, roof_steep, {"facing": "south", "half": "bottom", "shape": "straight"})
        place(x, 8, 3, roof_steep, {"facing": "north", "half": "bottom", "shape": "straight"})
        place(x, 8, 7, roof_steep, {"facing": "south", "half": "bottom", "shape": "straight"})
        place(x, 9, 4, roof_steep, {"facing": "north", "half": "bottom", "shape": "straight"})
        place(x, 9, 6, roof_steep, {"facing": "south", "half": "bottom", "shape": "straight"})
        place(x, 10, 5, roof_slab, {"facing": "east", "shape": "top"})

    for chimney_y in range(1, 10):
        place(9, chimney_y, 8, chimney)
    place(9, 10, 8, roof_slab, {"facing": "north", "shape": "top"})

    fill_box(8, 1, 2, 10, 1, 7, counter, counter_props)
    place(9, 2, 2, "minecraft:smoker", {"facing": "west", "lit": "false"})
    place(9, 2, 7, "minecraft:cauldron")
    place(10, 2, 4, barrel, {"facing": "west"})
    place(10, 2, 6, barrel, {"facing": "west"})
    place(9, 1, 8, "minecraft:bookshelf")
    place(10, 1, 8, laying_barrel, {"facing": "north"})
    place(8, 2, 8, "minecraft:potted_fern")

    place(2, 1, 2, board)
    place(3, 1, 2, laying_barrel, {"facing": "south"})
    place(4, 2, 2, "minecraft:potted_red_tulip")
    place(3, 1, 8, laying_barrel, {"facing": "north"})
    place(4, 2, 8, "minecraft:potted_dandelion")

    place(3, 1, 4, step, {"facing": "east", "half": "bottom", "shape": "straight", "waterlogged": "false"})
    place(5, 1, 4, step, {"facing": "west", "half": "bottom", "shape": "straight", "waterlogged": "false"})
    place(4, 1, 4, "minecraft:oak_fence")
    place(4, 2, 4, "minecraft:dark_oak_pressure_plate", {"powered": "false"})
    place(3, 1, 7, step, {"facing": "east", "half": "bottom", "shape": "straight", "waterlogged": "false"})
    place(5, 1, 7, step, {"facing": "west", "half": "bottom", "shape": "straight", "waterlogged": "false"})
    place(4, 1, 7, "minecraft:oak_fence")
    place(4, 2, 7, "minecraft:dark_oak_pressure_plate", {"powered": "false"})

    place(3, 4, 5, "minecraft:lantern", {"hanging": "true", "waterlogged": "false"})
    place(6, 4, 3, "minecraft:lantern", {"hanging": "true", "waterlogged": "false"})
    place(8, 4, 7, "minecraft:lantern", {"hanging": "true", "waterlogged": "false"})

    place(1, 1, 1, barrel, {"facing": "south"})
    place(1, 2, 1, "minecraft:potted_fern")
    place(1, 1, 9, laying_barrel, {"facing": "east"})
    place(11, 1, 1, laying_barrel, {"facing": "west"})
    place(11, 1, 9, barrel, {"facing": "north"})
    place(11, 2, 9, "minecraft:potted_red_mushroom")

    place(
        0,
        0,
        5,
        "minecraft:jigsaw",
        {"orientation": "west_up"},
        {
            "final_state": "minecraft:oak_stairs[facing=east,half=bottom,shape=straight,waterlogged=false]",
            "id": "minecraft:jigsaw",
            "joint": "aligned",
            "name": "minecraft:building_entrance",
            "pool": f"minecraft:village/{biome}/streets",
            "target": "minecraft:building_entrance",
        },
    )
    place(
        6,
        0,
        5,
        "minecraft:jigsaw",
        {"orientation": "up_north"},
        {
            "final_state": "minecraft:oak_planks",
            "id": "minecraft:jigsaw",
            "joint": "rollable",
            "name": "minecraft:bottom",
            "pool": f"minecraft:village/{biome}/villagers",
            "target": "minecraft:bottom",
        },
    )

    palette: list[dict] = []
    palette_lookup: dict[str, int] = {}
    block_entries: list[dict] = []

    def state_key(state: dict) -> str:
        return json.dumps(state, sort_keys=True)

    for y in range(size_y):
        for x in range(size_x):
            for z in range(size_z):
                state, nbt = blocks.get((x, y, z), ({"Name": "minecraft:air"}, None))
                key = state_key(state)
                if key not in palette_lookup:
                    palette_lookup[key] = len(palette)
                    palette.append(state)
                entry = {"pos": (TAG_INT, [x, y, z]), "state": palette_lookup[key]}
                if nbt:
                    entry["nbt"] = nbt
                block_entries.append(entry)

    return {
        "size": (TAG_INT, [size_x, size_y, size_z]),
        "entities": [],
        "blocks": block_entries,
        "palette": palette,
        "DataVersion": DATA_VERSION,
    }


def write_nbt(path: Path, root: dict) -> None:
    payload = io.BytesIO()
    write_named_tag(payload, TAG_COMPOUND, "", root)
    path.parent.mkdir(parents=True, exist_ok=True)
    with gzip.open(path, "wb") as out:
        out.write(payload.getvalue())


def main() -> None:
    for biome in BIOMES:
        structure = build_tavern_structure(biome)
        structure_path = TARGET_ROOT / "data" / "hc_characters" / "structure" / "village" / "taverns" / biome / "lucy_soa_tavern.nbt"
        write_nbt(structure_path, structure)

    print("Generated Lucy tavern structures.")


if __name__ == "__main__":
    main()
