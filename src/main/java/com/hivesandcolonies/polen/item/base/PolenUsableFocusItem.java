package com.hivesandcolonies.polen.item.base;

import com.hivesandcolonies.polen.item.meta.PolenItemFamily;
import com.hivesandcolonies.polen.item.meta.PolenProgressionStage;

public class PolenUsableFocusItem extends PolenTypedItem {
    protected PolenUsableFocusItem(
            Properties properties,
            PolenProgressionStage progressionStage,
            boolean unique,
            TooltipLine... tooltipLines
    ) {
        super(properties, PolenItemFamily.FOCUS, progressionStage, unique, tooltipLines);
    }
}
