package com.hivesandcolonies.polen.entity.ai.goal;

import java.util.EnumSet;

import com.hivesandcolonies.polen.dialogue.PolenDialogueManager;
import com.hivesandcolonies.polen.entity.PolenEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

public class PolenSeekSafetyGoal extends Goal {
    private static final double MOVE_SPEED = 1.05D;
    private static final double STOP_DISTANCE_SQR = 2.25D;
    private static final int REPATH_COOLDOWN_TICKS = 20;
    private static final int MAX_FAILED_REPATHS = 4;

    private final PolenEntity polen;

    private BlockPos safeSpot;
    private int repathCooldownTicks;
    private int failedRepathAttempts;
    private boolean unsafeDialoguePlayed;

    public PolenSeekSafetyGoal(PolenEntity polen) {
        this.polen = polen;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.polen.shouldSeekSafety()) {
            return false;
        }

        this.repathCooldownTicks = 0;
        this.failedRepathAttempts = 0;
        this.unsafeDialoguePlayed = false;
        return planEscapeRoute(20);
    }

    @Override
    public boolean canContinueToUse() {
        return this.safeSpot != null
                && this.failedRepathAttempts < MAX_FAILED_REPATHS
                && this.polen.shouldSeekSafety();
    }

    @Override
    public void start() {
        this.polen.stopQuietActivity();
        playUnsafeDialogueIfNeeded();
        moveToSafeSpot();
    }

    @Override
    public void tick() {
        if (this.safeSpot == null) {
            return;
        }

        this.polen.getLookControl().setLookAt(
                this.safeSpot.getX() + 0.5D,
                this.safeSpot.getY(),
                this.safeSpot.getZ() + 0.5D
        );

        if (this.repathCooldownTicks > 0) {
            this.repathCooldownTicks--;
        }

        if (!this.polen.shouldSeekSafety()) {
            return;
        }

        boolean reachedTarget = this.polen.distanceToSqr(Vec3.atCenterOf(this.safeSpot)) <= STOP_DISTANCE_SQR;
        if ((reachedTarget || this.polen.getNavigation().isDone()) && this.repathCooldownTicks == 0) {
            int nextRadius = reachedTarget ? 16 : 20 + this.failedRepathAttempts * 8;
            if (planEscapeRoute(nextRadius)) {
                moveToSafeSpot();
                this.failedRepathAttempts = 0;
            } else {
                this.failedRepathAttempts++;
            }

            this.repathCooldownTicks = REPATH_COOLDOWN_TICKS;
        }
    }

    @Override
    public void stop() {
        this.polen.getNavigation().stop();
        this.safeSpot = null;
        this.repathCooldownTicks = 0;
        this.failedRepathAttempts = 0;
        this.unsafeDialoguePlayed = false;
    }

    private boolean planEscapeRoute(int radius) {
        this.safeSpot = this.polen.findNearbySafeSurfaceSpot(radius);
        return this.safeSpot != null;
    }

    private void moveToSafeSpot() {
        if (this.safeSpot == null) {
            return;
        }

        this.polen.getNavigation().moveTo(
                this.safeSpot.getX() + 0.5D,
                this.safeSpot.getY(),
                this.safeSpot.getZ() + 0.5D,
                MOVE_SPEED
        );
    }

    private void playUnsafeDialogueIfNeeded() {
        if (!this.unsafeDialoguePlayed && this.polen.shouldUseUnsafeDialogue()) {
            this.polen.tryAmbientDialogue(PolenDialogueManager.AMBIENT_UNSAFE);
            this.unsafeDialoguePlayed = true;
        }
    }
}
