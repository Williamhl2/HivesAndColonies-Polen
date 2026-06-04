package com.hivesandcolonies.characters.item.base;

import com.hivesandcolonies.characters.item.meta.PolenItemFamily;
import com.hivesandcolonies.characters.item.meta.PolenProgressionStage;

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
