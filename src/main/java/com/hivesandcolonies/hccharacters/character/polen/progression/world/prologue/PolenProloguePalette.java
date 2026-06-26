package com.hivesandcolonies.hccharacters.character.polen.progression.world.prologue;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

final class PolenProloguePalette {
    static final Block FOUNDATION = resolve("brown_stone_bricks", Blocks.CHERRY_PLANKS);
    static final Block FLOOR = resolve("cream_bricks", Blocks.CHERRY_PLANKS);
    static final Block ACCENT = resolve("roan_bricks", Blocks.WHITE_WOOL);
    static final Block STORAGE = resolve("blockbarreldeco_standing", Blocks.BARREL);

    private PolenProloguePalette() {
    }

    private static Block resolve(String path, Block fallback) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("domum_ornamentum", path);
        return BuiltInRegistries.BLOCK.containsKey(id) ? BuiltInRegistries.BLOCK.get(id) : fallback;
    }
}
