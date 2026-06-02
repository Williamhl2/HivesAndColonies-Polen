package com.hivesandcolonies.polen.entity.ai.navigation.safety;

import com.hivesandcolonies.polen.entity.PolenDangerMemoryTracker;
import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.ai.ability.magic.PolenMagicController;
import com.hivesandcolonies.polen.entity.ai.brain.interest.PolenInterestLocator;
import com.hivesandcolonies.polen.entity.ai.brain.interest.PolenInterestTarget;
import com.hivesandcolonies.polen.entity.ai.brain.interest.PolenInterestType;
import com.hivesandcolonies.polen.entity.ai.brain.routine.PolenRoutinePlanner;
import com.hivesandcolonies.polen.entity.ai.navigation.search.PolenSearchDomain;
import com.hivesandcolonies.polen.entity.ai.navigation.search.PolenSearchPlanner;
import com.hivesandcolonies.polen.entity.ai.navigation.search.PolenSearchProfile;
import com.hivesandcolonies.polen.entity.ai.navigation.search.shelter.PolenShelterSpotHelper;
import com.hivesandcolonies.polen.entity.ai.world.affordance.PolenAffordanceResolver;
import com.hivesandcolonies.polen.entity.ai.world.affordance.PolenAffordanceTarget;
import com.hivesandcolonies.polen.entity.ai.world.home.PolenHomeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.Vec3;

public final class PolenSafetyNavigator {
    private static final double MIN_RELOCATION_DISTANCE_SQR = 4.0D;
    private static final int LOCAL_ESCAPE_VERTICAL_RANGE = 6;
    private static final int[] LOCAL_Y_OFFSETS = {1, 2, 0, 3, -1, 4, 5, -2, 6};
    private static final int[] SURFACE_Y_OFFSETS = {0, -1, 1, 2, -2};
    private static final int[] EMERGENCY_RELOCATION_RADII = {32, 64, 96};
    private static final int SAFE_BLINK_DISTANCE = 8;
    private static final int STANDABLE_BLINK_DISTANCE = 7;
    private static final int SHELTER_BLINK_DISTANCE = 9;
    private static final PolenSearchProfile LOCAL_SAFE_PROFILE = new PolenSearchProfile(
            PolenSearchDomain.LOCAL_RINGS,
            new int[] {6, 10},
            LOCAL_Y_OFFSETS,
            LOCAL_ESCAPE_VERTICAL_RANGE,
            SAFE_BLINK_DISTANCE,
            true
    );
    private static final PolenSearchProfile SURFACE_SAFE_PROFILE = new PolenSearchProfile(
            PolenSearchDomain.SURFACE_COLUMNS,
            new int[] {10, 20, 30},
            SURFACE_Y_OFFSETS,
            2,
            SAFE_BLINK_DISTANCE,
            true
    );
    private static final PolenSearchProfile LOCAL_SHELTER_PROFILE = new PolenSearchProfile(
            PolenSearchDomain.LOCAL_RINGS,
            new int[] {6, 8},
            LOCAL_Y_OFFSETS,
            LOCAL_ESCAPE_VERTICAL_RANGE,
            SHELTER_BLINK_DISTANCE,
            true
    );
    private static final PolenSearchProfile SURFACE_SHELTER_PROFILE = new PolenSearchProfile(
            PolenSearchDomain.SURFACE_COLUMNS,
            new int[] {12, 24},
            SURFACE_Y_OFFSETS,
            2,
            SHELTER_BLINK_DISTANCE,
            true
    );
    private static final PolenSearchProfile EXPLORATION_PROFILE = new PolenSearchProfile(
            PolenSearchDomain.LOCAL_RINGS,
            new int[] {8, 16, 32},
            LOCAL_Y_OFFSETS,
            LOCAL_ESCAPE_VERTICAL_RANGE,
            STANDABLE_BLINK_DISTANCE,
            false
    );

    private PolenSafetyNavigator() {
    }

    public static boolean isInUnsafeArea(PolenEntity polen) {
        BlockPos currentPos = polen.blockPosition();
        boolean unsafe = !PolenSafetyEvaluator.isSafeStandingSpot(polen, currentPos)
                || hasNearbyHostile(polen, 5.0D);

        if (PolenSafetyEvaluator.isTrulyDangerousStandingSpot(polen, currentPos)
                || hasNearbyHostile(polen, 5.0D)) {
            PolenDangerMemoryTracker.rememberDangerousSpot(polen, currentPos);
        }

        return unsafe;
    }

