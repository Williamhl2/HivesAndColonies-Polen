package com.hivesandcolonies.hccharacters.character.lucy.world;

import java.util.Optional;

import com.hivesandcolonies.hccharacters.bootstrap.HcCharacters;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.JigsawReplacementProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/**
 * Runtime fallback for the Lucy + Soa village tavern.
 *
 * The normal path still injects the tavern into the vanilla village house pools.
 * This class handles the cases where vanilla jigsaw generation rejects the piece
 * or the player visits a village that was already generated before the mod change.
 */
final class LucyVillageTavernStructurePlacer {
    private static final String TEMPLATE_FOLDER = "village/taverns/";
    private static final int[] FORWARD_DISTANCES = {10, 12, 16, 20, 24, 28, 32, 36, 42, 48, 56, 64, 72};
    private static final int[] LATERAL_OFFSETS = {0, 4, -4, 8, -8, 12, -12, 16, -16, 20, -20, 24, -24, 32, -32};
    private static final int MAX_SURFACE_DELTA = 12;
    private static final int MAX_BLOCKING_BLOCKS = 360;
    private static final int PLACE_FLAGS = 3;

    private static final BlockPos ENTRANCE_LOCAL_POS = new BlockPos(0, 0, 5);
    private static final BlockPos MARKER_LOCAL_POS = new BlockPos(3, 3, 5);
    private static final BlockPos ANCHOR_LOCAL_POS = new BlockPos(6, 2, 5);
    private static final BlockPos LUCY_LOCAL_POS = new BlockPos(5, 2, 5);
    private static final BlockPos SOA_LOCAL_POS = new BlockPos(6, 2, 4);

    private LucyVillageTavernStructurePlacer() {
    }

    static LucyVillageSceneLocator.SceneLocation tryPlaceNearVillage(ServerLevel level, BlockPos bellPos) {
        if (level == null || bellPos == null) {
            return null;
        }

        String villageType = resolveVillageType(level, bellPos);
        ResourceLocation templateId = ResourceLocation.fromNamespaceAndPath(
                HcCharacters.MODID,
                TEMPLATE_FOLDER + villageType + "/lucy_soa_tavern"
        );

        Optional<StructureTemplate> loadedTemplate = level.getStructureManager().get(templateId);
        if (loadedTemplate.isEmpty()) {
            HcCharacters.LOGGER.warn("Lucy/Soa tavern fallback could not load structure template {}", templateId);
            return null;
        }

        StructureTemplate template = loadedTemplate.get();
        Vec3i size = template.getSize();
        if (size.getX() <= 0 || size.getY() <= 0 || size.getZ() <= 0) {
            HcCharacters.LOGGER.warn("Lucy/Soa tavern fallback loaded an empty structure template {}", templateId);
            return null;
        }

        PlacementCandidate candidate = findPlacementCandidate(level, bellPos, size);
        if (candidate == null) {
            HcCharacters.LOGGER.warn("Lucy/Soa tavern fallback found no usable placement near village bell {}. No tavern was placed.", bellPos);
            return null;
        }

        RandomSource random = RandomSource.create(level.getSeed() ^ bellPos.asLong() ^ candidate.origin().asLong());
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setMirror(Mirror.NONE)
                .setRotation(candidate.rotation())
                .setRotationPivot(BlockPos.ZERO)
                .setIgnoreEntities(true)
                .setKnownShape(true)
                .setFinalizeEntities(false)
                .addProcessor(JigsawReplacementProcessor.INSTANCE)
                .setRandom(random);

        boolean placed = template.placeInWorld(level, candidate.origin(), candidate.origin(), settings, random, PLACE_FLAGS);
        if (!placed) {
            HcCharacters.LOGGER.warn("Lucy/Soa tavern fallback failed to place {} at {}", templateId, candidate.origin());
            return null;
        }

        BlockPos markerPos = candidate.localToWorld(MARKER_LOCAL_POS);
        LucyVillageBoardHelper.ensureBoardNearAnchor(level, markerPos);
        LucyVillageSceneLocator.SceneLocation scene = directSceneLocation(level, bellPos, candidate);
        if (scene == null) {
            scene = LucyVillageSceneLocator.findTavernScene(level, markerPos, bellPos);
        }

        if (scene == null) {
            HcCharacters.LOGGER.warn(
                    "Lucy/Soa tavern fallback placed {} at {}, but no valid scene positions were found around marker {}",
                    templateId,
                    candidate.origin(),
                    markerPos
            );
            return null;
        }

        HcCharacters.LOGGER.info(
                "Placed Lucy/Soa tavern fallback {} near village bell {} at {}",
                templateId,
                bellPos,
                candidate.origin()
        );
        return scene;
    }

