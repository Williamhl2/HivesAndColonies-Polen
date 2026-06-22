package com.hivesandcolonies.hccharacters.character.polen.progression.world.prologue;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

final class PolenPrologueSiteLocator {
    private PolenPrologueSiteLocator() {
    }

    static BlockPos findHiveheartCherrySpawnPos(ServerLevel level, BlockPos origin) {
        if (level == null || origin == null) {
            return null;
        }

        BlockPos searchOrigin = new BlockPos(origin.getX(), level.getSeaLevel(), origin.getZ());
        Pair<BlockPos, Holder<Biome>> located = locateBiome(level, searchOrigin, Biomes.CHERRY_GROVE);
        if (located == null || located.getFirst() == null) {
            return null;
        }

        return findUsableCherrySurfaceNear(level, located.getFirst());
    }

    static BlockPos resolveShelterPos(ServerLevel level, BlockPos clearingCenter) {
        if (level == null || clearingCenter == null) {
            return null;
        }

        int[][] offsets = {
                {0, -1},
                {0, -2},
                {1, -1},
                {-1, -1},
                {1, -2},
                {-1, -2},
                {0, 0},
                {2, -1},
                {-2, -1}
        };

        for (int[] offset : offsets) {
            BlockPos candidate = surfacePos(level, clearingCenter.offset(offset[0], 0, offset[1]));
            if (candidate != null
                    && candidate.closerToCenterThan(clearingCenter.getCenter(), PolenPrologueSiteLayout.CLEARING_RADIUS + 1.5D)
                    && canPlaceShelterFootprint(level, candidate)) {
                return candidate.immutable();
            }
        }

        for (int dx = -PolenPrologueSiteLayout.SHELTER_SEARCH_RADIUS; dx <= PolenPrologueSiteLayout.SHELTER_SEARCH_RADIUS; dx++) {
            for (int dz = -PolenPrologueSiteLayout.SHELTER_SEARCH_RADIUS; dz <= 0; dz++) {
                BlockPos candidate = surfacePos(level, clearingCenter.offset(dx, 0, dz));
                if (candidate != null && canPlaceShelterFootprint(level, candidate)) {
                    return candidate.immutable();
                }
            }
        }

        return null;
    }

    static BlockPos resolvePrologueSpawnPos(ServerLevel level, BlockPos clearingCenter, BlockPos shelterPos) {
        if (level == null) {
            return null;
        }

        if (shelterPos != null) {
            BlockPos interiorPos = shelterPos.immutable();
            if (isValidSpawnAt(level, interiorPos)) {
                return interiorPos;
            }

            BlockPos frontPos = shelterPos.offset(0, 0, 1);
            if (isValidSpawnAt(level, frontPos)) {
                return frontPos.immutable();
            }
        }

        return isValidSpawnAt(level, clearingCenter) ? clearingCenter.immutable() : null;
    }

    static BlockPos surfacePos(ServerLevel level, BlockPos pos) {
        if (level == null || pos == null) {
            return null;
        }
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        return new BlockPos(pos.getX(), y, pos.getZ());
    }

    static boolean isAllowedPrologueBiome(ServerLevel level, BlockPos pos) {
        return isBiomeMatch(level, pos, Biomes.CHERRY_GROVE) || isBiomeMatch(level, pos, Biomes.MEADOW);
    }

