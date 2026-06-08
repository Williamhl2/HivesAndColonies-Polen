package com.hivesandcolonies.hccharacters.character.polen.item.material;

import com.hivesandcolonies.hccharacters.character.polen.item.base.PolenMaterialItem;
import com.hivesandcolonies.hccharacters.character.polen.item.meta.PolenProgressionStage;
import com.hivesandcolonies.hccharacters.common.item.base.TranslatableTooltipItem.TooltipLine;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item.Properties;

public class ResonantWaxItem extends PolenMaterialItem {
    public ResonantWaxItem(Properties properties) {
        super(
                properties,
                PolenProgressionStage.ACT_II_DISCOVERY,
                new TooltipLine("tooltip.polen.resonant_wax.line1", ChatFormatting.GOLD),
                new TooltipLine("tooltip.polen.resonant_wax.line2", ChatFormatting.YELLOW)
        );
    }
}
