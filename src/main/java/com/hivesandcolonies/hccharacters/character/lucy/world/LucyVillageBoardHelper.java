package com.hivesandcolonies.hccharacters.character.lucy.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

final class LucyVillageBoardHelper {
    private static final ResourceLocation BOUNTY_BOARD_ID = ResourceLocation.fromNamespaceAndPath("bountiful", "bountyboard");

    private LucyVillageBoardHelper() {
    }

    static void ensureBoardNearAnchor(ServerLevel level, BlockPos anchor) {
        if (level == null || anchor == null) {
            return;
        }

        Block boardBlock = resolveBountyBoardBlock();
        if (boardBlock == null || hasBoardNearby(level, anchor, 10)) {
            return;
        }

        for (int radius = 1; radius <= 4; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos candidate = anchor.offset(x, 0, z);
                    if (!level.getBlockState(candidate).isAir()) {
                        continue;
                    }
                    if (!level.getBlockState(candidate.below()).isFaceSturdy(level, candidate.below(), Direction.UP)) {
                        continue;
                    }
                    level.setBlock(candidate, boardBlock.defaultBlockState(), 3);
                    return;
                }
            }
        }
    }

    static void ensureBoardNearBell(ServerLevel level, BlockPos bellPos) {
        ensureBoard(level, bellPos, 8);
    }

    private static boolean hasBoardNearby(ServerLevel level, BlockPos center, int radius) {
        Block boardBlock = resolveBountyBoardBlock();
        if (boardBlock == null) {
            return false;
        }
        for (int x = -radius; x <= radius; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockState state = level.getBlockState(center.offset(x, y, z));
                    if (state.is(boardBlock)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static void ensureBoard(ServerLevel level, BlockPos center, int radius) {
        Block boardBlock = resolveBountyBoardBlock();
        if (level == null || center == null || boardBlock == null || hasBoardNearby(level, center, radius)) {
            return;
        }

        for (int search = 1; search <= radius; search++) {
            for (int x = -search; x <= search; x++) {
                for (int z = -search; z <= search; z++) {
                    for (int y = -3; y <= 3; y++) {
                        BlockPos candidate = center.offset(x, y, z);
                        if (!level.getBlockState(candidate).isAir()) {
                            continue;
                        }
                        if (!level.getBlockState(candidate.below()).isFaceSturdy(level, candidate.below(), Direction.UP)) {
                            continue;
                        }
                        level.setBlock(candidate, boardBlock.defaultBlockState(), 3);
                        return;
                    }
                }
            }
        }
    }

    private static Block resolveBountyBoardBlock() {
        return BuiltInRegistries.BLOCK.containsKey(BOUNTY_BOARD_ID) ? BuiltInRegistries.BLOCK.get(BOUNTY_BOARD_ID) : null;
    }
}