    static boolean canPlaceShelterFootprint(ServerLevel level, BlockPos shelterPos) {
        if (level == null || shelterPos == null || hasNearbyWater(level, shelterPos, 2)) {
            return false;
        }

        int floorY = shelterPos.getY() - 1;
        for (int dx = PolenPrologueSiteLayout.SHELTER_MIN_DX; dx <= PolenPrologueSiteLayout.SHELTER_MAX_DX; dx++) {
            for (int dz = PolenPrologueSiteLayout.SHELTER_MIN_DZ; dz <= PolenPrologueSiteLayout.SHELTER_MAX_DZ; dz++) {
                int x = shelterPos.getX() + dx;
                int z = shelterPos.getZ() + dz;
                int sampleY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                if (Math.abs(sampleY - shelterPos.getY()) > 3) {
                    return false;
                }

                BlockPos standingPos = new BlockPos(x, sampleY, z);
                BlockPos groundPos = standingPos.below();
                BlockState groundState = level.getBlockState(groundPos);
                if (!groundState.isFaceSturdy(level, groundPos, Direction.UP)
                        || groundState.is(Blocks.WATER)
                        || groundState.is(Blocks.LAVA)
                        || !level.canSeeSky(standingPos)) {
                    return false;
                }

                for (int dy = 0; dy <= 4; dy++) {
                    BlockPos checkPos = new BlockPos(x, floorY + 1 + dy, z);
                    BlockState state = level.getBlockState(checkPos);
                    if (state.is(Blocks.WATER) || state.is(Blocks.LAVA)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    static boolean hasNearbyWater(ServerLevel level, BlockPos candidate, int radius) {
        if (level == null || candidate == null) {
            return true;
        }
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int x = candidate.getX() + dx;
                int z = candidate.getZ() + dz;
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                BlockPos standingPos = new BlockPos(x, y, z);
                BlockPos groundPos = standingPos.below();
                if (!level.getFluidState(standingPos).isEmpty()
                        || level.getBlockState(groundPos).is(Blocks.WATER)
                        || level.getBlockState(groundPos).is(Blocks.LAVA)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static BlockPos findUsableCherrySurfaceNear(ServerLevel level, BlockPos biomeCenter) {
        BlockPos directSurface = surfacePos(level, biomeCenter);
        if (isValidSimplePolenSpawnPos(level, directSurface)) {
            return directSurface.immutable();
        }

        for (int radius = PolenPrologueSiteLayout.HIVEHEART_CHERRY_SITE_SCAN_STEP;
             radius <= PolenPrologueSiteLayout.HIVEHEART_CHERRY_SITE_SCAN_RADIUS;
             radius += PolenPrologueSiteLayout.HIVEHEART_CHERRY_SITE_SCAN_STEP) {
            int samples = Math.max(PolenPrologueSiteLayout.BIOME_SAMPLE_STEPS, radius / 2);
            for (int step = 0; step < samples; step++) {
                double angle = step * (Math.PI * 2.0D / samples);
                int x = biomeCenter.getX() + (int) Math.round(Math.cos(angle) * radius);
                int z = biomeCenter.getZ() + (int) Math.round(Math.sin(angle) * radius);
                BlockPos candidate = surfacePos(level, new BlockPos(x, biomeCenter.getY(), z));
                if (isValidSimplePolenSpawnPos(level, candidate)) {
                    return candidate.immutable();
                }
            }
        }

        for (int radius = PolenPrologueSiteLayout.HIVEHEART_CHERRY_SITE_SCAN_STEP; radius <= 64; radius += PolenPrologueSiteLayout.HIVEHEART_CHERRY_SITE_SCAN_STEP) {
            for (int step = 0; step < PolenPrologueSiteLayout.BIOME_SAMPLE_STEPS; step++) {
                double angle = step * (Math.PI * 2.0D / PolenPrologueSiteLayout.BIOME_SAMPLE_STEPS);
                int x = biomeCenter.getX() + (int) Math.round(Math.cos(angle) * radius);
                int z = biomeCenter.getZ() + (int) Math.round(Math.sin(angle) * radius);
                BlockPos candidate = surfacePos(level, new BlockPos(x, biomeCenter.getY(), z));
                if (isLenientCherrySpawnPos(level, candidate)) {
                    return candidate.immutable();
                }
            }
        }

        return null;
    }

    private static boolean isValidSimplePolenSpawnPos(ServerLevel level, BlockPos candidate) {
        if (level == null || candidate == null) {
            return false;
        }
        if (!isBiomeMatch(level, candidate, Biomes.CHERRY_GROVE)) {
            return false;
        }
        if (candidate.getY() < level.getSeaLevel() + 1 || candidate.getY() > 180) {
            return false;
        }
        if (!level.canSeeSky(candidate)) {
            return false;
        }

        BlockPos groundPos = candidate.below();
        BlockState groundState = level.getBlockState(groundPos);
        if (!groundState.isFaceSturdy(level, groundPos, Direction.UP)
                || groundState.is(Blocks.WATER)
                || groundState.is(Blocks.LAVA)) {
            return false;
        }

        BlockState feetState = level.getBlockState(candidate);
        BlockState headState = level.getBlockState(candidate.above());
        return (feetState.isAir() || feetState.canBeReplaced())
                && (headState.isAir() || headState.canBeReplaced());
    }

    private static boolean isLenientCherrySpawnPos(ServerLevel level, BlockPos candidate) {
        if (level == null || candidate == null) {
            return false;
        }
        if (!isBiomeMatch(level, candidate, Biomes.CHERRY_GROVE)) {
            return false;
        }
        if (candidate.getY() < level.getSeaLevel() || candidate.getY() > 190) {
            return false;
        }

        BlockPos groundPos = candidate.below();
        BlockState groundState = level.getBlockState(groundPos);
        if (!groundState.isFaceSturdy(level, groundPos, Direction.UP)
                || groundState.is(Blocks.WATER)
                || groundState.is(Blocks.LAVA)) {
            return false;
        }

        BlockState feetState = level.getBlockState(candidate);
        BlockState headState = level.getBlockState(candidate.above());
        return (feetState.isAir() || feetState.canBeReplaced())
                && (headState.isAir() || headState.canBeReplaced());
    }

    private static boolean isValidSpawnAt(ServerLevel level, BlockPos candidate) {
        if (level == null || candidate == null) {
            return false;
        }

        BlockState feetState = level.getBlockState(candidate);
        BlockState headState = level.getBlockState(candidate.above());
        BlockPos groundPos = candidate.below();
        BlockState groundState = level.getBlockState(groundPos);
        return groundState.isFaceSturdy(level, groundPos, Direction.UP)
                && !groundState.is(Blocks.WATER)
                && !groundState.is(Blocks.LAVA)
                && (feetState.isAir() || feetState.canBeReplaced())
                && (headState.isAir() || headState.canBeReplaced());
    }

    private static Pair<BlockPos, Holder<Biome>> locateBiome(ServerLevel level, BlockPos origin, ResourceKey<Biome> biomeKey) {
        return level.getChunkSource()
                .getGenerator()
                .getBiomeSource()
                .findClosestBiome3d(
                        origin,
                        PolenPrologueSiteLayout.HIVEHEART_CHERRY_SEARCH_RANGE,
                        PolenPrologueSiteLayout.BIOME_LOCATE_VERTICAL_RANGE,
                        PolenPrologueSiteLayout.BIOME_LOCATE_STEP,
                        holder -> holder.is(biomeKey),
                        level.getChunkSource().randomState().sampler(),
                        level
                );
    }

    private static boolean isBiomeMatch(ServerLevel level, BlockPos pos, ResourceKey<Biome> biomeKey) {
        return level != null && pos != null && biomeKey != null && level.getBiome(pos).is(biomeKey);
    }
}
