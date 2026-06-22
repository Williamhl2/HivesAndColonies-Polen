package com.hivesandcolonies.hccharacters.character.polen.item.base;

import com.hivesandcolonies.hccharacters.character.polen.item.meta.PolenItemFamily;
import com.hivesandcolonies.hccharacters.character.polen.item.meta.PolenProgressionStage;

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