    public static boolean shouldSeekSafety(PolenEntity polen) {
        BlockPos currentPos = polen.blockPosition();
        boolean dangerous = PolenSafetyEvaluator.isTrulyDangerousStandingSpot(polen, currentPos)
                || hasNearbyHostile(polen, 6.0D);
        if (dangerous) {
            PolenDangerMemoryTracker.rememberDangerousSpot(polen, currentPos);
            return true;
        }

        return PolenSafetyEvaluator.isClaustrophobicStandingSpot(polen, currentPos)
                || shouldSeekRainShelter(polen)
                || shouldSeekNightLight(polen);
    }

    public static boolean shouldUseUnsafeDialogue(PolenEntity polen) {
        return PolenSafetyEvaluator.isClaustrophobicStandingSpot(polen, polen.blockPosition())
                || shouldSeekRainShelter(polen)
                || shouldSeekNightLight(polen);
    }

    public static BlockPos findNearbySafeSurfaceSpot(PolenEntity polen, int radius) {
        BlockPos localSpot = findBestReachableLocalEscapeSpot(polen, Math.max(6, radius));
        if (localSpot != null) {
            return localSpot;
        }

        return findBestReachableSurfaceSpot(polen, radius);
    }

    public static BlockPos findFallbackExplorationSpot(PolenEntity polen, int radius) {
        return findBestReachableExplorationSpot(polen, Math.max(8, radius));
    }

    public static Vec3 getNearestSafeSpotCenter(PolenEntity polen, int radius) {
        BlockPos pos = findNearbySafeSurfaceSpot(polen, radius);
        return pos == null ? null : Vec3.atCenterOf(pos);
    }

    public static boolean shouldSeekRainShelter(PolenEntity polen) {
        return PolenSafetyEvaluator.isExposedToRain(polen.level(), polen.blockPosition());
    }

    public static boolean shouldSeekNightLight(PolenEntity polen) {
        return polen.level().isNight()
                && polen.getAiState().getActiveLightPos() == null
                && PolenRoutinePlanner.isDarkEnoughForLightMagic(polen)
                && !PolenRoutinePlanner.isReadyToIlluminateHere(polen);
    }

    public static BlockPos findNearbyShelteredSpot(PolenEntity polen, int radius) {
        PolenAffordanceTarget preferredShelter = PolenAffordanceResolver.findBestRainShelter(polen, Math.max(8, radius));
        if (preferredShelter != null) {
            return preferredShelter.usePos();
        }

        BlockPos restingPos = PolenHomeManager.getValidResidenceUsePos(polen);
        if (restingPos == null) {
            restingPos = polen.getAiState().getRestingPos();
        }
        if (restingPos != null
                && restingPos.distSqr(polen.blockPosition()) >= MIN_RELOCATION_DISTANCE_SQR
                && PolenSafetyEvaluator.isSafeStandingSpot(polen, restingPos)
                && PolenSafetyEvaluator.isRainShelteredStandingSpot(polen.level(), restingPos)
                && !PolenDangerMemoryTracker.isDangerousMemorySpot(polen, restingPos)) {
            return restingPos;
        }

        BlockPos directShelter = PolenShelterSpotHelper.findNearbyShelterSpot(polen, Math.max(6, radius));
        if (directShelter != null) {
            return directShelter;
        }

        PolenInterestTarget nearbyLight = PolenInterestLocator.findPreferredInterestOfType(polen, PolenInterestType.LIGHT);
        if (nearbyLight != null
                && nearbyLight.observePos().distSqr(polen.blockPosition()) <= (double) (radius * radius)
                && PolenSafetyEvaluator.isRainShelteredStandingSpot(polen.level(), nearbyLight.observePos())
                && !PolenDangerMemoryTracker.isDangerousMemorySpot(polen, nearbyLight.observePos())) {
            return nearbyLight.observePos();
        }

        BlockPos origin = polen.blockPosition();
        BlockPos localShelter = PolenSearchPlanner.findBestReachable(
                polen,
                origin,
                withRadii(LOCAL_SHELTER_PROFILE, Math.max(6, radius), Math.max(8, radius)),
                candidate -> scoreShelterCandidate(polen, origin, candidate)
        );
        if (localShelter != null) {
            return localShelter;
        }

        return findSurfaceShelteredSpot(polen, Math.max(12, radius * 2));
    }

