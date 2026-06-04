package com.hivesandcolonies.characters.item.base;

import com.hivesandcolonies.characters.item.meta.PolenItemFamily;
import com.hivesandcolonies.characters.item.meta.PolenProgressionStage;

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
