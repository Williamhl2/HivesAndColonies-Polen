package com.hivesandcolonies.polen.item.base;

import com.hivesandcolonies.polen.item.meta.PolenItemFamily;
import com.hivesandcolonies.polen.item.meta.PolenProgressionStage;

public class PolenMaterialItem extends PolenTypedItem {
    protected PolenMaterialItem(
            Properties properties,
            PolenProgressionStage progressionStage,
            TooltipLine... tooltipLines
    ) {
        super(properties, PolenItemFamily.MATERIAL, progressionStage, false, tooltipLines);
    }
}
