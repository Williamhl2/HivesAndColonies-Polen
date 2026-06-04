package com.hivesandcolonies.characters.character.polen.entity.ai.navigation.goal;

import com.hivesandcolonies.characters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.characters.character.polen.entity.PolenAmbientDialogueController;
import com.hivesandcolonies.characters.character.polen.entity.ai.expression.gesture.PolenGesture;
import com.hivesandcolonies.characters.character.polen.entity.ai.expression.gesture.PolenGestureController;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.interest.PolenInterestTarget;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.interest.PolenInterestType;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.task.PolenTaskController;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.task.PolenTaskType;
import com.hivesandcolonies.characters.character.polen.entity.ai.navigation.safety.PolenSafetyNavigator;
import com.hivesandcolonies.characters.character.polen.entity.ai.world.affordance.PolenAffordanceResolver;
import com.hivesandcolonies.characters.character.polen.entity.ai.world.affordance.PolenAffordanceTarget;
import com.hivesandcolonies.characters.character.polen.entity.ai.world.affordance.PolenAffordanceType;
import com.hivesandcolonies.characters.character.polen.story.PolenMemoryManager;
import com.hivesandcolonies.characters.character.polen.story.PolenMemoryType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class PolenCuriousInterestGoal extends Goal {
    private static final double MOVE_SPEED = 0.9D;
    private static final double STOP_DISTANCE_SQR = 2.25D;
    private static final int MAX_TRAVEL_TICKS_WITHOUT_PROGRESS = 80;
    private static final int MAX_TOTAL_TICKS = 180;
    private static final int MAX_REPATH_ATTEMPTS = 4;
    private static final long OBSERVED_INTEREST_COOLDOWN_TICKS = 180L;
    private static final long FAILED_INTEREST_COOLDOWN_TICKS = 100L;

    private final PolenEntity polen;

    private PolenInterestTarget target;
    private int observeTicks;
    private int stuckTicks;
    private int blinkCooldownTicks;
    private int travelTimeoutTicks;
    private int totalTicks;
    private int repathAttempts;
    private double lastDistanceSqr;
    private boolean failedToReachTarget;

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

        PolenAffordanceTarget affordance = PolenAffordanceResolver.findBestInterest(this.polen, true);
        this.target = affordance == null ? null : mapAffordanceToInterest(affordance);
        this.observeTicks = 40 + this.polen.getRandom().nextInt(60);
        this.stuckTicks = 0;
        this.blinkCooldownTicks = 0;
        this.travelTimeoutTicks = MAX_TRAVEL_TICKS_WITHOUT_PROGRESS;
        this.totalTicks = MAX_TOTAL_TICKS;
        this.repathAttempts = 0;
        this.lastDistanceSqr = Double.MAX_VALUE;
        this.failedToReachTarget = false;
        return this.target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.target != null
                && this.observeTicks > 0
                && this.totalTicks > 0
                && !this.failedToReachTarget
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
                    com.hivesandcolonies.characters.character.polen.dialogue.PolenDialogueManager.AMBIENT_MAGIC
            );
        } else {
            this.polen.rememberInterestingSpot(this.target.pos());
            PolenAmbientDialogueController.tryPlay(
                    this.polen,
                    com.hivesandcolonies.characters.character.polen.dialogue.PolenDialogueManager.AMBIENT_CURIOSITY
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
        this.totalTicks--;

        if (this.totalTicks <= 0) {
            this.failedToReachTarget = true;
            return;
        }

        if (distanceSqr <= STOP_DISTANCE_SQR) {
            this.polen.getNavigation().stop();
            this.polen.getLookControl().setLookAt(focusCenter.x, focusCenter.y, focusCenter.z);
            this.observeTicks--;
            this.travelTimeoutTicks = MAX_TRAVEL_TICKS_WITHOUT_PROGRESS;
            return;
        }

        if (this.blinkCooldownTicks > 0) {
            this.blinkCooldownTicks--;
        }

        boolean madeProgress = updateStuckCounter(distanceSqr);
        if (madeProgress) {
            this.travelTimeoutTicks = MAX_TRAVEL_TICKS_WITHOUT_PROGRESS;
        } else {
            this.travelTimeoutTicks--;
        }

        if (this.travelTimeoutTicks <= 0) {
            this.failedToReachTarget = true;
            return;
        }

        if (this.blinkCooldownTicks == 0 && (this.stuckTicks >= 30 || this.polen.getNavigation().isDone())) {
            this.repathAttempts++;
            if (this.repathAttempts > MAX_REPATH_ATTEMPTS) {
                this.failedToReachTarget = true;
                return;
            }
            if (PolenSafetyNavigator.tryBlinkTowardStandableSpot(this.polen, this.target.observePos(), 6)) {
                this.blinkCooldownTicks = 40;
                this.stuckTicks = 0;
                this.lastDistanceSqr = this.polen.distanceToSqr(targetCenter);
                this.travelTimeoutTicks = MAX_TRAVEL_TICKS_WITHOUT_PROGRESS;
            }
            moveToTarget();
        }
    }

    @Override
    public void stop() {
        if (this.target != null) {
            rememberInterestCooldown(this.observeTicks <= 0 && !this.failedToReachTarget);
            if (this.observeTicks <= 0 && !this.failedToReachTarget) {
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
        this.travelTimeoutTicks = 0;
        this.totalTicks = 0;
        this.repathAttempts = 0;
        this.lastDistanceSqr = Double.MAX_VALUE;
        this.failedToReachTarget = false;
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
                this.travelTimeoutTicks = MAX_TRAVEL_TICKS_WITHOUT_PROGRESS;
            }
        }
    }

    private boolean updateStuckCounter(double distanceSqr) {
        if (distanceSqr < this.lastDistanceSqr - 0.04D) {
            this.stuckTicks = 0;
            this.lastDistanceSqr = distanceSqr;
            return true;
        } else {
            this.stuckTicks++;
        }

        this.lastDistanceSqr = distanceSqr;
        return false;
    }

    private void rememberInterestCooldown(boolean completedObservation) {
        if (this.target == null) {
            return;
        }

        long gameTime = this.polen.level().getGameTime();
        long cooldown = completedObservation ? OBSERVED_INTEREST_COOLDOWN_TICKS : FAILED_INTEREST_COOLDOWN_TICKS;
        this.polen.getAiState().setInterestCooldown(this.target.pos(), gameTime + cooldown);
    }

    private PolenInterestTarget mapAffordanceToInterest(PolenAffordanceTarget affordance) {
        PolenInterestType type = switch (affordance.type()) {
            case INTEREST_FLOWER -> PolenInterestType.FLOWER;
            case INTEREST_HIVE -> PolenInterestType.HIVE;
            case INTEREST_SOURCE -> PolenInterestType.SOURCE;
            case EXISTING_LIGHT -> PolenInterestType.LIGHT;
            default -> null;
        };
        return type == null ? null : new PolenInterestTarget(affordance.focusPos(), affordance.usePos(), type);
    }
}
