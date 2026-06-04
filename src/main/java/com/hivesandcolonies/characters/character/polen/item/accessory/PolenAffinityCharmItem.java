package com.hivesandcolonies.characters.character.polen.item.accessory;

import com.hivesandcolonies.characters.character.polen.entity.ai.world.identity.PolenAffinity;
import com.hivesandcolonies.characters.character.polen.item.meta.PolenProgressionStage;
import com.hivesandcolonies.characters.common.item.base.TranslatableTooltipItem.TooltipLine;
import net.minecraft.world.item.Item.Properties;

import java.util.List;

/**
 * Unique charm carried by Polen at the beginning of a world.
 *
 * It is not a Nest Core and it is not a craftable progression item. Its job is
 * to make the world's dominant affinity visible immediately.
 */
public class PolenAffinityCharmItem extends PolenAccessoryItem {
    private final PolenAffinity affinity;

    public PolenAffinityCharmItem(Properties properties, PolenAffinity affinity, PolenAccessoryBonus bonus, TooltipLine... tooltipLines) {
        super(
                properties,
                PolenProgressionStage.ACT_I_FOUNDATION,
                PolenAccessorySlot.CHARM,
                PolenAccessoryTarget.POLEN,
                true,
                List.of(bonus),
                tooltipLines
        );
        this.affinity = affinity;
    }

    public PolenAffinity getAffinity() {
        return affinity;
    }
}
