package com.hivesandcolonies.characters.character.polen.entity.ai.world.home;

import com.hivesandcolonies.characters.character.polen.entity.PolenDangerMemoryTracker;
import com.hivesandcolonies.characters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.characters.character.polen.entity.ai.navigation.search.shelter.PolenShelterContextResolver;
import com.hivesandcolonies.characters.character.polen.entity.ai.navigation.search.shelter.PolenShelterKind;
import com.hivesandcolonies.characters.character.polen.entity.ai.navigation.safety.PolenSafetyEvaluator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class PolenResidenceValidator {
    private static final int SEARCH_RADIUS = 4;
    private static final int[] LOCAL_Y_OFFSETS = {0, 1, -1, 2, -2};
    private static final int MIN_BORROWED_HABITABLE_SPOTS = 3;
    private static final int MIN_OWN_SPACE_HABITABLE_SPOTS = 5;

    private PolenResidenceValidator() {
    }

    public static PolenResidenceValidation validate(PolenEntity polen, BlockPos hintPos) {
        if (polen == null || hintPos == null) {
            return PolenResidenceValidation.failure("message.polen.item.residence_charm.invalid_place");
        }

        Level level = polen.level();
        PolenResidenceTarget bestTarget = null;
        double bestScore = Double.MAX_VALUE;
        boolean foundShelteredArea = false;
        boolean foundBed = false;
        boolean foundAccess = false;
        boolean foundHabitableSpace = false;

        for (int dx = -SEARCH_RADIUS; dx <= SEARCH_RADIUS; dx++) {
            for (int dz = -SEARCH_RADIUS; dz <= SEARCH_RADIUS; dz++) {
                for (int dy : LOCAL_Y_OFFSETS) {
                    BlockPos candidate = hintPos.offset(dx, dy, dz);
                    if (!PolenSafetyEvaluator.isSafeStandingSpot(polen, candidate)
                            || !PolenSafetyEvaluator.isRainShelteredStandingSpot(level, candidate)
                            || PolenDangerMemoryTracker.isDangerousMemorySpot(polen, candidate)) {
                        continue;
                    }

                    PolenShelterKind shelterKind = PolenShelterContextResolver.resolveShelterKind(level, candidate);
                    if (shelterKind == PolenShelterKind.NONE || shelterKind == PolenShelterKind.TREE) {
                        continue;
                    }
                    foundShelteredArea = true;

                    boolean hasBed = PolenShelterContextResolver.hasNearbyBed(level, candidate);
                    if (!hasBed) {
                        continue;
                    }
                    foundBed = true;

                    boolean hasAccess = hasResidenceAccess(level, candidate, shelterKind);
                    if (!hasAccess) {
                        continue;
                    }
                    foundAccess = true;

                    int habitableSpots = countHabitableStandingSpots(polen, candidate);
                    if (habitableSpots < MIN_BORROWED_HABITABLE_SPOTS) {
                        continue;
                    }
                    foundHabitableSpace = true;

                    PolenResidenceStage stage = determineStage(level, candidate, shelterKind, habitableSpots);
                    String context = shelterKind == PolenShelterKind.HOUSE ? "house" : "roof";
                    double score = scoreCandidate(hintPos, candidate, shelterKind, stage, habitableSpots, level);
                    if (score < bestScore) {
                        bestScore = score;
                        bestTarget = new PolenResidenceTarget(
                                hintPos.immutable(),
                                candidate.immutable(),
                                context,
                                stage
                        );
                    }
                }
            }
        }

        if (bestTarget != null) {
            return PolenResidenceValidation.success(bestTarget);
        }

        if (!foundShelteredArea) {
            return PolenResidenceValidation.failure("message.polen.item.residence_charm.needs_roof");
        }
        if (!foundBed) {
            return PolenResidenceValidation.failure("message.polen.item.residence_charm.needs_bed");
        }
        if (!foundAccess) {
            return PolenResidenceValidation.failure("message.polen.item.residence_charm.needs_access");
        }
        if (!foundHabitableSpace) {
            return PolenResidenceValidation.failure("message.polen.item.residence_charm.needs_space");
        }

        return PolenResidenceValidation.failure("message.polen.item.residence_charm.invalid_place");
    }

    public static boolean isStillValid(PolenEntity polen, PolenResidenceTarget target) {
        if (polen == null || target == null || target.usePos() == null) {
            return false;
        }

        Level level = polen.level();
        BlockPos usePos = target.usePos();
        if (!PolenSafetyEvaluator.isSafeStandingSpot(polen, usePos)
                || !PolenSafetyEvaluator.isRainShelteredStandingSpot(level, usePos)
                || PolenDangerMemoryTracker.isDangerousMemorySpot(polen, usePos)) {
            return false;
        }

        PolenShelterKind shelterKind = PolenShelterContextResolver.resolveShelterKind(level, usePos);
        if (shelterKind == PolenShelterKind.NONE || shelterKind == PolenShelterKind.TREE) {
            return false;
        }

        return PolenShelterContextResolver.hasNearbyBed(level, usePos)
                && hasResidenceAccess(level, usePos, shelterKind)
                && countHabitableStandingSpots(polen, usePos) >= MIN_BORROWED_HABITABLE_SPOTS;
    }

    private static PolenResidenceStage determineStage(
            Level level,
            BlockPos candidate,
            PolenShelterKind shelterKind,
            int habitableSpots
    ) {
        if (shelterKind == PolenShelterKind.HOUSE
                && PolenShelterContextResolver.isStrongHouseInterior(level, candidate)
                && habitableSpots >= MIN_OWN_SPACE_HABITABLE_SPOTS) {
            return PolenResidenceStage.OWN_SPACE;
        }

        return PolenResidenceStage.BORROWED_SHELTER;
    }

    private static boolean hasResidenceAccess(Level level, BlockPos candidate, PolenShelterKind shelterKind) {
        if (shelterKind == PolenShelterKind.HOUSE) {
            return PolenShelterContextResolver.hasNearbyDoor(level, candidate);
        }

        return PolenSafetyEvaluator.isNearOutdoorSurface(level, candidate);
    }

    private static int countHabitableStandingSpots(PolenEntity polen, BlockPos origin) {
        int count = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos candidate = origin.offset(dx, 0, dz);
                if (PolenSafetyEvaluator.isStandableSpot(polen, candidate)) {
                    count++;
                }
            }
        }
        return count;
    }

    private static double scoreCandidate(
            BlockPos hintPos,
            BlockPos candidate,
            PolenShelterKind shelterKind,
            PolenResidenceStage stage,
            int habitableSpots,
            Level level
    ) {
        double score = candidate.distSqr(hintPos);
        score -= habitableSpots * 2.5D;
        score -= level.getMaxLocalRawBrightness(candidate) * 1.5D;

        if (shelterKind == PolenShelterKind.HOUSE) {
            score -= 10.0D;
        } else if (shelterKind == PolenShelterKind.ROOF) {
            score -= 4.0D;
        }

        if (stage == PolenResidenceStage.OWN_SPACE) {
            score -= 14.0D;
        }

        return score;
    }
}
