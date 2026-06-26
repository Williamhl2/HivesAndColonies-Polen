package com.hivesandcolonies.hccharacters.character.polen.item.colony;

import com.hivesandcolonies.hccharacters.character.polen.item.base.PolenColonyItem;
import com.hivesandcolonies.hccharacters.character.polen.item.interaction.PolenItemInteractionController;
import com.hivesandcolonies.hccharacters.character.polen.item.meta.PolenProgressionStage;
import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;

public class SettlementCharmItem extends PolenColonyItem {
    public SettlementCharmItem(Properties properties) {
        super(
                properties.stacksTo(1).durability(16),
                PolenProgressionStage.ACT_I_FOUNDATION,
                false,
                new TooltipLine("tooltip.polen.settlement_charm.line1", ChatFormatting.GREEN),
                new TooltipLine("tooltip.polen.settlement_charm.line2", ChatFormatting.GRAY),
                new TooltipLine("tooltip.polen.settlement_charm.line3", ChatFormatting.DARK_GRAY)
        );
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return PolenItemInteractionController.useSettlementCharmOnBlock(context);
    }
}