    private static LucyVillageSceneLocator.SceneLocation directSceneLocation(
            ServerLevel level,
            BlockPos bellPos,
            PlacementCandidate candidate
    ) {
        BlockPos anchor = candidate.localToWorld(ANCHOR_LOCAL_POS);
        BlockPos lucy = candidate.localToWorld(LUCY_LOCAL_POS);
        BlockPos soa = candidate.localToWorld(SOA_LOCAL_POS);
        if (canStandAt(level, anchor) && canStandAt(level, lucy) && canStandAt(level, soa)) {
            return new LucyVillageSceneLocator.SceneLocation(
                    bellPos.immutable(),
                    anchor.immutable(),
                    lucy.immutable(),
                    soa.immutable()
            );
        }
        return null;
    }

    private static PlacementCandidate findPlacementCandidate(ServerLevel level, BlockPos bellPos, Vec3i size) {
        PlacementCandidate best = null;
        PlacementCandidate relaxedBest = null;
        PlacementCandidate emergencyBest = null;
        int bestScore = Integer.MAX_VALUE;
        int relaxedBestScore = Integer.MAX_VALUE;
        int emergencyBestScore = Integer.MAX_VALUE;

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            Rotation rotation = rotationForDirection(direction);
            Direction lateralDirection = clockwise(direction);
            for (int distance : FORWARD_DISTANCES) {
                for (int lateralOffset : LATERAL_OFFSETS) {
                    BlockPos entranceBase = bellPos.relative(direction, distance)
                            .relative(lateralDirection, lateralOffset);
                    int surfaceY = getSurfaceY(level, entranceBase.getX(), entranceBase.getZ());
                    if (surfaceY <= level.getMinBuildHeight()) {
                        continue;
                    }

                    BlockPos entrance = new BlockPos(entranceBase.getX(), surfaceY, entranceBase.getZ());
                    BlockPos origin = subtract(entrance, transformLocal(ENTRANCE_LOCAL_POS, rotation));
                    CandidateScore score = scoreCandidate(level, origin, size, rotation, bellPos);
                    if (score.usable() && score.score() < bestScore) {
                        bestScore = score.score();
                        best = new PlacementCandidate(origin, rotation);
                    }

                    CandidateScore relaxedScore = scoreRelaxedCandidate(level, origin, size, rotation, bellPos);
                    if (relaxedScore.usable() && relaxedScore.score() < relaxedBestScore) {
                        relaxedBestScore = relaxedScore.score();
                        relaxedBest = new PlacementCandidate(origin, rotation);
                    }

                    CandidateScore emergencyScore = scoreEmergencyCandidate(level, origin, size, rotation, bellPos);
                    if (emergencyScore.usable() && emergencyScore.score() < emergencyBestScore) {
                        emergencyBestScore = emergencyScore.score();
                        emergencyBest = new PlacementCandidate(origin, rotation);
                    }
                }
            }
        }

