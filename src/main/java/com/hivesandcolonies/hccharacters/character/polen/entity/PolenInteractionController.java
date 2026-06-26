package com.hivesandcolonies.hccharacters.character.polen.entity;

import com.hivesandcolonies.hccharacters.character.polen.interaction.PolenCompanionActionHandler;
import com.hivesandcolonies.hccharacters.character.polen.interaction.PolenSocialInteractionHandler;
import com.hivesandcolonies.hccharacters.character.polen.item.interaction.PolenItemInteractionController;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public final class PolenInteractionController {
    private PolenInteractionController() {
    }

    public static InteractionResult handleMobInteract(PolenEntity polen, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.SUCCESS;
        }

        if (polen.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!player.getItemInHand(hand).isEmpty()) {
            InteractionResult itemResult = PolenItemInteractionController.useItemOnPolen(polen, player, hand);
            if (itemResult.consumesAction()) {
                return itemResult;
            }
        }

        if (player.isShiftKeyDown()) {
            return PolenCompanionActionHandler.toggleTrustWalk(polen, player);
        }

        return PolenSocialInteractionHandler.handleEmptyHandInteraction(polen, player);
    }

    public static InteractionResult toggleTrustWalk(PolenEntity polen, Player player) {
        return PolenCompanionActionHandler.toggleTrustWalk(polen, player);
    }

    public static InteractionResult requestReturnHome(PolenEntity polen, Player player) {
        return PolenCompanionActionHandler.requestReturnHome(polen, player);
    }
}
