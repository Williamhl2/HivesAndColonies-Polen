package com.hivesandcolonies.characters.character.polen.item.story;

import com.hivesandcolonies.characters.character.polen.item.base.PolenLoreItem;
import com.hivesandcolonies.characters.character.polen.item.meta.PolenProgressionStage;
import com.hivesandcolonies.characters.common.item.base.TranslatableTooltipItem.TooltipLine;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item.Properties;

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
