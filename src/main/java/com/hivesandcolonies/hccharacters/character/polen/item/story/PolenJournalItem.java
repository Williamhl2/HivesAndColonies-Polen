package com.hivesandcolonies.hccharacters.character.polen.item.story;

import com.hivesandcolonies.hccharacters.character.polen.item.base.PolenLoreItem;
import com.hivesandcolonies.hccharacters.character.polen.item.meta.PolenProgressionStage;
import net.minecraft.ChatFormatting;

public class PolenJournalItem extends PolenLoreItem {
    public PolenJournalItem(Properties properties) {
        super(
                properties.stacksTo(1),
                PolenProgressionStage.ACT_I_FOUNDATION,
                true,
                new TooltipLine("tooltip.polen.polen_journal.line1", ChatFormatting.GRAY),
                new TooltipLine("tooltip.polen.polen_journal.line2", ChatFormatting.GOLD),
                new TooltipLine("tooltip.polen.polen_journal.line3", ChatFormatting.YELLOW)
        );
    }
}
