package com.hivesandcolonies.polen.entity.ai.goal;

import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.ai.intent.PolenIntent;
import com.hivesandcolonies.polen.entity.ai.routine.PolenRoutinePlanner;
import com.hivesandcolonies.polen.entity.ai.safety.PolenSafetyNavigator;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class PolenRoutineGoal extends Goal {
    private static final double MOVE_SPEED = 0.85D;
    private static final double STOP_DISTANCE_SQR = 2.25D;

    private final PolenEntity polen;

    private BlockPos targetPos;
    private int waitTicks;
    private int repathCooldown;
    private int stuckTicks;

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
        return this.targetPos != null && !this.polen.blockPosition().closerToCenterThan(Vec3.atCenterOf(this.targetPos), 1.5D);
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetPos != null
                && this.waitTicks > 0
                && !this.polen.isDoingQuietActivity()
                && !PolenSafetyNavigator.isInUnsafeArea(this.polen)
                && (this.polen.getCurrentIntent() == PolenIntent.SEEK_REST
                || this.polen.getCurrentIntent() == PolenIntent.QUIET_CREATION)
                && this.stuckTicks < 40
                && PolenRoutinePlanner.isRememberedSpotStillValid(this.polen, this.targetPos);
    }

    @Override
    public void start() {
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
            this.waitTicks--;
            return;
        }

        if (this.polen.getNavigation().isDone()) {
            this.stuckTicks++;
            if (this.repathCooldown-- <= 0) {
                this.repathCooldown = 10;
                moveToTarget();
            }
        } else {
            this.stuckTicks = 0;
        }
    }

    @Override
    public void stop() {
        this.polen.getNavigation().stop();
        this.targetPos = null;
        this.waitTicks = 0;
        this.repathCooldown = 0;
        this.stuckTicks = 0;
    }

    private void moveToTarget() {
        if (this.targetPos == null) {
            return;
        }

        boolean started = this.polen.getNavigation().moveTo(
                this.targetPos.getX() + 0.5D,
                this.targetPos.getY(),
                this.targetPos.getZ() + 0.5D,
                MOVE_SPEED
        );

        if (!started) {
            this.stuckTicks += 10;
        }
    }
}
