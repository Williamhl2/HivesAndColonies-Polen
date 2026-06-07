package com.hivesandcolonies.characters.common.item.spawn;

import com.hivesandcolonies.characters.bootstrap.config.CharactersGameplayConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;

import java.util.function.Supplier;

public class SecondaryCharacterSpawnEggItem extends DeferredSpawnEggItem {
    private static final Component DISABLED_MESSAGE = Component.translatable("item.characters.secondary_spawn_egg.disabled")
            .withStyle(ChatFormatting.YELLOW);

    public SecondaryCharacterSpawnEggItem(
            Supplier<? extends EntityType<? extends Mob>> entityType,
            int backgroundColor,
            int highlightColor,
            Properties properties
    ) {
        super(entityType, backgroundColor, highlightColor, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!CharactersGameplayConfig.secondaryCharacterSpawnEggsEnabled()) {
            Player player = context.getPlayer();
            if (player != null) {
                player.displayClientMessage(DISABLED_MESSAGE, true);
            }
            return InteractionResult.FAIL;
        }

        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!CharactersGameplayConfig.secondaryCharacterSpawnEggsEnabled()) {
            player.displayClientMessage(DISABLED_MESSAGE, true);
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }

        return super.use(level, player, hand);
    }
}
