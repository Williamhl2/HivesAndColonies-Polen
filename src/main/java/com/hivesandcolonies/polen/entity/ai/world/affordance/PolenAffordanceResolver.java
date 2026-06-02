package com.hivesandcolonies.polen.entity.ai.world.affordance;

import com.hivesandcolonies.polen.entity.PolenDangerMemoryTracker;
import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.ai.brain.interest.PolenInterestLocator;
import com.hivesandcolonies.polen.entity.ai.brain.interest.PolenInterestTarget;
import com.hivesandcolonies.polen.entity.ai.brain.interest.PolenInterestType;
import com.hivesandcolonies.polen.entity.ai.brain.routine.PolenRoutinePlanner;
import com.hivesandcolonies.polen.entity.ai.navigation.search.light.PolenLightSpotHelper;
import com.hivesandcolonies.polen.entity.ai.navigation.search.shelter.PolenShelterContextResolver;
import com.hivesandcolonies.polen.entity.ai.navigation.search.shelter.PolenShelterKind;
import com.hivesandcolonies.polen.entity.ai.navigation.search.shelter.PolenShelterSpotHelper;
import com.hivesandcolonies.polen.entity.ai.navigation.safety.PolenSafetyEvaluator;
import com.hivesandcolonies.polen.entity.ai.navigation.safety.PolenSafetyNavigator;
import com.hivesandcolonies.polen.entity.ai.world.home.PolenHomeManager;
import com.hivesandcolonies.polen.entity.ai.world.home.PolenResidenceTarget;
import com.hivesandcolonies.polen.entity.ai.world.interests.PolenAffinityBehaviorHooks;
import net.minecraft.core.BlockPos;

public final class PolenAffordanceResolver {
    private PolenAffordanceResolver() {
    }

    public static PolenAffordanceTarget findBestRainShelter(PolenEntity polen, int radius) {
        if (polen == null) {
            return null;
        }

        PolenResidenceTarget rememberedResidence = PolenHomeManager.getRememberedResidence(polen);
        if (rememberedResidence != null
                && rememberedResidence.usePos().distSqr(polen.blockPosition()) <= (double) ((radius + 8) * (radius + 8))) {
            return new PolenAffordanceTarget(
                    rememberedResidence.anchorPos().immutable(),
                    rememberedResidence.usePos().immutable(),
                    PolenAffordanceType.RESIDENCE,
                    "residence_" + rememberedResidence.context()
            );
        }

        BlockPos houseShelter = PolenShelterSpotHelper.findNearbyHouseShelterSpot(polen, Math.max(8, radius + 4));
        if (houseShelter != null) {
            return shelterAffordance(houseShelter, houseShelter, PolenShelterKind.HOUSE);
        }

        BlockPos generalShelter = PolenShelterSpotHelper.findNearbyShelterSpot(polen, radius);
        if (generalShelter != null) {
            PolenShelterKind kind = PolenShelterContextResolver.resolveShelterKind(polen.level(), generalShelter);
            return shelterAffordance(generalShelter, generalShelter, kind);
        }

        return null;
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

        PolenResidenceTarget rememberedResidence = PolenHomeManager.getRememberedResidence(polen);
        if (rememberedResidence != null) {
            return new PolenAffordanceTarget(
                    rememberedResidence.anchorPos().immutable(),
                    rememberedResidence.usePos().immutable(),
                    PolenAffordanceType.RESIDENCE,
                    "residence_" + rememberedResidence.context()
            );
        }

        BlockPos normalizedRestingPos = PolenRoutinePlanner.normalizeRestingAnchor(polen, polen.getAiState().getRestingPos());
        if (normalizedRestingPos != null) {
            return new PolenAffordanceTarget(
                    normalizedRestingPos.immutable(),
                    normalizedRestingPos.immutable(),
                    PolenAffordanceType.REST,
                    "remembered_rest"
            );
        }

        BlockPos fallbackRest = PolenSafetyNavigator.findNearbySafeSurfaceSpot(polen, safeRadius);
        if (fallbackRest != null) {
            return new PolenAffordanceTarget(
                    fallbackRest.immutable(),
                    fallbackRest.immutable(),
                    PolenAffordanceType.REST,
                    "fallback_rest"
            );
        }

        return null;
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
}
