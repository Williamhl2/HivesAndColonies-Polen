package com.hivesandcolonies.polen.entity.ai.goal;

import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.PolenAmbientDialogueController;

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
        Player player = this.polen.level().getNearestPlayer(this.polen, TOO_CLOSE_DISTANCE);
        if (player == null || this.polen.isComfortableWith(player)) {
            return false;
        }

        if (this.polen.getMood() == com.hivesandcolonies.polen.entity.ai.PolenMood.CONFIDENT
                || this.polen.getMood() == com.hivesandcolonies.polen.entity.ai.PolenMood.JOYFUL) {
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
                && this.nearbyPlayer.distanceToSqr(this.polen) < 16.0D
                && !this.polen.getNavigation().isDone();
    }

    @Override
    public void start() {
        if (this.targetPos != null) {
            this.polen.stopQuietActivity();
            PolenAmbientDialogueController.tryPlay(
                    this.polen,
                    com.hivesandcolonies.polen.dialogue.PolenDialogueManager.AMBIENT_TIMID
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
        this.polen.getNavigation().stop();
        this.nearbyPlayer = null;
        this.targetPos = null;
    }
}
