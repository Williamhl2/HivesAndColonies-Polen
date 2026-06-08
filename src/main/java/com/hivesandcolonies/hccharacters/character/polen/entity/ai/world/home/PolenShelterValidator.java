package com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home;

import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.safety.PolenSafetyEvaluator;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.search.shelter.PolenShelterContextResolver;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.search.shelter.PolenShelterKind;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class PolenShelterValidator {
    private static final int SEARCH_RADIUS = 3;
    private static final int[] LOCAL_Y_OFFSETS = {0, 1, -1, 2};

    private PolenShelterValidator() {
    }

    public static BlockPos findStoryShelter(PolenEntity polen, BlockPos hintPos) {
        if (polen == null || hintPos == null) {
            return null;
        }

        Level level = polen.level();
        BlockPos bestSpot = null;
        double bestScore = Double.MAX_VALUE;

        for (int dx = -SEARCH_RADIUS; dx <= SEARCH_RADIUS; dx++) {
            for (int dz = -SEARCH_RADIUS; dz <= SEARCH_RADIUS; dz++) {
                for (int dy : LOCAL_Y_OFFSETS) {
                    BlockPos candidate = hintPos.offset(dx, dy, dz);
                    if (!PolenSafetyEvaluator.isSafeStandingSpot(polen, candidate)
                            || !PolenSafetyEvaluator.isRainShelteredStandingSpot(level, candidate)) {
                        continue;
                    }

                    PolenShelterKind shelterKind = PolenShelterContextResolver.resolveShelterKind(level, candidate);
                    if (shelterKind == PolenShelterKind.NONE || shelterKind == PolenShelterKind.TREE) {
                        continue;
                    }

                    boolean hasBed = PolenShelterContextResolver.hasNearbyBed(level, candidate);
                    boolean hasDoor = PolenShelterContextResolver.hasNearbyDoor(level, candidate);
                    boolean hasLight = PolenShelterContextResolver.hasNearbyLight(level, candidate);
                    if (!hasBed || (!hasDoor && !hasLight)) {
                        continue;
                    }

                    double score = candidate.distSqr(hintPos);
                    if (shelterKind == PolenShelterKind.HOUSE) {
                        score -= 6.0D;
                    }
                    if (hasDoor) {
                        score -= 3.0D;
                    }
                    if (hasLight) {
                        score -= 2.0D;
                    }

                    if (score < bestScore) {
                        bestScore = score;
                        bestSpot = candidate.immutable();
                    }
                }
            }
        }

        return bestSpot;
    }
}
