package com.hivesandcolonies.polen.item;

import net.minecraft.ChatFormatting;

public class PrincessLetterItem extends TranslatableTooltipItem {
    public PrincessLetterItem(Properties properties) {
        super(
                properties,
                new TooltipLine("tooltip.polen.princess_letter.line1", ChatFormatting.GRAY),
                new TooltipLine("tooltip.polen.princess_letter.line2", ChatFormatting.GOLD),
                new TooltipLine("tooltip.polen.princess_letter.line3", ChatFormatting.YELLOW)
        );
    }
}
