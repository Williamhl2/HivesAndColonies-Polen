package com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.goal;

import java.util.EnumSet;

import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.core.PolenSleepController;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.environment.PolenEnvironmentResolver;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home.PolenHomeManager;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;

public class PolenTrustWalkGoal extends Goal {
    private static final double SPEED = 0.92D;
    private static final double MIN_DISTANCE_SQR = 3.0D * 3.0D;
    private static final double MAX_DISTANCE_SQR = 28.0D * 28.0D;
    private static final double CANCEL_DISTANCE_SQR = 42.0D * 42.0D;

    private final PolenEntity polen;

    public PolenTrustWalkGoal(PolenEntity polen) {
        this.polen = polen;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.polen.isTrustWalkActive() && this.polen.getTrustWalkPlayer() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void tick() {
        ServerPlayer player = this.polen.getTrustWalkPlayer();
        if (player == null) {
            this.polen.stopTrustWalk();
            return;
        }

        if (this.polen.isSleeping()) {
            stopTrustWalk("message.polen.trust_walk.sleeping");
            return;
        }
        boolean unsafeArea = PolenEnvironmentResolver.inspect(this.polen).isInUnsafeArea();
        if (this.polen.getCurrentTask().isUrgent()
                || PolenSleepController.hasImmediateThreat(this.polen)
                || unsafeArea) {
            stopTrustWalk("message.polen.trust_walk.unsafe");
            return;
        }
        if (this.polen.hasAssignedHome() && PolenHomeManager.isNearHomeCenter(this.polen, 3.0D)) {
            stopTrustWalk("message.polen.trust_walk.reached_home");
            return;
        }

        double distance = this.polen.distanceToSqr(player);
        if (distance > CANCEL_DISTANCE_SQR) {
            stopTrustWalk("message.polen.trust_walk.too_far");
            return;
        }

        this.polen.getLookControl().setLookAt(player, 20.0F, 20.0F);
        if (distance > MIN_DISTANCE_SQR && distance < MAX_DISTANCE_SQR) {
            this.polen.getNavigation().moveTo(player, SPEED);
        } else if (distance <= MIN_DISTANCE_SQR) {
            this.polen.getNavigation().stop();
        }
    }

    @Override
    public void stop() {
        this.polen.getNavigation().stop();
    }

    private void stopTrustWalk(String translationKey) {
        ServerPlayer player = this.polen.getTrustWalkPlayer();
        this.polen.stopTrustWalk();
        if (player != null) {
            player.displayClientMessage(Component.translatable(translationKey), true);
        }
    }
}
