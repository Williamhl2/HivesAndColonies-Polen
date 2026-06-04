package com.hivesandcolonies.characters.item.spawn;

import com.hivesandcolonies.characters.registry.ModEntities;
import com.hivesandcolonies.characters.world.PolenSingletonManager;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;

public class UniquePolenSpawnEggItem extends DeferredSpawnEggItem {
    private static final Component ALREADY_EXISTS_MESSAGE = Component.translatable("item.characters.polen_spawn_egg.already_exists")
            .withStyle(ChatFormatting.YELLOW);

    public UniquePolenSpawnEggItem(Properties properties) {
        super(ModEntities.POLEN, 0xF4C430, 0x7B3F98, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();

        if (level instanceof ServerLevel serverLevel && PolenSingletonManager.hasLivingPolen(serverLevel)) {
            if (player != null) {
                player.displayClientMessage(ALREADY_EXISTS_MESSAGE, true);
            }
            return InteractionResult.FAIL;
        }

        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel serverLevel && PolenSingletonManager.hasLivingPolen(serverLevel)) {
            player.displayClientMessage(ALREADY_EXISTS_MESSAGE, true);
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }

        return super.use(level, player, hand);
    }
}
