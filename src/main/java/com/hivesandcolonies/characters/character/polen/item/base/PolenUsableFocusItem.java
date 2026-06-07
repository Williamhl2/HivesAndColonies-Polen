package com.hivesandcolonies.characters.character.polen.item.base;

import com.hivesandcolonies.characters.character.polen.item.meta.PolenItemFamily;
import com.hivesandcolonies.characters.character.polen.item.meta.PolenProgressionStage;
import com.hivesandcolonies.characters.common.item.base.TranslatableTooltipItem.TooltipLine;
import net.minecraft.world.item.Item.Properties;

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
