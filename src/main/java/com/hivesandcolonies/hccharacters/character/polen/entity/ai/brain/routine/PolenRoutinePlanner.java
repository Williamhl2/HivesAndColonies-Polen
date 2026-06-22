package com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.routine;

import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.PolenDangerMemoryTracker;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.interest.PolenInterestLocator;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.interest.PolenInterestTarget;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.interest.PolenInterestType;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.intent.PolenIntent;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.core.PolenSleepController;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.search.light.PolenLightSpotHelper;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.safety.PolenSafetyEvaluator;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.safety.PolenSafetyNavigator;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.affordance.PolenAffordanceResolver;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.affordance.PolenAffordanceTarget;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.affordance.PolenAffordanceType;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home.PolenHomeManager;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home.PolenHomeSnapshot;
import com.hivesandcolonies.hccharacters.common.util.LevelBrightnessHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;

public final class PolenRoutinePlanner {

    private static final int DEFAULT_SAFE_SPOT_RADIUS = 10;
    private static final int DEFAULT_HOME_ROUTINE_RADIUS = 36;
    private static final int BAD_WEATHER_HOME_ROUTINE_RADIUS = 96;
    private static final int HOME_WANDER_RADIUS = 18;
    private static final int HOME_RETURN_RADIUS = 6;
    private static final int MIN_INTEREST_BRIGHTNESS = 8;
    private static final int[] ROUTINE_ANCHOR_Y_OFFSETS = {0, 1, -1, 2, -2};
    private static final int ROUTINE_ANCHOR_RADIUS = 2;

    private PolenRoutinePlanner() {
    }

    public static BlockPos getRoutineTarget(PolenEntity polen, PolenIntent intent) {
        return switch (intent) {
            case SEEK_REST -> findRestTarget(polen);
            case QUIET_CREATION -> findQuietCreationTarget(polen);
            case WANDER_SAFE -> findHomeAnchoredSafeWanderTarget(polen, HOME_WANDER_RADIUS);
            default -> null;
        };
    }

    public static boolean isRememberedSpotStillValid(PolenEntity polen, BlockPos pos) {
        if (pos == null) {
            return false;
        }

        return PolenSafetyEvaluator.isSafeStandingSpot(polen, pos)
                && !PolenDangerMemoryTracker.isDangerousMemorySpot(polen, pos);
    }

    public static BlockPos normalizeRestingAnchor(PolenEntity polen, BlockPos preferredPos) {
        return resolveRoutineAnchor(polen, preferredPos, ROUTINE_ANCHOR_RADIUS, true);
    }

    public static BlockPos normalizeQuietCreationAnchor(PolenEntity polen, BlockPos preferredPos) {
        return resolveRoutineAnchor(polen, preferredPos, ROUTINE_ANCHOR_RADIUS, true);
    }

    public static boolean isDarkEnoughForLightMagic(PolenEntity polen) {
        return PolenLightSpotHelper.isDarkEnoughForLightMagic(polen);
    }

    public static boolean isReadyToIlluminateHere(PolenEntity polen) {
        return PolenLightSpotHelper.isReadyToIlluminateHere(polen);
    }

    public static boolean isNearRestingSpot(PolenEntity polen) {
        return PolenHomeManager.isNearResidence(polen)
                || polen.getAiState().getRestingPos() != null
                && polen.getAiState().getRestingPos().closerToCenterThan(polen.position(), 3.0D);
    }

    public static BlockPos findLightMagicTarget(PolenEntity polen) {
        PolenAffordanceTarget target = PolenAffordanceResolver.findBestNightLight(polen, 12);
        if (target != null && target.type() == com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.affordance.PolenAffordanceType.MAGIC_LIGHT) {
            return target.usePos();
        }
        return PolenLightSpotHelper.findLightMagicTarget(polen);
    }

    public static BlockPos findHomeAnchoredSafeWanderTarget(PolenEntity polen, int radius) {
        BlockPos homeCenter = PolenHomeManager.getHomeSnapshot(polen).homeCenterPos();
        if (homeCenter == null) {
            return PolenSafetyNavigator.findNearbySafeSurfaceSpot(polen, Math.max(DEFAULT_SAFE_SPOT_RADIUS, radius));
        }

        if (!homeCenter.closerToCenterThan(polen.position(), Math.max(radius, HOME_RETURN_RADIUS))) {
            BlockPos returnSpot = findSafeSpotAroundHome(polen, homeCenter, HOME_RETURN_RADIUS, true);
            return returnSpot != null ? returnSpot : normalizeRestingAnchor(polen, homeCenter);
        }

        BlockPos homeSpot = findSafeSpotAroundHome(polen, homeCenter, Math.max(HOME_RETURN_RADIUS, radius), false);
        if (homeSpot != null) {
            return homeSpot;
        }

        BlockPos localSpot = PolenSafetyNavigator.findNearbySafeSurfaceSpot(polen, DEFAULT_SAFE_SPOT_RADIUS);
        if (localSpot != null && PolenHomeManager.isPositionWithinHomeRadius(polen, localSpot, radius)) {
            return localSpot;
        }

        return normalizeRestingAnchor(polen, homeCenter);
    }

