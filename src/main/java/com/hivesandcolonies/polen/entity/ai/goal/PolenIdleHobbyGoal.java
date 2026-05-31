package com.hivesandcolonies.polen.entity.ai.goal;

import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.PolenAmbientDialogueController;
import com.hivesandcolonies.polen.entity.ai.activity.PolenQuietActivityController;
import com.hivesandcolonies.polen.entity.ai.intent.PolenIntent;
import com.hivesandcolonies.polen.entity.ai.safety.PolenSafetyNavigator;

import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class PolenIdleHobbyGoal extends Goal {
    private static final int MIN_DURATION = 80;
    private static final int MAX_DURATION = 160;

    private final PolenEntity polen;

    private int activityTicks;
    private int activityType = PolenQuietActivityController.QUIET_ACTIVITY_NONE;

    public PolenIdleHobbyGoal(PolenEntity polen) {
        this.polen = polen;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.polen.isDoingQuietActivity()
                || PolenSafetyNavigator.isInUnsafeArea(this.polen)
                || this.polen.getCurrentIntent() != PolenIntent.QUIET_CREATION
                || this.polen.hasNearbyPlayer(3.0D)
                || !this.polen.onGround()
                || this.polen.isInWaterOrBubble()
                || this.polen.getNavigation().isInProgress()
                || this.polen.getDeltaMovement().horizontalDistanceSqr() > 0.002D) {
            return false;
        }

        this.activityType = this.polen.pickQuietActivity();
        this.activityTicks = MIN_DURATION + this.polen.getRandom().nextInt(MAX_DURATION - MIN_DURATION + 1);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.activityTicks > 0
                && this.polen.isDoingQuietActivity()
                && this.polen.getCurrentIntent() == PolenIntent.QUIET_CREATION
                && !PolenSafetyNavigator.isInUnsafeArea(this.polen)
                && !this.polen.hasNearbyPlayer(2.5D);
    }

    @Override
    public void start() {
        this.polen.startQuietActivity(this.activityType, this.activityTicks);
        this.polen.getNavigation().stop();
        PolenAmbientDialogueController.tryPlay(
                this.polen,
                this.activityType == PolenQuietActivityController.QUIET_ACTIVITY_SINGING
                        ? com.hivesandcolonies.polen.dialogue.PolenDialogueManager.AMBIENT_SINGING
                        : com.hivesandcolonies.polen.dialogue.PolenDialogueManager.AMBIENT_DRAWING
        );
    }

    @Override
    public void tick() {
        this.activityTicks--;
        this.polen.getNavigation().stop();
        this.polen.getLookControl().setLookAt(
                this.polen.getX() + this.polen.getLookAngle().x,
                this.polen.getEyeY(),
                this.polen.getZ() + this.polen.getLookAngle().z
        );
    }

    @Override
    public void stop() {
        this.polen.stopQuietActivity();
        this.activityTicks = 0;
        this.activityType = PolenQuietActivityController.QUIET_ACTIVITY_NONE;
    }
}
