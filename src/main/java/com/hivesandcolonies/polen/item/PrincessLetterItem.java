package com.hivesandcolonies.polen.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class PrincessLetterItem extends Item {
    public PrincessLetterItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.polen.princess_letter.line1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.polen.princess_letter.line2").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.polen.princess_letter.line3").withStyle(ChatFormatting.YELLOW));
    }
}