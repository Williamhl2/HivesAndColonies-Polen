package com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation;

import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.safety.PolenSafetyEvaluator;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public final class PolenMovementHelper {
    private static final double STEP_UP_ASSIST_HORIZONTAL_RANGE_SQR = 2.25D * 2.25D;
    private static final double STEP_UP_ASSIST_PUSH = 0.08D;
    private static final double RANGED_WEAVE_PUSH = 0.035D;
    private static final double LOCAL_ROUTE_OVERRIDE_MARGIN = 1.2D;
    private static final int[] TARGET_Y_OFFSETS = {0, 1, -1, 2, -2, 3};
    private static final int[] APPROACH_Y_OFFSETS = {0, 1, -1, 2};
    private static final int[][] TARGET_XZ_OFFSETS = {
            {0, 0},
            {1, 0}, {-1, 0}, {0, 1}, {0, -1},
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1},
            {2, 0}, {-2, 0}, {0, 2}, {0, -2},
            {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
            {1, 2}, {-1, 2}, {1, -2}, {-1, -2}
    };
    private static final int[] APPROACH_STEP_DISTANCES = {2, 3, 4, 5, 6};

    private PolenMovementHelper() {
    }

    public static void configureNavigation(PathNavigation navigation) {
        if (navigation == null) {
            return;
        }

        if (navigation instanceof GroundPathNavigation groundNavigation) {
            groundNavigation.setCanOpenDoors(true);
            groundNavigation.setCanPassDoors(true);
            groundNavigation.setCanFloat(true);
        }
    }

    public static BlockPos resolveReachableTarget(PolenEntity polen, BlockPos desiredPos, boolean requireSafeSpot) {
        if (polen == null || desiredPos == null) {
            return null;
        }

        PathCandidate bestCandidate = findBestReachableCandidateAroundTarget(polen, desiredPos, requireSafeSpot);
        return bestCandidate == null ? null : bestCandidate.pos();
    }

    public static BlockPos resolveNavigationAnchor(PolenEntity polen, BlockPos desiredPos, boolean requireSafeSpot) {
        if (polen == null || desiredPos == null) {
            return null;
        }

        PathCandidate bestCandidate = findBestReachableCandidateAroundTarget(polen, desiredPos, requireSafeSpot);
        PathCandidate immediateClimbAnchor = findImmediateClimbAnchorCandidate(polen, desiredPos, requireSafeSpot);
        PathCandidate progressAnchor = findProgressAnchorCandidate(polen, desiredPos, requireSafeSpot);

        bestCandidate = pickPreferredAnchor(bestCandidate, immediateClimbAnchor);
        bestCandidate = pickPreferredAnchor(bestCandidate, progressAnchor);
        return bestCandidate == null ? null : bestCandidate.pos();
    }

    public static BlockPos moveToReachableTarget(
            PolenEntity polen,
            BlockPos desiredPos,
            double speed,
            boolean requireSafeSpot
    ) {
        return startAnchoredMove(polen, desiredPos, null, speed, requireSafeSpot);
    }

    public static boolean isNavigationAnchorStillUseful(
            PolenEntity polen,
            BlockPos desiredPos,
            BlockPos anchorPos,
            boolean requireSafeSpot
    ) {
        if (polen == null || desiredPos == null || anchorPos == null) {
            return false;
        }

        if (!isNavigableCandidate(polen, anchorPos, requireSafeSpot)) {
            return false;
        }

        double distanceToAnchorSqr = polen.distanceToSqr(Vec3.atCenterOf(anchorPos));
        if (distanceToAnchorSqr <= 2.25D) {
            return false;
        }

        Path path = polen.getNavigation().createPath(anchorPos, 0);
        if (path == null || !path.canReach()) {
            return false;
        }

        return !polen.getNavigation().isDone() || anchorPos.distSqr(desiredPos) <= 2.25D;
    }

    public static BlockPos ensureNavigationAnchor(
            PolenEntity polen,
            BlockPos desiredPos,
            BlockPos currentAnchor,
            boolean requireSafeSpot
    ) {
        if (isNavigationAnchorStillUseful(polen, desiredPos, currentAnchor, requireSafeSpot)) {
            return currentAnchor.immutable();
        }

        return resolveNavigationAnchor(polen, desiredPos, requireSafeSpot);
    }

    public static void steerLookTowardMovement(
            PolenEntity polen,
            BlockPos desiredPos,
            BlockPos anchorPos,
            boolean requireSafeSpot
    ) {
        if (polen == null || desiredPos == null) {
            return;
        }

        BlockPos lookTarget = anchorPos != null ? anchorPos : desiredPos;

        polen.getLookControl().setLookAt(
                lookTarget.getX() + 0.5D,
                lookTarget.getY(),
                lookTarget.getZ() + 0.5D,
                20.0F,
                20.0F
        );
    }

    public static BlockPos startAnchoredMove(
            PolenEntity polen,
            BlockPos desiredPos,
            BlockPos currentAnchor,
            double speed,
            boolean requireSafeSpot
    ) {
        BlockPos navigationAnchor = ensureNavigationAnchor(polen, desiredPos, currentAnchor, requireSafeSpot);
        if (navigationAnchor == null) {
            return null;
        }

        boolean started = polen.getNavigation().moveTo(
                navigationAnchor.getX() + 0.5D,
                navigationAnchor.getY(),
                navigationAnchor.getZ() + 0.5D,
                speed
        );
        return started ? navigationAnchor : null;
    }

    public static boolean tryAssistStepUp(PolenEntity polen) {
        if (polen == null
                || !polen.onGround()
                || !polen.horizontalCollision
                || polen.getNavigation().isDone()) {
            return false;
        }

        Path path = polen.getNavigation().getPath();
        if (path == null || path.isDone()) {
            return false;
        }

        BlockPos nextNodePos = path.getNextNodePos();
        if (nextNodePos == null
                || nextNodePos.getY() <= polen.blockPosition().getY()
                || nextNodePos.distSqr(polen.blockPosition()) > STEP_UP_ASSIST_HORIZONTAL_RANGE_SQR) {
            return false;
        }

        Vec3 pushDirection = Vec3.atCenterOf(nextNodePos).subtract(polen.position());
        Vec3 horizontalPush = new Vec3(pushDirection.x, 0.0D, pushDirection.z);
        if (horizontalPush.lengthSqr() > 0.0001D) {
            polen.performNavigationStepAssist(horizontalPush.normalize().scale(STEP_UP_ASSIST_PUSH));
            return true;
        }
        polen.performNavigationStepAssist(Vec3.ZERO);
        return true;
    }

    public static boolean applyRangedThreatWeave(PolenEntity polen, BlockPos desiredPos) {
        if (polen == null
                || desiredPos == null
                || !polen.onGround()
                || polen.isInWaterOrBubble()
                || polen.horizontalCollision
                || polen.getNavigation().isDone()
                || polen.distanceToSqr(Vec3.atCenterOf(desiredPos)) < 9.0D) {
            return false;
        }

        Vec3 toTarget = Vec3.atCenterOf(desiredPos).subtract(polen.position());
        Vec3 horizontal = new Vec3(toTarget.x, 0.0D, toTarget.z);
        if (horizontal.lengthSqr() < 0.0001D) {
            return false;
        }

        Vec3 lateral = new Vec3(-horizontal.z, 0.0D, horizontal.x).normalize();
        double direction = ((polen.tickCount / 8) & 1) == 0 ? 1.0D : -1.0D;
        polen.setDeltaMovement(polen.getDeltaMovement().add(lateral.scale(RANGED_WEAVE_PUSH * direction)));
        return true;
    }

    private static boolean isNavigableCandidate(PolenEntity polen, BlockPos candidate, boolean requireSafeSpot) {
        return requireSafeSpot
                ? PolenSafetyEvaluator.isSafeStandingSpot(polen, candidate)
                : PolenSafetyEvaluator.isStandableSpot(polen, candidate);
    }

    private static PathCandidate findBestReachableCandidateAroundTarget(
            PolenEntity polen,
            BlockPos desiredPos,
            boolean requireSafeSpot
    ) {
        PathCandidate bestCandidate = null;

        for (int[] xzOffset : TARGET_XZ_OFFSETS) {
            for (int yOffset : TARGET_Y_OFFSETS) {
                bestCandidate = pickLowerScoreCandidate(
                        bestCandidate,
                        evaluatePathCandidate(
                                polen,
                                desiredPos,
                                desiredPos.offset(xzOffset[0], yOffset, xzOffset[1]),
                                requireSafeSpot,
                                false
                        )
                );
            }
        }

        return bestCandidate;
    }

    private static PathCandidate findProgressAnchorCandidate(PolenEntity polen, BlockPos desiredPos, boolean requireSafeSpot) {
        BlockPos origin = polen.blockPosition();
        Vec3 originCenter = Vec3.atCenterOf(origin);
        Vec3 desiredCenter = Vec3.atCenterOf(desiredPos);
        Vec3 delta = desiredCenter.subtract(originCenter);
        double distance = delta.length();
        if (distance < 1.5D) {
            return null;
        }

        Vec3 direction = delta.normalize();
        PathCandidate bestCandidate = null;

        for (int step : APPROACH_STEP_DISTANCES) {
            if (step >= distance) {
                continue;
            }

            BlockPos base = BlockPos.containing(originCenter.add(direction.scale(step)));
            for (int[] xzOffset : TARGET_XZ_OFFSETS) {
                for (int yOffset : APPROACH_Y_OFFSETS) {
                    bestCandidate = pickLowerScoreCandidate(
                            bestCandidate,
                            evaluatePathCandidate(
                                    polen,
                                    desiredPos,
                                    base.offset(xzOffset[0], yOffset, xzOffset[1]),
                                    requireSafeSpot,
                                    true
                            )
                    );
                }
            }
        }

        return bestCandidate;
    }

    private static PathCandidate findImmediateClimbAnchorCandidate(PolenEntity polen, BlockPos desiredPos, boolean requireSafeSpot) {
        BlockPos origin = polen.blockPosition();
        PathCandidate bestCandidate = null;

        for (int[] xzOffset : TARGET_XZ_OFFSETS) {
            if (Math.abs(xzOffset[0]) > 1 || Math.abs(xzOffset[1]) > 1) {
                continue;
            }

            for (int yOffset : APPROACH_Y_OFFSETS) {
                bestCandidate = pickLowerScoreCandidate(
                        bestCandidate,
                        evaluatePathCandidate(
                                polen,
                                desiredPos,
                                origin.offset(xzOffset[0], yOffset, xzOffset[1]),
                                requireSafeSpot,
                                true
                        )
                );
            }
        }

        return bestCandidate;
    }

    private static PathCandidate evaluatePathCandidate(
            PolenEntity polen,
            BlockPos desiredPos,
            BlockPos candidate,
            boolean requireSafeSpot,
            boolean preferLocalProgress
    ) {
        if (polen == null || desiredPos == null || candidate == null || candidate.equals(polen.blockPosition())) {
            return null;
        }

        if (!isNavigableCandidate(polen, candidate, requireSafeSpot)) {
            return null;
        }

        double currentDistanceSqr = polen.blockPosition().distSqr(desiredPos);
        if (preferLocalProgress && candidate.distSqr(desiredPos) >= currentDistanceSqr - 0.25D) {
            return null;
        }

        Path path = polen.getNavigation().createPath(candidate, 0);
        if (path == null || !path.canReach()) {
            return null;
        }

        return new PathCandidate(candidate.immutable(), scorePathCandidate(polen, desiredPos, candidate, path, preferLocalProgress));
    }

    private static double scorePathCandidate(
            PolenEntity polen,
            BlockPos desiredPos,
            BlockPos candidate,
            Path path,
            boolean preferLocalProgress
    ) {
        BlockPos origin = polen.blockPosition();
        double desiredDistanceSqr = candidate.distSqr(desiredPos);
        double originDistanceSqr = candidate.distSqr(origin);
        double score = desiredDistanceSqr * 1.35D + originDistanceSqr * (preferLocalProgress ? 0.18D : 0.10D);

        if (candidate.getY() == desiredPos.getY()) {
            score -= 1.0D;
        } else if (candidate.getY() > desiredPos.getY()) {
            score += (candidate.getY() - desiredPos.getY()) * 0.65D;
        }

        BlockPos nextNodePos = path.getNextNodePos();
        if (nextNodePos != null) {
            double currentDesiredDistance = Math.sqrt(origin.distSqr(desiredPos));
            double nextDesiredDistance = Math.sqrt(nextNodePos.distSqr(desiredPos));
            double progress = currentDesiredDistance - nextDesiredDistance;
            score -= progress * (preferLocalProgress ? 4.5D : 2.5D);
            if (progress <= 0.10D) {
                score += (0.10D - progress) * 10.0D;
            }

            double alignment = horizontalAlignment(origin, desiredPos, nextNodePos);
            score -= alignment * (preferLocalProgress ? 4.0D : 1.8D);
            if (alignment < 0.15D) {
                score += (0.15D - alignment) * 6.0D;
            }

            int desiredClimb = desiredPos.getY() - origin.getY();
            int nextClimb = nextNodePos.getY() - origin.getY();
            if (desiredClimb > 0) {
                if (nextClimb > 0) {
                    score -= Math.min(2, nextClimb) * (preferLocalProgress ? 4.0D : 2.0D);
                } else {
                    score += preferLocalProgress ? 6.0D : 2.5D;
                }
            } else if (nextClimb > 1) {
                score += nextClimb * 2.5D;
            }

            if (preferLocalProgress && origin.distSqr(nextNodePos) <= 3.0D && nextClimb > 0) {
                score -= 3.5D;
            }
        }

        return score;
    }

    private static double horizontalAlignment(BlockPos origin, BlockPos desiredPos, BlockPos nextNodePos) {
        Vec3 desiredVector = new Vec3(
                desiredPos.getX() - origin.getX(),
                0.0D,
                desiredPos.getZ() - origin.getZ()
        );
        Vec3 nextVector = new Vec3(
                nextNodePos.getX() - origin.getX(),
                0.0D,
                nextNodePos.getZ() - origin.getZ()
        );
        if (desiredVector.lengthSqr() < 0.0001D || nextVector.lengthSqr() < 0.0001D) {
            return 0.0D;
        }

        return desiredVector.normalize().dot(nextVector.normalize());
    }

    private static PathCandidate pickLowerScoreCandidate(PathCandidate currentBest, PathCandidate candidate) {
        if (candidate == null) {
            return currentBest;
        }
        if (currentBest == null || candidate.score() < currentBest.score()) {
            return candidate;
        }

        return currentBest;
    }

    private static PathCandidate pickPreferredAnchor(PathCandidate currentBest, PathCandidate candidate) {
        if (candidate == null) {
            return currentBest;
        }
        if (currentBest == null) {
            return candidate;
        }

        return candidate.score() + LOCAL_ROUTE_OVERRIDE_MARGIN < currentBest.score() ? candidate : currentBest;
    }

    private record PathCandidate(BlockPos pos, double score) {
    }
}
