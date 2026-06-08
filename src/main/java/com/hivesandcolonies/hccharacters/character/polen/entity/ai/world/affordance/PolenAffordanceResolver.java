package com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.affordance;

import com.hivesandcolonies.hccharacters.character.polen.entity.PolenDangerMemoryTracker;
import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.interest.PolenInterestLocator;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.interest.PolenInterestTarget;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.interest.PolenInterestType;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.routine.PolenRoutinePlanner;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.search.light.PolenLightSpotHelper;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.search.shelter.PolenShelterContextResolver;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.search.shelter.PolenShelterKind;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.search.shelter.PolenShelterSpotHelper;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.safety.PolenSafetyEvaluator;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.safety.PolenSafetyNavigator;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.comfort.PolenComfortEvaluator;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.comfort.PolenComfortProfile;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home.PolenHomeManager;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home.PolenResidenceTarget;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.interests.PolenAffinityBehaviorHooks;
import net.minecraft.core.BlockPos;

public final class PolenAffordanceResolver {
    private static final int DEFAULT_REST_TRAVEL_RADIUS = 64;
    private static final int BAD_WEATHER_REST_TRAVEL_RADIUS = 96;
    private static final int DEFAULT_HOME_TRAVEL_RADIUS = 64;
    private static final int BAD_WEATHER_HOME_TRAVEL_RADIUS = 96;
    private static final double KNOWN_HOME_PRIORITY_RADIUS = 28.0D;
    private static final double HOME_INTEREST_RADIUS = 28.0D;

    private PolenAffordanceResolver() {
    }

    public static PolenAffordanceTarget findBestRainShelter(PolenEntity polen, int radius) {
        if (polen == null) {
            return null;
        }

        BlockPos origin = polen.blockPosition();
        PolenAffordanceTarget bestTarget = null;
        double bestScore = Double.MAX_VALUE;

        PolenResidenceTarget rememberedResidence = PolenHomeManager.getRememberedResidence(polen);
        if (rememberedResidence != null
                && shouldConsiderResidenceForShelter(polen, origin, rememberedResidence.usePos(), radius)) {
            PolenAffordanceTarget target = new PolenAffordanceTarget(
                    rememberedResidence.anchorPos().immutable(),
                    rememberedResidence.usePos().immutable(),
                    PolenAffordanceType.RESIDENCE,
                    "residence_" + rememberedResidence.context()
            );
            bestScore = scoreResidenceRestCandidate(polen, origin, rememberedResidence, true);
            bestTarget = target;
        }

        BlockPos normalizedRestingPos = PolenRoutinePlanner.normalizeRestingAnchor(polen, polen.getAiState().getRestingPos());
        if (normalizedRestingPos != null
                && shouldConsiderRestingSpotForShelter(polen, origin, normalizedRestingPos, radius)) {
            PolenAffordanceTarget target = new PolenAffordanceTarget(
                    normalizedRestingPos.immutable(),
                    normalizedRestingPos.immutable(),
                    PolenAffordanceType.REST,
                    "remembered_rest"
            );
            double candidateScore = scoreRememberedRestCandidate(polen, origin, target.usePos(), true);
            if (candidateScore < bestScore) {
                bestScore = candidateScore;
                bestTarget = target;
            }
        }

        BlockPos houseShelter = PolenShelterSpotHelper.findNearbyHouseShelterSpot(polen, Math.max(8, radius + 4));
        if (houseShelter != null) {
            PolenAffordanceTarget candidate = shelterAffordance(houseShelter, houseShelter, PolenShelterKind.HOUSE);
            double candidateScore = scoreShelterCandidate(polen, origin, candidate.usePos());
            if (candidateScore < bestScore) {
                bestScore = candidateScore;
                bestTarget = candidate;
            }
        }

        BlockPos generalShelter = PolenShelterSpotHelper.findNearbyShelterSpot(polen, radius);
        if (generalShelter != null) {
            PolenShelterKind kind = PolenShelterContextResolver.resolveShelterKind(polen.level(), generalShelter);
            PolenAffordanceTarget candidate = shelterAffordance(generalShelter, generalShelter, kind);
            double candidateScore = scoreShelterCandidate(polen, origin, candidate.usePos());
            if (candidateScore < bestScore) {
                bestScore = candidateScore;
                bestTarget = candidate;
            }
        }

        return bestTarget;
    }

