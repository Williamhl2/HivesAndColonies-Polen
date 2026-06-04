package com.hivesandcolonies.characters.character.polen.entity.ai.world.home;

import com.hivesandcolonies.characters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.routine.PolenRoutinePlanner;
import com.hivesandcolonies.characters.character.polen.progression.PolenStoryFlag;
import com.hivesandcolonies.characters.character.polen.progression.PolenStoryFlagsManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public final class PolenHomeManager {
    private PolenHomeManager() {
    }

    public static void rememberResidence(PolenEntity polen, PolenResidenceTarget target) {
        if (polen == null || target == null) {
            return;
        }

        polen.getAiState().setResidenceState(
                target.anchorPos().immutable(),
                target.usePos().immutable(),
                target.context(),
                target.stage()
        );

        BlockPos normalizedRestingPos = PolenRoutinePlanner.normalizeRestingAnchor(polen, target.usePos());
        if (normalizedRestingPos != null) {
            polen.getAiState().setRestingPos(normalizedRestingPos);
        }

        if (polen.level() instanceof ServerLevel serverLevel) {
            PolenStoryFlagsManager.setFlag(serverLevel, PolenStoryFlag.POLEN_FOUND_RESIDENCE);
        }
    }

    public static PolenResidenceTarget getRememberedResidence(PolenEntity polen) {
        if (polen == null) {
            return null;
        }

        PolenResidenceStage stage = polen.getAiState().getResidenceStage();
        BlockPos anchorPos = polen.getAiState().getResidenceAnchorPos();
        BlockPos usePos = polen.getAiState().getResidenceUsePos();
        if (stage == PolenResidenceStage.NONE || anchorPos == null || usePos == null) {
            return null;
        }

        PolenResidenceTarget target = new PolenResidenceTarget(
                anchorPos.immutable(),
                usePos.immutable(),
                polen.getAiState().getResidenceContext(),
                stage
        );

        if (!PolenResidenceValidator.isStillValid(polen, target)) {
            return null;
        }

        return target;
    }

    public static BlockPos getValidResidenceUsePos(PolenEntity polen) {
        PolenResidenceTarget target = getRememberedResidence(polen);
        return target == null ? null : target.usePos();
    }

    public static boolean isNearResidence(PolenEntity polen) {
        BlockPos usePos = getValidResidenceUsePos(polen);
        return usePos != null && usePos.closerToCenterThan(polen.position(), 3.0D);
    }
}
