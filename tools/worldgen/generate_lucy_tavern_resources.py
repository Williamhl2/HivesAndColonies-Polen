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
    size_x, size_y, size_z = 13, 10, 11
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

    foundation = "domum_ornamentum:brown_stone_bricks"
    floor = "domum_ornamentum:cream_bricks"
    plaster = "domum_ornamentum:paper_extra"
    trim = "domum_ornamentum:red_brick_extra"
    accent = "domum_ornamentum:sand_bricks"
    chimney = "domum_ornamentum:roan_bricks"
    barrel = "domum_ornamentum:blockbarreldeco_standing"
    laying_barrel = "domum_ornamentum:blockbarreldeco_onside"
    board = "bountiful:bountyboard"
    support = "minecraft:stripped_oak_log"
    roof_stairs = "minecraft:dark_oak_stairs"
    roof_slab = "minecraft:dark_oak_slab"

    for x in range(size_x):
        for z in range(size_z):
            border = x in (0, size_x - 1) or z in (0, size_z - 1)
            place(x, 0, z, foundation if border else floor)
            fill_air_column(x, z, 1, 8)

    fill_box(0, 0, 4, 1, 0, 6, foundation)
    fill_box(1, 0, 4, 2, 0, 6, floor)

    post_x = {0, 3, 9, 12}
    post_z = {0, 3, 7, 10}
    for y in range(1, 6):
        for x in range(size_x):
            for z in (0, size_z - 1):
                if x in post_x:
                    block = support
                elif y == 1:
                    block = trim
                elif y == 5:
                    block = accent
                else:
                    block = plaster
                place(x, y, z, block)
        for z in range(1, size_z - 1):
            for x in (0, size_x - 1):
                if z in post_z:
                    block = support
                elif y == 1:
                    block = trim
                elif y == 5:
                    block = accent
                else:
                    block = plaster
                place(x, y, z, block)

    for z in (4, 5, 6):
        for y in (1, 2):
            place(0, y, z, "minecraft:air")
    place(0, 3, 5, support)
    place(1, 1, 5, "minecraft:dark_oak_slab", {"type": "bottom", "waterlogged": "false"})

    for x in (4, 6, 8):
        for y in (3, 4):
            place(x, y, 0, "minecraft:glass_pane")
            place(x, y, 10, "minecraft:glass_pane")
    for z in (2, 8):
        for y in (3, 4):
            place(12, y, z, "minecraft:glass_pane")
            place(0, y, z, "minecraft:glass_pane")

    place(2, 1, 4, barrel, {"facing": "east"})
    place(2, 1, 6, barrel, {"facing": "east"})

    roof_layers = ((0, 6), (1, 7), (2, 8))
    for inset, y in roof_layers:
        x_min = inset
        x_max = size_x - 1 - inset
        z_min = inset
        z_max = size_z - 1 - inset

        for x in range(x_min, x_max + 1):
            place(x, y, z_min, roof_stairs, {"facing": "north", "half": "bottom", "shape": "straight", "waterlogged": "false"})
            place(x, y, z_max, roof_stairs, {"facing": "south", "half": "bottom", "shape": "straight", "waterlogged": "false"})
        for z in range(z_min + 1, z_max):
            place(x_min, y, z, roof_stairs, {"facing": "west", "half": "bottom", "shape": "straight", "waterlogged": "false"})
            place(x_max, y, z, roof_stairs, {"facing": "east", "half": "bottom", "shape": "straight", "waterlogged": "false"})

    for x in range(3, 10):
        for z in range(3, 8):
            place(x, 9, z, roof_slab, {"type": "top", "waterlogged": "false"})

    for chimney_y in range(1, 9):
        place(10, chimney_y, 2, chimney)
    place(10, 9, 2, roof_slab, {"type": "bottom", "waterlogged": "false"})

    fill_box(9, 1, 2, 11, 1, 3, barrel, {"facing": "north"})
    fill_box(9, 1, 7, 11, 1, 8, barrel, {"facing": "south"})
    place(11, 1, 4, laying_barrel, {"facing": "west"})
    place(11, 1, 5, barrel, {"facing": "west"})
    place(11, 1, 6, laying_barrel, {"facing": "west"})
    place(10, 1, 2, "minecraft:cauldron")
    place(11, 2, 3, "minecraft:smoker", {"facing": "west", "lit": "false"})
    place(9, 2, 8, "minecraft:potted_fern")

    place(1, 1, 2, board)
    place(2, 1, 2, laying_barrel, {"facing": "south"})
    place(3, 2, 2, "minecraft:potted_red_tulip")
    place(2, 1, 8, laying_barrel, {"facing": "north"})
    place(3, 2, 8, "minecraft:potted_dandelion")

    place(3, 1, 4, "minecraft:dark_oak_stairs", {"facing": "east", "half": "bottom", "shape": "straight", "waterlogged": "false"})
    place(5, 1, 4, "minecraft:dark_oak_stairs", {"facing": "west", "half": "bottom", "shape": "straight", "waterlogged": "false"})
    place(4, 1, 4, "minecraft:oak_fence")
    place(4, 2, 4, "minecraft:dark_oak_pressure_plate", {"powered": "false"})

    place(3, 1, 7, "minecraft:dark_oak_stairs", {"facing": "east", "half": "bottom", "shape": "straight", "waterlogged": "false"})
    place(5, 1, 7, "minecraft:dark_oak_stairs", {"facing": "west", "half": "bottom", "shape": "straight", "waterlogged": "false"})
    place(4, 1, 7, "minecraft:oak_fence")
    place(4, 2, 7, "minecraft:dark_oak_pressure_plate", {"powered": "false"})

    place(7, 1, 5, "minecraft:dark_oak_stairs", {"facing": "east", "half": "bottom", "shape": "straight", "waterlogged": "false"})
    place(9, 1, 5, "minecraft:dark_oak_stairs", {"facing": "west", "half": "bottom", "shape": "straight", "waterlogged": "false"})
    place(8, 1, 5, "minecraft:oak_fence")
    place(8, 2, 5, "minecraft:dark_oak_pressure_plate", {"powered": "false"})

    place(7, 1, 8, "minecraft:bookshelf")
    place(8, 1, 8, "minecraft:bookshelf")
    place(7, 2, 8, "minecraft:dark_oak_trapdoor", {"facing": "south", "half": "bottom", "open": "true", "powered": "false", "waterlogged": "false"})
    place(8, 2, 8, "minecraft:dark_oak_trapdoor", {"facing": "south", "half": "bottom", "open": "true", "powered": "false", "waterlogged": "false"})

    place(2, 3, 3, "minecraft:lantern", {"hanging": "true", "waterlogged": "false"})
    place(6, 3, 5, "minecraft:lantern", {"hanging": "true", "waterlogged": "false"})
    place(10, 3, 6, "minecraft:lantern", {"hanging": "true", "waterlogged": "false"})
    place(3, 3, 5, "minecraft:lantern", {"hanging": "true", "waterlogged": "false"})

    place(1, 1, 1, barrel, {"facing": "south"})
    place(1, 2, 1, "minecraft:potted_fern")
    place(11, 1, 9, barrel, {"facing": "north"})
    place(11, 2, 9, "minecraft:potted_red_mushroom")
    place(1, 1, 9, laying_barrel, {"facing": "east"})
    place(11, 1, 1, laying_barrel, {"facing": "west"})

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
                entry = {"pos": [x, y, z], "state": palette_lookup[key]}
                if nbt:
                    entry["nbt"] = nbt
                block_entries.append(entry)

    return {
        "size": [size_x, size_y, size_z],
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
