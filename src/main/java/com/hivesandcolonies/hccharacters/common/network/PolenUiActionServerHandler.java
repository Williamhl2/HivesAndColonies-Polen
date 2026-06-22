package com.hivesandcolonies.hccharacters.common.network;

import com.hivesandcolonies.hccharacters.bootstrap.HcCharacters;
import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.PolenInteractionController;
import com.hivesandcolonies.hccharacters.character.polen.interaction.PolenUiActionIds;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class PolenUiActionServerHandler {
    private static final double MAX_UI_ACTION_DISTANCE_SQR = 12.0D * 12.0D;

    private PolenUiActionServerHandler() {
    }

    public static void handle(ServerboundPolenUiActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            Entity entity = player.serverLevel().getEntity(payload.entityId());
            if (!(entity instanceof PolenEntity polen)) {
                player.displayClientMessage(Component.translatable("message.polen.ui.action.no_polen"), true);
                return;
            }

            if (polen.distanceToSqr(player) > MAX_UI_ACTION_DISTANCE_SQR) {
                player.displayClientMessage(Component.translatable("message.polen.ui.action.too_far"), true);
                return;
            }

            String action = payload.action() == null ? "" : payload.action();
            switch (action) {
                case PolenUiActionIds.FOLLOW_TOGGLE -> PolenInteractionController.toggleTrustWalk(polen, player);
                case PolenUiActionIds.RETURN_HOME -> PolenInteractionController.requestReturnHome(polen, player);
                default -> player.displayClientMessage(Component.translatable("message.polen.ui.action.unknown"), true);
            }
        }).exceptionally(exception -> {
            HcCharacters.LOGGER.warn("Could not handle Polen UI action", exception);
            context.disconnect(Component.literal("Failed to handle Polen UI action: " + exception.getMessage()));
            return null;
        });
    }
}
