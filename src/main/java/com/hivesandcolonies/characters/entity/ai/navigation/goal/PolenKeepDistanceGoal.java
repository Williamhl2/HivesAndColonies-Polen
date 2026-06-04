package com.hivesandcolonies.characters.entity.ai.navigation.goal;

import com.hivesandcolonies.characters.entity.PolenEntity;
import com.hivesandcolonies.characters.entity.PolenAmbientDialogueController;
import com.hivesandcolonies.characters.entity.ai.expression.gesture.PolenGesture;
import com.hivesandcolonies.characters.entity.ai.expression.gesture.PolenGestureController;
import com.hivesandcolonies.characters.entity.ai.brain.mood.PolenMood;
import com.hivesandcolonies.characters.entity.ai.brain.task.PolenTaskController;
import com.hivesandcolonies.characters.entity.ai.brain.task.PolenTaskType;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class PolenKeepDistanceGoal extends Goal {
    private static final double TOO_CLOSE_DISTANCE = 2.25D;
    private static final double MOVE_SPEED = 1.0D;

    private final PolenEntity polen;

    private Player nearbyPlayer;
    private Vec3 targetPos;

    public PolenKeepDistanceGoal(PolenEntity polen) {
        this.polen = polen;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.polen.getCurrentTask() != PolenTaskType.KEEP_DISTANCE) {
            return false;
        }

        Player player = this.polen.level().getNearestPlayer(this.polen, TOO_CLOSE_DISTANCE);
        if (player == null || this.polen.isComfortableWith(player)) {
            return false;
        }

        if (this.polen.getMood() == PolenMood.CONFIDENT
                || this.polen.getMood() == PolenMood.JOYFUL) {
            return false;
        }

        Vec3 awayPos = LandRandomPos.getPosAway(this.polen, 8, 4, player.position());
        if (awayPos == null) {
            return false;
        }

        this.nearbyPlayer = player;
        this.targetPos = awayPos;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.nearbyPlayer != null
                && this.nearbyPlayer.isAlive()
                && this.polen.getCurrentTask() == PolenTaskType.KEEP_DISTANCE
                && this.nearbyPlayer.distanceToSqr(this.polen) < 16.0D
                && !this.polen.getNavigation().isDone();
    }

    @Override
    public void start() {
        if (this.targetPos != null) {
            this.polen.stopQuietActivity();
            PolenTaskController.markActive(this.polen, PolenTaskType.KEEP_DISTANCE, "creating_personal_space");
            PolenGestureController.triggerGesture(this.polen, PolenGesture.WITHDRAWN);
            PolenAmbientDialogueController.tryPlay(
                    this.polen,
                    com.hivesandcolonies.characters.dialogue.PolenDialogueManager.AMBIENT_TIMID
            );
            this.polen.getNavigation().moveTo(this.targetPos.x, this.targetPos.y, this.targetPos.z, MOVE_SPEED);
        }
    }

    @Override
    public void tick() {
        if (this.nearbyPlayer != null) {
            this.polen.getLookControl().setLookAt(this.nearbyPlayer, 20.0F, 20.0F);
        }
    }

    @Override
    public void stop() {
        if (this.targetPos != null) {
            if (this.nearbyPlayer == null || !this.nearbyPlayer.isAlive() || this.nearbyPlayer.distanceToSqr(this.polen) >= 16.0D) {
                PolenTaskController.markCompleted(this.polen, PolenTaskType.KEEP_DISTANCE, "personal_space_restored");
            } else {
                PolenTaskController.markFailed(this.polen, PolenTaskType.KEEP_DISTANCE, "still_too_close", 20L);
            }
        }
        this.polen.getNavigation().stop();
        this.nearbyPlayer = null;
        this.targetPos = null;
    }
}