    public static PolenAffordanceTarget findBestNightLight(PolenEntity polen, int radius) {
        if (polen == null) {
            return null;
        }

        PolenInterestTarget nearbyLight = PolenInterestLocator.findPreferredInterestOfType(polen, PolenInterestType.LIGHT);
        if (nearbyLight != null
                && nearbyLight.observePos().distSqr(polen.blockPosition()) <= (double) (radius * radius)
                && PolenSafetyEvaluator.isSafeStandingSpot(polen, nearbyLight.observePos())
                && !PolenDangerMemoryTracker.isDangerousMemorySpot(polen, nearbyLight.observePos())) {
            return new PolenAffordanceTarget(
                    nearbyLight.pos().immutable(),
                    nearbyLight.observePos().immutable(),
                    PolenAffordanceType.EXISTING_LIGHT,
                    "existing_light"
            );
        }

        BlockPos lightTarget = PolenLightSpotHelper.findLightMagicTarget(polen);
        if (lightTarget != null) {
            return new PolenAffordanceTarget(
                    lightTarget.immutable(),
                    lightTarget.immutable(),
                    PolenAffordanceType.MAGIC_LIGHT,
                    "magic_light"
            );
        }

        return null;
    }

    public static PolenAffordanceTarget findBestRestSpot(PolenEntity polen, int safeRadius) {
        if (polen == null) {
            return null;
        }

        BlockPos origin = polen.blockPosition();
        PolenAffordanceTarget bestTarget = null;
        double bestScore = Double.MAX_VALUE;
        boolean prioritizeKnownHome = shouldPrioritizeKnownHome(polen);

        PolenResidenceTarget rememberedResidence = PolenHomeManager.getRememberedResidence(polen);
        if (rememberedResidence != null && shouldConsiderResidenceForRest(polen, origin, rememberedResidence.usePos(), safeRadius)) {
            PolenAffordanceTarget target = new PolenAffordanceTarget(
                    rememberedResidence.anchorPos().immutable(),
                    rememberedResidence.usePos().immutable(),
                    PolenAffordanceType.RESIDENCE,
                    "residence_" + rememberedResidence.context()
            );
            bestScore = scoreResidenceRestCandidate(polen, origin, rememberedResidence, false);
            bestTarget = target;
            if (prioritizeKnownHome) {
                return bestTarget;
            }
        }

        BlockPos normalizedRestingPos = PolenRoutinePlanner.normalizeRestingAnchor(polen, polen.getAiState().getRestingPos());
        if (normalizedRestingPos != null && shouldConsiderRememberedRest(polen, origin, normalizedRestingPos, safeRadius)) {
            PolenAffordanceTarget target = new PolenAffordanceTarget(
                    normalizedRestingPos.immutable(),
                    normalizedRestingPos.immutable(),
                    PolenAffordanceType.REST,
                    "remembered_rest"
            );
            double candidateScore = scoreRememberedRestCandidate(polen, origin, target.usePos(), false);
            if (candidateScore < bestScore) {
                bestScore = candidateScore;
                bestTarget = target;
            }
            if (prioritizeKnownHome && bestTarget != null) {
                return bestTarget;
            }
        }

        BlockPos fallbackRest = PolenSafetyNavigator.findNearbySafeSurfaceSpot(polen, safeRadius);
        if (fallbackRest != null) {
            PolenAffordanceTarget target = new PolenAffordanceTarget(
                    fallbackRest.immutable(),
                    fallbackRest.immutable(),
                    PolenAffordanceType.REST,
                    "fallback_rest"
            );
            double candidateScore = scoreLocalRestCandidate(polen, origin, target.usePos());
            if (candidateScore < bestScore) {
                bestScore = candidateScore;
                bestTarget = target;
            }
        }

        return bestTarget;
    }

    public static PolenAffordanceTarget findBestInterest(PolenEntity polen, boolean includeSource) {
        PolenAffordanceTarget affinityTarget = PolenAffinityBehaviorHooks.findAffinityInterestTarget(polen);
        if (affinityTarget != null && isAllowedInterestTarget(polen, affinityTarget)) {
            return affinityTarget;
        }

        PolenInterestTarget target = PolenInterestLocator.findPreferredInterest(polen, includeSource);
        PolenAffordanceTarget affordance = target == null ? null : fromInterestTarget(target);
        return isAllowedInterestTarget(polen, affordance) ? affordance : null;
    }

