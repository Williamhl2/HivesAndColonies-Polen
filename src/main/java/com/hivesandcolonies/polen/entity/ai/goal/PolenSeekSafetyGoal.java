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

    private final PolenEntity polen;

    private BlockPos safeSpot;

    public PolenSeekSafetyGoal(PolenEntity polen) {
        this.polen = polen;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.polen.isInUnsafeArea()) {
            return false;
        }

        this.safeSpot = this.polen.findNearbySafeSurfaceSpot(20);
        return this.safeSpot != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.safeSpot != null
                && this.polen.distanceToSqr(Vec3.atCenterOf(this.safeSpot)) > STOP_DISTANCE_SQR
                && !this.polen.getNavigation().isDone();
    }

    @Override
    public void start() {
        if (this.safeSpot != null) {
            this.polen.stopQuietActivity();
            this.polen.tryAmbientDialogue(PolenDialogueManager.AMBIENT_UNSAFE);
            this.polen.getNavigation().moveTo(
                    this.safeSpot.getX() + 0.5D,
                    this.safeSpot.getY(),
                    this.safeSpot.getZ() + 0.5D,
                    MOVE_SPEED
            );
        }
    }

    @Override
    public void tick() {
        if (this.safeSpot != null) {
            this.polen.getLookControl().setLookAt(
                    this.safeSpot.getX() + 0.5D,
                    this.safeSpot.getY(),
                    this.safeSpot.getZ() + 0.5D
            );
        }
    }

    @Override
    public void stop() {
        this.safeSpot = null;
    }
}
