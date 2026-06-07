package com.hivesandcolonies.characters.character.polen.item.accessory;

import com.hivesandcolonies.characters.character.polen.item.base.PolenTypedItem;
import com.hivesandcolonies.characters.character.polen.item.meta.PolenItemFamily;
import com.hivesandcolonies.characters.character.polen.item.meta.PolenProgressionStage;
import com.hivesandcolonies.characters.common.item.base.TranslatableTooltipItem.TooltipLine;
import net.minecraft.world.item.Item.Properties;

import java.util.List;

public class PolenAccessoryItem extends PolenTypedItem {
    private final PolenAccessorySlot slot;
    private final PolenAccessoryTarget target;
    private final List<PolenAccessoryBonus> bonuses;

    protected PolenAccessoryItem(
            Properties properties,
            PolenProgressionStage progressionStage,
            PolenAccessorySlot slot,
            PolenAccessoryTarget target,
            boolean unique,
            List<PolenAccessoryBonus> bonuses,
            TooltipLine... tooltipLines
    ) {
        super(properties, PolenItemFamily.ACCESSORY, progressionStage, unique, tooltipLines);
        this.slot = slot;
        this.target = target;
        this.bonuses = List.copyOf(bonuses);
    }

    public final PolenAccessorySlot getSlot() {
        return this.slot;
    }

    public final PolenAccessoryTarget getTarget() {
        return this.target;
    }

    public final List<PolenAccessoryBonus> getBonuses() {
        return this.bonuses;
    }
}