    public static PolenAffordanceTarget findBestInterestOfType(PolenEntity polen, PolenInterestType type) {
        PolenInterestTarget target = PolenInterestLocator.findPreferredInterestOfType(polen, type);
        PolenAffordanceTarget affordance = target == null ? null : fromInterestTarget(target);
        return isAllowedInterestTarget(polen, affordance) ? affordance : null;
    }

    private static boolean shouldPrioritizeKnownHome(PolenEntity polen) {
        return polen != null
                && (polen.level().isNight()
                || polen.level().isRaining()
                || PolenHomeManager.isFarFromHome(polen, KNOWN_HOME_PRIORITY_RADIUS));
    }

    private static boolean isAllowedInterestTarget(PolenEntity polen, PolenAffordanceTarget target) {
        if (polen == null || target == null) {
            return false;
        }

        if (polen.level().isNight() || polen.level().isRaining()) {
            return target.type() == PolenAffordanceType.EXISTING_LIGHT
                    && PolenHomeManager.isPositionWithinHomeRadius(polen, target.usePos(), Math.min(12.0D, HOME_INTEREST_RADIUS));
        }

        return PolenHomeManager.isPositionWithinHomeRadius(polen, target.usePos(), HOME_INTEREST_RADIUS)
                && !PolenHomeManager.isFarFromHome(polen, HOME_INTEREST_RADIUS + 6.0D);
    }

    private static PolenAffordanceTarget fromInterestTarget(PolenInterestTarget target) {
        PolenAffordanceType type = switch (target.type()) {
            case FLOWER -> PolenAffordanceType.INTEREST_FLOWER;
            case HIVE -> PolenAffordanceType.INTEREST_HIVE;
            case SOURCE -> PolenAffordanceType.INTEREST_SOURCE;
            case LIGHT -> PolenAffordanceType.EXISTING_LIGHT;
        };

        String contextKey = switch (target.type()) {
            case FLOWER -> "flower";
            case HIVE -> "hive";
            case SOURCE -> "source";
            case LIGHT -> "existing_light";
        };

        return new PolenAffordanceTarget(
                target.pos().immutable(),
                target.observePos().immutable(),
                type,
                contextKey
        );
    }

    private static PolenAffordanceTarget shelterAffordance(BlockPos focusPos, BlockPos usePos, PolenShelterKind kind) {
        PolenAffordanceType type = switch (kind) {
            case HOUSE -> PolenAffordanceType.SHELTER_HOUSE;
            case TREE -> PolenAffordanceType.SHELTER_TREE;
            case ROOF, NONE -> PolenAffordanceType.SHELTER_ROOF;
        };

        String contextKey = switch (kind) {
            case HOUSE -> "house";
            case TREE -> "tree";
            case ROOF, NONE -> "roof";
        };

        return new PolenAffordanceTarget(
                focusPos.immutable(),
                usePos.immutable(),
                type,
                contextKey
        );
    }

    private static boolean shouldConsiderResidenceForRest(
            PolenEntity polen,
            BlockPos origin,
            BlockPos residenceUsePos,
            int safeRadius
    ) {
        if (origin == null || residenceUsePos == null) {
            return false;
        }

        int maxRadius = polen.level().isNight() || polen.level().isRaining() || PolenHomeManager.isFarFromHome(polen, KNOWN_HOME_PRIORITY_RADIUS)
                ? Math.max(BAD_WEATHER_REST_TRAVEL_RADIUS, safeRadius * 8)
                : Math.max(DEFAULT_REST_TRAVEL_RADIUS, safeRadius * 4);
        return residenceUsePos.distSqr(origin) <= (double) (maxRadius * maxRadius);
    }

    private static boolean shouldConsiderResidenceForShelter(
            PolenEntity polen,
            BlockPos origin,
            BlockPos residenceUsePos,
            int radius
    ) {
        if (origin == null || residenceUsePos == null) {
            return false;
        }

        int maxRadius = polen.level().isNight() || polen.level().isRaining()
                ? Math.max(BAD_WEATHER_HOME_TRAVEL_RADIUS, radius * 3)
                : Math.max(DEFAULT_HOME_TRAVEL_RADIUS, radius * 2);
        return residenceUsePos.distSqr(origin) <= (double) (maxRadius * maxRadius);
    }

