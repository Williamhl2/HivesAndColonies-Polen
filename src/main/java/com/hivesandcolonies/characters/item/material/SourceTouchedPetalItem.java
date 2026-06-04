package com.hivesandcolonies.characters.item.material;

import com.hivesandcolonies.characters.item.base.PolenMaterialItem;
import com.hivesandcolonies.characters.item.meta.PolenProgressionStage;
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
