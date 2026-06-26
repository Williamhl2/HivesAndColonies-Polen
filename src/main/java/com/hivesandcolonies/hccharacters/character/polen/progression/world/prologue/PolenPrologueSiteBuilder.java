package com.hivesandcolonies.hccharacters.character.polen.progression.world.prologue;

import com.hivesandcolonies.hccharacters.bootstrap.registry.ModBlocks;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.search.shelter.PolenShelterContextResolver;
import com.hivesandcolonies.hccharacters.character.polen.progression.world.PolenWorldStoryData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;

final class PolenPrologueSiteBuilder {
    private PolenPrologueSiteBuilder() {
    }

    static BlockPos prepareSite(ServerLevel level, PolenWorldStoryData data, BlockPos clearingCenter) {
        if (level == null || data == null || clearingCenter == null) {
            return null;
        }

        if (isStoredPrologueSiteUsable(
                level,
                data.getPrologueClearingCenter(),
                data.getPrologueShelterPos(),
                data.getPrologueBeeBedPos()
        )) {
            return data.getPrologueShelterPos();
        }

        BlockPos normalizedCenter = PolenPrologueSiteLocator.surfacePos(level, clearingCenter);
        if (normalizedCenter == null || !PolenPrologueSiteLocator.isAllowedPrologueBiome(level, normalizedCenter)) {
            return null;
        }

        BlockPos shelterPos = PolenPrologueSiteLocator.resolveShelterPos(level, normalizedCenter);
        if (shelterPos == null) {
            return null;
        }

        clearClearing(level, normalizedCenter);
        seedClearingFlowers(level, normalizedCenter);
        flattenShelterFootprint(level, shelterPos);
        buildShelter(level, shelterPos);
        placeNearbyBeeNest(level, normalizedCenter, shelterPos);
        return shelterPos.immutable();
    }

    static boolean isBeeBedStillPresent(ServerLevel level, BlockPos beeBedPos) {
        return level != null
                && beeBedPos != null
                && level.getBlockState(beeBedPos).is(ModBlocks.POLEN_BEE_BED.get());
    }

    static BlockPos resolveBeeBedMarkerPos(ServerLevel level, BlockPos shelterPos) {
        return level == null || shelterPos == null ? null : PolenShelterContextResolver.findNearbyBeeBed(level, shelterPos);
    }

    private static boolean isStoredPrologueSiteUsable(ServerLevel level, BlockPos clearingCenter, BlockPos shelterPos, BlockPos beeBedPos) {
        if (level == null || clearingCenter == null) {
            return false;
        }
        if (!PolenPrologueSiteLocator.isAllowedPrologueBiome(level, clearingCenter)) {
            return false;
        }
        if (PolenPrologueSiteLocator.surfacePos(level, clearingCenter) == null
                || PolenPrologueSiteLocator.hasNearbyWater(level, clearingCenter, PolenPrologueSiteLayout.SITE_WATER_RADIUS)) {
            return false;
        }
        if (shelterPos != null) {
            if (beeBedPos != null
                    && !isBeeBedStillPresent(level, beeBedPos)
                    && !PolenPrologueSiteLocator.canPlaceShelterFootprint(level, shelterPos)) {
                return false;
            }
            return isShelterStillPresent(level, shelterPos) || PolenPrologueSiteLocator.canPlaceShelterFootprint(level, shelterPos);
        }
        return PolenPrologueSiteLocator.resolveShelterPos(level, clearingCenter) != null;
    }

    private static boolean isShelterStillPresent(ServerLevel level, BlockPos shelterPos) {
        if (level == null || shelterPos == null) {
            return false;
        }
        return !level.canSeeSky(shelterPos.above())
                && PolenShelterContextResolver.hasNearbyBed(level, shelterPos)
                && PolenShelterContextResolver.hasNearbyLight(level, shelterPos);
    }

    private static void clearClearing(ServerLevel level, BlockPos center) {
        for (int dx = -PolenPrologueSiteLayout.CLEARING_RADIUS; dx <= PolenPrologueSiteLayout.CLEARING_RADIUS; dx++) {
            for (int dz = -PolenPrologueSiteLayout.CLEARING_RADIUS; dz <= PolenPrologueSiteLayout.CLEARING_RADIUS; dz++) {
                if (dx * dx + dz * dz > PolenPrologueSiteLayout.CLEARING_RADIUS * PolenPrologueSiteLayout.CLEARING_RADIUS) {
                    continue;
                }

                int x = center.getX() + dx;
                int z = center.getZ() + dz;
                int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                BlockPos standingPos = new BlockPos(x, Math.max(center.getY(), surfaceY), z);
                BlockPos groundPos = standingPos.below();
                BlockState groundState = level.getBlockState(groundPos);

                for (int y = standingPos.getY(); y <= standingPos.getY() + 4; y++) {
                    BlockPos airPos = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(airPos);
                    if (!state.isAir() && (state.is(BlockTags.LEAVES) || state.canBeReplaced())) {
                        setBlock(level, airPos, Blocks.AIR.defaultBlockState());
                    }
                }

                if (groundState.is(Blocks.WATER) || groundState.is(Blocks.LAVA)) {
                    setBlock(level, groundPos, Blocks.DIRT.defaultBlockState());
                    groundState = level.getBlockState(groundPos);
                }

                if (groundState.is(Blocks.GRASS_BLOCK) || groundState.is(Blocks.DIRT) || groundState.is(Blocks.COARSE_DIRT)) {
                    if (dx * dx + dz * dz <= 5) {
                        setBlock(level, groundPos, ((Math.abs(dx) + Math.abs(dz)) & 1) == 0
                                ? Blocks.DIRT_PATH.defaultBlockState()
                                : Blocks.COARSE_DIRT.defaultBlockState());
                    }
                }
            }
        }
    }