    public static boolean isSafeInterestSpot(PolenEntity polen, BlockPos pos) {
        if (pos == null || PolenDangerMemoryTracker.isDangerousMemorySpot(polen, pos)) {
            return false;
        }

        int surfaceY = polen.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        boolean nearSurface = pos.getY() >= surfaceY - 2;
        boolean brightEnough = LevelBrightnessHelper.maxLocalRawBrightness(polen.level(), pos.above()) >= MIN_INTEREST_BRIGHTNESS;

        return nearSurface && brightEnough;
    }

    private static BlockPos findRestTarget(PolenEntity polen) {
        PolenHomeSnapshot homeSnapshot = PolenHomeManager.getHomeSnapshot(polen);
        if (PolenSleepController.shouldSleepNow(polen)) {
            BlockPos bedPos = PolenSleepController.findBestKnownBed(polen);
            BlockPos bedAccessPos = homeSnapshot.homeBed() != null
                    && homeSnapshot.homeBed().bedPos() != null
                    && homeSnapshot.homeBed().bedPos().equals(bedPos)
                    ? homeSnapshot.homeBed().accessPos()
                    : PolenSleepController.findBestBedAccessPos(polen, bedPos);
            if (bedAccessPos != null) {
                return bedAccessPos;
            }
        }

        PolenAffordanceTarget restTarget = PolenAffordanceResolver.findBestRestSpot(polen, DEFAULT_SAFE_SPOT_RADIUS);
        if (restTarget != null) {
            if (restTarget.type() == PolenAffordanceType.REST
                    && !restTarget.usePos().equals(polen.getAiState().getRestingPos())) {
                polen.getAiState().setRestingPos(restTarget.usePos());
            }
            return restTarget.usePos();
        }

        BlockPos homeCenter = homeSnapshot.homeCenterPos();
        if (homeCenter != null) {
            BlockPos homeReturnSpot = findSafeSpotAroundHome(polen, homeCenter, HOME_RETURN_RADIUS, true);
            if (homeReturnSpot != null) {
                return homeReturnSpot;
            }
        }

        return PolenSafetyNavigator.findNearbySafeSurfaceSpot(polen, DEFAULT_SAFE_SPOT_RADIUS);
    }

    private static BlockPos findQuietCreationTarget(PolenEntity polen) {
        PolenHomeSnapshot homeSnapshot = PolenHomeManager.getHomeSnapshot(polen);
        BlockPos preferredHomeTarget = findPreferredHomeRoutineTarget(polen, homeSnapshot);
        if (preferredHomeTarget != null) {
            return preferredHomeTarget;
        }

        BlockPos lightMagicTarget = findLightMagicTarget(polen);
        if (lightMagicTarget != null) {
            return lightMagicTarget;
        }

        BlockPos rememberedSourceAnchor = normalizeQuietCreationAnchor(polen, polen.getAiState().getFavoriteSourcePos());
        if (rememberedSourceAnchor != null
                && isSafeInterestSpot(polen, polen.getAiState().getFavoriteSourcePos())
                && PolenHomeManager.isPositionWithinHomeRadius(polen, rememberedSourceAnchor, DEFAULT_HOME_ROUTINE_RADIUS)) {
            return rememberedSourceAnchor;
        }

        PolenInterestTarget localSource = PolenInterestLocator.findPreferredInterestOfType(polen, PolenInterestType.SOURCE);
        if (localSource != null && isSafeInterestSpot(polen, localSource.pos())) {
            BlockPos localSourceAnchor = normalizeQuietCreationAnchor(polen, localSource.pos());
            if (localSourceAnchor != null
                    && PolenHomeManager.isPositionWithinHomeRadius(polen, localSourceAnchor, DEFAULT_HOME_ROUTINE_RADIUS)) {
                return localSourceAnchor;
            }
        }

        BlockPos normalizedRestingPos = normalizeRestingAnchor(polen, polen.getAiState().getRestingPos());
        if (normalizedRestingPos != null) {
            return normalizedRestingPos;
        }

        BlockPos residenceUsePos = homeSnapshot.residenceUsePos();
        BlockPos normalizedResidencePos = normalizeQuietCreationAnchor(polen, residenceUsePos);
        if (normalizedResidencePos != null && residenceUsePos.distSqr(polen.blockPosition()) <= 18.0D * 18.0D) {
            return normalizedResidencePos;
        }

        return findHomeAnchoredSafeWanderTarget(polen, HOME_RETURN_RADIUS);
    }

