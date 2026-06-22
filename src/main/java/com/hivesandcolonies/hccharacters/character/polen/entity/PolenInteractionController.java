package com.hivesandcolonies.hccharacters.character.polen.entity;

import com.hivesandcolonies.hccharacters.character.polen.dialogue.PolenDialogueManager;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenAdvancementManager;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenAffinityLevels;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenAffinityManager;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenChapterManager;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenStoryFlag;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenStoryFlagsManager;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenRelationshipEvents;
import com.hivesandcolonies.hccharacters.character.polen.progression.world.PolenWorldStateManager;
import com.hivesandcolonies.hccharacters.character.polen.item.interaction.PolenItemInteractionController;
import com.hivesandcolonies.hccharacters.character.polen.progression.player.PolenPlayerRelationshipManager;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.core.PolenSleepController;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.environment.PolenEnvironmentResolver;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home.PolenShelterValidator;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home.PolenHomeManager;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.interests.PolenInterest;
import com.hivesandcolonies.hccharacters.common.network.ClientboundPolenProfilePayload;
import net.minecraft.core.BlockPos;
import com.hivesandcolonies.hccharacters.character.polen.story.PolenStoryEventManager;
import com.hivesandcolonies.hccharacters.character.polen.story.PolenMemoryType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Locale;

public final class PolenInteractionController {
    private static final int TRUST_WALK_DURATION_TICKS = 20 * 60 * 4;
    private static final int RETURN_HOME_REQUEST_TICKS = 20 * 45;

    private PolenInteractionController() {
    }

    public static InteractionResult handleMobInteract(PolenEntity polen, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.SUCCESS;
        }

        if (polen.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!player.getItemInHand(hand).isEmpty()) {
            InteractionResult itemResult = PolenItemInteractionController.useItemOnPolen(polen, player, hand);
            if (itemResult.consumesAction()) {
                return itemResult;
            }
        }

        if (player.isShiftKeyDown()) {
            return toggleTrustWalk(polen, player);
        }

        polen.refreshDisplayName();
        PolenPlayerRelationshipManager.recordInteraction(player);
        PolenRelationshipEvents.firstMeeting(player);

        if (player instanceof ServerPlayer serverPlayer) {
            PolenAdvancementManager.grantFirstMeeting(serverPlayer);
        }

        int currentChapter = PolenChapterManager.getCurrentChapter(player);
        int affinity = PolenAffinityManager.getAffinity(player);

        if (player instanceof ServerPlayer serverPlayer
                && affinity >= PolenAffinityLevels.FIRST_TRUST) {
            PolenAdvancementManager.grantFirstTrust(serverPlayer);
        }

        if (shouldRevealName(player, affinity)) {
            PolenStoryEventManager.playNameReveal(player);
            polen.refreshDisplayName();
            sendProfileToClient(polen, player);
            return InteractionResult.SUCCESS;
        }

        if (shouldRecognizeShelter(polen, player, currentChapter)) {
            PolenStoryEventManager.playShelterRecognition(player);
            polen.refreshDisplayName();
            sendProfileToClient(polen, player);
            return InteractionResult.SUCCESS;
        }

        player.displayClientMessage(
                PolenDialogueManager.getInteractionDialogue(
                        player,
                        polen,
                        currentChapter,
                        polen.getRandom()
                ),
                false
        );

