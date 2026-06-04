package com.hivesandcolonies.characters.item.base;

import com.hivesandcolonies.characters.item.meta.PolenItemFamily;
import com.hivesandcolonies.characters.item.meta.PolenProgressionStage;

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