    private static BlockPos findPreferredHomeRoutineTarget(PolenEntity polen, PolenHomeSnapshot homeSnapshot) {
        int maxRadius = polen.level().isNight() || polen.level().isRaining()
                ? BAD_WEATHER_HOME_ROUTINE_RADIUS
                : DEFAULT_HOME_ROUTINE_RADIUS;

        BlockPos residenceUsePos = homeSnapshot.residenceUsePos();
        BlockPos normalizedResidencePos = normalizeQuietCreationAnchor(polen, residenceUsePos);
        if (normalizedResidencePos != null
                && residenceUsePos != null
                && residenceUsePos.distSqr(polen.blockPosition()) <= (double) (maxRadius * maxRadius)) {
            return normalizedResidencePos;
        }

        BlockPos normalizedRestingPos = normalizeRestingAnchor(polen, polen.getAiState().getRestingPos());
        if (normalizedRestingPos != null
                && normalizedRestingPos.distSqr(polen.blockPosition()) <= (double) (maxRadius * maxRadius)) {
            return normalizedRestingPos;
        }

        return null;
    }

    private static BlockPos findSafeSpotAroundHome(PolenEntity polen, BlockPos homeCenter, int radius, boolean preferClosestToHome) {
        if (polen == null || homeCenter == null) {
            return null;
        }

        BlockPos bestPos = null;
        double bestScore = Double.MAX_VALUE;
        int effectiveRadius = Math.max(2, radius);

        for (int dx = -effectiveRadius; dx <= effectiveRadius; dx++) {
            for (int dz = -effectiveRadius; dz <= effectiveRadius; dz++) {
                for (int dy : ROUTINE_ANCHOR_Y_OFFSETS) {
                    BlockPos candidate = homeCenter.offset(dx, dy, dz);
                    if (!PolenSafetyEvaluator.isSafeStandingSpot(polen, candidate)
                            || PolenDangerMemoryTracker.isDangerousMemorySpot(polen, candidate)) {
                        continue;
                    }

                    double distanceToHome = candidate.distSqr(homeCenter);
                    double distanceToPolen = candidate.distSqr(polen.blockPosition());
                    double score = preferClosestToHome
                            ? distanceToHome + distanceToPolen * 0.20D
                            : distanceToHome * 0.85D + distanceToPolen * 0.25D;
                    score -= LevelBrightnessHelper.maxLocalRawBrightness(polen.level(), candidate) * 0.35D;
                    if (!polen.level().isNight() && !polen.level().isRaining() && polen.level().canSeeSky(candidate)) {
                        score -= 2.5D;
                    }

                    if (score < bestScore) {
                        bestScore = score;
                        bestPos = candidate.immutable();
                    }
                }
            }
        }

        return bestPos;
    }

    private static BlockPos resolveRoutineAnchor(
            PolenEntity polen,
            BlockPos preferredPos,
            int radius,
            boolean requireSafe
    ) {
        if (polen == null || preferredPos == null) {
            return null;
        }

        BlockPos bestPos = null;
        double bestScore = Double.MAX_VALUE;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy : ROUTINE_ANCHOR_Y_OFFSETS) {
                    BlockPos candidate = preferredPos.offset(dx, dy, dz);
                    boolean valid = requireSafe
                            ? PolenSafetyEvaluator.isSafeStandingSpot(polen, candidate)
                            : PolenSafetyEvaluator.isStandableSpot(polen, candidate);
                    if (!valid || PolenDangerMemoryTracker.isDangerousMemorySpot(polen, candidate)) {
                        continue;
                    }

                    double score = candidate.distSqr(preferredPos) + candidate.distSqr(polen.blockPosition()) * 0.15D;
                    if (candidate.getY() == preferredPos.getY()) {
                        score -= 0.5D;
                    }

                    if (score < bestScore) {
                        bestScore = score;
                        bestPos = candidate.immutable();
                    }
                }
            }
        }

        return bestPos;
    }
}
