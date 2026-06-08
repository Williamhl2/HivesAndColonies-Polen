package com.hivesandcolonies.hccharacters.character.polen.item.base;

import com.hivesandcolonies.hccharacters.character.polen.item.meta.PolenItemFamily;
import com.hivesandcolonies.hccharacters.character.polen.item.meta.PolenProgressionStage;
import com.hivesandcolonies.hccharacters.common.item.base.TranslatableTooltipItem.TooltipLine;
import net.minecraft.world.item.Item.Properties;

public class PolenMaterialItem extends PolenTypedItem {
    protected PolenMaterialItem(
            Properties properties,
            PolenProgressionStage progressionStage,
            TooltipLine... tooltipLines
    ) {
        super(properties, PolenItemFamily.MATERIAL, progressionStage, false, tooltipLines);
    }
}
