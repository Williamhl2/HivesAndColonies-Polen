package com.hivesandcolonies.characters.item.interaction;

import com.hivesandcolonies.characters.dialogue.PolenDialogueManager;
import com.hivesandcolonies.characters.entity.PolenAmbientDialogueController;
import com.hivesandcolonies.characters.entity.PolenEntity;
import com.hivesandcolonies.characters.entity.ai.world.home.PolenHomeManager;
import com.hivesandcolonies.characters.entity.ai.world.home.PolenResidenceStage;
import com.hivesandcolonies.characters.entity.ai.world.home.PolenResidenceTarget;
import com.hivesandcolonies.characters.entity.ai.world.home.PolenResidenceValidation;
import com.hivesandcolonies.characters.entity.ai.world.home.PolenResidenceValidator;
import com.hivesandcolonies.characters.entity.ai.navigation.safety.PolenSafetyEvaluator;
import com.hivesandcolonies.characters.progression.PolenAffinityManager;
import com.hivesandcolonies.characters.progression.PolenStoryFlag;
import com.hivesandcolonies.characters.progression.PolenStoryFlagsManager;
import com.hivesandcolonies.characters.story.PolenMemoryManager;
import com.hivesandcolonies.characters.story.PolenMemoryType;
import com.hivesandcolonies.characters.story.PolenWorldEventTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;

public final class PolenItemInteractionController {

    private static final double POLEN_ITEM_RANGE = 10.0D;
    private static final int BLOOM_FOCUS_COOLDOWN = 50;
    private static final int SETTLEMENT_CHARM_COOLDOWN = 60;
    private static final int RESIDENCE_CHARM_COOLDOWN = 80;

    private PolenItemInteractionController() {
    }

    public static InteractionResult useBloomFocusOnBlock(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        Player player = context.getPlayer();
        if (player == null || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.FAIL;
        }

        PolenEntity polen = findNearestPolen(player, serverLevel);
        if (polen == null) {
            sendStatus(player, "message.polen.item.no_nearby_polen");
            return InteractionResult.FAIL;
        }

        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);
        ItemStack stack = context.getItemInHand();

        if (clickedState.is(BlockTags.FLOWERS) || isHive(clickedState)) {
            polen.rememberInterestingSpot(clickedPos);
            PolenAffinityManager.addAffinity(player, 1);
            spawnFocusBurst(serverLevel, clickedPos);
            spawnPolenResponse(serverLevel, polen);
            PolenAmbientDialogueController.tryPlay(
                    polen,
                    clickedState.is(BlockTags.FLOWERS)
                            ? PolenDialogueManager.AMBIENT_CURIOSITY
                            : PolenDialogueManager.AMBIENT_MAGIC
            );
            damageUtilityItem(stack);
            player.getCooldowns().addCooldown(stack.getItem(), BLOOM_FOCUS_COOLDOWN);
            sendStatus(
                    player,
                    clickedState.is(BlockTags.FLOWERS)
                            ? "message.polen.item.bloom_focus.marked_flower"
                            : "message.polen.item.bloom_focus.marked_hive"
            );
            return InteractionResult.SUCCESS;
        }

        if (isSourceLike(clickedState)) {
            polen.rememberInterestingSpot(clickedPos);
            PolenMemoryManager.unlockMemory(
                    serverLevel,
                    PolenMemoryType.FIRST_SOURCE,
                    clickedPos.getX() + 0.5D,
                    clickedPos.getY() + 0.5D,
                    clickedPos.getZ() + 0.5D
            );
            PolenAffinityManager.addAffinity(player, 1);
            spawnSourceBurst(serverLevel, clickedPos);
            spawnPolenResponse(serverLevel, polen);
            PolenAmbientDialogueController.tryPlay(polen, PolenDialogueManager.AMBIENT_MAGIC);
            damageUtilityItem(stack);
            player.getCooldowns().addCooldown(stack.getItem(), BLOOM_FOCUS_COOLDOWN);
            sendStatus(player, "message.polen.item.bloom_focus.resonated_source");
            return InteractionResult.SUCCESS;
        }

