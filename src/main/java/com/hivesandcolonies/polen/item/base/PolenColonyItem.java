package com.hivesandcolonies.polen.item.base;

import com.hivesandcolonies.polen.item.meta.PolenItemFamily;
import com.hivesandcolonies.polen.item.meta.PolenProgressionStage;

public class PolenColonyItem extends PolenTypedItem {
    protected PolenColonyItem(
            Properties properties,
            PolenProgressionStage progressionStage,
            boolean unique,
            TooltipLine... tooltipLines
    ) {
        super(properties, PolenItemFamily.COLONY, progressionStage, unique, tooltipLines);
    }
}
