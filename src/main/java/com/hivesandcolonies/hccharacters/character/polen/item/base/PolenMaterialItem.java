package com.hivesandcolonies.hccharacters.character.polen.item.base;

import com.hivesandcolonies.hccharacters.character.polen.item.meta.PolenItemFamily;
import com.hivesandcolonies.hccharacters.character.polen.item.meta.PolenProgressionStage;

public class PolenMaterialItem extends PolenTypedItem {
    protected PolenMaterialItem(
            Properties properties,
            PolenProgressionStage progressionStage,
            TooltipLine... tooltipLines
    ) {
        super(properties, PolenItemFamily.MATERIAL, progressionStage, false, tooltipLines);
    }
}
