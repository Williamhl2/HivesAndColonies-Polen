package com.hivesandcolonies.polen.entity.ai.goal;

import com.hivesandcolonies.polen.dialogue.PolenDialogueManager;
import com.hivesandcolonies.polen.entity.PolenAmbientDialogueController;
import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.ai.PolenMood;
import com.hivesandcolonies.polen.entity.ai.safety.PolenSafetyNavigator;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

public class PolenApproachTrustedPlayerGoal extends Goal {
    private static final double MIN_DISTANCE_SQR = 9.0D;
    private static final double MAX_DISTANCE = 10.0D;
    private static final double MAX_DISTANCE_SQR = MAX_DISTANCE * MAX_DISTANCE;
    private static final double MOVE_SPEED = 0.9D;
    private static final int MIN_OBSERVE_TICKS = 40;
    private static final int MAX_EXTRA_OBSERVE_TICKS = 80;
    private static final int MIN_COOLDOWN = 220;
    private static final int MAX_EXTRA_COOLDOWN = 260;

    private final PolenEntity polen;

    private Player targetPlayer;
    private int observeTicks;
    private int cooldownTicks = MIN_COOLDOWN;
    private boolean dialoguePlayed;

    public PolenApproachTrustedPlayerGoal(PolenEntity polen) {
        this.polen = polen;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.cooldownTicks > 0) {
            this.cooldownTicks--;
            return false;
        }

        if (this.polen.isDoingQuietActivity()
                || PolenSafetyNavigator.isInUnsafeArea(this.polen)
                || this.polen.getRandom().nextInt(40) != 0) {
            return false;
        }

        PolenMood mood = this.polen.getMood();
        if (mood == PolenMood.TIMID || mood == PolenMood.UNSETTLED) {
            return false;
        }

        Player player = this.polen.level().getNearestPlayer(this.polen, MAX_DISTANCE);
        if (player == null
                || !player.isAlive()
                || !this.polen.hasLineOfSight(player)
                || !this.polen.isComfortableWith(player)) {
            return false;
        }

        double distanceSqr = this.polen.distanceToSqr(player);
        if (distanceSqr <= MIN_DISTANCE_SQR || distanceSqr > MAX_DISTANCE_SQR) {
            return false;
        }

        this.targetPlayer = player;
        this.observeTicks = MIN_OBSERVE_TICKS + this.polen.getRandom().nextInt(MAX_EXTRA_OBSERVE_TICKS + 1);
        this.dialoguePlayed = false;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetPlayer != null
                && this.targetPlayer.isAlive()
                && this.polen.isComfortableWith(this.targetPlayer)
                && !PolenSafetyNavigator.isInUnsafeArea(this.polen)
                && this.polen.distanceToSqr(this.targetPlayer) <= MAX_DISTANCE_SQR
                && this.observeTicks > 0;
    }

    @Override
    public void start() {
        this.polen.stopQuietActivity();
        moveToTargetPlayer();
    }

    @Override
    public void tick() {
        if (this.targetPlayer == null) {
            return;
        }

        this.polen.getLookControl().setLookAt(this.targetPlayer, 20.0F, 20.0F);

        if (this.polen.distanceToSqr(this.targetPlayer) <= MIN_DISTANCE_SQR) {
            this.polen.getNavigation().stop();
            playApproachDialogueIfNeeded();
            this.observeTicks--;
            return;
        }

        if (this.polen.getNavigation().isDone()) {
            moveToTargetPlayer();
        }
    }

    @Override
    public void stop() {
        this.polen.getNavigation().stop();
        this.targetPlayer = null;
        this.observeTicks = 0;
        this.dialoguePlayed = false;
        this.cooldownTicks = MIN_COOLDOWN + this.polen.getRandom().nextInt(MAX_EXTRA_COOLDOWN + 1);
    }

    private void moveToTargetPlayer() {
        if (this.targetPlayer != null) {
            this.polen.getNavigation().moveTo(this.targetPlayer, MOVE_SPEED);
        }
    }

    private void playApproachDialogueIfNeeded() {
        if (!this.dialoguePlayed) {
            PolenAmbientDialogueController.tryPlay(this.polen, PolenDialogueManager.AMBIENT_APPROACH);
            this.dialoguePlayed = true;
        }
    }
}