    public static BlockPos findNearbyNightLightSpot(PolenEntity polen, int radius) {
        int effectiveRadius = polen.level().isRaining() ? Math.max(6, radius - 2) : radius;
        PolenAffordanceTarget preferredLight = PolenAffordanceResolver.findBestNightLight(polen, effectiveRadius);
        if (preferredLight != null) {
            return preferredLight.usePos();
        }

        PolenInterestTarget nearbyLight = PolenInterestLocator.findPreferredInterestOfType(polen, PolenInterestType.LIGHT);
        if (nearbyLight != null
                && nearbyLight.observePos().distSqr(polen.blockPosition()) <= (double) (effectiveRadius * effectiveRadius)
                && PolenSafetyEvaluator.isSafeStandingSpot(polen, nearbyLight.observePos())
                && !PolenDangerMemoryTracker.isDangerousMemorySpot(polen, nearbyLight.observePos())) {
            return nearbyLight.observePos();
        }

        BlockPos lightTarget = PolenRoutinePlanner.findLightMagicTarget(polen);
        if (lightTarget != null
                && lightTarget.distSqr(polen.blockPosition()) <= (double) (effectiveRadius * effectiveRadius)) {
            return lightTarget;
        }

        return findNearbySafeSurfaceSpot(polen, effectiveRadius);
    }

    public static boolean tryBlinkTowardSafeSpot(PolenEntity polen, BlockPos target, int maxDistance) {
        if (target == null) {
            return false;
        }

        polen.getNavigation().stop();
        polen.stopQuietActivity();
        return PolenMagicController.blinkToward(polen, target, maxDistance, true)
                || PolenMagicController.blinkToward(polen, target, maxDistance, false);
    }

    public static boolean tryBlinkTowardStandableSpot(PolenEntity polen, BlockPos target, int maxDistance) {
        if (target == null) {
            return false;
        }

        polen.getNavigation().stop();
        polen.stopQuietActivity();
        return PolenMagicController.blinkToward(polen, target, maxDistance, false);
    }

    public static boolean tryEmergencyRelocateToSafeSurface(PolenEntity polen) {
        BlockPos target = findEmergencyRelocationSpot(polen);
        if (target == null) {
            return false;
        }

        polen.getNavigation().stop();
        polen.stopQuietActivity();
        return PolenMagicController.blinkToSafety(polen, target);
    }

    private static BlockPos findBestReachableLocalEscapeSpot(PolenEntity polen, int radius) {
        BlockPos origin = polen.blockPosition();
        return PolenSearchPlanner.findBestReachable(
                polen,
                origin,
                withRadii(LOCAL_SAFE_PROFILE, Math.max(6, radius), radius),
                candidate -> scoreCandidate(polen, origin, candidate)
        );
    }

    private static BlockPos findBestReachableSurfaceSpot(PolenEntity polen, int radius) {
        BlockPos origin = polen.blockPosition();
        return PolenSearchPlanner.findBestReachable(
                polen,
                origin,
                withRadii(SURFACE_SAFE_PROFILE, radius, radius * 2, radius * 3),
                candidate -> scoreCandidate(polen, origin, candidate)
        );
    }

    private static BlockPos findBestReachableExplorationSpot(PolenEntity polen, int radius) {
        BlockPos origin = polen.blockPosition();
        return PolenSearchPlanner.findBestReachable(
                polen,
                origin,
                withRadii(
                        EXPLORATION_PROFILE,
                        Math.max(8, radius / 2),
                        Math.max(8, radius),
                        Math.max(8, radius * 2)
                ),
                candidate -> scoreExplorationCandidate(polen, origin, candidate)
        );
    }

