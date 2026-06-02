package com.hivesandcolonies.polen.entity.ai.navigation.search;

import com.hivesandcolonies.polen.entity.PolenEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;
import java.util.function.ToDoubleFunction;

public final class PolenSearchPlanner {
    private PolenSearchPlanner() {
    }

    public static BlockPos findBestReachable(
            PolenEntity polen,
            BlockPos origin,
            PolenSearchProfile profile,
            ToDoubleFunction<BlockPos> scorer
    ) {
        if (polen == null || origin == null || profile == null || scorer == null) {
            return null;
        }

        return switch (profile.domain()) {
            case LOCAL_RINGS -> searchLocalRings(polen, origin, profile, scorer);
            case SURFACE_COLUMNS -> searchSurfaceColumns(polen, origin, profile, scorer);
        };
    }

    private static BlockPos searchLocalRings(
            PolenEntity polen,
            BlockPos origin,
            PolenSearchProfile profile,
            ToDoubleFunction<BlockPos> scorer
    ) {
        for (int radius : profile.searchRadii()) {
            List<PolenScoredSpot> shortlist = PolenSpotSelectionHelper.createShortlist();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                        continue;
                    }

                    for (int yOffset : profile.verticalOffsets()) {
                        if (Math.abs(yOffset) > profile.verticalLimit()) {
                            continue;
                        }

                        BlockPos candidate = origin.offset(dx, yOffset, dz);
                        PolenSpotSelectionHelper.offerCandidate(shortlist, candidate, scorer.applyAsDouble(candidate));
                    }
                }
            }

            BlockPos resolved = PolenSpotSelectionHelper.resolveBestReachable(
                    polen,
                    shortlist,
                    profile.blinkDistance(),
                    profile.requireSafeBlink()
            );
            if (resolved != null) {
                return resolved;
            }
        }

        return null;
    }

    private static BlockPos searchSurfaceColumns(
            PolenEntity polen,
            BlockPos origin,
            PolenSearchProfile profile,
            ToDoubleFunction<BlockPos> scorer
    ) {
        Level level = polen.level();
        for (int radius : profile.searchRadii()) {
            List<PolenScoredSpot> shortlist = PolenSpotSelectionHelper.createShortlist();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    int x = origin.getX() + dx;
                    int z = origin.getZ() + dz;
                    int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

                    for (int yOffset : profile.verticalOffsets()) {
                        if (Math.abs(yOffset) > profile.verticalLimit()) {
                            continue;
                        }

                        BlockPos candidate = new BlockPos(x, surfaceY + yOffset, z);
                        PolenSpotSelectionHelper.offerCandidate(shortlist, candidate, scorer.applyAsDouble(candidate));
                    }
                }
            }

            BlockPos resolved = PolenSpotSelectionHelper.resolveBestReachable(
                    polen,
                    shortlist,
                    profile.blinkDistance(),
                    profile.requireSafeBlink()
            );
            if (resolved != null) {
                return resolved;
            }
        }

        return null;
    }
}
