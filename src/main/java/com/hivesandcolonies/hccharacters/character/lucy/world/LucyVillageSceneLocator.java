package com.hivesandcolonies.hccharacters.character.lucy.world;

import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.search.shelter.PolenShelterContextResolver;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class LucyVillageSceneLocator {
    private static final int BELL_SCAN_HORIZONTAL_RADIUS = 64;
    private static final int BELL_SCAN_VERTICAL_RADIUS = 10;
    private static final int INTERIOR_SCAN_RADIUS = 18;
    private static final int INTERIOR_SCAN_VERTICAL_RADIUS = 6;
    private static final int ROOM_SCAN_RADIUS = 4;
    private static final int MIN_DECOR_SCORE = 5;
    private static final int MIN_INTERIOR_TILES = 18;
    private static final int MIN_STAND_POSITIONS = 8;
    private static final int MIN_ROOM_WIDTH = 5;
    private static final int MIN_ROOM_DEPTH = 5;
    private static final int TAVERN_INTERIOR_SCAN_RADIUS = 12;
    private static final int TAVERN_INTERIOR_SCAN_VERTICAL_RADIUS = 5;
    private static final int MIN_TAVERN_DECOR_SCORE = 3;
    private static final int MIN_TAVERN_MATERIAL_SCORE = 10;
    private static final int MIN_TAVERN_STAND_POSITIONS = 4;

    private LucyVillageSceneLocator() {
    }

    public static SceneLocation findScene(ServerLevel level, BlockPos origin) {
        if (level == null || origin == null) {
            return null;
        }

        BlockPos bellPos = findNearestBell(level, origin);
        if (bellPos == null) {
            return null;
        }

        BlockPos anchor = findBestGatheringInterior(level, bellPos, origin);
        if (anchor == null) {
            return null;
        }

        BlockPos lucyPos = findStandNear(level, anchor, 3, null);
        if (lucyPos == null) {
            return null;
        }

        BlockPos soaPos = findStandNear(level, anchor, 4, lucyPos);
        if (soaPos == null) {
            return null;
        }

        return new SceneLocation(bellPos.immutable(), anchor.immutable(), lucyPos.immutable(), soaPos.immutable());
    }

    public static SceneLocation findTavernScene(ServerLevel level, BlockPos markerPos, BlockPos bellPos) {
        if (level == null || markerPos == null) {
            return null;
        }

        BlockPos resolvedBell = bellPos != null ? bellPos : findNearestBell(level, markerPos);
        if (resolvedBell == null) {
            return null;
        }

        BlockPos anchor = findBestTavernInterior(level, markerPos, resolvedBell);
        if (anchor == null) {
            return null;
        }

        BlockPos lucyPos = findStandNear(level, anchor, 3, null, true);
        if (lucyPos == null) {
            return null;
        }

        BlockPos soaPos = findStandNear(level, anchor, 4, lucyPos, true);
        if (soaPos == null) {
            return null;
        }

        return new SceneLocation(resolvedBell.immutable(), anchor.immutable(), lucyPos.immutable(), soaPos.immutable());
    }

    public static BlockPos findNearestBell(ServerLevel level, BlockPos origin) {
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int y = -BELL_SCAN_VERTICAL_RADIUS; y <= BELL_SCAN_VERTICAL_RADIUS; y++) {
            for (int x = -BELL_SCAN_HORIZONTAL_RADIUS; x <= BELL_SCAN_HORIZONTAL_RADIUS; x++) {
                for (int z = -BELL_SCAN_HORIZONTAL_RADIUS; z <= BELL_SCAN_HORIZONTAL_RADIUS; z++) {
                    cursor.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    if (!level.getBlockState(cursor).is(Blocks.BELL)) {
                        continue;
                    }
                    double distance = distanceSqr(origin, cursor);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        best = cursor.immutable();
                    }
                }
            }
        }
        return best;
    }

    private static BlockPos findBestGatheringInterior(ServerLevel level, BlockPos bellPos, BlockPos origin) {
        BlockPos best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int y = -INTERIOR_SCAN_VERTICAL_RADIUS; y <= INTERIOR_SCAN_VERTICAL_RADIUS; y++) {
            for (int x = -INTERIOR_SCAN_RADIUS; x <= INTERIOR_SCAN_RADIUS; x++) {
                for (int z = -INTERIOR_SCAN_RADIUS; z <= INTERIOR_SCAN_RADIUS; z++) {
                    cursor.set(bellPos.getX() + x, bellPos.getY() + y, bellPos.getZ() + z);
                    if (!canStandAt(level, cursor) || !PolenShelterContextResolver.isStrongHouseInterior(level, cursor)) {
                        continue;
                    }

                    int decorScore = scoreGatheringDecor(level, cursor);
                    if (decorScore < MIN_DECOR_SCORE) {
                        continue;
                    }

                    RoomMetrics room = measureRoom(level, cursor);
                    if (!room.isSuitableGatheringRoom()) {
                        continue;
                    }

                    double score = decorScore * 18.0D;
                    score += room.interiorTiles() * 1.8D;
                    score += room.standableTiles() * 2.2D;
                    score += room.width() * 10.0D;
                    score += room.depth() * 10.0D;
                    if (PolenShelterContextResolver.hasNearbyBed(level, cursor)) {
                        score += 10.0D;
                    }
                    if (PolenShelterContextResolver.hasNearbyLight(level, cursor)) {
                        score += 8.0D;
                    }
                    score -= distanceSqr(cursor, bellPos) * 0.08D;
                    score -= distanceSqr(cursor, origin) * 0.03D;

                    if (score > bestScore) {
                        bestScore = score;
                        best = cursor.immutable();
                    }
                }
            }
        }

        return best;
    }

    private static int scoreGatheringDecor(ServerLevel level, BlockPos origin) {
        int score = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int y = -2; y <= 2; y++) {
            for (int x = -4; x <= 4; x++) {
                for (int z = -4; z <= 4; z++) {
                    cursor.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    BlockState state = level.getBlockState(cursor);
                    if (state.is(Blocks.BARREL) || state.is(Blocks.CAMPFIRE) || state.is(Blocks.LANTERN) || state.is(Blocks.SOUL_LANTERN)) {
                        score += 2;
                        continue;
                    }

                    ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
                    String path = key.getPath();
                    if (path.contains("table")
                            || path.contains("chair")
                            || path.contains("stool")
                            || path.contains("bench")
                            || path.contains("barrel")
                            || path.contains("board")) {
                        score += 2;
                        continue;
                    }

                    if (isSeatLikeBlock(state.getBlock())) {
                        score += 1;
                    }
                }
            }
        }
        return score;
    }

    private static RoomMetrics measureRoom(ServerLevel level, BlockPos origin) {
        int interiorTiles = 0;
        int standableTiles = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -ROOM_SCAN_RADIUS; x <= ROOM_SCAN_RADIUS; x++) {
            for (int z = -ROOM_SCAN_RADIUS; z <= ROOM_SCAN_RADIUS; z++) {
                cursor.set(origin.getX() + x, origin.getY(), origin.getZ() + z);
                if (!isInteriorCandidate(level, cursor)) {
                    continue;
                }
                interiorTiles++;
                if (canStandAt(level, cursor)) {
                    standableTiles++;
                }
            }
        }

        int width = spanAcross(level, origin, Direction.WEST, Direction.EAST);
        int depth = spanAcross(level, origin, Direction.NORTH, Direction.SOUTH);
        return new RoomMetrics(interiorTiles, standableTiles, width, depth);
    }

    private static int spanAcross(ServerLevel level, BlockPos origin, Direction negative, Direction positive) {
        return 1 + measureSpan(level, origin, negative) + measureSpan(level, origin, positive);
    }

    private static int measureSpan(ServerLevel level, BlockPos origin, Direction direction) {
        int span = 0;
        for (int step = 1; step <= ROOM_SCAN_RADIUS; step++) {
            BlockPos candidate = origin.relative(direction, step);
            if (!isInteriorCandidate(level, candidate)) {
                break;
            }
            span++;
        }
        return span;
    }

    private static boolean isInteriorCandidate(ServerLevel level, BlockPos pos) {
        return PolenShelterContextResolver.isStrongHouseInterior(level, pos)
                && level.getBlockState(pos).isAir()
                && level.getBlockState(pos.above()).isAir();
    }

    private static boolean isSeatLikeBlock(Block block) {
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
        String path = key.getPath();
        return path.endsWith("_stairs")
                || path.endsWith("_slab")
                || path.endsWith("_bench")
                || path.endsWith("_chair");
    }

    private static BlockPos findBestTavernInterior(ServerLevel level, BlockPos markerPos, BlockPos bellPos) {
        BlockPos best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int y = -TAVERN_INTERIOR_SCAN_VERTICAL_RADIUS; y <= TAVERN_INTERIOR_SCAN_VERTICAL_RADIUS; y++) {
            for (int x = -TAVERN_INTERIOR_SCAN_RADIUS; x <= TAVERN_INTERIOR_SCAN_RADIUS; x++) {
                for (int z = -TAVERN_INTERIOR_SCAN_RADIUS; z <= TAVERN_INTERIOR_SCAN_RADIUS; z++) {
                    cursor.set(markerPos.getX() + x, markerPos.getY() + y, markerPos.getZ() + z);
                    if (!canStandAt(level, cursor) || !isTavernInteriorCandidate(level, cursor)) {
                        continue;
                    }

                    int materialScore = countTavernMaterialsNearby(level, cursor);
                    if (materialScore < MIN_TAVERN_MATERIAL_SCORE) {
                        continue;
                    }

                    int decorScore = scoreGatheringDecor(level, cursor);
                    if (decorScore < MIN_TAVERN_DECOR_SCORE) {
                        continue;
                    }

                    int standableTiles = countStandableTavernTiles(level, cursor);
                    if (standableTiles < MIN_TAVERN_STAND_POSITIONS) {
                        continue;
                    }

                    double score = materialScore * 1.5D;
                    score += decorScore * 18.0D;
                    score += standableTiles * 3.0D;
                    if (PolenShelterContextResolver.hasNearbyLight(level, cursor)) {
                        score += 12.0D;
                    }
                    score -= distanceSqr(cursor, markerPos) * 0.15D;
                    score -= distanceSqr(cursor, bellPos) * 0.02D;

                    if (score > bestScore) {
                        bestScore = score;
                        best = cursor.immutable();
                    }
                }
            }
        }

        return best;
    }

    private static boolean isTavernInteriorCandidate(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).isAir()
                && level.getBlockState(pos.above()).isAir()
                && !level.canSeeSky(pos.above())
                && countProtectiveSides(level, pos) >= 2
                && (PolenShelterContextResolver.hasNearbyLight(level, pos)
                || countTavernMaterialsNearby(level, pos) >= MIN_TAVERN_MATERIAL_SCORE);
    }

    private static int countStandableTavernTiles(ServerLevel level, BlockPos origin) {
        int count = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -ROOM_SCAN_RADIUS; x <= ROOM_SCAN_RADIUS; x++) {
            for (int z = -ROOM_SCAN_RADIUS; z <= ROOM_SCAN_RADIUS; z++) {
                cursor.set(origin.getX() + x, origin.getY(), origin.getZ() + z);
                if (canStandAt(level, cursor) && isTavernInteriorCandidate(level, cursor)) {
                    count++;
                }
            }
        }
        return count;
    }

    private static int countTavernMaterialsNearby(ServerLevel level, BlockPos origin) {
        int score = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int y = -2; y <= 5; y++) {
            for (int x = -5; x <= 5; x++) {
                for (int z = -5; z <= 5; z++) {
                    cursor.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    BlockState state = level.getBlockState(cursor);
                    ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
                    String namespace = key.getNamespace();
                    String path = key.getPath();
                    if (namespace.equals("domum_ornamentum")) {
                        score++;
                    } else if (namespace.equals("hc_characters") && path.contains("lantern")) {
                        score += 4;
                    } else if (state.is(Blocks.LANTERN) || state.is(Blocks.SOUL_LANTERN)) {
                        score += 2;
                    } else if (path.contains("barrel") || path.contains("board") || path.contains("bookshelf")) {
                        score++;
                    }
                }
            }
        }
        return score;
    }

    private static int countProtectiveSides(ServerLevel level, BlockPos pos) {
        int sides = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos footPos = pos.relative(direction);
            BlockPos headPos = pos.above().relative(direction);
            if (isProtectiveBlock(level, footPos) || isProtectiveBlock(level, headPos)) {
                sides++;
            }
        }
        return sides;
    }

    private static boolean isProtectiveBlock(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.isAir() && (state.canOcclude() || !state.getCollisionShape(level, pos).isEmpty());
    }

    private static BlockPos findStandNear(ServerLevel level, BlockPos anchor, int radius, BlockPos forbidden) {
        return findStandNear(level, anchor, radius, forbidden, false);
    }

    private static BlockPos findStandNear(ServerLevel level, BlockPos anchor, int radius, BlockPos forbidden, boolean allowTavernInterior) {
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = -1; y <= 2; y++) {
                    BlockPos candidate = anchor.offset(x, y, z);
                    if (forbidden != null && forbidden.equals(candidate)) {
                        continue;
                    }
                    if (!canStandAt(level, candidate)) {
                        continue;
                    }
                    boolean houseInterior = PolenShelterContextResolver.isHouseInterior(level, candidate)
                            || PolenShelterContextResolver.isStrongHouseInterior(level, candidate);
                    boolean tavernInterior = allowTavernInterior && isTavernInteriorCandidate(level, candidate);
                    if (!houseInterior && !tavernInterior) {
                        continue;
                    }
                    double distance = distanceSqr(anchor, candidate);
                    if (distance < 1.0D || distance > bestDistance) {
                        continue;
                    }
                    bestDistance = distance;
                    best = candidate.immutable();
                }
            }
        }
        if (best != null) {
            return best;
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos candidate = anchor.relative(direction);
            if ((forbidden == null || !forbidden.equals(candidate))
                    && canStandAt(level, candidate)
                    && (!allowTavernInterior || isTavernInteriorCandidate(level, candidate))) {
                return candidate.immutable();
            }
        }
        return null;
    }

    private static double distanceSqr(BlockPos a, BlockPos b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        double dz = a.getZ() - b.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    private static boolean canStandAt(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).isAir()
                && level.getBlockState(pos.above()).isAir()
                && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    private record RoomMetrics(int interiorTiles, int standableTiles, int width, int depth) {
        private boolean isSuitableGatheringRoom() {
            return this.interiorTiles >= MIN_INTERIOR_TILES
                    && this.standableTiles >= MIN_STAND_POSITIONS
                    && this.width >= MIN_ROOM_WIDTH
                    && this.depth >= MIN_ROOM_DEPTH;
        }
    }

    public record SceneLocation(BlockPos bellPos, BlockPos anchorPos, BlockPos lucyPos, BlockPos soaPos) {
    }
}
