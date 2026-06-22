package com.hivesandcolonies.hccharacters.character.polen.item.story;

import com.hivesandcolonies.hccharacters.character.polen.item.base.PolenLoreItem;
import com.hivesandcolonies.hccharacters.character.polen.item.meta.PolenProgressionStage;
import net.minecraft.ChatFormatting;

public class PrincessSealItem extends PolenLoreItem {
    public PrincessSealItem(Properties properties) {
        super(
                properties.stacksTo(1),
                PolenProgressionStage.PROLOGUE,
                true,
                new TooltipLine("tooltip.polen.princess_seal.line1", ChatFormatting.GOLD),
                new TooltipLine("tooltip.polen.princess_seal.line2", ChatFormatting.GRAY)
        );
    }
}
