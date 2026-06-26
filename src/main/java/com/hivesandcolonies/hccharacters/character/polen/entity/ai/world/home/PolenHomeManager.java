package com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home;

import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.routine.PolenRoutinePlanner;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenStoryFlag;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenStoryFlagsManager;
import com.hivesandcolonies.hccharacters.character.polen.progression.world.PolenWorldStateManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public final class PolenHomeManager {
    private static final int AUTO_BEE_BED_HORIZONTAL_RADIUS = 8;
    private static final int AUTO_BEE_BED_VERTICAL_RADIUS = 3;
    private static final int HOME_BED_SCAN_RADIUS = 14;

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

    public static boolean hasRememberedResidence(PolenEntity polen) {
        if (polen == null) {
            return false;
        }

        return polen.getAiState().getResidenceStage() != PolenResidenceStage.NONE
                && polen.getAiState().getResidenceAnchorPos() != null
                && polen.getAiState().getResidenceUsePos() != null;
    }

    public static boolean hasValidRememberedResidence(PolenEntity polen) {
        return getRememberedResidence(polen) != null;
    }

    public static boolean clearInvalidResidence(PolenEntity polen) {
        if (!hasRememberedResidence(polen) || hasValidRememberedResidence(polen)) {
            return false;
        }

        polen.getAiState().setResidenceState(null, null, "", PolenResidenceStage.NONE);
        polen.syncHomeState();
        return true;
    }

    public static boolean tryAutoAssignNearbyBeeBed(PolenEntity polen) {
        if (polen == null || hasValidRememberedResidence(polen)) {
            return false;
        }

        PolenResidenceTarget target = findNearbyBeeBedResidence(
                polen,
                polen.blockPosition(),
                AUTO_BEE_BED_HORIZONTAL_RADIUS,
                AUTO_BEE_BED_VERTICAL_RADIUS
        );
        if (target == null) {
            return false;
        }

        rememberResidence(polen, target);
        return true;
    }

    public static PolenResidenceTarget findNearbyBeeBedResidence(
            PolenEntity polen,
            BlockPos origin,
            int horizontalRadius,
            int verticalRadius
    ) {
        PolenBedTarget target = PolenBedLocator.findNearestBedTarget(
                polen,
                origin,
                horizontalRadius,
                verticalRadius,
                true
        );
        if (target == null || target.bedPos() == null || target.accessPos() == null) {
            return null;
        }

        return new PolenResidenceTarget(
                target.anchorPos() == null ? target.bedPos() : target.anchorPos(),
                target.accessPos(),
                "bee_bed",
                PolenResidenceStage.OWN_SPACE
        );
    }

    public static PolenHomeSnapshot getHomeSnapshot(PolenEntity polen) {
        if (polen == null) {
            return new PolenHomeSnapshot(null, null, null);
        }

        PolenResidenceTarget residence = getRememberedResidence(polen);
        PolenBedTarget homeBed = findHomeBedTarget(polen, residence);
        BlockPos restingPos = polen.getAiState().getRestingPos();
        return new PolenHomeSnapshot(residence, homeBed, restingPos);
    }

    public static BlockPos getValidResidenceUsePos(PolenEntity polen) {
        return getHomeSnapshot(polen).residenceUsePos();
    }

    public static boolean isNearResidence(PolenEntity polen) {
        BlockPos usePos = getHomeSnapshot(polen).residenceUsePos();
        return usePos != null && usePos.closerToCenterThan(polen.position(), 3.0D);
    }

    public static BlockPos getHomeCenterPos(PolenEntity polen) {
        return getHomeSnapshot(polen).homeCenterPos();
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

    private static PolenBedTarget findHomeBedTarget(PolenEntity polen, PolenResidenceTarget residence) {
        if (polen == null) {
            return null;
        }

        BlockPos residenceUsePos = residence == null ? null : residence.usePos();
        PolenBedTarget best = PolenBedLocator.findNearestBedTarget(polen, residenceUsePos, HOME_BED_SCAN_RADIUS, 3, false);
        if (best != null) {
            return best;
        }

        BlockPos residenceAnchorPos = polen.getAiState().getResidenceAnchorPos();
        best = PolenBedLocator.findNearestBedTarget(polen, residenceAnchorPos, HOME_BED_SCAN_RADIUS, 3, false);
        if (best != null) {
            return best;
        }

        return PolenBedLocator.findNearestBedTarget(polen, polen.getAiState().getRestingPos(), HOME_BED_SCAN_RADIUS, 3, false);
    }
}
