package com.hivesandcolonies.polen.entity.ai.navigation.search;

import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.ai.ability.magic.PolenMagicController;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.pathfinder.Path;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class PolenSpotSelectionHelper {
    private static final int SHORTLIST_SIZE = 8;

    private PolenSpotSelectionHelper() {
    }

    public static List<PolenScoredSpot> createShortlist() {
        return new ArrayList<>(SHORTLIST_SIZE);
    }

    public static void offerCandidate(List<PolenScoredSpot> shortlist, BlockPos pos, double score) {
        if (pos == null || !Double.isFinite(score)) {
            return;
        }

        for (int i = 0; i < shortlist.size(); i++) {
            PolenScoredSpot existing = shortlist.get(i);
            if (existing.pos().equals(pos)) {
                if (score < existing.score()) {
                    shortlist.set(i, new PolenScoredSpot(pos.immutable(), score));
                }
                return;
            }
        }

        if (shortlist.size() < SHORTLIST_SIZE) {
            shortlist.add(new PolenScoredSpot(pos.immutable(), score));
            return;
        }

        int worstIndex = 0;
        double worstScore = shortlist.get(0).score();
        for (int i = 1; i < shortlist.size(); i++) {
            double candidateScore = shortlist.get(i).score();
            if (candidateScore > worstScore) {
                worstScore = candidateScore;
                worstIndex = i;
            }
        }

        if (score < worstScore) {
            shortlist.set(worstIndex, new PolenScoredSpot(pos.immutable(), score));
        }
    }

    public static BlockPos resolveBestReachable(
            PolenEntity polen,
            List<PolenScoredSpot> shortlist,
            int blinkDistance,
            boolean requireSafeBlink
    ) {
        if (shortlist.isEmpty()) {
            return null;
        }

        shortlist.sort(Comparator.comparingDouble(PolenScoredSpot::score));

        BlockPos bestBlinkable = null;
        for (PolenScoredSpot candidate : shortlist) {
            if (isPathReachable(polen, candidate.pos())) {
                return candidate.pos();
            }

            if (bestBlinkable == null
                    && PolenMagicController.canBlinkToward(polen, candidate.pos(), blinkDistance, requireSafeBlink)) {
                bestBlinkable = candidate.pos();
            }
        }

        return bestBlinkable;
    }

    private static boolean isPathReachable(PolenEntity polen, BlockPos target) {
        Path path = polen.getNavigation().createPath(target, 0);
        return path != null && path.canReach();
    }
}
