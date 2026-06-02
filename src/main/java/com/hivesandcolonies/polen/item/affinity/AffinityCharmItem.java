package com.hivesandcolonies.polen.item.affinity;

import com.hivesandcolonies.polen.entity.ai.world.identity.PolenWorldAffinity;
import com.hivesandcolonies.polen.item.accessory.PolenAccessoryBonus;
import com.hivesandcolonies.polen.item.accessory.PolenAccessoryItem;
import com.hivesandcolonies.polen.item.accessory.PolenAccessorySlot;
import com.hivesandcolonies.polen.item.accessory.PolenAccessoryTarget;
import com.hivesandcolonies.polen.item.meta.PolenProgressionStage;
import net.minecraft.ChatFormatting;

import java.util.List;

public class AffinityCharmItem extends PolenAccessoryItem {
    private final PolenWorldAffinity affinity;

    public AffinityCharmItem(Properties properties, PolenWorldAffinity affinity) {
        super(
                properties.stacksTo(1),
                PolenProgressionStage.PROLOGUE,
                PolenAccessorySlot.CHARM,
                PolenAccessoryTarget.POLEN,
                true,
                List.<PolenAccessoryBonus>of(),
                new TooltipLine("tooltip.polen." + affinity.getSerializedName() + "_charm.line1", ChatFormatting.GRAY),
                new TooltipLine("tooltip.polen." + affinity.getSerializedName() + "_charm.line2", ChatFormatting.DARK_GRAY)
        );
        this.affinity = affinity;
    }

    public PolenWorldAffinity getAffinity() {
        return this.affinity;
    }
}
