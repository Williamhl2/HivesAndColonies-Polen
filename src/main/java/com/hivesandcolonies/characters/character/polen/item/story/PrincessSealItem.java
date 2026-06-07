package com.hivesandcolonies.characters.character.polen.item.story;

import com.hivesandcolonies.characters.character.polen.item.base.PolenLoreItem;
import com.hivesandcolonies.characters.character.polen.item.meta.PolenProgressionStage;
import com.hivesandcolonies.characters.common.item.base.TranslatableTooltipItem.TooltipLine;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item.Properties;

public class PrincessSealItem extends PolenLoreItem {
    public PrincessSealItem(Properties properties) {
        super(
                properties.stacksTo(1),
                PolenProgressionStage.PROLOGUE,
                true,
                new TooltipLine("tooltip.polen.princess_seal.line1", ChatFormatting.GOLD),
                new TooltipLine("tooltip.polen.princess_seal.line2", ChatFormatting.GRAY)
        );
    }
}
