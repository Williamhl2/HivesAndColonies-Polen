package com.hivesandcolonies.characters.entity.ai.navigation.search.shelter;

import com.hivesandcolonies.characters.entity.PolenDangerMemoryTracker;
import com.hivesandcolonies.characters.entity.PolenEntity;
import com.hivesandcolonies.characters.entity.ai.navigation.search.PolenScoredSpot;
import com.hivesandcolonies.characters.entity.ai.navigation.search.PolenSpotSelectionHelper;
import com.hivesandcolonies.characters.entity.ai.navigation.safety.PolenSafetyEvaluator;
import com.hivesandcolonies.characters.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public final class PolenShelterSpotHelper {
    private static final int[] LOCAL_Y_OFFSETS = {0, 1, -1, 2, -2, 3, -3, 4};
    private static final int NEARBY_LIGHT_RADIUS = 4;
    private static final int HOUSE_SEARCH_RADIUS_BONUS = 4;
    private static final int NATURAL_SHELTER_SEARCH_RADIUS_BONUS = 6;

    private PolenShelterSpotHelper() {
    }

    public static BlockPos findNearbyShelterSpot(PolenEntity polen, int radius) {
        if (polen == null) {
            return null;
        }

        BlockPos houseShelter = findNearbyHouseShelterSpot(polen, Math.max(8, radius + HOUSE_SEARCH_RADIUS_BONUS));
        if (houseShelter != null) {
            return houseShelter;
        }

        BlockPos naturalShelter = findNearbyNaturalRainShelterSpot(
                polen,
                Math.max(10, radius + NATURAL_SHELTER_SEARCH_RADIUS_BONUS)
        );
        if (naturalShelter != null) {
            return naturalShelter;
        }

        BlockPos origin = polen.blockPosition();
        BlockPos bestSpot = null;
        double bestScore = Double.MAX_VALUE;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy : LOCAL_Y_OFFSETS) {
                    BlockPos candidate = origin.offset(dx, dy, dz);
                    double score = scoreShelterCandidate(polen, origin, candidate);
                    if (score < bestScore) {
                        bestScore = score;
                        bestSpot = candidate.immutable();
                    }
                }
            }
        }

        return bestSpot;
    }

    public static BlockPos findNearbyNaturalRainShelterSpot(PolenEntity polen, int radius) {
        if (polen == null) {
            return null;
        }

        BlockPos origin = polen.blockPosition();
        Level level = polen.level();
        List<PolenScoredSpot> shortlist = PolenSpotSelectionHelper.createShortlist();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy : LOCAL_Y_OFFSETS) {
                    BlockPos candidate = origin.offset(dx, dy, dz);
                    double score = scoreNaturalRainShelterCandidate(polen, origin, candidate);
                    if (Double.isFinite(score)) {
                        PolenSpotSelectionHelper.offerCandidate(shortlist, candidate, score);
                    }
                }
            }
        }

        return PolenSpotSelectionHelper.resolveBestReachable(polen, shortlist, 9, true);
    }

    public static BlockPos findNearbyHouseShelterSpot(PolenEntity polen, int radius) {
        BlockPos origin = polen.blockPosition();
        Level level = polen.level();
        List<PolenScoredSpot> strongHouseShortlist = PolenSpotSelectionHelper.createShortlist();
        List<PolenScoredSpot> fallbackHouseShortlist = PolenSpotSelectionHelper.createShortlist();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy : LOCAL_Y_OFFSETS) {
                    BlockPos scannedPos = origin.offset(dx, dy, dz);
                    BlockState scannedState = level.getBlockState(scannedPos);
                    if (!scannedState.is(BlockTags.DOORS)) {
                        continue;
                    }

                    BlockPos doorBase = PolenShelterContextResolver.normalizeDoorBase(scannedPos, scannedState);
                    offerDoorwayShelterCandidates(polen, strongHouseShortlist, fallbackHouseShortlist, doorBase);
                }
            }
        }

        BlockPos strongHouse = PolenSpotSelectionHelper.resolveBestReachable(polen, strongHouseShortlist, 10, true);
        if (strongHouse != null) {
            return strongHouse;
        }

        return PolenSpotSelectionHelper.resolveBestReachable(polen, fallbackHouseShortlist, 9, true);
    }

    private static void offerDoorwayShelterCandidates(
            PolenEntity polen,
            List<PolenScoredSpot> strongHouseShortlist,
            List<PolenScoredSpot> fallbackHouseShortlist,
            BlockPos doorBase
    ) {
        offerHouseCandidate(polen, strongHouseShortlist, fallbackHouseShortlist, doorBase);
        offerHouseCandidate(polen, strongHouseShortlist, fallbackHouseShortlist, doorBase.above());

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos adjacent = doorBase.relative(direction);
            BlockPos upperAdjacent = adjacent.above();
            offerHouseCandidate(polen, strongHouseShortlist, fallbackHouseShortlist, adjacent);
            offerHouseCandidate(polen, strongHouseShortlist, fallbackHouseShortlist, upperAdjacent);
            offerHouseCandidate(
                    polen,
                    strongHouseShortlist,
                    fallbackHouseShortlist,
                    adjacent.relative(direction)
            );
            offerHouseCandidate(
                    polen,
                    strongHouseShortlist,
                    fallbackHouseShortlist,
                    adjacent.relative(direction).above()
            );
        }
    }

    private static void offerHouseCandidate(
            PolenEntity polen,
            List<PolenScoredSpot> strongHouseShortlist,
            List<PolenScoredSpot> fallbackHouseShortlist,
            BlockPos candidate
    ) {
        double strongScore = scoreStrongHouseCandidate(polen, candidate);
        if (Double.isFinite(strongScore)) {
            PolenSpotSelectionHelper.offerCandidate(strongHouseShortlist, candidate, strongScore);
        }

        double fallbackScore = scoreHouseCandidate(polen, candidate);
        if (Double.isFinite(fallbackScore)) {
            PolenSpotSelectionHelper.offerCandidate(fallbackHouseShortlist, candidate, fallbackScore);
        }
    }

    private static double scoreStrongHouseCandidate(PolenEntity polen, BlockPos candidate) {
        Level level = polen.level();
        if (!PolenSafetyEvaluator.isRainShelteredStandingSpot(level, candidate)
                || PolenDangerMemoryTracker.isDangerousMemorySpot(polen, candidate)
                || !PolenShelterContextResolver.isStrongHouseInterior(level, candidate)) {
            return Double.MAX_VALUE;
        }

        BlockPos origin = polen.blockPosition();
        double score = candidate.distSqr(origin);
        score -= level.getMaxLocalRawBrightness(candidate) * 2.25D;
        score -= 34.0D;

        if (PolenShelterContextResolver.hasNearbyBed(level, candidate)) {
            score -= 8.0D;
        }

        return score;
    }

    private static double scoreHouseCandidate(PolenEntity polen, BlockPos candidate) {
        Level level = polen.level();
        if (!PolenSafetyEvaluator.isRainShelteredStandingSpot(level, candidate)
                || PolenDangerMemoryTracker.isDangerousMemorySpot(polen, candidate)
                || PolenShelterContextResolver.resolveShelterKind(level, candidate) != PolenShelterKind.HOUSE) {
            return Double.MAX_VALUE;
        }

        BlockPos origin = polen.blockPosition();
        double score = candidate.distSqr(origin);
        score -= level.getMaxLocalRawBrightness(candidate) * 2.0D;
        score -= 24.0D;

        if (PolenShelterContextResolver.hasNearbyDoor(level, candidate)) {
            score -= 8.0D;
        }

        if (PolenShelterContextResolver.hasNearbyLight(level, candidate)) {
            score -= 6.0D;
        }

        if (!level.canSeeSky(candidate.above())) {
            score -= 4.0D;
        }

        return score;
    }

    private static double scoreShelterCandidate(PolenEntity polen, BlockPos origin, BlockPos candidate) {
        Level level = polen.level();
        if (!PolenSafetyEvaluator.isRainShelteredStandingSpot(level, candidate)
                || PolenDangerMemoryTracker.isDangerousMemorySpot(polen, candidate)
                || candidate.distSqr(origin) < 1.0D
                || PolenShelterContextResolver.isCaveLikeShelter(level, candidate)) {
            return Double.MAX_VALUE;
        }

        PolenShelterKind shelterKind = PolenShelterContextResolver.resolveShelterKind(level, candidate);
        if (level.isRaining() && (shelterKind == PolenShelterKind.ROOF || shelterKind == PolenShelterKind.NONE)) {
            return Double.MAX_VALUE;
        }

        double score = candidate.distSqr(origin);
        score -= level.getMaxLocalRawBrightness(candidate) * 1.35D;

        if (PolenSafetyEvaluator.hasOverheadCover(level, candidate)) {
            score -= 4.0D;
        }

        if (!level.canSeeSky(candidate.above())) {
            score -= 1.5D;
        }

        if (shelterKind == PolenShelterKind.HOUSE) {
            score -= 30.0D;
        } else if (shelterKind == PolenShelterKind.TREE) {
            score -= 42.0D;
        }

        if (PolenShelterContextResolver.isFlowerFriendlyShelter(level, candidate)) {
            score -= 12.0D;
        }

        if (hasNearbyInterestingLight(level, candidate)) {
            score -= 10.0D;
        }

        int verticalDelta = Math.abs(candidate.getY() - origin.getY());
        score += verticalDelta * 10.0D;
        if (candidate.getY() > origin.getY() + 2) {
            score += 40.0D;
        }

        return score;
    }

    private static double scoreNaturalRainShelterCandidate(PolenEntity polen, BlockPos origin, BlockPos candidate) {
        Level level = polen.level();
        if (!PolenSafetyEvaluator.isRainShelteredStandingSpot(level, candidate)
                || PolenDangerMemoryTracker.isDangerousMemorySpot(polen, candidate)
                || candidate.distSqr(origin) < 1.0D
                || !PolenShelterContextResolver.isNaturalRainShelter(level, candidate)) {
            return Double.MAX_VALUE;
        }

        double score = candidate.distSqr(origin);
        score -= 55.0D;
        score -= level.getMaxLocalRawBrightness(candidate) * 0.75D;

        if (PolenShelterContextResolver.isFlowerFriendlyShelter(level, candidate)) {
            score -= 18.0D;
        }

        int verticalDelta = Math.abs(candidate.getY() - origin.getY());
        score += verticalDelta * 12.0D;
        if (candidate.getY() > origin.getY() + 2) {
            score += 45.0D;
        }

        return score;
    }

    private static boolean hasNearbyInterestingLight(Level level, BlockPos origin) {
        for (int dx = -NEARBY_LIGHT_RADIUS; dx <= NEARBY_LIGHT_RADIUS; dx++) {
            for (int dz = -NEARBY_LIGHT_RADIUS; dz <= NEARBY_LIGHT_RADIUS; dz++) {
                for (int dy = -1; dy <= 2; dy++) {
                    BlockPos candidate = origin.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(candidate);
                    if (state.is(ModBlocks.POLEN_LANTERN.get())) {
                        continue;
                    }

                    if (state.getLightEmission() >= 10) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