    private static BlockPos findEmergencyRelocationSpot(PolenEntity polen) {
        Level level = polen.level();
        BlockPos origin = polen.blockPosition();
        BlockPos bestPos = null;
        double bestScore = Double.MAX_VALUE;

        for (int searchRadius : EMERGENCY_RELOCATION_RADII) {
            for (int dx = -searchRadius; dx <= searchRadius; dx++) {
                for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                    int x = origin.getX() + dx;
                    int z = origin.getZ() + dz;
                    int surfaceY = level.getHeight(
                            net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                            x,
                            z
                    );

                    for (int yOffset : SURFACE_Y_OFFSETS) {
                        BlockPos candidate = new BlockPos(x, surfaceY + yOffset, z);
                        if (!PolenSafetyEvaluator.isSafeStandingSpot(polen, candidate)) {
                            continue;
                        }

                        double score = candidate.distSqr(origin);
                        if (level.canSeeSky(candidate)) {
                            score -= 8.0D;
                        }

                        if (score < bestScore) {
                            bestScore = score;
                            bestPos = candidate.immutable();
                        }
                    }
                }
            }

            if (bestPos != null) {
                return bestPos;
            }
        }

        return null;
    }

    private static BlockPos findSurfaceShelteredSpot(PolenEntity polen, int radius) {
        BlockPos origin = polen.blockPosition();
        return PolenSearchPlanner.findBestReachable(
                polen,
                origin,
                withRadii(SURFACE_SHELTER_PROFILE, radius, radius * 2),
                candidate -> scoreShelterCandidate(polen, origin, candidate)
        );
    }

    private static double scoreCandidate(PolenEntity polen, BlockPos origin, BlockPos candidate) {
        if (!PolenSafetyEvaluator.isSafeStandingSpot(polen, candidate)
                || PolenDangerMemoryTracker.isDangerousMemorySpot(polen, candidate)
                || candidate.distSqr(origin) < MIN_RELOCATION_DISTANCE_SQR) {
            return Double.MAX_VALUE;
        }

        double distanceSqr = candidate.distSqr(origin);
        double score = distanceSqr;

        int climb = candidate.getY() - origin.getY();
        if (climb > 0) {
            score -= climb * 6.0D;
        }

        if (polen.level().canSeeSky(candidate)) {
            score -= 12.0D;
        } else if (PolenSafetyEvaluator.isNearOutdoorSurface(polen.level(), candidate)) {
            score -= 6.0D;
        }

        return score;
    }

    private static double scoreExplorationCandidate(PolenEntity polen, BlockPos origin, BlockPos candidate) {
        if (!PolenSafetyEvaluator.isStandableSpot(polen, candidate)
                || PolenDangerMemoryTracker.isDangerousMemorySpot(polen, candidate)
                || candidate.distSqr(origin) < MIN_RELOCATION_DISTANCE_SQR) {
            return Double.MAX_VALUE;
        }

        double score = candidate.distSqr(origin);
        score -= Math.max(0, candidate.getY() - origin.getY()) * 10.0D;
        score -= polen.level().getMaxLocalRawBrightness(candidate) * 0.75D;

        if (polen.level().canSeeSky(candidate)) {
            score -= 20.0D;
        } else if (PolenSafetyEvaluator.isNearOutdoorSurface(polen.level(), candidate)) {
            score -= 8.0D;
        }

        return score;
    }

    private static double scoreShelterCandidate(PolenEntity polen, BlockPos origin, BlockPos candidate) {
        if (!PolenSafetyEvaluator.isSafeStandingSpot(polen, candidate)
                || !PolenSafetyEvaluator.isRainShelteredStandingSpot(polen.level(), candidate)
                || PolenDangerMemoryTracker.isDangerousMemorySpot(polen, candidate)
                || candidate.distSqr(origin) < MIN_RELOCATION_DISTANCE_SQR) {
            return Double.MAX_VALUE;
        }

        double score = candidate.distSqr(origin);
        if (candidate.getY() > origin.getY()) {
            score -= (candidate.getY() - origin.getY()) * 5.0D;
        }
        if (PolenSafetyEvaluator.hasOverheadCover(polen.level(), candidate)) {
            score -= 8.0D;
        }

        return score;
    }


    private static boolean hasNearbyHostile(PolenEntity polen, double radius) {
        return !polen.level().getEntitiesOfClass(
                Monster.class,
                polen.getBoundingBox().inflate(radius),
                monster -> monster.isAlive() && !monster.isSpectator()
        ).isEmpty();
    }

    private static PolenSearchProfile withRadii(PolenSearchProfile baseProfile, int... radii) {
        return new PolenSearchProfile(
                baseProfile.domain(),
                radii,
                baseProfile.verticalOffsets(),
                baseProfile.verticalLimit(),
                baseProfile.blinkDistance(),
                baseProfile.requireSafeBlink()
        );
    }
}
