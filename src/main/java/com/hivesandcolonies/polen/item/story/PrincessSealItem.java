package com.hivesandcolonies.polen.item.story;

import com.hivesandcolonies.polen.item.base.PolenLoreItem;
import com.hivesandcolonies.polen.item.meta.PolenProgressionStage;
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
