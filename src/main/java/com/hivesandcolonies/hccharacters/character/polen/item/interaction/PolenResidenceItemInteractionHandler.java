package com.hivesandcolonies.hccharacters.character.polen.item.interaction;

import com.hivesandcolonies.hccharacters.character.polen.block.PolenBeeBedBlock;
import com.hivesandcolonies.hccharacters.character.polen.dialogue.PolenDialogueManager;
import com.hivesandcolonies.hccharacters.character.polen.entity.PolenAmbientDialogueController;
import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.safety.PolenSafetyEvaluator;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home.PolenBedLocator;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home.PolenHomeManager;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home.PolenResidenceStage;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home.PolenResidenceTarget;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home.PolenResidenceValidation;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home.PolenResidenceValidator;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.interests.PolenInterest;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenRelationshipEvents;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenStoryFlag;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenStoryFlagsManager;
import com.hivesandcolonies.hccharacters.character.polen.progression.world.PolenWorldStateManager;
import com.hivesandcolonies.hccharacters.character.polen.story.PolenWorldEventTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

final class PolenResidenceItemInteractionHandler {
    private static final int SETTLEMENT_CHARM_COOLDOWN = 60;
    private static final int RESIDENCE_CHARM_COOLDOWN = 80;

    private PolenResidenceItemInteractionHandler() {
    }

