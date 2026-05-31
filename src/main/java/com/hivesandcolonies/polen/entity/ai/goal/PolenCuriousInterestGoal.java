package com.hivesandcolonies.polen.entity.ai.goal;

import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.PolenAmbientDialogueController;
import com.hivesandcolonies.polen.entity.ai.safety.PolenSafetyNavigator;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class PolenCuriousInterestGoal extends Goal {
    private static final int SEARCH_RADIUS = 6;
    private static final int SEARCH_HEIGHT = 2;
    private static final double MOVE_SPEED = 0.9D;
    private static final double STOP_DISTANCE_SQR = 2.25D;

    private final PolenEntity polen;

    private BlockPos targetPos;
    private int observeTicks;

    public PolenCuriousInterestGoal(PolenEntity polen) {
        this.polen = polen;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.polen.isDoingQuietActivity()
                || PolenSafetyNavigator.isInUnsafeArea(this.polen)
                || this.polen.hasNearbyPlayer(2.5D)
                || this.polen.getRandom().nextInt(100) != 0) {
            return false;
        }

        this.targetPos = findInterestingBlock();
        this.observeTicks = 40 + this.polen.getRandom().nextInt(60);
        return this.targetPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetPos != null
                && this.observeTicks > 0
                && !this.polen.hasNearbyPlayer(2.0D)
                && isInteresting(this.polen.level().getBlockState(this.targetPos));
    }

    @Override
    public void start() {
        this.polen.rememberInterestingSpot(this.targetPos);
        PolenAmbientDialogueController.tryPlay(
                this.polen,
                com.hivesandcolonies.polen.dialogue.PolenDialogueManager.AMBIENT_CURIOSITY
        );
        moveToTarget();
    }

    @Override
    public void tick() {
        if (this.targetPos == null) {
            return;
        }

        Vec3 targetCenter = Vec3.atCenterOf(this.targetPos);
        double distanceSqr = this.polen.distanceToSqr(targetCenter);

        if (distanceSqr <= STOP_DISTANCE_SQR) {
            this.polen.getNavigation().stop();
            this.polen.getLookControl().setLookAt(targetCenter.x, targetCenter.y, targetCenter.z);
            this.observeTicks--;
            return;
        }

        if (this.polen.getNavigation().isDone()) {
            moveToTarget();
        }
    }

    @Override
    public void stop() {
        this.polen.getNavigation().stop();
        this.targetPos = null;
        this.observeTicks = 0;
    }

    private void moveToTarget() {
        if (this.targetPos == null) {
            return;
        }

        this.polen.getNavigation().moveTo(
                this.targetPos.getX() + 0.5D,
                this.targetPos.getY(),
                this.targetPos.getZ() + 0.5D,
                MOVE_SPEED
        );
    }

    private BlockPos findInterestingBlock() {
        BlockPos origin = this.polen.blockPosition();
        BlockPos bestPos = null;
        double bestDistanceSqr = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-SEARCH_RADIUS, -SEARCH_HEIGHT, -SEARCH_RADIUS),
                origin.offset(SEARCH_RADIUS, SEARCH_HEIGHT, SEARCH_RADIUS)
        )) {
            BlockState state = this.polen.level().getBlockState(pos);
            if (!isInteresting(state)) {
                continue;
            }

            double distanceSqr = pos.distSqr(origin);
            if (distanceSqr < bestDistanceSqr) {
                bestDistanceSqr = distanceSqr;
                bestPos = pos.immutable();
            }
        }

        return bestPos;
    }

    private static boolean isInteresting(BlockState state) {
        return state.is(BlockTags.FLOWERS)
                || state.is(Blocks.BEE_NEST)
                || state.is(Blocks.BEEHIVE);
    }
}
