package com.hivesandcolonies.polen.item;

import net.minecraft.ChatFormatting;

public class PrincessSealItem extends TranslatableTooltipItem {
    public PrincessSealItem(Properties properties) {
        super(
                properties,
                new TooltipLine("tooltip.polen.princess_seal.line1", ChatFormatting.GOLD),
                new TooltipLine("tooltip.polen.princess_seal.line2", ChatFormatting.GRAY)
        );
    }
}
