package com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.safety;

import com.hivesandcolonies.hccharacters.character.polen.entity.PolenDangerMemoryTracker;
import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.ability.magic.PolenMagicController;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.interest.PolenInterestLocator;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.interest.PolenInterestTarget;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.interest.PolenInterestType;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.routine.PolenRoutinePlanner;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.core.PolenSleepController;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.search.PolenSearchDomain;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.search.PolenSearchPlanner;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.search.PolenSearchProfile;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.search.shelter.PolenShelterContextResolver;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.search.shelter.PolenShelterKind;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.search.shelter.PolenShelterSpotHelper;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.affordance.PolenAffordanceResolver;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.affordance.PolenAffordanceTarget;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home.PolenHomeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class PolenSafetyNavigator {
    private static final double MIN_RELOCATION_DISTANCE_SQR = 4.0D;
    private static final double HOSTILE_MEMORY_RANGE = 8.0D;
    private static final double HOSTILE_REJECTION_DISTANCE_SQR = 3.5D * 3.5D;
    private static final double HOSTILE_AWARENESS_DISTANCE_SQR = 9.0D * 9.0D;
    private static final double IMMEDIATE_HOSTILE_THREAT_RANGE = 5.5D;
    private static final double IMMEDIATE_ESCAPE_MIN_GAIN_SQR = 6.0D;
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
        BlockPos nearestHostilePos = findNearestHostilePos(polen, HOSTILE_MEMORY_RANGE);
        boolean hostileNearby = nearestHostilePos != null;
        boolean unsafe = !PolenSafetyEvaluator.isSafeStandingSpot(polen, currentPos)
                || hostileNearby;

        if (PolenSafetyEvaluator.isTrulyDangerousStandingSpot(polen, currentPos)
                || hostileNearby) {
            PolenDangerMemoryTracker.rememberDangerousSpot(polen, hostileNearby ? nearestHostilePos : currentPos);
        }

        return unsafe;
    }

    public static boolean shouldSeekSafety(PolenEntity polen) {
        BlockPos currentPos = polen.blockPosition();
        BlockPos nearestHostilePos = findNearestHostilePos(polen, 6.0D);
        boolean dangerous = PolenSafetyEvaluator.isTrulyDangerousStandingSpot(polen, currentPos)
                || nearestHostilePos != null;
        if (dangerous) {
            PolenDangerMemoryTracker.rememberDangerousSpot(polen, nearestHostilePos != null ? nearestHostilePos : currentPos);
            return true;
        }

        if (PolenSleepController.shouldPrioritizeBedReturn(polen)) {
            return PolenSafetyEvaluator.isClaustrophobicStandingSpot(polen, currentPos);
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

    public static boolean hasImmediateHostileThreat(PolenEntity polen) {
        return findNearestHostilePos(polen, IMMEDIATE_HOSTILE_THREAT_RANGE) != null;
    }

    public static BlockPos getNearestHostileThreatPos(PolenEntity polen, double radius) {
        return findNearestHostilePos(polen, radius);
    }

    public static BlockPos findImmediateHostileEscapeSpot(PolenEntity polen, int radius) {
        Level level = polen.level();
        BlockPos origin = polen.blockPosition();
        Vec3 originCenter = Vec3.atCenterOf(origin);
        List<Monster> hostiles = level.getEntitiesOfClass(
                Monster.class,
                new AABB(origin).inflate(Math.max(6, radius)),
                monster -> monster.isAlive() && !monster.isSpectator()
        );
        if (hostiles.isEmpty()) {
            return null;
        }

        Monster anchorHostile = hostiles.stream()
                .min((left, right) -> Double.compare(left.distanceToSqr(originCenter), right.distanceToSqr(originCenter)))
                .orElse(null);
        if (anchorHostile == null) {
            return null;
        }

        Vec3 anchorPos = anchorHostile.position();
        Vec3 awayVector = originCenter.subtract(anchorPos);
        if (awayVector.lengthSqr() < 0.001D) {
            awayVector = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            awayVector = awayVector.normalize();
        }

        BlockPos bestPos = null;
        double bestScore = Double.MAX_VALUE;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -2; dy <= 4; dy++) {
                    BlockPos candidate = origin.offset(dx, dy, dz);
                    if (candidate.equals(origin)
                            || candidate.distSqr(origin) > (double) (radius * radius)
                            || !PolenSafetyEvaluator.isSafeStandingSpot(polen, candidate)
                            || PolenDangerMemoryTracker.isDangerousMemorySpot(polen, candidate)) {
                        continue;
                    }

                    Vec3 candidateCenter = Vec3.atCenterOf(candidate);
                    Vec3 stepVector = candidateCenter.subtract(originCenter);
                    if (stepVector.lengthSqr() < 0.001D) {
                        continue;
                    }

                    double awayDot = stepVector.normalize().dot(awayVector);
                    if (awayDot < 0.45D) {
                        continue;
                    }

                    double score = candidate.distSqr(origin) * 0.65D - awayDot * 42.0D;
                    boolean rejected = false;
                    for (Monster hostile : hostiles) {
                        double currentDistanceSqr = hostile.distanceToSqr(originCenter);
                        double candidateDistanceSqr = hostile.distanceToSqr(candidateCenter);
                        if (candidateDistanceSqr <= HOSTILE_REJECTION_DISTANCE_SQR
                                || candidateDistanceSqr <= currentDistanceSqr + IMMEDIATE_ESCAPE_MIN_GAIN_SQR) {
                            rejected = true;
                            break;
                        }

                        score -= Math.min(80.0D, candidateDistanceSqr * 0.24D);
                    }

                    if (rejected) {
                        continue;
                    }

                    if (level.canSeeSky(candidate)) {
                        score -= 14.0D;
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

    public static boolean tryImmediateHostileBlink(PolenEntity polen, int searchRadius, int blinkDistance) {
        BlockPos blinkTarget = findImmediateHostileBlinkTarget(polen, Math.max(4, searchRadius), Math.max(4, blinkDistance));
        if (blinkTarget == null) {
            return false;
        }

        polen.getNavigation().stop();
        polen.stopQuietActivity();
        return PolenMagicController.blinkToSafety(polen, blinkTarget);
    }

    public static boolean isEscapeTargetStillUseful(PolenEntity polen, BlockPos target) {
        if (target == null) {
            return false;
        }

        Monster nearestHostile = findNearestHostile(polen, Vec3.atCenterOf(polen.blockPosition()), HOSTILE_MEMORY_RANGE);
        if (nearestHostile == null) {
            return true;
        }

        double currentDistanceSqr = nearestHostile.distanceToSqr(Vec3.atCenterOf(polen.blockPosition()));
        double targetDistanceSqr = nearestHostile.distanceToSqr(Vec3.atCenterOf(target));
        return targetDistanceSqr >= currentDistanceSqr + 4.0D;
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

    private static BlockPos findImmediateHostileBlinkTarget(PolenEntity polen, int searchRadius, int blinkDistance) {
        Level level = polen.level();
        BlockPos origin = polen.blockPosition();
        Vec3 originCenter = Vec3.atCenterOf(origin);
        List<Monster> hostiles = level.getEntitiesOfClass(
                Monster.class,
                new AABB(origin).inflate(searchRadius + 2),
                monster -> monster.isAlive() && !monster.isSpectator()
        );
        if (hostiles.isEmpty()) {
            return null;
        }

        Monster anchorHostile = hostiles.stream()
                .min((left, right) -> Double.compare(left.distanceToSqr(originCenter), right.distanceToSqr(originCenter)))
                .orElse(null);
        if (anchorHostile == null) {
            return null;
        }

        Vec3 hostileCenter = anchorHostile.position();
        Vec3 awayVector = originCenter.subtract(hostileCenter);
        if (awayVector.lengthSqr() < 0.001D) {
            awayVector = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            awayVector = awayVector.normalize();
        }

        BlockPos bestPos = null;
        double bestScore = Double.MAX_VALUE;
        for (int dx = -blinkDistance; dx <= blinkDistance; dx++) {
            for (int dz = -blinkDistance; dz <= blinkDistance; dz++) {
                for (int dy = -2; dy <= 3; dy++) {
                    BlockPos candidate = origin.offset(dx, dy, dz);
                    if (candidate.equals(origin)
                            || candidate.distSqr(origin) > (double) (blinkDistance * blinkDistance)
                            || !PolenSafetyEvaluator.isSafeStandingSpot(polen, candidate)
                            || PolenDangerMemoryTracker.isDangerousMemorySpot(polen, candidate)) {
                        continue;
                    }

                    Vec3 candidateCenter = Vec3.atCenterOf(candidate);
                    Vec3 stepVector = candidateCenter.subtract(originCenter);
                    if (stepVector.lengthSqr() < 0.001D) {
                        continue;
                    }

                    double awayDot = stepVector.normalize().dot(awayVector);
                    if (awayDot < 0.35D) {
                        continue;
                    }

                    double score = candidate.distSqr(origin) * 0.45D - awayDot * 36.0D;
                    boolean rejected = false;
                    for (Monster hostile : hostiles) {
                        double candidateDistanceSqr = hostile.distanceToSqr(candidateCenter);
                        double currentDistanceSqr = hostile.distanceToSqr(originCenter);
                        if (candidateDistanceSqr <= HOSTILE_REJECTION_DISTANCE_SQR
                                || candidateDistanceSqr <= currentDistanceSqr + 1.0D) {
                            rejected = true;
                            break;
                        }

                        score -= Math.min(60.0D, candidateDistanceSqr * 0.18D);
                    }

                    if (rejected) {
                        continue;
                    }

                    if (level.canSeeSky(candidate)) {
                        score -= 12.0D;
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
        double hostilePenalty = scoreHostileExposure(polen, origin, candidate);
        if (hostilePenalty == Double.MAX_VALUE) {
            return Double.MAX_VALUE;
        }
        score += hostilePenalty;

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
        double hostilePenalty = scoreHostileExposure(polen, origin, candidate);
        if (hostilePenalty == Double.MAX_VALUE) {
            return Double.MAX_VALUE;
        }
        score += hostilePenalty;
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
        Level level = polen.level();
        if (!PolenSafetyEvaluator.isSafeStandingSpot(polen, candidate)
                || !PolenSafetyEvaluator.isRainShelteredStandingSpot(level, candidate)
                || PolenDangerMemoryTracker.isDangerousMemorySpot(polen, candidate)
                || candidate.distSqr(origin) < MIN_RELOCATION_DISTANCE_SQR
                || PolenShelterContextResolver.isCaveLikeShelter(level, candidate)) {
            return Double.MAX_VALUE;
        }

        PolenShelterKind kind = PolenShelterContextResolver.resolveShelterKind(level, candidate);
        if (level.isRaining() && (kind == PolenShelterKind.ROOF || kind == PolenShelterKind.NONE)) {
            return Double.MAX_VALUE;
        }

        double score = candidate.distSqr(origin);
        double hostilePenalty = scoreHostileExposure(polen, origin, candidate);
        if (hostilePenalty == Double.MAX_VALUE) {
            return Double.MAX_VALUE;
        }
        score += hostilePenalty;

        if (kind == PolenShelterKind.TREE) {
            score -= 60.0D;
        } else if (kind == PolenShelterKind.HOUSE) {
            score -= 48.0D;
        }

        if (PolenShelterContextResolver.isFlowerFriendlyShelter(level, candidate)) {
            score -= 18.0D;
        }

        if (PolenSafetyEvaluator.hasOverheadCover(level, candidate)) {
            score -= 8.0D;
        }

        int verticalDelta = Math.abs(candidate.getY() - origin.getY());
        score += verticalDelta * 12.0D;
        if (candidate.getY() > origin.getY() + 2) {
            score += 45.0D;
        }

        return score;
    }


    private static boolean hasNearbyHostile(PolenEntity polen, double radius) {
        return findNearestHostilePos(polen, radius) != null;
    }

    private static BlockPos findNearestHostilePos(PolenEntity polen, double radius) {
        Monster nearestHostile = findNearestHostile(polen, Vec3.atCenterOf(polen.blockPosition()), radius);
        return nearestHostile == null ? null : nearestHostile.blockPosition().immutable();
    }

    private static double scoreHostileExposure(PolenEntity polen, BlockPos origin, BlockPos candidate) {
        Vec3 originCenter = Vec3.atCenterOf(origin);
        Vec3 candidateCenter = Vec3.atCenterOf(candidate);
        List<Monster> hostiles = polen.level().getEntitiesOfClass(
                Monster.class,
                new AABB(candidate).inflate(Math.sqrt(HOSTILE_AWARENESS_DISTANCE_SQR)),
                monster -> monster.isAlive() && !monster.isSpectator()
        );
        if (hostiles.isEmpty()) {
            return 0.0D;
        }

        double penalty = 0.0D;
        double currentNearestSqr = Double.MAX_VALUE;
        double candidateNearestSqr = Double.MAX_VALUE;

        for (Monster hostile : hostiles) {
            double currentDistanceSqr = hostile.distanceToSqr(originCenter);
            double candidateDistanceSqr = hostile.distanceToSqr(candidateCenter);
            currentNearestSqr = Math.min(currentNearestSqr, currentDistanceSqr);
            candidateNearestSqr = Math.min(candidateNearestSqr, candidateDistanceSqr);

            if (candidateDistanceSqr <= HOSTILE_REJECTION_DISTANCE_SQR) {
                return Double.MAX_VALUE;
            }

            double proximityPenalty = Math.max(0.0D, HOSTILE_AWARENESS_DISTANCE_SQR - candidateDistanceSqr);
            penalty += proximityPenalty * (hostile.hasLineOfSight(polen) ? 1.8D : 0.9D);

            if (candidateDistanceSqr < currentDistanceSqr) {
                penalty += (currentDistanceSqr - candidateDistanceSqr) * 1.4D;
            }
        }

        if (candidateNearestSqr >= currentNearestSqr) {
            penalty -= Math.min(24.0D, (candidateNearestSqr - currentNearestSqr) * 0.20D);
        } else {
            penalty += (currentNearestSqr - candidateNearestSqr) * 2.2D;
        }

        return penalty;
    }

    private static Monster findNearestHostile(PolenEntity polen, Vec3 center, double radius) {
        return polen.level().getEntitiesOfClass(
                        Monster.class,
                        new AABB(center, center).inflate(radius),
                        monster -> monster.isAlive() && !monster.isSpectator()
                ).stream()
                .min((left, right) -> Double.compare(left.distanceToSqr(center), right.distanceToSqr(center)))
                .orElse(null);
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
