package com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home;

import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.routine.PolenRoutinePlanner;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.core.PolenSleepController;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenStoryFlag;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenStoryFlagsManager;
import com.hivesandcolonies.hccharacters.character.polen.progression.world.PolenWorldStateManager;
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

        polen.syncHomeState();

        if (polen.level() instanceof ServerLevel serverLevel) {
            PolenStoryFlagsManager.setFlag(serverLevel, PolenStoryFlag.POLEN_FOUND_RESIDENCE);
            PolenStoryFlagsManager.setFlag(serverLevel, PolenStoryFlag.PLAYER_HAS_SHELTER);
            PolenWorldStateManager.rememberPolenHomeBed(serverLevel, target.anchorPos());
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

    public static BlockPos getHomeCenterPos(PolenEntity polen) {
        if (polen == null) {
            return null;
        }

        BlockPos homeBedPos = PolenSleepController.findHomeBed(polen);
        BlockPos homeBedAccessPos = PolenSleepController.findBestBedAccessPos(polen, homeBedPos);
        if (homeBedAccessPos != null) {
            return homeBedAccessPos.immutable();
        }

        BlockPos residenceUsePos = getValidResidenceUsePos(polen);
        if (residenceUsePos != null) {
            return residenceUsePos;
        }

        BlockPos restingPos = polen.getAiState().getRestingPos();
        return restingPos == null ? null : restingPos.immutable();
    }

    public static boolean hasHomeCenter(PolenEntity polen) {
        return getHomeCenterPos(polen) != null;
    }

    public static boolean isNearHomeCenter(PolenEntity polen, double radius) {
        BlockPos homeCenter = getHomeCenterPos(polen);
        return homeCenter != null && homeCenter.closerToCenterThan(polen.position(), radius);
    }

    public static boolean isFarFromHome(PolenEntity polen, double radius) {
        BlockPos homeCenter = getHomeCenterPos(polen);
        return homeCenter != null && !homeCenter.closerToCenterThan(polen.position(), radius);
    }

    public static boolean isPositionWithinHomeRadius(PolenEntity polen, BlockPos pos, double radius) {
        BlockPos homeCenter = getHomeCenterPos(polen);
        return homeCenter == null || pos != null && homeCenter.closerToCenterThan(
                net.minecraft.world.phys.Vec3.atCenterOf(pos),
                radius
        );
    }
}
