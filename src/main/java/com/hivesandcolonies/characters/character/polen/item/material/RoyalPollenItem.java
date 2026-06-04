package com.hivesandcolonies.characters.character.polen.item.material;

import com.hivesandcolonies.characters.character.polen.item.base.PolenMaterialItem;
import com.hivesandcolonies.characters.character.polen.item.meta.PolenProgressionStage;
import com.hivesandcolonies.characters.common.item.base.TranslatableTooltipItem.TooltipLine;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item.Properties;

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
