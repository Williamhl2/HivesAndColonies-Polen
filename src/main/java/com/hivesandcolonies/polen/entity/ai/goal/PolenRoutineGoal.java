package com.hivesandcolonies.polen.entity.ai.goal;

import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.ai.intent.PolenIntent;
import com.hivesandcolonies.polen.entity.ai.navigation.PolenSearchStatus;
import com.hivesandcolonies.polen.entity.ai.navigation.PolenSearchType;
import com.hivesandcolonies.polen.entity.ai.routine.PolenRoutinePlanner;
import com.hivesandcolonies.polen.entity.ai.safety.PolenSafetyNavigator;

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

    public PolenRoutineGoal(PolenEntity polen) {
        this.polen = polen;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.polen.isDoingQuietActivity()
                || PolenSafetyNavigator.isInUnsafeArea(this.polen)
                || this.polen.getCurrentIntent() != PolenIntent.SEEK_REST
                && this.polen.getCurrentIntent() != PolenIntent.QUIET_CREATION) {
            return false;
        }

        this.targetPos = PolenRoutinePlanner.getRoutineTarget(this.polen, this.polen.getCurrentIntent());
        this.waitTicks = 60 + this.polen.getRandom().nextInt(60);
        this.stuckTicks = 0;
        this.blinkCooldownTicks = 0;
        this.lastDistanceSqr = Double.MAX_VALUE;
        boolean canUse = this.targetPos != null
                && !this.polen.blockPosition().closerToCenterThan(Vec3.atCenterOf(this.targetPos), 1.5D);
        this.polen.getAiState().setSearchState(
                getSearchType(),
                canUse ? PolenSearchStatus.SCANNING : PolenSearchStatus.FAILED,
                this.targetPos,
                this.targetPos,
                canUse ? "routine_target_planned" : "no_routine_target"
        );
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
        this.polen.getNavigation().stop();
        this.polen.getAiState().clearSearchState();
        this.targetPos = null;
        this.waitTicks = 0;
        this.stuckTicks = 0;
        this.blinkCooldownTicks = 0;
        this.lastDistanceSqr = Double.MAX_VALUE;
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
                && this.polen.getCurrentIntent() == PolenIntent.QUIET_CREATION
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
        return this.polen.getCurrentIntent() == PolenIntent.SEEK_REST
                ? PolenSearchType.REST
                : PolenSearchType.QUIET_CREATION;
    }
}