    private static void seedClearingFlowers(ServerLevel level, BlockPos center) {
        List<Block> flowers = List.of(Blocks.ALLIUM, Blocks.WHITE_TULIP, Blocks.OXEYE_DAISY, Blocks.AZURE_BLUET);
        int index = 0;
        for (int dx = -PolenPrologueSiteLayout.FLOWER_RING_RADIUS; dx <= PolenPrologueSiteLayout.FLOWER_RING_RADIUS; dx++) {
            for (int dz = -PolenPrologueSiteLayout.FLOWER_RING_RADIUS; dz <= PolenPrologueSiteLayout.FLOWER_RING_RADIUS; dz++) {
                int distanceSq = dx * dx + dz * dz;
                if (distanceSq < 30 || distanceSq > PolenPrologueSiteLayout.FLOWER_RING_RADIUS * PolenPrologueSiteLayout.FLOWER_RING_RADIUS) {
                    continue;
                }
                if (((dx + dz) & 3) != 0) {
                    continue;
                }

                int x = center.getX() + dx;
                int z = center.getZ() + dz;
                int standingY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                BlockPos flowerPos = new BlockPos(x, standingY, z);
                BlockPos groundPos = flowerPos.below();
                BlockState groundState = level.getBlockState(groundPos);
                if (!groundState.isFaceSturdy(level, groundPos, Direction.UP)) {
                    continue;
                }
                if (!level.getBlockState(flowerPos).canBeReplaced()) {
                    continue;
                }
                setBlock(level, flowerPos, flowers.get(index % flowers.size()).defaultBlockState());
                index++;
            }
        }
    }

    private static void flattenShelterFootprint(ServerLevel level, BlockPos shelterPos) {
        int floorY = shelterPos.getY() - 1;
        for (int dx = PolenPrologueSiteLayout.SHELTER_MIN_DX; dx <= PolenPrologueSiteLayout.SHELTER_MAX_DX; dx++) {
            for (int dz = PolenPrologueSiteLayout.SHELTER_MIN_DZ; dz <= PolenPrologueSiteLayout.SHELTER_MAX_DZ; dz++) {
                BlockPos groundPos = shelterPos.offset(dx, -1, dz);
                for (int y = floorY - 2; y < floorY; y++) {
                    BlockPos fillPos = new BlockPos(groundPos.getX(), y, groundPos.getZ());
                    BlockState existing = level.getBlockState(fillPos);
                    if (existing.isAir() || existing.canBeReplaced() || existing.is(Blocks.WATER)) {
                        setBlock(level, fillPos, Blocks.DIRT.defaultBlockState());
                    }
                }

                setBlock(level, groundPos, dz >= 0
                        ? Blocks.DIRT_PATH.defaultBlockState()
                        : PolenProloguePalette.FOUNDATION.defaultBlockState());
                for (int y = floorY + 1; y <= floorY + 4; y++) {
                    setBlock(level, new BlockPos(groundPos.getX(), y, groundPos.getZ()), Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    private static void buildShelter(ServerLevel level, BlockPos shelterPos) {
        placeLogColumn(level, shelterPos.offset(-2, 0, -2), 3);
        placeLogColumn(level, shelterPos.offset(2, 0, -2), 3);
        placeLogColumn(level, shelterPos.offset(-2, 0, 0), 2);
        placeLogColumn(level, shelterPos.offset(2, 0, 0), 2);

        setBlock(level, shelterPos.offset(-1, 0, -2), PolenProloguePalette.FLOOR.defaultBlockState());
        setBlock(level, shelterPos.offset(0, 0, -2), PolenProloguePalette.ACCENT.defaultBlockState());
        setBlock(level, shelterPos.offset(1, 0, -2), PolenProloguePalette.FLOOR.defaultBlockState());

        setBlock(level, shelterPos.offset(-2, 0, -1), PolenProloguePalette.FLOOR.defaultBlockState());
        setBlock(level, shelterPos.offset(2, 0, -1), PolenProloguePalette.FLOOR.defaultBlockState());

        BlockState rearRoof = Blocks.CHERRY_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, Direction.SOUTH)
                .setValue(StairBlock.HALF, Half.TOP);
        BlockState midRoof = Blocks.CHERRY_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
        BlockState frontRoof = Blocks.CHERRY_SLAB.defaultBlockState();
        for (int dx = -2; dx <= 2; dx++) {
            setBlock(level, shelterPos.offset(dx, 3, -2), rearRoof);
            setBlock(level, shelterPos.offset(dx, 2, -1), midRoof);
            setBlock(level, shelterPos.offset(dx, 2, 0), frontRoof);
        }

        placePrologueBed(level, shelterPos);

        BlockState barrel = PolenProloguePalette.STORAGE.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP);
        setBlock(level, shelterPos.offset(-1, 0, -1), Blocks.WHITE_CARPET.defaultBlockState());
        setBlock(level, shelterPos.offset(2, 0, -1), barrel);
        setBlock(level, shelterPos.offset(-2, 0, 1), Blocks.PINK_PETALS.defaultBlockState());
        setBlock(level, shelterPos.offset(-1, 0, 1), Blocks.WHITE_CARPET.defaultBlockState());

        BlockState campfire = Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true);
        setBlock(level, shelterPos.offset(0, 0, 2), PolenProloguePalette.FOUNDATION.defaultBlockState());
        setBlock(level, shelterPos.offset(1, 0, 2), campfire);
    }

    private static void placePrologueBed(ServerLevel level, BlockPos shelterPos) {
        Direction facing = Direction.EAST;
        BlockPos footPos = shelterPos.offset(0, 0, -1);
        BlockPos headPos = footPos.relative(facing);
        BlockState foot = Blocks.WHITE_BED.defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, facing)
                .setValue(BedBlock.PART, BedPart.FOOT);
        BlockState head = Blocks.WHITE_BED.defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, facing)
                .setValue(BedBlock.PART, BedPart.HEAD);
        setBlock(level, footPos, foot);
        setBlock(level, headPos, head);
    }

