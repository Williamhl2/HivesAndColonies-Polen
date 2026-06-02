package com.hivesandcolonies.polen.item.accessory;

import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.item.base.PolenTypedItem;
import com.hivesandcolonies.polen.item.meta.PolenItemFamily;
import com.hivesandcolonies.polen.item.meta.PolenProgressionStage;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class PolenAccessoryItem extends PolenTypedItem {
    private final PolenAccessorySlot slot;
    private final PolenAccessoryTarget target;
    private final List<PolenAccessoryBonus> bonuses;

    public PolenAccessoryItem(
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

    @Override
    public InteractionResult interactLivingEntity(
            ItemStack stack,
            Player player,
            LivingEntity interactionTarget,
            InteractionHand usedHand
    ) {
        if (!(interactionTarget instanceof PolenEntity polen)) {
            return super.interactLivingEntity(stack, player, interactionTarget, usedHand);
        }

        if (this.target == PolenAccessoryTarget.PLAYER) {
            player.displayClientMessage(Component.translatable("message.polen.accessory.player_only"), true);
            return InteractionResult.FAIL;
        }

        if (interactionTarget.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(this);
        if (!polen.equipAccessory(this.slot, itemId)) {
            return InteractionResult.FAIL;
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        player.displayClientMessage(Component.translatable("message.polen.accessory.equipped"), true);
        return InteractionResult.SUCCESS;
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

    public boolean canBeEquippedByPolen() {
        return this.target == PolenAccessoryTarget.POLEN || this.target == PolenAccessoryTarget.BOTH;
    }

    public boolean canBeEquippedByPlayer() {
        return this.target == PolenAccessoryTarget.PLAYER || this.target == PolenAccessoryTarget.BOTH;
    }
}
