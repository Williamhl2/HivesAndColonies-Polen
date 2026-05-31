package com.hivesandcolonies.polen.entity.ai.goal;

import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.PolenAmbientDialogueController;
import com.hivesandcolonies.polen.entity.ai.intent.PolenIntent;
import com.hivesandcolonies.polen.entity.ai.interest.PolenInterestLocator;
import com.hivesandcolonies.polen.entity.ai.interest.PolenInterestTarget;
import com.hivesandcolonies.polen.entity.ai.interest.PolenInterestType;
import com.hivesandcolonies.polen.entity.ai.safety.PolenSafetyNavigator;
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

    public PolenCuriousInterestGoal(PolenEntity polen) {
        this.polen = polen;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.polen.isDoingQuietActivity()
                || PolenSafetyNavigator.isInUnsafeArea(this.polen)
                || this.polen.hasNearbyPlayer(2.5D)
                || this.polen.getCurrentIntent() != PolenIntent.INVESTIGATE_INTEREST) {
            return false;
        }

        this.target = PolenInterestLocator.findPreferredInterest(this.polen, true);
        this.observeTicks = 40 + this.polen.getRandom().nextInt(60);
        return this.target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.target != null
                && this.observeTicks > 0
                && !this.polen.hasNearbyPlayer(2.0D)
                && this.polen.getCurrentIntent() == PolenIntent.INVESTIGATE_INTEREST;
    }

    @Override
    public void start() {
        if (this.target == null) {
            return;
        }

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

        moveToTarget();
    }

    @Override
    public void tick() {
        if (this.target == null) {
            return;
        }

        Vec3 targetCenter = Vec3.atCenterOf(this.target.pos());
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
        this.target = null;
        this.observeTicks = 0;
    }

    private void moveToTarget() {
        if (this.target == null) {
            return;
        }

        this.polen.getNavigation().moveTo(
                this.target.pos().getX() + 0.5D,
                this.target.pos().getY(),
                this.target.pos().getZ() + 0.5D,
                MOVE_SPEED
        );
    }
}
