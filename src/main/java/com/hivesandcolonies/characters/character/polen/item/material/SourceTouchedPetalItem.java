package com.hivesandcolonies.characters.character.polen.item.material;

import com.hivesandcolonies.characters.character.polen.item.base.PolenMaterialItem;
import com.hivesandcolonies.characters.character.polen.item.meta.PolenProgressionStage;
import com.hivesandcolonies.characters.common.item.base.TranslatableTooltipItem.TooltipLine;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item.Properties;

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
