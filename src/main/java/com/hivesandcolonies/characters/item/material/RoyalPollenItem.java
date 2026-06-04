package com.hivesandcolonies.characters.item.material;

import com.hivesandcolonies.characters.item.base.PolenMaterialItem;
import com.hivesandcolonies.characters.item.meta.PolenProgressionStage;
import net.minecraft.ChatFormatting;

public class RoyalPollenItem extends PolenMaterialItem {
    public RoyalPollenItem(Properties properties) {
        super(
                properties,
                PolenProgressionStage.ACT_II_DISCOVERY,
                new TooltipLine("tooltip.polen.royal_pollen.line1", ChatFormatting.GOLD),
                new TooltipLine("tooltip.polen.royal_pollen.line2", ChatFormatting.YELLOW)
        );
    }
}
