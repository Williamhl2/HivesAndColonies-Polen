package com.hivesandcolonies.polen.item.base;

import com.hivesandcolonies.polen.item.meta.PolenItemFamily;
import com.hivesandcolonies.polen.item.meta.PolenProgressionStage;

public abstract class PolenTypedItem extends TranslatableTooltipItem {
    private final PolenItemFamily family;
    private final PolenProgressionStage progressionStage;
    private final boolean unique;

    protected PolenTypedItem(
            Properties properties,
            PolenItemFamily family,
            PolenProgressionStage progressionStage,
            boolean unique,
            TooltipLine... tooltipLines
    ) {
        super(properties, tooltipLines);
        this.family = family;
        this.progressionStage = progressionStage;
        this.unique = unique;
    }

    public final PolenItemFamily getFamily() {
        return this.family;
    }

    public final PolenProgressionStage getProgressionStage() {
        return this.progressionStage;
    }

    public final boolean isUnique() {
        return this.unique;
    }
}
