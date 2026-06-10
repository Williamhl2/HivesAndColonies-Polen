package com.hivesandcolonies.hccharacters.character.polen.item.interaction;

import com.hivesandcolonies.hccharacters.bootstrap.registry.ModItems;
import com.hivesandcolonies.hccharacters.character.polen.block.PolenBeeBedBlock;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.interests.PolenInterest;
import com.hivesandcolonies.hccharacters.character.polen.item.meta.PolenItemTags;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenRelationshipEvents;
import com.hivesandcolonies.hccharacters.character.polen.progression.world.PolenWorldStateManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.FlowerBlock;
import com.hivesandcolonies.hccharacters.character.polen.dialogue.PolenDialogueManager;
import com.hivesandcolonies.hccharacters.character.polen.entity.PolenAmbientDialogueController;
import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home.PolenHomeManager;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home.PolenResidenceStage;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home.PolenResidenceTarget;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home.PolenResidenceValidation;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home.PolenResidenceValidator;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.safety.PolenSafetyEvaluator;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenAffinityManager;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenStoryFlag;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenStoryFlagsManager;
import com.hivesandcolonies.hccharacters.character.polen.story.PolenWorldEventTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.Block;
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


    public static InteractionResult useItemOnPolen(PolenEntity polen, Player player, InteractionHand hand) {
        if (polen == null || player == null || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) {
            return InteractionResult.PASS;
        }

        GiftResult gift = classifyGift(stack);
        if (gift == null) {
            return InteractionResult.PASS;
        }

        if (!PolenRelationshipEvents.gift(player, gift.category(), gift.affinity(), gift.reasonText())) {
            sendStatus(player, "message.polen.gift.cooldown");
            return InteractionResult.SUCCESS;
        }

        PolenWorldStateManager.adjustInterest(serverPlayer.serverLevel(), gift.interest(), gift.interestAmount());
        spawnPolenResponse(serverPlayer.serverLevel(), polen);
        PolenAmbientDialogueController.tryPlay(polen, gift.dialogueCategory());

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        player.getCooldowns().addCooldown(stack.getItem(), 40);
        sendStatus(player, gift.messageKey());
        return InteractionResult.SUCCESS;
    }

    public static InteractionResult tryBindBeeBed(Player player, Level level, BlockPos clickedPos) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.FAIL;
        }

        PolenEntity polen = findNearestPolen(player, serverLevel);
        if (polen == null) {
            sendStatus(player, "message.polen.item.no_nearby_polen");
            return InteractionResult.FAIL;
        }

        BlockState state = level.getBlockState(clickedPos);
        if (!(state.getBlock() instanceof PolenBeeBedBlock)) {
            return InteractionResult.FAIL;
        }

        BlockPos footPos = PolenBeeBedBlock.getFootPos(state, clickedPos).immutable();
        BlockPos usePos = findSafeBedAccessPos(polen, footPos);
        if (usePos == null) {
            sendStatus(player, "message.polen.bee_bed.bind.no_access");
            return InteractionResult.FAIL;
        }

        PolenResidenceTarget target = new PolenResidenceTarget(
                footPos,
                usePos.immutable(),
                "bee_bed",
                PolenResidenceStage.OWN_SPACE
        );
        PolenHomeManager.rememberResidence(polen, target);
        PolenWorldStateManager.rememberPolenHomeBed(serverLevel, footPos);
        PolenRelationshipEvents.homeAssigned(player);
        spawnResidenceBurst(serverLevel, usePos);
        spawnPolenResponse(serverLevel, polen);
        PolenAmbientDialogueController.tryPlay(polen, PolenDialogueManager.AMBIENT_REFLECTION);
        sendStatus(player, "message.polen.bee_bed.bind.success");
        return InteractionResult.SUCCESS;
    }

    private static BlockPos findSafeBedAccessPos(PolenEntity polen, BlockPos footPos) {
        BlockState state = polen.level().getBlockState(footPos);
        if (!(state.getBlock() instanceof PolenBeeBedBlock)) {
            return null;
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos candidate = footPos.relative(direction);
            if (PolenSafetyEvaluator.isSafeStandingSpot(polen, candidate)) {
                return candidate;
            }
        }

        BlockPos headPos = PolenBeeBedBlock.getHeadPos(state, footPos);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos candidate = headPos.relative(direction);
            if (PolenSafetyEvaluator.isSafeStandingSpot(polen, candidate)) {
                return candidate;
            }
        }

        return PolenSafetyEvaluator.isSafeStandingSpot(polen, footPos) ? footPos : null;
    }

    private static GiftResult classifyGift(ItemStack stack) {
        Item item = stack.getItem();
        if (stack.is(PolenItemTags.POLEN_GIFTS_BEES)
                || item == Items.HONEYCOMB
                || item == Items.HONEY_BOTTLE
                || item == ModItems.RESONANT_WAX.get()) {
            return new GiftResult(
                    "bees",
                    2,
                    PolenInterest.BEES,
                    5,
                    "message.polen.gift.accepted_bees",
                    "Polen recognizes something gentle in this bee gift.",
                    PolenDialogueManager.AMBIENT_CURIOSITY
            );
        }

        if (stack.is(PolenItemTags.POLEN_GIFTS_NATURE) || isFlowerLike(stack)) {
            return new GiftResult(
                    "nature",
                    1,
                    PolenInterest.EXPLORATION,
                    3,
                    "message.polen.gift.accepted_nature",
                    "Polen studies the gift as if it might remember her too.",
                    PolenDialogueManager.AMBIENT_CURIOSITY
            );
        }

        if (stack.is(PolenItemTags.POLEN_GIFTS_SOURCE)
                || item == Items.AMETHYST_SHARD
                || item == ModItems.SOURCE_TOUCHED_PETAL.get()) {
            return new GiftResult(
                    "source",
                    2,
                    PolenInterest.MAGIC,
                    5,
                    "message.polen.gift.accepted_source",
                    "A small thread of Source answers the gift.",
                    PolenDialogueManager.AMBIENT_MAGIC
            );
        }

        if (stack.is(PolenItemTags.POLEN_GIFTS_FOOD)
                || item == Items.BREAD
                || item == Items.SWEET_BERRIES
                || item == Items.GLOW_BERRIES) {
            return new GiftResult(
                    "food",
                    1,
                    PolenInterest.FOOD,
                    4,
                    "message.polen.gift.accepted_food",
                    "Polen accepts the food quietly.",
                    PolenDialogueManager.AMBIENT_APPROACH
            );
        }

        if (stack.is(PolenItemTags.POLEN_GIFTS_HOME)
                || item == Items.LANTERN
                || item == Items.CANDLE
                || item == ModItems.POLEN_LANTERN.get()) {
            return new GiftResult(
                    "home",
                    2,
                    PolenInterest.DECORATION,
                    4,
                    "message.polen.gift.accepted_home",
                    "Polen seems calmer around things meant to make a place safe.",
                    PolenDialogueManager.AMBIENT_REFLECTION
            );
        }

        return null;
    }

    private static boolean isFlowerLike(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }
        Block block = blockItem.getBlock();
        return block instanceof FlowerBlock || block.defaultBlockState().is(BlockTags.FLOWERS);
    }

    private record GiftResult(
            String category,
            int affinity,
            PolenInterest interest,
            int interestAmount,
            String messageKey,
            String reasonText,
            String dialogueCategory
    ) {
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
            PolenRelationshipEvents.focus(
                    player,
                    clickedState.is(BlockTags.FLOWERS) ? "nature" : "bees",
                    1,
                    clickedState.is(BlockTags.FLOWERS)
                            ? "Polen notices that you pointed her toward something alive."
                            : "Polen recognizes the quiet rhythm around a hive."
            );
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
            PolenWorldEventTriggers.onFirstSourceDiscovered(serverLevel, clickedPos);
            PolenRelationshipEvents.focus(player, "source", 1, "A faint thread of Source answers near Polen.");
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

        PolenRelationshipEvents.restingMarker(player);
        PolenWorldEventTriggers.onFirstColonyFounded(serverLevel, targetPos);
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

        BlockState clickedState = level.getBlockState(context.getClickedPos());
        if (clickedState.getBlock() instanceof PolenBeeBedBlock) {
            InteractionResult result = tryBindBeeBed(player, level, context.getClickedPos());
            if (result.consumesAction()) {
                damageUtilityItem(context.getItemInHand());
                player.getCooldowns().addCooldown(context.getItemInHand().getItem(), RESIDENCE_CHARM_COOLDOWN);
            }
            return result;
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

        PolenRelationshipEvents.homeAssigned(player);
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
