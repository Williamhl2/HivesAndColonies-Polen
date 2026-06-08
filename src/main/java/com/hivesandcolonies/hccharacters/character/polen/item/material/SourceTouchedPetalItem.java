package com.hivesandcolonies.hccharacters.character.polen.item.material;

import com.hivesandcolonies.hccharacters.character.polen.item.base.PolenMaterialItem;
import com.hivesandcolonies.hccharacters.character.polen.item.meta.PolenProgressionStage;
import com.hivesandcolonies.hccharacters.common.item.base.TranslatableTooltipItem.TooltipLine;
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
