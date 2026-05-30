package com.hivesandcolonies.polen.item;

import net.minecraft.ChatFormatting;

public class PolenJournalItem extends TranslatableTooltipItem {
    public PolenJournalItem(Properties properties) {
        super(
                properties,
                new TooltipLine("tooltip.polen.polen_journal.line1", ChatFormatting.GRAY),
                new TooltipLine("tooltip.polen.polen_journal.line2", ChatFormatting.GOLD),
                new TooltipLine("tooltip.polen.polen_journal.line3", ChatFormatting.YELLOW)
        );
    }
}
