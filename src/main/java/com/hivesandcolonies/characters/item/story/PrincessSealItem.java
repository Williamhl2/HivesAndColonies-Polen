package com.hivesandcolonies.characters.item.story;

import com.hivesandcolonies.characters.item.base.PolenLoreItem;
import com.hivesandcolonies.characters.item.meta.PolenProgressionStage;
import net.minecraft.ChatFormatting;

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
