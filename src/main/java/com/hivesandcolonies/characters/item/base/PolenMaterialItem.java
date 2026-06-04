package com.hivesandcolonies.characters.item.base;

import com.hivesandcolonies.characters.item.meta.PolenItemFamily;
import com.hivesandcolonies.characters.item.meta.PolenProgressionStage;

public class PolenMaterialItem extends PolenTypedItem {
    protected PolenMaterialItem(
            Properties properties,
            PolenProgressionStage progressionStage,
            TooltipLine... tooltipLines
    ) {
        super(properties, PolenItemFamily.MATERIAL, progressionStage, false, tooltipLines);
    }
}
