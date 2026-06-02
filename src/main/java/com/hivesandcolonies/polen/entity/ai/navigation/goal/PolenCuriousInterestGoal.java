package com.hivesandcolonies.polen.entity.ai.navigation.goal;

import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.PolenAmbientDialogueController;
import com.hivesandcolonies.polen.entity.ai.expression.gesture.PolenGesture;
import com.hivesandcolonies.polen.entity.ai.expression.gesture.PolenGestureController;
import com.hivesandcolonies.polen.entity.ai.brain.interest.PolenInterestLocator;
import com.hivesandcolonies.polen.entity.ai.brain.interest.PolenInterestTarget;
import com.hivesandcolonies.polen.entity.ai.brain.interest.PolenInterestType;
import com.hivesandcolonies.polen.entity.ai.brain.task.PolenTaskController;
import com.hivesandcolonies.polen.entity.ai.brain.task.PolenTaskType;
import com.hivesandcolonies.polen.entity.ai.navigation.safety.PolenSafetyNavigator;
import com.hivesandcolonies.polen.story.PolenMemoryManager;
import com.hivesandcolonies.polen.story.PolenMemoryType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class PolenCuriousInterestGoal extends Goal {
    private static final int SEARCH_RADIUS = 6;
    private static final int SEARCH_HEIGHT = 2;
    private static final double MOVE_SPEED = 0.9D;
    private static final double STOP_DISTANCE_SQR = 2.25D;

    private final PolenEntity polen;

    private PolenInterestTarget target;
    private int observeTicks;
    private int stuckTicks;
    private int blinkCooldownTicks;
    private double lastDistanceSqr;

    public PolenCuriousInterestGoal(PolenEntity polen) {
        this.polen = polen;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.polen.isDoingQuietActivity()
                || PolenSafetyNavigator.isInUnsafeArea(this.polen)
                || this.polen.hasNearbyPlayer(2.5D)
                || this.polen.getCurrentTask() != PolenTaskType.INVESTIGATE_INTEREST) {
            return false;
        }

        this.target = PolenInterestLocator.findPreferredInterest(this.polen, true);
        this.observeTicks = 40 + this.polen.getRandom().nextInt(60);
        this.stuckTicks = 0;
        this.blinkCooldownTicks = 0;
        this.lastDistanceSqr = Double.MAX_VALUE;
        return this.target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.target != null
                && this.observeTicks > 0
                && !this.polen.hasNearbyPlayer(2.0D)
                && this.polen.getCurrentTask() == PolenTaskType.INVESTIGATE_INTEREST;
    }

    @Override
    public void start() {
        if (this.target == null) {
            return;
        }

        PolenGestureController.triggerGesture(this.polen, PolenGesture.CURIOUS);
        PolenTaskController.markActive(this.polen, PolenTaskType.INVESTIGATE_INTEREST, "investigating_interest");
        if (this.target.type() == PolenInterestType.SOURCE) {
            this.polen.rememberInterestingSpot(this.target.pos());
            PolenMemoryManager.unlockMemory(
                    (net.minecraft.server.level.ServerLevel) this.polen.level(),
                    PolenMemoryType.FIRST_SOURCE,
                    this.target.pos().getX() + 0.5D,
                    this.target.pos().getY() + 0.5D,
                    this.target.pos().getZ() + 0.5D
            );
            PolenAmbientDialogueController.tryPlay(
                    this.polen,
                    com.hivesandcolonies.polen.dialogue.PolenDialogueManager.AMBIENT_MAGIC
            );
        } else {
            this.polen.rememberInterestingSpot(this.target.pos());
            PolenAmbientDialogueController.tryPlay(
                    this.polen,
                    com.hivesandcolonies.polen.dialogue.PolenDialogueManager.AMBIENT_CURIOSITY
            );
        }

        this.lastDistanceSqr = this.polen.distanceToSqr(Vec3.atCenterOf(this.target.observePos()));
        moveToTarget();
    }

    @Override
    public void tick() {
        if (this.target == null) {
            return;
        }

        Vec3 targetCenter = Vec3.atCenterOf(this.target.observePos());
        Vec3 focusCenter = Vec3.atCenterOf(this.target.pos());
        double distanceSqr = this.polen.distanceToSqr(targetCenter);

        if (distanceSqr <= STOP_DISTANCE_SQR) {
            this.polen.getNavigation().stop();
            this.polen.getLookControl().setLookAt(focusCenter.x, focusCenter.y, focusCenter.z);
            this.observeTicks--;
            return;
        }

        if (this.blinkCooldownTicks > 0) {
            this.blinkCooldownTicks--;
        }

        updateStuckCounter(distanceSqr);
        if (this.blinkCooldownTicks == 0 && (this.stuckTicks >= 30 || this.polen.getNavigation().isDone())) {
            if (PolenSafetyNavigator.tryBlinkTowardStandableSpot(this.polen, this.target.observePos(), 6)) {
                this.blinkCooldownTicks = 40;
                this.stuckTicks = 0;
                this.lastDistanceSqr = this.polen.distanceToSqr(targetCenter);
            }
            moveToTarget();
        }
    }

    @Override
    public void stop() {
        if (this.target != null) {
            if (this.observeTicks <= 0) {
                PolenTaskController.markCompleted(this.polen, PolenTaskType.INVESTIGATE_INTEREST, "interest_observed");
            } else {
                PolenTaskController.markFailed(
                        this.polen,
                        PolenTaskType.INVESTIGATE_INTEREST,
                        "interest_investigation_interrupted",
                        60L
                );
            }
        }
        this.polen.getNavigation().stop();
        this.target = null;
        this.observeTicks = 0;
        this.stuckTicks = 0;
        this.blinkCooldownTicks = 0;
        this.lastDistanceSqr = Double.MAX_VALUE;
    }

    private void moveToTarget() {
        if (this.target == null) {
            return;
        }

        boolean pathStarted = this.polen.getNavigation().moveTo(
                this.target.observePos().getX() + 0.5D,
                this.target.observePos().getY(),
                this.target.observePos().getZ() + 0.5D,
                MOVE_SPEED
        );

        if (!pathStarted && this.blinkCooldownTicks == 0) {
            if (PolenSafetyNavigator.tryBlinkTowardStandableSpot(this.polen, this.target.observePos(), 6)) {
                this.blinkCooldownTicks = 40;
                this.stuckTicks = 0;
            }
        }
    }

    private void updateStuckCounter(double distanceSqr) {
        if (distanceSqr < this.lastDistanceSqr - 0.04D) {
            this.stuckTicks = 0;
        } else {
            this.stuckTicks++;
        }

        this.lastDistanceSqr = distanceSqr;
    }
}