        polen.refreshDisplayName();
        sendProfileToClient(polen, player);
        return InteractionResult.SUCCESS;
    }


    public static InteractionResult toggleTrustWalk(PolenEntity polen, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        if (polen.isTrustWalkActive()) {
            ServerPlayer activePlayer = polen.getTrustWalkPlayer();
            if (activePlayer == serverPlayer) {
                polen.stopTrustWalk();
                player.displayClientMessage(Component.translatable("message.polen.trust_walk.stopped"), true);
                return InteractionResult.SUCCESS;
            }
            if (activePlayer != null && activePlayer.isAlive()) {
                player.displayClientMessage(Component.translatable("message.polen.trust_walk.busy"), true);
                return InteractionResult.SUCCESS;
            }
            polen.stopTrustWalk();
        }

        String blockedReasonKey = getTrustWalkBlockedReason(polen, player);
        if (blockedReasonKey != null) {
            player.displayClientMessage(Component.translatable(blockedReasonKey), true);
            return InteractionResult.SUCCESS;
        }

        polen.startTrustWalk(serverPlayer, TRUST_WALK_DURATION_TICKS);
        PolenRelationshipEvents.trustWalkStarted(player);
        player.displayClientMessage(Component.translatable("message.polen.trust_walk.started"), true);
        return InteractionResult.SUCCESS;
    }

    public static InteractionResult requestReturnHome(PolenEntity polen, Player player) {
        if (polen == null || player == null) {
            return InteractionResult.PASS;
        }

        BlockPos homePos = PolenHomeManager.getHomeCenterPos(polen);
        if (homePos == null) {
            player.displayClientMessage(Component.translatable("message.polen.ui.action.no_home"), true);
            return InteractionResult.SUCCESS;
        }

        polen.requestReturnHome(RETURN_HOME_REQUEST_TICKS);
        polen.getNavigation().moveTo(homePos.getX() + 0.5D, homePos.getY(), homePos.getZ() + 0.5D, 0.9D);
        player.displayClientMessage(
                Component.translatable("message.polen.ui.action.return_home"),
                true
        );
        return InteractionResult.SUCCESS;
    }

    private static void openClientProfile(PolenEntity polen) {
        try {
            Class<?> opener = Class.forName("com.hivesandcolonies.hccharacters.character.polen.client.profile.PolenClientProfileOpener");
            opener.getMethod("open", PolenEntity.class).invoke(null, polen);
        } catch (ReflectiveOperationException ignored) {
            // Client-only profile UI failed to load; keep interaction gameplay-safe.
        }
    }

    private static void sendProfileToClient(PolenEntity polen, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || polen == null) {
            return;
        }

        String storyStageKey = "screen.polen.profile.story."
                + PolenWorldStateManager.storyStage(serverPlayer.serverLevel()).name().toLowerCase(Locale.ROOT);
        PacketDistributor.sendToPlayer(serverPlayer, new ClientboundPolenProfilePayload(
                polen.getId(),
                PolenPlayerRelationshipManager.getAffinity(serverPlayer),
                PolenPlayerRelationshipManager.getNextAffinityThreshold(serverPlayer),
                PolenPlayerRelationshipManager.getInteractionCount(serverPlayer),
                PolenChapterManager.getCurrentChapter(serverPlayer),
                PolenPlayerRelationshipManager.isTrustWalkUnlocked(serverPlayer),
                PolenPlayerRelationshipManager.hasAnyGiftCooldown(serverPlayer),
                PolenPlayerRelationshipManager.getRelationshipRankText(serverPlayer),
                storyStageKey,
                PolenWorldStateManager.interest(serverPlayer.serverLevel(), PolenInterest.BEES),
                PolenWorldStateManager.interest(serverPlayer.serverLevel(), PolenInterest.MAGIC),
                PolenWorldStateManager.interest(serverPlayer.serverLevel(), PolenInterest.COLONIES),
                PolenWorldStateManager.interest(serverPlayer.serverLevel(), PolenInterest.FOOD),
                PolenWorldStateManager.interest(serverPlayer.serverLevel(), PolenInterest.DECORATION),
                PolenWorldStateManager.interest(serverPlayer.serverLevel(), PolenInterest.EXPLORATION),
                PolenStoryFlagsManager.hasFlag(serverPlayer.serverLevel(), PolenMemoryType.FIRST_FLOWER.getFlag()),
                PolenStoryFlagsManager.hasFlag(serverPlayer.serverLevel(), PolenMemoryType.FIRST_HIVE.getFlag()),
                PolenStoryFlagsManager.hasFlag(serverPlayer.serverLevel(), PolenMemoryType.FIRST_SOURCE.getFlag()),
                PolenStoryFlagsManager.hasFlag(serverPlayer.serverLevel(), PolenMemoryType.FIRST_COLONY.getFlag()),
                PolenStoryFlagsManager.hasFlag(serverPlayer.serverLevel(), PolenMemoryType.FIRST_RESIDENCE.getFlag())
        ));
    }

    private static boolean shouldRevealName(Player player, int affinity) {
        return !PolenStoryFlagsManager.hasFlag(player, PolenStoryFlag.NAME_REVEALED)
                && affinity >= PolenAffinityLevels.NAME_REVEAL;
    }

    private static String getTrustWalkBlockedReason(PolenEntity polen, Player player) {
        int affinity = PolenAffinityManager.getAffinity(player);
        if (affinity < PolenAffinityLevels.FIRST_TRUST) {
            return "message.polen.trust_walk.not_enough_trust";
        }
        if (polen.isSleeping()) {
            return "message.polen.trust_walk.sleeping";
        }
        boolean unsafeArea = PolenEnvironmentResolver.inspect(polen).isInUnsafeArea();
        if (polen.getCurrentTask().isUrgent()
                || PolenSleepController.hasImmediateThreat(polen)
                || unsafeArea) {
            return "message.polen.trust_walk.unsafe";
        }
        return null;
    }

    private static boolean shouldRecognizeShelter(PolenEntity polen, Player player, int currentChapter) {
        if (currentChapter < PolenChapterManager.FOUNDATION
                || PolenStoryFlagsManager.hasFlag(player, PolenStoryFlag.PLAYER_HAS_SHELTER)
                || !PolenStoryFlagsManager.hasFlag(player, PolenStoryFlag.CHAPTER_0_COMPLETE)) {
            return false;
        }

        return PolenShelterValidator.findStoryShelter(polen, player.blockPosition()) != null
                || PolenShelterValidator.findStoryShelter(polen, polen.blockPosition()) != null;
    }
}