        return best != null ? best : relaxedBest != null ? relaxedBest : emergencyBest;
    }

    private static CandidateScore scoreCandidate(
            ServerLevel level,
            BlockPos origin,
            Vec3i size,
            Rotation rotation,
            BlockPos bellPos
    ) {
        Bounds bounds = Bounds.from(origin, size, rotation);
        BlockPos min = new BlockPos(bounds.minX(), origin.getY(), bounds.minZ());
        BlockPos max = new BlockPos(bounds.maxX(), origin.getY() + size.getY() + 2, bounds.maxZ());
        if (!level.hasChunksAt(min, max)) {
            return CandidateScore.unusable();
        }

        int minSurface = Integer.MAX_VALUE;
        int maxSurface = Integer.MIN_VALUE;
        int blockingBlocks = 0;
        int criticalBlocks = 0;
        int liquids = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
            for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                int surfaceY = getSurfaceY(level, x, z);
                minSurface = Math.min(minSurface, surfaceY);
                maxSurface = Math.max(maxSurface, surfaceY);

                for (int y = origin.getY() + 2; y <= origin.getY() + size.getY(); y++) {
                    cursor.set(x, y, z);
                    BlockState state = level.getBlockState(cursor);
                    if (!state.getFluidState().isEmpty()) {
                        liquids++;
                    }
                    if (isCriticalVillageBlock(state)) {
                        criticalBlocks++;
                    }
                    if (!state.isAir() && !state.getCollisionShape(level, cursor).isEmpty()) {
                        blockingBlocks++;
                    }
                }
            }
        }

        int surfaceDelta = maxSurface - minSurface;
        if (liquids > 0 || criticalBlocks > 0 || surfaceDelta > MAX_SURFACE_DELTA || blockingBlocks > MAX_BLOCKING_BLOCKS) {
            return CandidateScore.unusable();
        }

        int distancePenalty = Math.min(400, distanceSqr(bellPos, origin));
        int score = blockingBlocks * 6 + surfaceDelta * 35 + Math.abs(origin.getY() - bellPos.getY()) * 12 + distancePenalty;
        return new CandidateScore(true, score);
    }

    private static CandidateScore scoreRelaxedCandidate(
            ServerLevel level,
            BlockPos origin,
            Vec3i size,
            Rotation rotation,
            BlockPos bellPos
    ) {
        Bounds bounds = Bounds.from(origin, size, rotation);
        BlockPos min = new BlockPos(bounds.minX(), origin.getY(), bounds.minZ());
        BlockPos max = new BlockPos(bounds.maxX(), origin.getY() + size.getY() + 2, bounds.maxZ());
        if (!level.hasChunksAt(min, max)) {
            return CandidateScore.unusable();
        }

        int minSurface = Integer.MAX_VALUE;
        int maxSurface = Integer.MIN_VALUE;
        int blockingBlocks = 0;
        int criticalBlocks = 0;
        int liquids = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
            for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                int surfaceY = getSurfaceY(level, x, z);
                minSurface = Math.min(minSurface, surfaceY);
                maxSurface = Math.max(maxSurface, surfaceY);

                for (int y = origin.getY() + 1; y <= origin.getY() + size.getY(); y++) {
                    cursor.set(x, y, z);
                    BlockState state = level.getBlockState(cursor);
                    if (!state.getFluidState().isEmpty()) {
                        liquids++;
                    }
                    if (isCriticalVillageBlock(state)) {
                        criticalBlocks++;
                    }
                    if (!state.isAir() && !state.getCollisionShape(level, cursor).isEmpty()) {
                        blockingBlocks++;
                    }
                }
            }
        }

        int surfaceDelta = maxSurface - minSurface;
        if (liquids > 0 || criticalBlocks > 0 || surfaceDelta > MAX_SURFACE_DELTA + 6) {
            return CandidateScore.unusable();
        }

        int distancePenalty = Math.min(900, distanceSqr(bellPos, origin));
        int score = blockingBlocks * 2 + surfaceDelta * 25 + Math.abs(origin.getY() - bellPos.getY()) * 10 + distancePenalty;
        return new CandidateScore(true, score);
    }

    private static CandidateScore scoreEmergencyCandidate(
            ServerLevel level,
            BlockPos origin,
            Vec3i size,
            Rotation rotation,
            BlockPos bellPos
    ) {
        Bounds bounds = Bounds.from(origin, size, rotation);
        BlockPos min = new BlockPos(bounds.minX(), origin.getY(), bounds.minZ());
        BlockPos max = new BlockPos(bounds.maxX(), origin.getY() + size.getY() + 2, bounds.maxZ());
        if (!level.hasChunksAt(min, max)) {
            return CandidateScore.unusable();
        }

        int minSurface = Integer.MAX_VALUE;
        int maxSurface = Integer.MIN_VALUE;
        int criticalBlocks = 0;
        int liquids = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int x = bounds.minX(); x <= bounds.maxX(); x++) {
            for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                int surfaceY = getSurfaceY(level, x, z);
                minSurface = Math.min(minSurface, surfaceY);
                maxSurface = Math.max(maxSurface, surfaceY);

                cursor.set(x, surfaceY, z);
                BlockState surfaceState = level.getBlockState(cursor);
                if (!surfaceState.getFluidState().isEmpty()) {
                    liquids++;
                }

                for (int y = origin.getY() + 1; y <= origin.getY() + Math.min(size.getY(), 5); y++) {
                    cursor.set(x, y, z);
                    BlockState state = level.getBlockState(cursor);
                    if (isCriticalVillageBlock(state)) {
                        criticalBlocks++;
                    }
                }
            }
        }

        int surfaceDelta = maxSurface - minSurface;
        if (liquids > 12 || criticalBlocks > 0 || surfaceDelta > MAX_SURFACE_DELTA + 12) {
            return CandidateScore.unusable();
        }

        int distancePenalty = Math.min(1600, distanceSqr(bellPos, origin));
        int score = surfaceDelta * 40 + Math.abs(origin.getY() - bellPos.getY()) * 20 + distancePenalty;
        return new CandidateScore(true, score);
    }

    private static boolean isCriticalVillageBlock(BlockState state) {
        if (state.is(Blocks.BELL)
                || state.is(Blocks.CHEST)
                || state.is(Blocks.TRAPPED_CHEST)
                || state.is(Blocks.FURNACE)
                || state.is(Blocks.BLAST_FURNACE)
                || state.is(Blocks.SMOKER)) {
            return true;
        }

        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        return path.endsWith("_bed") || path.contains("bountyboard");
    }

    private static boolean canStandAt(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).isAir()
                && level.getBlockState(pos.above()).isAir()
                && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    private static int getSurfaceY(ServerLevel level, int x, int z) {
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
    }

    private static String resolveVillageType(ServerLevel level, BlockPos bellPos) {
        ResourceLocation biomeId = level.registryAccess()
                .registryOrThrow(Registries.BIOME)
                .getKey(level.getBiome(bellPos).value());
        String biomePath = biomeId == null ? "" : biomeId.getPath();

        if (biomePath.contains("desert") || biomePath.contains("badlands")) {
            return "desert";
        }
        if (biomePath.contains("savanna")) {
            return "savanna";
        }
        if (biomePath.contains("snow") || biomePath.contains("ice") || biomePath.contains("frozen")) {
            return "snowy";
        }
        if (biomePath.contains("taiga") || biomePath.contains("spruce") || biomePath.contains("pine")) {
            return "taiga";
        }
        return "plains";
    }

    private static int distanceSqr(BlockPos first, BlockPos second) {
        int dx = first.getX() - second.getX();
        int dy = first.getY() - second.getY();
        int dz = first.getZ() - second.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    private static Rotation rotationForDirection(Direction direction) {
        return switch (direction) {
            case EAST -> Rotation.NONE;
            case SOUTH -> Rotation.CLOCKWISE_90;
            case WEST -> Rotation.CLOCKWISE_180;
            case NORTH -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    private static Direction clockwise(Direction direction) {
        return switch (direction) {
            case NORTH -> Direction.EAST;
            case EAST -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.NORTH;
            default -> Direction.EAST;
        };
    }

    private static BlockPos transformLocal(BlockPos localPos, Rotation rotation) {
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setMirror(Mirror.NONE)
                .setRotation(rotation)
                .setRotationPivot(BlockPos.ZERO);
        return StructureTemplate.calculateRelativePosition(settings, localPos);
    }

    private static BlockPos subtract(BlockPos pos, BlockPos offset) {
        return new BlockPos(pos.getX() - offset.getX(), pos.getY() - offset.getY(), pos.getZ() - offset.getZ());
    }

    private static BlockPos add(BlockPos pos, BlockPos offset) {
        return new BlockPos(pos.getX() + offset.getX(), pos.getY() + offset.getY(), pos.getZ() + offset.getZ());
    }

    private record PlacementCandidate(BlockPos origin, Rotation rotation) {
        private BlockPos localToWorld(BlockPos localPos) {
            return add(this.origin, transformLocal(localPos, this.rotation));
        }
    }

    private record CandidateScore(boolean usable, int score) {
        private static CandidateScore unusable() {
            return new CandidateScore(false, Integer.MAX_VALUE);
        }
    }

    private record Bounds(int minX, int maxX, int minZ, int maxZ) {
        private static Bounds from(BlockPos origin, Vec3i size, Rotation rotation) {
            int minX = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int minZ = Integer.MAX_VALUE;
            int maxZ = Integer.MIN_VALUE;
            int[] xs = {0, size.getX() - 1};
            int[] zs = {0, size.getZ() - 1};

            for (int x : xs) {
                for (int z : zs) {
                    BlockPos transformed = transformLocal(new BlockPos(x, 0, z), rotation);
                    int worldX = origin.getX() + transformed.getX();
                    int worldZ = origin.getZ() + transformed.getZ();
                    minX = Math.min(minX, worldX);
                    maxX = Math.max(maxX, worldX);
                    minZ = Math.min(minZ, worldZ);
                    maxZ = Math.max(maxZ, worldZ);
                }
            }

            return new Bounds(minX, maxX, minZ, maxZ);
        }
    }
}
