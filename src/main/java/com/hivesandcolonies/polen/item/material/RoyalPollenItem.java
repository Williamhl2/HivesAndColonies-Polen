package com.hivesandcolonies.polen.item.material;

import com.hivesandcolonies.polen.item.base.PolenMaterialItem;
import com.hivesandcolonies.polen.item.meta.PolenProgressionStage;
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
