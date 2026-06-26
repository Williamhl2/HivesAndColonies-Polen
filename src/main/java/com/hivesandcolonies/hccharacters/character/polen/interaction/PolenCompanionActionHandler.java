package com.hivesandcolonies.hccharacters.character.polen.interaction;

import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.core.PolenSleepController;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.environment.PolenEnvironmentResolver;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home.PolenHomeManager;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenAffinityLevels;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenAffinityManager;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenRelationshipEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public final class PolenCompanionActionHandler {
    private static final int TRUST_WALK_DURATION_TICKS = 20 * 60 * 4;
    private static final int RETURN_HOME_REQUEST_TICKS = 20 * 45;

    private PolenCompanionActionHandler() {
    }

    public static InteractionResult toggleTrustWalk(PolenEntity polen, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        if (polen.isTrustWalkActive()) {
            ServerPlayer activePlayer = polen.getTrustWalkPlayer();
            if (activePlayer == serverPlayer) {
                polen.stopTrustWalk();
                player.displayClientMessage(Component.translatable("message.polen.trust_walk.stopped"), true);
                return InteractionResult.SUCCESS;
            }
            if (activePlayer != null && activePlayer.isAlive()) {
                player.displayClientMessage(Component.translatable("message.polen.trust_walk.busy"), true);
                return InteractionResult.SUCCESS;
            }
            polen.stopTrustWalk();
        }

        String blockedReasonKey = getTrustWalkBlockedReason(polen, player);
        if (blockedReasonKey != null) {
            player.displayClientMessage(Component.translatable(blockedReasonKey), true);
            return InteractionResult.SUCCESS;
        }

        polen.startTrustWalk(serverPlayer, TRUST_WALK_DURATION_TICKS);
        PolenRelationshipEvents.trustWalkStarted(player);
        player.displayClientMessage(Component.translatable("message.polen.trust_walk.started"), true);
        return InteractionResult.SUCCESS;
    }

    public static InteractionResult requestReturnHome(PolenEntity polen, Player player) {
        if (polen == null || player == null) {
            return InteractionResult.PASS;
        }

        BlockPos homePos = PolenHomeManager.getHomeCenterPos(polen);
        if (homePos == null) {
            player.displayClientMessage(Component.translatable("message.polen.ui.action.no_home"), true);
            return InteractionResult.SUCCESS;
        }

        polen.requestReturnHome(RETURN_HOME_REQUEST_TICKS);
        polen.getNavigation().moveTo(homePos.getX() + 0.5D, homePos.getY(), homePos.getZ() + 0.5D, 0.9D);
        player.displayClientMessage(Component.translatable("message.polen.ui.action.return_home"), true);
        return InteractionResult.SUCCESS;
    }

    private static String getTrustWalkBlockedReason(PolenEntity polen, Player player) {
        int affinity = PolenAffinityManager.getAffinity(player);
        if (affinity < PolenAffinityLevels.FIRST_TRUST) {
            return "message.polen.trust_walk.not_enough_trust";
        }
        if (polen.isSleeping()) {
            return "message.polen.trust_walk.sleeping";
        }
        boolean unsafeArea = PolenEnvironmentResolver.inspect(polen).isInUnsafeArea();
        if (polen.getCurrentTask().isUrgent()
                || PolenSleepController.hasImmediateThreat(polen)
                || unsafeArea) {
            return "message.polen.trust_walk.unsafe";
        }
        return null;
    }
}
