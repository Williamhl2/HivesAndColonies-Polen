package com.hivesandcolonies.characters.character.polen.entity.ai.world.affordance;

import com.hivesandcolonies.characters.character.polen.entity.PolenDangerMemoryTracker;
import com.hivesandcolonies.characters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.interest.PolenInterestLocator;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.interest.PolenInterestTarget;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.interest.PolenInterestType;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.routine.PolenRoutinePlanner;
import com.hivesandcolonies.characters.character.polen.entity.ai.navigation.search.light.PolenLightSpotHelper;
import com.hivesandcolonies.characters.character.polen.entity.ai.navigation.search.shelter.PolenShelterContextResolver;
import com.hivesandcolonies.characters.character.polen.entity.ai.navigation.search.shelter.PolenShelterKind;
import com.hivesandcolonies.characters.character.polen.entity.ai.navigation.search.shelter.PolenShelterSpotHelper;
import com.hivesandcolonies.characters.character.polen.entity.ai.navigation.safety.PolenSafetyEvaluator;
import com.hivesandcolonies.characters.character.polen.entity.ai.navigation.safety.PolenSafetyNavigator;
import com.hivesandcolonies.characters.character.polen.entity.ai.world.comfort.PolenComfortEvaluator;
import com.hivesandcolonies.characters.character.polen.entity.ai.world.comfort.PolenComfortProfile;
import com.hivesandcolonies.characters.character.polen.entity.ai.world.home.PolenHomeManager;
import com.hivesandcolonies.characters.character.polen.entity.ai.world.home.PolenResidenceTarget;
import com.hivesandcolonies.characters.character.polen.entity.ai.world.interests.PolenAffinityBehaviorHooks;
import net.minecraft.core.BlockPos;

public final class PolenAffordanceResolver {
    private static final int DEFAULT_REST_TRAVEL_RADIUS = 18;
    private static final int BAD_WEATHER_REST_TRAVEL_RADIUS = 36;

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
                && rememberedResidence.usePos().distSqr(polen.blockPosition()) <= (double) ((radius + 8) * (radius + 8))) {
            PolenAffordanceTarget target = new PolenAffordanceTarget(
                    rememberedResidence.anchorPos().immutable(),
                    rememberedResidence.usePos().immutable(),
                    PolenAffordanceType.RESIDENCE,
                    "residence_" + rememberedResidence.context()
            );
            bestScore = scoreResidenceRestCandidate(polen, origin, target.usePos(), true);
            bestTarget = target;
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

        PolenResidenceTarget rememberedResidence = PolenHomeManager.getRememberedResidence(polen);
        if (rememberedResidence != null && shouldConsiderResidenceForRest(polen, origin, rememberedResidence.usePos(), safeRadius)) {
            PolenAffordanceTarget target = new PolenAffordanceTarget(
                    rememberedResidence.anchorPos().immutable(),
                    rememberedResidence.usePos().immutable(),
                    PolenAffordanceType.RESIDENCE,
                    "residence_" + rememberedResidence.context()
            );
            bestScore = scoreResidenceRestCandidate(polen, origin, target.usePos(), false);
            bestTarget = target;
        }

        BlockPos normalizedRestingPos = PolenRoutinePlanner.normalizeRestingAnchor(polen, polen.getAiState().getRestingPos());
        if (normalizedRestingPos != null && shouldConsiderLocalRest(origin, normalizedRestingPos, safeRadius)) {
            PolenAffordanceTarget target = new PolenAffordanceTarget(
                    normalizedRestingPos.immutable(),
                    normalizedRestingPos.immutable(),
                    PolenAffordanceType.REST,
                    "remembered_rest"
            );
            double candidateScore = scoreLocalRestCandidate(polen, origin, target.usePos());
            if (candidateScore < bestScore) {
                bestScore = candidateScore;
                bestTarget = target;
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
        if (affinityTarget != null) {
            return affinityTarget;
        }

        PolenInterestTarget target = PolenInterestLocator.findPreferredInterest(polen, includeSource);
        return target == null ? null : fromInterestTarget(target);
    }

    public static PolenAffordanceTarget findBestInterestOfType(PolenEntity polen, PolenInterestType type) {
        PolenInterestTarget target = PolenInterestLocator.findPreferredInterestOfType(polen, type);
        return target == null ? null : fromInterestTarget(target);
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

        int maxRadius = polen.level().isNight() || polen.level().isRaining()
                ? Math.max(BAD_WEATHER_REST_TRAVEL_RADIUS, safeRadius * 3)
                : Math.max(DEFAULT_REST_TRAVEL_RADIUS, safeRadius * 2);
        return residenceUsePos.distSqr(origin) <= (double) (maxRadius * maxRadius);
    }

    private static boolean shouldConsiderLocalRest(BlockPos origin, BlockPos candidate, int safeRadius) {
        if (origin == null || candidate == null) {
            return false;
        }

        int maxRadius = Math.max(DEFAULT_REST_TRAVEL_RADIUS, safeRadius * 2);
        return candidate.distSqr(origin) <= (double) (maxRadius * maxRadius);
    }

    private static double scoreResidenceRestCandidate(
            PolenEntity polen,
            BlockPos origin,
            BlockPos candidate,
            boolean shelterContext
    ) {
        double score = PolenComfortEvaluator.comfortAdjustedDistanceScore(
                polen,
                origin,
                candidate,
                PolenComfortProfile.RESIDENCE
        );
        if (!shelterContext) {
            score -= 10.0D;
        }
        if (polen.level().isNight() || polen.level().isRaining()) {
            score -= 14.0D;
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
