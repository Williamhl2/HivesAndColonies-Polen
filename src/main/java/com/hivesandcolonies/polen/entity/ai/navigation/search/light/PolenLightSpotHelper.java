package com.hivesandcolonies.polen.entity.ai.navigation.search.light;

import com.hivesandcolonies.polen.entity.PolenDangerMemoryTracker;
import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.registry.ModBlocks;
import com.hivesandcolonies.polen.entity.ai.navigation.search.PolenSearchDomain;
import com.hivesandcolonies.polen.entity.ai.navigation.search.PolenSearchPlanner;
import com.hivesandcolonies.polen.entity.ai.navigation.search.PolenSearchProfile;
import com.hivesandcolonies.polen.entity.ai.navigation.safety.PolenSafetyEvaluator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class PolenLightSpotHelper {
    private static final int OPEN_DARK_THRESHOLD = 5;
    private static final int RAIN_DARK_THRESHOLD = 6;
    private static final int[] LIGHT_SPOT_Y_OFFSETS = {0, 1, -1, 2, -2};
    private static final int OPEN_IMMEDIATE_RADIUS = 1;
    private static final int RAIN_IMMEDIATE_RADIUS = 2;
    private static final int[] IMMEDIATE_CENTER_Y_OFFSETS = {0, 1, -1};
    private static final int[] EMERGENCY_PLACEMENT_Y_OFFSETS = {0, 1, -1, 2};

    private static final LightSearchProfile OPEN_PROFILE = new LightSearchProfile(
            new PolenSearchProfile(PolenSearchDomain.LOCAL_RINGS, new int[] {1, 2, 3, 4, 5, 6}, LIGHT_SPOT_Y_OFFSETS, 2, 8, false),
            new PolenSearchProfile(PolenSearchDomain.SURFACE_COLUMNS, new int[] {8, 12}, LIGHT_SPOT_Y_OFFSETS, 2, 8, false),
            5,
            7,
            4,
            false
    );
    private static final LightSearchProfile RAIN_PROFILE = new LightSearchProfile(
            new PolenSearchProfile(PolenSearchDomain.LOCAL_RINGS, new int[] {1, 2, 3, 4}, LIGHT_SPOT_Y_OFFSETS, 2, 6, false),
            new PolenSearchProfile(PolenSearchDomain.SURFACE_COLUMNS, new int[] {6, 8}, LIGHT_SPOT_Y_OFFSETS, 2, 6, false),
            6,
            3,
            1,
            true
    );

    private PolenLightSpotHelper() {
    }

    public static boolean isDarkEnoughForLightMagic(PolenEntity polen) {
        return polen.level().getMaxLocalRawBrightness(polen.blockPosition()) <= getDarkThreshold(polen);
    }

    public static boolean isReadyToIlluminateHere(PolenEntity polen) {
        return isDarkEnoughForLightMagic(polen) && findImmediateManagedLightPlacement(polen) != null;
    }

    public static BlockPos findLightMagicTarget(PolenEntity polen) {
        if (!isDarkEnoughForLightMagic(polen)) {
            return null;
        }

        BlockPos immediateTarget = findImmediateLightMagicTarget(polen);
        if (immediateTarget != null) {
            return immediateTarget;
        }

        LightSearchProfile profile = getProfile(polen);
        BlockPos localTarget = findLocalLightMagicTarget(polen, profile);
        return localTarget != null ? localTarget : findSurfaceLightMagicTarget(polen, profile);
    }

    public static BlockPos findImmediateLightMagicTarget(PolenEntity polen) {
        if (polen == null) {
            return null;
        }

        LightSearchProfile profile = getProfile(polen);
        BlockPos origin = polen.blockPosition();
        BlockPos bestCandidate = null;
        double bestScore = Double.MAX_VALUE;
        int radius = profile.preferSheltered() ? RAIN_IMMEDIATE_RADIUS : OPEN_IMMEDIATE_RADIUS;

        for (int dy : LIGHT_SPOT_Y_OFFSETS) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos candidate = origin.offset(dx, dy, dz);
                    double score = scoreImmediateCandidate(polen, origin, candidate, profile);
                    if (score < bestScore) {
                        bestScore = score;
                        bestCandidate = candidate.immutable();
                    }
                }
            }
        }

        return bestCandidate;
    }

    public static ManagedLightPlacement findManagedLightPlacement(PolenEntity polen, BlockPos center) {
        if (polen == null || center == null) {
            return null;
        }

        Level level = polen.level();
        ManagedLightPlacement bestPlacement = null;
        double bestScore = Double.MAX_VALUE;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }

                BlockPos candidate = center.offset(dx, 0, dz);
                ManagedLightPlacement placement = createPlacement(level, candidate);
                if (placement == null) {
                    continue;
                }

                double score = center.distSqr(candidate)
                        + level.getMaxLocalRawBrightness(candidate) * 0.8D
                        + (placement.hanging() ? 0.35D : 0.0D);
                if (PolenSafetyEvaluator.hasOverheadCover(level, candidate)) {
                    score -= 0.75D;
                }
                if (score < bestScore) {
                    bestScore = score;
                    bestPlacement = placement;
                }
            }
        }

        return bestPlacement;
    }

    public static ManagedLightPlacement findImmediateManagedLightPlacement(PolenEntity polen) {
        if (polen == null || !isDarkEnoughForLightMagic(polen)) {
            return null;
        }

        LightSearchProfile profile = getProfile(polen);
        BlockPos origin = polen.blockPosition();
        ManagedLightPlacement bestPlacement = null;
        double bestScore = Double.MAX_VALUE;
        int radius = profile.preferSheltered() ? RAIN_IMMEDIATE_RADIUS : OPEN_IMMEDIATE_RADIUS;

        for (int dy : IMMEDIATE_CENTER_Y_OFFSETS) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos center = origin.offset(dx, dy, dz);
                    ManagedLightPlacement placement = findRelaxedImmediatePlacement(polen, center, profile);
                    if (placement == null) {
                        continue;
                    }

                    double score = center.distSqr(origin)
                            + placement.pos().distSqr(origin) * 0.35D
                            + polen.level().getMaxLocalRawBrightness(center) * 0.35D;
                    if (profile.preferSheltered() && PolenSafetyEvaluator.isRainShelteredStandingSpot(polen.level(), center)) {
                        score -= 2.0D;
                    }

                    if (score < bestScore) {
                        bestScore = score;
                        bestPlacement = placement;
                    }
                }
            }
        }

        return bestPlacement != null ? bestPlacement : findEmergencyNearbyManagedLightPlacement(polen);
    }

    private static BlockPos findLocalLightMagicTarget(PolenEntity polen, LightSearchProfile profile) {
        BlockPos origin = polen.blockPosition();
        return PolenSearchPlanner.findBestReachable(
                polen,
                origin,
                profile.localProfile(),
                candidate -> scoreLightCandidate(polen, origin, candidate, profile)
        );
    }

    private static BlockPos findSurfaceLightMagicTarget(PolenEntity polen, LightSearchProfile profile) {
        BlockPos origin = polen.blockPosition();
        return PolenSearchPlanner.findBestReachable(
                polen,
                origin,
                profile.surfaceProfile(),
                candidate -> scoreLightCandidate(polen, origin, candidate, profile)
        );
    }

    private static double scoreLightCandidate(
            PolenEntity polen,
            BlockPos origin,
            BlockPos candidate,
            LightSearchProfile profile
    ) {
        if (!evaluateLightActivitySpot(polen, candidate, profile)) {
            return Double.MAX_VALUE;
        }

        Level level = polen.level();
        double score = candidate.distSqr(origin) + level.getMaxLocalRawBrightness(candidate) * 0.55D;
        if (candidate.getY() > origin.getY()) {
            score -= (candidate.getY() - origin.getY()) * 3.5D;
        }
        if (level.canSeeSky(candidate)) {
            score -= 4.0D;
        } else if (PolenSafetyEvaluator.isNearOutdoorSurface(level, candidate)) {
            score -= 2.0D;
        }
        if (PolenSafetyEvaluator.isRainShelteredStandingSpot(level, candidate)) {
            score += profile.preferSheltered() ? -6.0D : -1.0D;
        } else if (profile.preferSheltered()) {
            score += 12.0D;
        }
        score -= 4.0D;

        return score;
    }

    private static boolean evaluateLightActivitySpot(PolenEntity polen, BlockPos center, LightSearchProfile profile) {
        Level level = polen.level();
        if (!PolenSafetyEvaluator.isSafeStandingSpot(polen, center)
                || PolenDangerMemoryTracker.isDangerousMemorySpot(polen, center)
                || level.getMaxLocalRawBrightness(center) > profile.maxBrightness()) {
            return false;
        }

        int standableTiles = 0;
        int crossTiles = 0;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos tile = center.offset(dx, 0, dz);
                if (!isLightAreaTileUsable(polen, tile, profile)) {
                    continue;
                }

                standableTiles++;
                if (dx == 0 || dz == 0) {
                    crossTiles++;
                }
            }
        }

        if (standableTiles < profile.requiredAreaTiles() || crossTiles < profile.requiredCrossTiles()) {
            return false;
        }

        return (!profile.preferSheltered() || PolenSafetyEvaluator.isRainShelteredStandingSpot(level, center))
                && findManagedLightPlacement(polen, center) != null;
    }

    private static boolean isLightAreaTileUsable(PolenEntity polen, BlockPos tile, LightSearchProfile profile) {
        Level level = polen.level();
        if (!PolenSafetyEvaluator.isStandableSpot(polen, tile)
                || PolenDangerMemoryTracker.isDangerousMemorySpot(polen, tile)) {
            return false;
        }

        if (profile.preferSheltered()) {
            return true;
        }

        return level.getFluidState(tile.above(2)).isEmpty()
                && level.getBlockState(tile.above(2)).getCollisionShape(level, tile.above(2)).isEmpty();
    }

    private static double scoreImmediateCandidate(
            PolenEntity polen,
            BlockPos origin,
            BlockPos candidate,
            LightSearchProfile profile
    ) {
        if (!evaluateLightActivitySpot(polen, candidate, profile)) {
            return Double.MAX_VALUE;
        }

        Level level = polen.level();
        double score = candidate.distSqr(origin);
        score += level.getMaxLocalRawBrightness(candidate) * 0.4D;
        if (profile.preferSheltered() && PolenSafetyEvaluator.isRainShelteredStandingSpot(level, candidate)) {
            score -= 2.5D;
        }
        if (candidate.equals(origin)) {
            score -= 1.0D;
        }

        return score;
    }

    private static ManagedLightPlacement createPlacement(Level level, BlockPos candidate) {
        if (!level.getBlockState(candidate).canBeReplaced()) {
            return null;
        }

        if (!level.getFluidState(candidate).isEmpty()) {
            return null;
        }

        BlockState belowState = level.getBlockState(candidate.below());
        if (belowState.isFaceSturdy(level, candidate.below(), Direction.UP)) {
            return new ManagedLightPlacement(
                    candidate.immutable(),
                    ModBlocks.POLEN_LANTERN.get().defaultBlockState()
            );
        }

        BlockState aboveState = level.getBlockState(candidate.above());
        if (aboveState.isFaceSturdy(level, candidate.above(), Direction.DOWN)) {
            return new ManagedLightPlacement(
                    candidate.immutable(),
                    ModBlocks.POLEN_LANTERN.get().defaultBlockState().setValue(LanternBlock.HANGING, true)
            );
        }

        return null;
    }

    private static ManagedLightPlacement findRelaxedImmediatePlacement(
            PolenEntity polen,
            BlockPos center,
            LightSearchProfile profile
    ) {
        Level level = polen.level();
        if (!PolenSafetyEvaluator.isStandableSpot(polen, center)
                || PolenDangerMemoryTracker.isDangerousMemorySpot(polen, center)
                || level.getMaxLocalRawBrightness(center) > profile.maxBrightness()) {
            return null;
        }

        if (profile.preferSheltered() && !PolenSafetyEvaluator.isRainShelteredStandingSpot(level, center)) {
            return null;
        }

        return findManagedLightPlacement(polen, center);
    }

    private static ManagedLightPlacement findEmergencyNearbyManagedLightPlacement(PolenEntity polen) {
        Level level = polen.level();
        LightSearchProfile profile = getProfile(polen);
        BlockPos origin = polen.blockPosition();
        int radius = profile.preferSheltered() ? 3 : 2;
        ManagedLightPlacement bestPlacement = null;
        double bestScore = Double.MAX_VALUE;

        for (int dy : EMERGENCY_PLACEMENT_Y_OFFSETS) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos candidate = origin.offset(dx, dy, dz);
                    if (candidate.equals(origin) || level.getMaxLocalRawBrightness(candidate) > profile.maxBrightness()) {
                        continue;
                    }

                    if (profile.preferSheltered() && PolenSafetyEvaluator.isExposedToRain(level, candidate)) {
                        continue;
                    }

                    ManagedLightPlacement placement = createPlacement(level, candidate);
                    if (placement == null) {
                        continue;
                    }

                    double score = placement.pos().distSqr(origin)
                            + level.getMaxLocalRawBrightness(placement.pos()) * 0.45D
                            + (placement.hanging() ? 0.25D : 0.0D);
                    if (profile.preferSheltered() && PolenSafetyEvaluator.hasOverheadCover(level, placement.pos())) {
                        score -= 1.5D;
                    }

                    if (score < bestScore) {
                        bestScore = score;
                        bestPlacement = placement;
                    }
                }
            }
        }

        return bestPlacement;
    }

    private static int getDarkThreshold(PolenEntity polen) {
        return polen.level().isRaining() ? RAIN_DARK_THRESHOLD : OPEN_DARK_THRESHOLD;
    }

    private static LightSearchProfile getProfile(PolenEntity polen) {
        return polen.level().isRaining() ? RAIN_PROFILE : OPEN_PROFILE;
    }

    public record ManagedLightPlacement(BlockPos pos, BlockState state, boolean hanging) {
        public ManagedLightPlacement(BlockPos pos, BlockState state) {
            this(pos, state, state.hasProperty(LanternBlock.HANGING) && state.getValue(LanternBlock.HANGING));
        }
    }

    private record LightSearchProfile(
            PolenSearchProfile localProfile,
            PolenSearchProfile surfaceProfile,
            int maxBrightness,
            int requiredAreaTiles,
            int requiredCrossTiles,
            boolean preferSheltered
    ) {
    }
}
