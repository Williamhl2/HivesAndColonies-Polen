package com.hivesandcolonies.hccharacters.character.polen.item.interaction;

import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public final class PolenItemInteractionController {
    private PolenItemInteractionController() {
    }

    public static InteractionResult useItemOnPolen(PolenEntity polen, Player player, InteractionHand hand) {
        return PolenGiftInteractionHandler.useItemOnPolen(polen, player, hand);
    }

    public static InteractionResult tryBindBeeBed(Player player, Level level, BlockPos clickedPos) {
        return PolenResidenceItemInteractionHandler.tryBindBeeBed(player, level, clickedPos);
    }

    public static InteractionResult tryBindNearestBeeBed(Player player, ServerLevel level, PolenEntity polen) {
        return PolenResidenceItemInteractionHandler.tryBindNearestBeeBed(player, level, polen);
    }

    public static InteractionResult useBloomFocusOnBlock(UseOnContext context) {
        return PolenFocusItemInteractionHandler.useBloomFocusOnBlock(context);
    }

    public static InteractionResult useSettlementCharmOnBlock(UseOnContext context) {
        return PolenResidenceItemInteractionHandler.useSettlementCharmOnBlock(context);
    }

    public static InteractionResult useResidenceCharmOnBlock(UseOnContext context) {
        return PolenResidenceItemInteractionHandler.useResidenceCharmOnBlock(context);
    }
}
