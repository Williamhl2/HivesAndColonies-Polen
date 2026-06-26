package com.hivesandcolonies.hccharacters.character.polen.item.accessory;

import java.util.List;

import com.hivesandcolonies.hccharacters.character.polen.item.meta.PolenProgressionStage;
import com.hivesandcolonies.hccharacters.common.item.base.TranslatableTooltipItem.TooltipLine;

/**
 * Simple player-facing Curios accessory without active behavior yet.
 *
 * This keeps craftable accessory items registered as Polen typed items, so recipes, tags and future progression systems can
 * identify them consistently while their gameplay bonuses are introduced later.
 */
public class BasicPolenAccessoryItem extends PolenAccessoryItem {
    public BasicPolenAccessoryItem(
            Properties properties,
            PolenProgressionStage progressionStage,
            PolenAccessorySlot slot,
            PolenAccessoryTarget target,
            TooltipLine... tooltipLines
    ) {
        super(
                properties.stacksTo(1),
                progressionStage,
                slot,
                target,
                false,
                List.of(),
                tooltipLines
        );
    }
}
