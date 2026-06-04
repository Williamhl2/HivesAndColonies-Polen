package com.hivesandcolonies.characters.common.item.base;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class TranslatableTooltipItem extends Item {
    private final TooltipLine[] tooltipLines;

    protected TranslatableTooltipItem(Properties properties, TooltipLine... tooltipLines) {
        super(properties);
        this.tooltipLines = tooltipLines;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        for (TooltipLine tooltipLine : tooltipLines) {
            tooltip.add(Component.translatable(tooltipLine.key()).withStyle(tooltipLine.style()));
        }
    }

    public record TooltipLine(String key, net.minecraft.ChatFormatting style) {
    }
}
