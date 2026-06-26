package com.hivesandcolonies.hccharacters.character.polen.item.interaction;

import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;

final class PolenItemInteractionSupport {
    static final double POLEN_ITEM_RANGE = 10.0D;

    private PolenItemInteractionSupport() {
    }

    static PolenEntity findNearestPolen(Player player, ServerLevel level) {
        return level.getEntitiesOfClass(
                        PolenEntity.class,
                        new AABB(player.blockPosition()).inflate(POLEN_ITEM_RANGE)
                ).stream()
                .min(Comparator.comparingDouble(polen -> polen.distanceToSqr(player)))
                .orElse(null);
    }

    static void sendStatus(Player player, String translationKey) {
        player.displayClientMessage(Component.translatable(translationKey), true);
    }

    static void damageUtilityItem(ItemStack stack) {
        if (!stack.isDamageableItem()) {
            return;
        }

        int nextDamage = stack.getDamageValue() + 1;
        if (nextDamage >= stack.getMaxDamage()) {
            stack.shrink(1);
            return;
        }

        stack.setDamageValue(nextDamage);
    }

    static void spawnFocusBurst(ServerLevel level, BlockPos pos) {
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5D, pos.getY() + 0.8D, pos.getZ() + 0.5D, 6, 0.25D, 0.18D, 0.25D, 0.02D);
        level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5D, pos.getY() + 0.8D, pos.getZ() + 0.5D, 4, 0.18D, 0.18D, 0.18D, 0.01D);
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.45F, 1.3F);
    }

    static void spawnSourceBurst(ServerLevel level, BlockPos pos) {
        level.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5D, pos.getY() + 0.8D, pos.getZ() + 0.5D, 12, 0.30D, 0.25D, 0.30D, 0.05D);
        level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 6, 0.20D, 0.20D, 0.20D, 0.02D);
        level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.45F, 1.15F);
    }

    static void spawnRestingBurst(ServerLevel level, BlockPos pos) {
        level.sendParticles(ParticleTypes.HEART, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 4, 0.18D, 0.15D, 0.18D, 0.02D);
        level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 3, 0.20D, 0.15D, 0.20D, 0.01D);
        level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.55F, 1.0F);
    }

    static void spawnResidenceBurst(ServerLevel level, BlockPos pos) {
        level.sendParticles(ParticleTypes.HEART, pos.getX() + 0.5D, pos.getY() + 1.05D, pos.getZ() + 0.5D, 6, 0.22D, 0.18D, 0.22D, 0.02D);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5D, pos.getY() + 0.95D, pos.getZ() + 0.5D, 6, 0.24D, 0.18D, 0.24D, 0.03D);
        level.playSound(null, pos, SoundEvents.AMETHYST_CLUSTER_HIT, SoundSource.PLAYERS, 0.55F, 1.2F);
    }

    static void spawnPolenResponse(ServerLevel level, PolenEntity polen) {
        level.sendParticles(ParticleTypes.END_ROD, polen.getX(), polen.getEyeY(), polen.getZ(), 5, 0.25D, 0.20D, 0.25D, 0.01D);
    }
}
