package com.hivesandcolonies.polen.item.base;

import com.hivesandcolonies.polen.item.meta.PolenItemFamily;
import com.hivesandcolonies.polen.item.meta.PolenProgressionStage;

public class PolenLoreItem extends PolenTypedItem {
    protected PolenLoreItem(
            Properties properties,
            PolenProgressionStage progressionStage,
            boolean unique,
            TooltipLine... tooltipLines
    ) {
        super(properties, PolenItemFamily.STORY, progressionStage, unique, tooltipLines);
    }
}