        sendStatus(player, "message.polen.item.bloom_focus.no_resonance");
        return InteractionResult.FAIL;
    }

    public static InteractionResult useSettlementCharmOnBlock(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        Player player = context.getPlayer();
        if (player == null || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.FAIL;
        }

        PolenEntity polen = findNearestPolen(player, serverLevel);
        if (polen == null) {
            sendStatus(player, "message.polen.item.no_nearby_polen");
            return InteractionResult.FAIL;
        }

        if (!PolenStoryFlagsManager.hasFlag(serverLevel, PolenStoryFlag.PLAYER_HAS_SHELTER)) {
            sendStatus(player, "message.polen.item.settlement_charm.requires_shelter");
            return InteractionResult.FAIL;
        }

        BlockPos targetPos = context.getClickedPos().relative(context.getClickedFace());
        if (!PolenSafetyEvaluator.isSafeStandingSpot(polen, targetPos)) {
            sendStatus(player, "message.polen.item.settlement_charm.spot_not_safe");
            return InteractionResult.FAIL;
        }

        polen.rememberRestingSpot(targetPos);
        if (polen.getAiState().getRestingPos() == null
                || polen.getAiState().getRestingPos().distSqr(targetPos) > 4.0D) {
            sendStatus(player, "message.polen.item.settlement_charm.spot_not_safe");
            return InteractionResult.FAIL;
        }

        PolenAffinityManager.addAffinity(player, 2);
        PolenMemoryManager.unlockMemory(
                serverLevel,
                PolenMemoryType.FIRST_COLONY,
                targetPos.getX() + 0.5D,
                targetPos.getY() + 0.5D,
                targetPos.getZ() + 0.5D
        );
        spawnRestingBurst(serverLevel, targetPos);
        spawnPolenResponse(serverLevel, polen);
        PolenAmbientDialogueController.tryPlay(polen, PolenDialogueManager.AMBIENT_APPROACH);
        damageUtilityItem(context.getItemInHand());
        player.getCooldowns().addCooldown(context.getItemInHand().getItem(), SETTLEMENT_CHARM_COOLDOWN);
        sendStatus(player, "message.polen.item.settlement_charm.marked_resting_spot");
        return InteractionResult.SUCCESS;
    }

    public static InteractionResult useResidenceCharmOnBlock(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        Player player = context.getPlayer();
        if (player == null || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.FAIL;
        }

        PolenEntity polen = findNearestPolen(player, serverLevel);
        if (polen == null) {
            sendStatus(player, "message.polen.item.no_nearby_polen");
            return InteractionResult.FAIL;
        }

        if (!PolenStoryFlagsManager.hasFlag(serverLevel, PolenStoryFlag.PLAYER_HAS_SHELTER)) {
            sendStatus(player, "message.polen.item.residence_charm.requires_shelter");
            return InteractionResult.FAIL;
        }

        BlockPos targetPos = context.getClickedPos().relative(context.getClickedFace());
        PolenResidenceValidation validation = PolenResidenceValidator.validate(polen, targetPos);
        if (!validation.isSuccess()) {
            sendStatus(player, validation.failureTranslationKey());
            return InteractionResult.FAIL;
        }

        PolenResidenceTarget residenceTarget = validation.target();
        PolenHomeManager.rememberResidence(polen, residenceTarget);
        if (PolenHomeManager.getValidResidenceUsePos(polen) == null) {
            sendStatus(player, "message.polen.item.residence_charm.invalid_place");
            return InteractionResult.FAIL;
        }

        PolenAffinityManager.addAffinity(player, 3);
        PolenWorldEventTriggers.onFirstResidenceClaimed(serverLevel, residenceTarget.usePos());
        spawnResidenceBurst(serverLevel, residenceTarget.usePos());
        spawnPolenResponse(serverLevel, polen);
        PolenAmbientDialogueController.tryPlay(polen, PolenDialogueManager.AMBIENT_REFLECTION);
        damageUtilityItem(context.getItemInHand());
        player.getCooldowns().addCooldown(context.getItemInHand().getItem(), RESIDENCE_CHARM_COOLDOWN);
        sendStatus(
                player,
                residenceTarget.stage() == PolenResidenceStage.OWN_SPACE
                        ? "message.polen.item.residence_charm.marked_own_space"
                        : "message.polen.item.residence_charm.marked_borrowed_shelter"
        );
        return InteractionResult.SUCCESS;
    }

    private static PolenEntity findNearestPolen(Player player, ServerLevel level) {
        return level.getEntitiesOfClass(
                        PolenEntity.class,
                        new AABB(player.blockPosition()).inflate(POLEN_ITEM_RANGE)
                ).stream()
                .min(Comparator.comparingDouble(polen -> polen.distanceToSqr(player)))
                .orElse(null);
    }

    private static void sendStatus(Player player, String translationKey) {
        player.displayClientMessage(Component.translatable(translationKey), true);
    }

    private static void damageUtilityItem(ItemStack stack) {
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

    private static boolean isHive(BlockState state) {
        return state.is(Blocks.BEE_NEST) || state.is(Blocks.BEEHIVE);
    }

    private static boolean isSourceLike(BlockState state) {
        return state.is(Blocks.ENCHANTING_TABLE)
                || state.is(Blocks.AMETHYST_BLOCK)
                || state.is(Blocks.BUDDING_AMETHYST)
                || state.is(Blocks.AMETHYST_CLUSTER);
    }

    private static void spawnFocusBurst(ServerLevel level, BlockPos pos) {
        level.sendParticles(
                ParticleTypes.HAPPY_VILLAGER,
                pos.getX() + 0.5D,
                pos.getY() + 0.8D,
                pos.getZ() + 0.5D,
                6,
                0.25D,
                0.18D,
                0.25D,
                0.02D
        );
        level.sendParticles(
                ParticleTypes.END_ROD,
                pos.getX() + 0.5D,
                pos.getY() + 0.8D,
                pos.getZ() + 0.5D,
                4,
                0.18D,
                0.18D,
                0.18D,
                0.01D
        );
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.45F, 1.3F);
    }

    private static void spawnSourceBurst(ServerLevel level, BlockPos pos) {
        level.sendParticles(
                ParticleTypes.ENCHANT,
                pos.getX() + 0.5D,
                pos.getY() + 0.8D,
                pos.getZ() + 0.5D,
                12,
                0.30D,
                0.25D,
                0.30D,
                0.05D
        );
        level.sendParticles(
                ParticleTypes.END_ROD,
                pos.getX() + 0.5D,
                pos.getY() + 1.0D,
                pos.getZ() + 0.5D,
                6,
                0.20D,
                0.20D,
                0.20D,
                0.02D
        );
        level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.45F, 1.15F);
    }

    private static void spawnRestingBurst(ServerLevel level, BlockPos pos) {
        level.sendParticles(
                ParticleTypes.HEART,
                pos.getX() + 0.5D,
                pos.getY() + 1.0D,
                pos.getZ() + 0.5D,
                4,
                0.18D,
                0.15D,
                0.18D,
                0.02D
        );
        level.sendParticles(
                ParticleTypes.END_ROD,
                pos.getX() + 0.5D,
                pos.getY() + 1.0D,
                pos.getZ() + 0.5D,
                3,
                0.20D,
                0.15D,
                0.20D,
                0.01D
        );
        level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.55F, 1.0F);
    }

    private static void spawnResidenceBurst(ServerLevel level, BlockPos pos) {
        level.sendParticles(
                ParticleTypes.HEART,
                pos.getX() + 0.5D,
                pos.getY() + 1.05D,
                pos.getZ() + 0.5D,
                6,
                0.22D,
                0.18D,
                0.22D,
                0.02D
        );
        level.sendParticles(
                ParticleTypes.HAPPY_VILLAGER,
                pos.getX() + 0.5D,
                pos.getY() + 0.95D,
                pos.getZ() + 0.5D,
                6,
                0.24D,
                0.18D,
                0.24D,
                0.03D
        );
        level.playSound(null, pos, SoundEvents.AMETHYST_CLUSTER_HIT, SoundSource.PLAYERS, 0.55F, 1.2F);
    }

    private static void spawnPolenResponse(ServerLevel level, PolenEntity polen) {
        level.sendParticles(
                ParticleTypes.END_ROD,
                polen.getX(),
                polen.getEyeY(),
                polen.getZ(),
                5,
                0.25D,
                0.20D,
                0.25D,
                0.01D
        );
    }
}
