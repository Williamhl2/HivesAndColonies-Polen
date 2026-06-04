package com.hivesandcolonies.characters.character.polen.item.material;

import com.hivesandcolonies.characters.character.polen.item.base.PolenMaterialItem;
import com.hivesandcolonies.characters.character.polen.item.meta.PolenProgressionStage;
import com.hivesandcolonies.characters.common.item.base.TranslatableTooltipItem.TooltipLine;
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