    private static boolean shouldConsiderRestingSpotForShelter(
            PolenEntity polen,
            BlockPos origin,
            BlockPos restingPos,
            int radius
    ) {
        if (origin == null || restingPos == null) {
            return false;
        }

        return restingPos.distSqr(origin) <= (double) (Math.max(DEFAULT_HOME_TRAVEL_RADIUS, radius * 2) * Math.max(DEFAULT_HOME_TRAVEL_RADIUS, radius * 2))
                && PolenSafetyEvaluator.isRainShelteredStandingSpot(polen.level(), restingPos)
                && !PolenDangerMemoryTracker.isDangerousMemorySpot(polen, restingPos);
    }

    private static boolean shouldConsiderRememberedRest(
            PolenEntity polen,
            BlockPos origin,
            BlockPos candidate,
            int safeRadius
    ) {
        if (origin == null || candidate == null) {
            return false;
        }

        int maxRadius = polen.level().isNight() || polen.level().isRaining() || PolenHomeManager.isFarFromHome(polen, KNOWN_HOME_PRIORITY_RADIUS)
                ? Math.max(BAD_WEATHER_REST_TRAVEL_RADIUS, safeRadius * 8)
                : Math.max(DEFAULT_REST_TRAVEL_RADIUS, safeRadius * 4);
        return candidate.distSqr(origin) <= (double) (maxRadius * maxRadius);
    }

    private static double scoreResidenceRestCandidate(
            PolenEntity polen,
            BlockPos origin,
            PolenResidenceTarget target,
            boolean shelterContext
    ) {
        BlockPos candidate = target.usePos();
        double score = PolenComfortEvaluator.comfortAdjustedDistanceScore(
                polen,
                origin,
                candidate,
                PolenComfortProfile.RESIDENCE
        );
        if (!shelterContext) {
            score -= 14.0D;
        }
        if (polen.level().isNight() || polen.level().isRaining()) {
            score -= 22.0D;
        }
        if (target.stage().ordinal() >= com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home.PolenResidenceStage.OWN_SPACE.ordinal()) {
            score -= 12.0D;
        } else {
            score -= 5.0D;
        }
        return score;
    }

    private static double scoreLocalRestCandidate(PolenEntity polen, BlockPos origin, BlockPos candidate) {
        double score = PolenComfortEvaluator.comfortAdjustedDistanceScore(
                polen,
                origin,
                candidate,
                PolenComfortProfile.SHELTER
        );
        if (candidate.closerToCenterThan(polen.position(), 4.0D)) {
            score -= 4.0D;
        }
        return score;
    }

    private static double scoreRememberedRestCandidate(
            PolenEntity polen,
            BlockPos origin,
            BlockPos candidate,
            boolean shelterContext
    ) {
        double score = scoreLocalRestCandidate(polen, origin, candidate);
        score -= shelterContext ? 12.0D : 9.0D;
        if (polen.level().isNight() || polen.level().isRaining()) {
            score -= 12.0D;
        }
        return score;
    }

    private static double scoreShelterCandidate(PolenEntity polen, BlockPos origin, BlockPos candidate) {
        if (PolenShelterContextResolver.isCaveLikeShelter(polen.level(), candidate)) {
            return Double.MAX_VALUE;
        }

        PolenShelterKind kind = PolenShelterContextResolver.resolveShelterKind(polen.level(), candidate);
        if (polen.level().isRaining() && (kind == PolenShelterKind.ROOF || kind == PolenShelterKind.NONE)) {
            return Double.MAX_VALUE;
        }

        double score = PolenComfortEvaluator.comfortAdjustedDistanceScore(
                polen,
                origin,
                candidate,
                PolenComfortProfile.SHELTER
        );
        if (kind == PolenShelterKind.TREE) {
            score -= 28.0D;
        } else if (kind == PolenShelterKind.HOUSE) {
            score -= 22.0D;
        }
        if (PolenShelterContextResolver.isFlowerFriendlyShelter(polen.level(), candidate)) {
            score -= 10.0D;
        }
        if (polen.level().isRaining()) {
            score -= 6.0D;
        }
        return score;
    }
}
