package com.hivesandcolonies.hccharacters.character.polen.item.focus;

import com.hivesandcolonies.hccharacters.character.polen.item.base.PolenUsableFocusItem;
import com.hivesandcolonies.hccharacters.character.polen.item.interaction.PolenItemInteractionController;
import com.hivesandcolonies.hccharacters.character.polen.item.meta.PolenProgressionStage;
import com.hivesandcolonies.hccharacters.common.item.base.TranslatableTooltipItem.TooltipLine;
import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;

public class BloomFocusItem extends PolenUsableFocusItem {
    public BloomFocusItem(Properties properties) {
        super(
                properties.stacksTo(1).durability(32),
                PolenProgressionStage.ACT_II_DISCOVERY,
                false,
                new TooltipLine("tooltip.polen.bloom_focus.line1", ChatFormatting.AQUA),
                new TooltipLine("tooltip.polen.bloom_focus.line2", ChatFormatting.GRAY),
                new TooltipLine("tooltip.polen.bloom_focus.line3", ChatFormatting.DARK_GRAY)
        );
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return PolenItemInteractionController.useBloomFocusOnBlock(context);
    }
}
