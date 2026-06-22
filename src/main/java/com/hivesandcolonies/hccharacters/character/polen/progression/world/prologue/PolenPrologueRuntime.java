package com.hivesandcolonies.hccharacters.character.polen.progression.world.prologue;

import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.routine.PolenRoutinePlanner;
import com.hivesandcolonies.hccharacters.character.polen.progression.world.PolenWorldStoryData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

final class PolenPrologueRuntime {
    private PolenPrologueRuntime() {
    }

    static BlockPos resolveLocatorTarget(ServerLevel level, PolenWorldStoryData data) {
        if (level == null || data == null) {
            return null;
        }

        if (PolenPrologueSiteBuilder.isBeeBedStillPresent(level, data.getPrologueBeeBedPos())) {
            return data.getPrologueBeeBedPos();
        }
        if (data.getPrologueShelterPos() != null) {
            BlockPos nearbyBeeBed = PolenPrologueSiteBuilder.resolveBeeBedMarkerPos(level, data.getPrologueShelterPos());
            if (nearbyBeeBed != null) {
                return nearbyBeeBed;
            }
            return data.getPrologueShelterPos();
        }
        return data.getPrologueClearingCenter();
    }

    static void applyTemporaryResidence(PolenEntity polen, BlockPos shelterPos, BlockPos beeBedPos) {
        if (polen == null || shelterPos == null) {
            return;
        }

        BlockPos preferredRestingPos = beeBedPos != null ? beeBedPos : shelterPos;
        BlockPos normalizedRestingPos = PolenRoutinePlanner.normalizeRestingAnchor(polen, preferredRestingPos);
        if (normalizedRestingPos != null) {
            polen.getAiState().setRestingPos(normalizedRestingPos.immutable());
        }
    }
}