    private static void placeNearbyBeeNest(ServerLevel level, BlockPos clearingCenter, BlockPos shelterPos) {
        if (level == null || clearingCenter == null || shelterPos == null || hasNearbyHive(level, clearingCenter, 16)) {
            return;
        }

        int[][] offsets = {
                {10, 0},
                {8, 6},
                {-8, 6},
                {-10, 0},
                {8, -6},
                {-8, -6}
        };

        for (int[] offset : offsets) {
            BlockPos standingPos = PolenPrologueSiteLocator.surfacePos(level, clearingCenter.offset(offset[0], 0, offset[1]));
            if (standingPos == null || standingPos.closerToCenterThan(shelterPos.getCenter(), 4.0D)) {
                continue;
            }

            BlockPos trunkBase = standingPos.below();
            if (!level.getBlockState(trunkBase).isFaceSturdy(level, trunkBase, Direction.UP)) {
                continue;
            }

            Direction facing = facingToward(clearingCenter, trunkBase);
            BlockPos nestPos = trunkBase.above(2).relative(facing);
            if (!level.getBlockState(nestPos).canBeReplaced() || !level.getBlockState(trunkBase.above(2)).canBeReplaced()) {
                continue;
            }

            placeLogColumn(level, trunkBase.above(), 3);
            BlockState beeNest = Blocks.BEE_NEST.defaultBlockState().setValue(BeehiveBlock.FACING, facing);
            setBlock(level, nestPos, beeNest);
            return;
        }
    }

    private static boolean hasNearbyHive(ServerLevel level, BlockPos center, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -3; dy <= 3; dy++) {
                    BlockState state = level.getBlockState(center.offset(dx, dy, dz));
                    if (state.is(Blocks.BEE_NEST) || state.is(Blocks.BEEHIVE)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static Direction facingToward(BlockPos target, BlockPos origin) {
        int dx = target.getX() - origin.getX();
        int dz = target.getZ() - origin.getZ();
        if (Math.abs(dx) > Math.abs(dz)) {
            return dx >= 0 ? Direction.EAST : Direction.WEST;
        }
        return dz >= 0 ? Direction.SOUTH : Direction.NORTH;
    }

    private static void placeLogColumn(ServerLevel level, BlockPos basePos, int height) {
        for (int dy = 0; dy < height; dy++) {
            setBlock(level, basePos.above(dy), Blocks.STRIPPED_CHERRY_LOG.defaultBlockState());
        }
    }

    private static void setBlock(ServerLevel level, BlockPos pos, BlockState state) {
        if (!level.getBlockState(pos).equals(state)) {
            level.setBlock(pos, state, 3);
        }
    }
}
