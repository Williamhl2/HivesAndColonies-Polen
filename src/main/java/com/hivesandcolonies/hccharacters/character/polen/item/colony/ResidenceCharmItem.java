package com.hivesandcolonies.hccharacters.character.polen.item.colony;

import com.hivesandcolonies.hccharacters.character.polen.item.base.PolenColonyItem;
import com.hivesandcolonies.hccharacters.character.polen.item.interaction.PolenItemInteractionController;
import com.hivesandcolonies.hccharacters.character.polen.item.meta.PolenProgressionStage;
import com.hivesandcolonies.hccharacters.common.item.base.TranslatableTooltipItem.TooltipLine;
import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;

public class ResidenceCharmItem extends PolenColonyItem {
    public ResidenceCharmItem(Properties properties) {
        super(
                properties.stacksTo(1).durability(12),
                PolenProgressionStage.ACT_II_DISCOVERY,
                false,
                new TooltipLine("tooltip.polen.residence_charm.line1", ChatFormatting.GREEN),
                new TooltipLine("tooltip.polen.residence_charm.line2", ChatFormatting.GRAY),
                new TooltipLine("tooltip.polen.residence_charm.line3", ChatFormatting.DARK_GRAY),
                new TooltipLine("tooltip.polen.residence_charm.line4", ChatFormatting.DARK_GRAY)
        );
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return PolenItemInteractionController.useResidenceCharmOnBlock(context);
    }
}
