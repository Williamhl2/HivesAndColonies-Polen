package com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.goal;

import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.PolenAmbientDialogueController;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.expression.activity.PolenQuietActivityController;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.action.PolenAutonomousActionPlan;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.ability.magic.PolenMagicController;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.core.PolenSleepController;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.task.PolenTaskController;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.task.PolenTaskType;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.safety.PolenSafetyNavigator;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class PolenIdleHobbyGoal extends Goal {
    private static final int MIN_DURATION = 80;
    private static final int MAX_DURATION = 160;
    private static final long QUIET_ACTIVITY_COOLDOWN_TICKS = 60L;
    private static final long SAME_SPOT_HOBBY_COOLDOWN_TICKS = 160L;
    private static final double SAME_SPOT_DISTANCE_SQR = 4.0D;

    private final PolenEntity polen;

    private int activityTicks;
    private int activityType = PolenQuietActivityController.QUIET_ACTIVITY_NONE;
    private String dialogueSituation = com.hivesandcolonies.hccharacters.character.polen.dialogue.PolenDialogueManager.AMBIENT_DRAWING;

    public PolenIdleHobbyGoal(PolenEntity polen) {
        this.polen = polen;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.polen.isSleeping()
                || this.polen.isDoingQuietActivity()
                || PolenSafetyNavigator.isInUnsafeArea(this.polen)
                || PolenSleepController.shouldPrioritizeBedReturn(this.polen)
                || this.polen.getCurrentTask() != PolenTaskType.QUIET_CREATION
                || this.polen.hasNearbyPlayer(3.0D)
                || !this.polen.onGround()
                || this.polen.isInWaterOrBubble()
                || this.polen.getNavigation().isInProgress()
                || this.polen.getDeltaMovement().horizontalDistanceSqr() > 0.002D) {
            return false;
        }

        PolenAutonomousActionPlan plan = this.polen.pickQuietActionPlan();
        if (!isQuietActivityAllowedNow(plan.quietActivityType())) {
            return false;
        }
        this.activityType = plan.quietActivityType();
        this.dialogueSituation = plan.dialogueSituation();
        this.activityTicks = plan.minDuration()
                + this.polen.getRandom().nextInt(plan.maxDuration() - plan.minDuration() + 1);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.polen.isSleeping()
                && this.activityTicks > 0
                && this.polen.isDoingQuietActivity()
                && this.polen.getCurrentTask() == PolenTaskType.QUIET_CREATION
                && !PolenSleepController.shouldPrioritizeBedReturn(this.polen)
                && !PolenSafetyNavigator.isInUnsafeArea(this.polen)
                && !this.polen.hasNearbyPlayer(2.5D)
                && this.polen.onGround()
                && !this.polen.isInWaterOrBubble()
                && (this.activityType != PolenQuietActivityController.QUIET_ACTIVITY_ATTUNING
                || PolenMagicController.hasNearbySourceLikeInterest(this.polen))
                && (this.activityType != PolenQuietActivityController.QUIET_ACTIVITY_REFLECTING
                || PolenMagicController.hasNearbyManagedLight(this.polen)
                || com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.routine.PolenRoutinePlanner.isNearRestingSpot(this.polen));
    }

    @Override
    public void start() {
        this.polen.startQuietActivity(this.activityType, this.activityTicks);
        this.polen.getNavigation().stop();
        PolenTaskController.markActive(this.polen, PolenTaskType.QUIET_CREATION, "quiet_activity_started");
        PolenAmbientDialogueController.tryPlay(this.polen, this.dialogueSituation);
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
        rememberQuietActivityCooldown();
        if (this.activityType != PolenQuietActivityController.QUIET_ACTIVITY_NONE) {
            if (this.activityTicks <= 0) {
                PolenTaskController.markCompleted(this.polen, PolenTaskType.QUIET_CREATION, "quiet_activity_finished");
            } else {
                PolenTaskController.markFailed(
                        this.polen,
                        PolenTaskType.QUIET_CREATION,
                        "quiet_activity_interrupted",
                        80L
                );
            }
        }
        this.polen.stopQuietActivity();
        this.activityTicks = 0;
        this.activityType = PolenQuietActivityController.QUIET_ACTIVITY_NONE;
        this.dialogueSituation = com.hivesandcolonies.hccharacters.character.polen.dialogue.PolenDialogueManager.AMBIENT_DRAWING;
    }

    private boolean isQuietActivityAllowedNow(int nextActivityType) {
        if (!(this.polen.level() instanceof ServerLevel serverLevel)) {
            return true;
        }

        long gameTime = serverLevel.getGameTime();
        if (gameTime < this.polen.getAiState().getNextQuietActivityAllowedGameTime()) {
            return false;
        }

        BlockPos lastPos = this.polen.getAiState().getLastQuietActivityPos();
        if (lastPos == null) {
            return true;
        }

        boolean sameSpot = lastPos.distSqr(this.polen.blockPosition()) <= SAME_SPOT_DISTANCE_SQR;
        boolean leisureActivity = nextActivityType == PolenQuietActivityController.QUIET_ACTIVITY_SINGING
                || nextActivityType == PolenQuietActivityController.QUIET_ACTIVITY_DRAWING
                || nextActivityType == PolenQuietActivityController.QUIET_ACTIVITY_REFLECTING;

        return !sameSpot
                || !leisureActivity
                || gameTime >= this.polen.getAiState().getNextQuietActivityAllowedGameTime() + SAME_SPOT_HOBBY_COOLDOWN_TICKS;
    }

    private void rememberQuietActivityCooldown() {
        if (!(this.polen.level() instanceof ServerLevel serverLevel)
                || this.activityType == PolenQuietActivityController.QUIET_ACTIVITY_NONE) {
            return;
        }

        long gameTime = serverLevel.getGameTime();
        this.polen.getAiState().setLastQuietActivityType(this.activityType);
        this.polen.getAiState().setLastQuietActivityPos(this.polen.blockPosition().immutable());
        this.polen.getAiState().setNextQuietActivityAllowedGameTime(gameTime + QUIET_ACTIVITY_COOLDOWN_TICKS);
    }
}
