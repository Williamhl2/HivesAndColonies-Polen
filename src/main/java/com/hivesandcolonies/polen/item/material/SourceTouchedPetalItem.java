package com.hivesandcolonies.polen.item.material;

import com.hivesandcolonies.polen.item.base.PolenMaterialItem;
import com.hivesandcolonies.polen.item.meta.PolenProgressionStage;
import net.minecraft.ChatFormatting;

public class SourceTouchedPetalItem extends PolenMaterialItem {
    public SourceTouchedPetalItem(Properties properties) {
        super(
                properties,
                PolenProgressionStage.ACT_II_DISCOVERY,
                new TooltipLine("tooltip.polen.source_touched_petal.line1", ChatFormatting.LIGHT_PURPLE),
                new TooltipLine("tooltip.polen.source_touched_petal.line2", ChatFormatting.GRAY)
        );
    }
}
