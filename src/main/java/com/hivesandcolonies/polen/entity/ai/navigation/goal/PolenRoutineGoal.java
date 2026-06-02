package com.hivesandcolonies.polen.entity.ai.navigation.goal;

import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.ai.brain.intent.PolenIntent;
import com.hivesandcolonies.polen.entity.ai.brain.routine.PolenRoutinePlanner;
import com.hivesandcolonies.polen.entity.ai.brain.task.PolenTaskController;
import com.hivesandcolonies.polen.entity.ai.brain.task.PolenTaskType;
import com.hivesandcolonies.polen.entity.ai.navigation.safety.PolenSafetyNavigator;
import com.hivesandcolonies.polen.entity.ai.navigation.search.PolenSearchStatus;
import com.hivesandcolonies.polen.entity.ai.navigation.search.PolenSearchType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class PolenRoutineGoal extends Goal {
    private static final double MOVE_SPEED = 0.85D;
    private static final double STOP_DISTANCE_SQR = 2.25D;
    private static final int DEFAULT_STUCK_TICKS = 30;
    private static final int QUICK_ESCAPE_STUCK_TICKS = 18;
    private static final int DEFAULT_BLINK_DISTANCE = 6;
    private static final int QUICK_ESCAPE_BLINK_DISTANCE = 8;

    private final PolenEntity polen;

    private BlockPos targetPos;
    private int waitTicks;
    private int stuckTicks;
    private int blinkCooldownTicks;
    private double lastDistanceSqr;
    private boolean failedGoal;
    private boolean reachedTarget;

    public PolenRoutineGoal(PolenEntity polen) {
        this.polen = polen;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.polen.isDoingQuietActivity()
                || PolenSafetyNavigator.isInUnsafeArea(this.polen)
                || getTaskType() == null) {
            return false;
        }

        this.targetPos = PolenRoutinePlanner.getRoutineTarget(this.polen, this.polen.getCurrentIntent());
        this.waitTicks = 60 + this.polen.getRandom().nextInt(60);
        this.stuckTicks = 0;
        this.blinkCooldownTicks = 0;
        this.lastDistanceSqr = Double.MAX_VALUE;
        this.failedGoal = false;
        this.reachedTarget = false;

        boolean canUse = this.targetPos != null
                && !this.polen.blockPosition().closerToCenterThan(Vec3.atCenterOf(this.targetPos), 1.5D);
        this.polen.getAiState().setSearchState(
                getSearchType(),
                canUse ? PolenSearchStatus.SCANNING : PolenSearchStatus.FAILED,
                this.targetPos,
                this.targetPos,
                canUse ? "routine_target_planned" : "no_routine_target"
        );

        if (!canUse) {
            PolenTaskController.markFailed(this.polen, getTaskType(), "routine_target_unavailable", 80L);
        }

        return canUse;
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetPos != null
                && this.waitTicks > 0
                && PolenRoutinePlanner.isRememberedSpotStillValid(this.polen, this.targetPos);
    }

    @Override
    public void start() {
        PolenTaskController.markActive(this.polen, getTaskType(), "moving_to_routine_target");
        this.polen.getAiState().setSearchState(
                getSearchType(),
                PolenSearchStatus.PATHING,
                this.targetPos,
                this.targetPos,
                "starting_routine_path"
        );
        this.lastDistanceSqr = this.targetPos == null ? Double.MAX_VALUE : this.polen.distanceToSqr(Vec3.atCenterOf(this.targetPos));
        moveToTarget();
    }

    @Override
    public void tick() {
        if (this.targetPos == null) {
            return;
        }

        Vec3 targetCenter = Vec3.atCenterOf(this.targetPos);
        if (this.polen.distanceToSqr(targetCenter) <= STOP_DISTANCE_SQR) {
            this.polen.getNavigation().stop();
            this.polen.getLookControl().setLookAt(targetCenter.x, targetCenter.y, targetCenter.z);
            this.reachedTarget = true;
            this.polen.getAiState().setSearchState(
                    getSearchType(),
                    PolenSearchStatus.ARRIVED,
                    this.targetPos,
                    this.targetPos,
                    "routine_target_reached"
            );
            this.waitTicks--;
            return;
        }

        if (this.blinkCooldownTicks > 0) {
            this.blinkCooldownTicks--;
        }

        updateStuckCounter(this.polen.distanceToSqr(targetCenter));
        if (this.blinkCooldownTicks == 0
                && (this.stuckTicks >= getStuckTicksBeforeBlink() || this.polen.getNavigation().isDone())) {
            if (PolenSafetyNavigator.tryBlinkTowardStandableSpot(this.polen, this.targetPos, getBlinkDistance())) {
                this.polen.getAiState().setSearchState(
                        getSearchType(),
                        PolenSearchStatus.BLINKING,
                        this.targetPos,
                        this.targetPos,
                        "blink_toward_routine_target"
                );
                this.blinkCooldownTicks = 40;
                this.stuckTicks = 0;
                this.lastDistanceSqr = this.polen.distanceToSqr(targetCenter);
            }
            moveToTarget();
        }
    }

    @Override
    public void stop() {
        PolenTaskType taskType = getTaskType();
        if (taskType != null && this.targetPos != null) {
            if (this.failedGoal) {
                PolenTaskController.markFailed(this.polen, taskType, "routine_goal_aborted", 80L);
            } else if (this.reachedTarget && taskType == PolenTaskType.SEEK_REST) {
                PolenTaskController.markCompleted(this.polen, taskType, "resting_spot_reached");
            } else if (this.reachedTarget && taskType == PolenTaskType.QUIET_CREATION) {
                PolenTaskController.markActive(this.polen, taskType, "quiet_creation_area_ready");
            }
        }

        this.polen.getNavigation().stop();
        this.polen.getAiState().clearSearchState();
        this.targetPos = null;
        this.waitTicks = 0;
        this.stuckTicks = 0;
        this.blinkCooldownTicks = 0;
        this.lastDistanceSqr = Double.MAX_VALUE;
        this.failedGoal = false;
        this.reachedTarget = false;
    }

    private void moveToTarget() {
        if (this.targetPos == null) {
            return;
        }

        boolean pathStarted = this.polen.getNavigation().moveTo(
                this.targetPos.getX() + 0.5D,
                this.targetPos.getY(),
                this.targetPos.getZ() + 0.5D,
                MOVE_SPEED
        );

        if (!pathStarted && this.blinkCooldownTicks == 0) {
            if (PolenSafetyNavigator.tryBlinkTowardStandableSpot(this.polen, this.targetPos, getBlinkDistance())) {
                this.polen.getAiState().setSearchState(
                        getSearchType(),
                        PolenSearchStatus.BLINKING,
                        this.targetPos,
                        this.targetPos,
                        "blink_after_routine_path_fail"
                );
                this.blinkCooldownTicks = 40;
                this.stuckTicks = 0;
            } else {
                this.polen.getAiState().setSearchState(
                        getSearchType(),
                        PolenSearchStatus.FAILED,
                        this.targetPos,
                        this.targetPos,
                        "routine_path_failed"
                );
                this.failedGoal = true;
                PolenTaskController.markFailed(this.polen, getTaskType(), "routine_path_failed", 80L);
                this.targetPos = null;
                this.waitTicks = 0;
            }
        } else if (pathStarted) {
            this.polen.getAiState().setSearchState(
                    getSearchType(),
                    PolenSearchStatus.PATHING,
                    this.targetPos,
                    this.targetPos,
                    "following_routine_path"
            );
        }
    }

    private int getStuckTicksBeforeBlink() {
        return shouldUseQuickEscapeBlink() ? QUICK_ESCAPE_STUCK_TICKS : DEFAULT_STUCK_TICKS;
    }

    private int getBlinkDistance() {
        return shouldUseQuickEscapeBlink() ? QUICK_ESCAPE_BLINK_DISTANCE : DEFAULT_BLINK_DISTANCE;
    }

    private boolean shouldUseQuickEscapeBlink() {
        return this.targetPos != null
                && getTaskType() == PolenTaskType.QUIET_CREATION
                && (PolenRoutinePlanner.isDarkEnoughForLightMagic(this.polen)
                || this.targetPos.getY() >= this.polen.blockPosition().getY() + 2);
    }

    private void updateStuckCounter(double distanceSqr) {
        if (distanceSqr < this.lastDistanceSqr - 0.04D) {
            this.stuckTicks = 0;
        } else {
            this.stuckTicks++;
        }

        this.lastDistanceSqr = distanceSqr;
    }

    private PolenSearchType getSearchType() {
        return getTaskType() == PolenTaskType.SEEK_REST ? PolenSearchType.REST : PolenSearchType.QUIET_CREATION;
    }

    private PolenTaskType getTaskType() {
        return switch (this.polen.getCurrentTask()) {
            case SEEK_REST -> PolenTaskType.SEEK_REST;
            case QUIET_CREATION -> PolenTaskType.QUIET_CREATION;
            default -> null;
        };
    }
}