    static InteractionResult tryBindBeeBed(Player player, Level level, BlockPos clickedPos) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.FAIL;
        }

        PolenEntity polen = PolenItemInteractionSupport.findNearestPolen(player, serverLevel);
        if (polen == null) {
            PolenItemInteractionSupport.sendStatus(player, "message.polen.item.no_nearby_polen");
            return InteractionResult.FAIL;
        }

        BlockState state = level.getBlockState(clickedPos);
        if (!(state.getBlock() instanceof PolenBeeBedBlock)) {
            return InteractionResult.FAIL;
        }

        return bindBeeBed(player, level, polen, clickedPos);
    }

    static InteractionResult tryBindNearestBeeBed(Player player, ServerLevel level, PolenEntity polen) {
        if (player == null || level == null || polen == null) {
            return InteractionResult.FAIL;
        }

        PolenResidenceTarget target = PolenHomeManager.findNearbyBeeBedResidence(polen, polen.blockPosition(), 8, 3);
        if (target == null) {
            PolenItemInteractionSupport.sendStatus(player, "message.polen.ui.action.no_nearby_bed");
            return InteractionResult.FAIL;
        }
        return bindBeeBed(player, level, polen, target.anchorPos());
    }

    static InteractionResult useSettlementCharmOnBlock(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        Player player = context.getPlayer();
        if (player == null || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.FAIL;
        }

        PolenEntity polen = PolenItemInteractionSupport.findNearestPolen(player, serverLevel);
        if (polen == null) {
            PolenItemInteractionSupport.sendStatus(player, "message.polen.item.no_nearby_polen");
            return InteractionResult.FAIL;
        }

        if (!PolenStoryFlagsManager.hasFlag(serverLevel, PolenStoryFlag.PLAYER_HAS_SHELTER)) {
            PolenItemInteractionSupport.sendStatus(player, "message.polen.item.settlement_charm.requires_shelter");
            return InteractionResult.FAIL;
        }

        BlockPos targetPos = context.getClickedPos().relative(context.getClickedFace());
        if (!PolenSafetyEvaluator.isSafeStandingSpot(polen, targetPos)) {
            PolenItemInteractionSupport.sendStatus(player, "message.polen.item.settlement_charm.spot_not_safe");
            return InteractionResult.FAIL;
        }

        polen.rememberRestingSpot(targetPos);
        if (polen.getAiState().getRestingPos() == null
                || polen.getAiState().getRestingPos().distSqr(targetPos) > 4.0D) {
            PolenItemInteractionSupport.sendStatus(player, "message.polen.item.settlement_charm.spot_not_safe");
            return InteractionResult.FAIL;
        }

        PolenRelationshipEvents.restingMarker(player);
        PolenWorldStateManager.adjustInterest(serverLevel, PolenInterest.COLONIES, 3);
        PolenWorldEventTriggers.onFirstColonyFounded(serverLevel, targetPos);
        PolenItemInteractionSupport.spawnRestingBurst(serverLevel, targetPos);
        PolenItemInteractionSupport.spawnPolenResponse(serverLevel, polen);
        PolenAmbientDialogueController.tryPlay(polen, PolenDialogueManager.AMBIENT_APPROACH);
        PolenItemInteractionSupport.damageUtilityItem(context.getItemInHand());
        player.getCooldowns().addCooldown(context.getItemInHand().getItem(), SETTLEMENT_CHARM_COOLDOWN);
        PolenItemInteractionSupport.sendStatus(player, "message.polen.item.settlement_charm.marked_resting_spot");
        return InteractionResult.SUCCESS;
    }

    static InteractionResult useResidenceCharmOnBlock(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        Player player = context.getPlayer();
        if (player == null || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.FAIL;
        }

        PolenEntity polen = PolenItemInteractionSupport.findNearestPolen(player, serverLevel);
        if (polen == null) {
            PolenItemInteractionSupport.sendStatus(player, "message.polen.item.no_nearby_polen");
            return InteractionResult.FAIL;
        }

        BlockState clickedState = level.getBlockState(context.getClickedPos());
        if (clickedState.getBlock() instanceof PolenBeeBedBlock) {
            InteractionResult result = tryBindBeeBed(player, level, context.getClickedPos());
            if (result.consumesAction()) {
                PolenItemInteractionSupport.damageUtilityItem(context.getItemInHand());
                player.getCooldowns().addCooldown(context.getItemInHand().getItem(), RESIDENCE_CHARM_COOLDOWN);
            }
            return result;
        }

        if (!PolenStoryFlagsManager.hasFlag(serverLevel, PolenStoryFlag.PLAYER_HAS_SHELTER)) {
            PolenItemInteractionSupport.sendStatus(player, "message.polen.item.residence_charm.requires_shelter");
            return InteractionResult.FAIL;
        }

        BlockPos targetPos = context.getClickedPos().relative(context.getClickedFace());
        PolenResidenceValidation validation = PolenResidenceValidator.validate(polen, targetPos);
        if (!validation.isSuccess()) {
            PolenItemInteractionSupport.sendStatus(player, validation.failureTranslationKey());
            return InteractionResult.FAIL;
        }

        PolenResidenceTarget residenceTarget = validation.target();
        PolenHomeManager.rememberResidence(polen, residenceTarget);
        if (PolenHomeManager.getValidResidenceUsePos(polen) == null) {
            PolenItemInteractionSupport.sendStatus(player, "message.polen.item.residence_charm.invalid_place");
            return InteractionResult.FAIL;
        }

        PolenRelationshipEvents.homeAssigned(player);
        PolenWorldStateManager.adjustInterest(serverLevel, PolenInterest.COLONIES, 4);
        PolenWorldEventTriggers.onFirstResidenceClaimed(serverLevel, residenceTarget.usePos());
        PolenItemInteractionSupport.spawnResidenceBurst(serverLevel, residenceTarget.usePos());
        PolenItemInteractionSupport.spawnPolenResponse(serverLevel, polen);
        PolenAmbientDialogueController.tryPlay(polen, PolenDialogueManager.AMBIENT_REFLECTION);
        PolenItemInteractionSupport.damageUtilityItem(context.getItemInHand());
        player.getCooldowns().addCooldown(context.getItemInHand().getItem(), RESIDENCE_CHARM_COOLDOWN);
        PolenItemInteractionSupport.sendStatus(
                player,
                residenceTarget.stage() == PolenResidenceStage.OWN_SPACE
                        ? "message.polen.item.residence_charm.marked_own_space"
                        : "message.polen.item.residence_charm.marked_borrowed_shelter"
        );
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult bindBeeBed(Player player, Level level, PolenEntity polen, BlockPos clickedPos) {
        BlockState state = level.getBlockState(clickedPos);
        if (!(state.getBlock() instanceof PolenBeeBedBlock)) {
            return InteractionResult.FAIL;
        }

        BlockPos footPos = PolenBeeBedBlock.getFootPos(state, clickedPos).immutable();
        BlockPos usePos = PolenBedLocator.findBestBedAccessPos(polen, footPos);
        if (usePos == null) {
            PolenItemInteractionSupport.sendStatus(player, "message.polen.bee_bed.bind.no_access");
            return InteractionResult.FAIL;
        }

        PolenResidenceTarget target = new PolenResidenceTarget(
                footPos,
                usePos.immutable(),
                "bee_bed",
                PolenResidenceStage.OWN_SPACE
        );
        PolenHomeManager.rememberResidence(polen, target);
        if (level instanceof ServerLevel serverLevel) {
            PolenWorldStateManager.rememberPolenHomeBed(serverLevel, footPos);
            PolenWorldStateManager.adjustInterest(serverLevel, PolenInterest.COLONIES, 4);
            PolenRelationshipEvents.homeAssigned(player);
            PolenItemInteractionSupport.spawnResidenceBurst(serverLevel, usePos);
            PolenItemInteractionSupport.spawnPolenResponse(serverLevel, polen);
            PolenAmbientDialogueController.tryPlay(polen, PolenDialogueManager.AMBIENT_REFLECTION);
        }
        PolenItemInteractionSupport.sendStatus(player, "message.polen.bee_bed.bind.success");
        return InteractionResult.SUCCESS;
    }
}
