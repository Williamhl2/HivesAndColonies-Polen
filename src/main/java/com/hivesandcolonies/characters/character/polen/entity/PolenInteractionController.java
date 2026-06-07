package com.hivesandcolonies.characters.character.polen.entity;

import com.hivesandcolonies.characters.character.polen.dialogue.PolenDialogueManager;
import com.hivesandcolonies.characters.character.polen.progression.PolenAdvancementManager;
import com.hivesandcolonies.characters.character.polen.progression.PolenAffinityLevels;
import com.hivesandcolonies.characters.character.polen.progression.PolenAffinityManager;
import com.hivesandcolonies.characters.character.polen.progression.PolenChapterManager;
import com.hivesandcolonies.characters.character.polen.progression.PolenStoryFlag;
import com.hivesandcolonies.characters.character.polen.progression.PolenStoryFlagsManager;
import com.hivesandcolonies.characters.character.polen.progression.player.PolenPlayerRelationshipManager;
import com.hivesandcolonies.characters.character.polen.entity.ai.world.home.PolenShelterValidator;
import com.hivesandcolonies.characters.character.polen.story.PolenStoryEventManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public final class PolenInteractionController {

    private PolenInteractionController() {
    }

    public static InteractionResult handleMobInteract(PolenEntity polen, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.SUCCESS;
        }

        if (polen.level().isClientSide) {
            openClientProfile(polen);
            return InteractionResult.SUCCESS;
        }

        polen.refreshDisplayName();
        PolenPlayerRelationshipManager.recordInteraction(player);

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
            return InteractionResult.SUCCESS;
        }

        if (shouldRecognizeShelter(polen, player, currentChapter)) {
            PolenStoryEventManager.playShelterRecognition(player);
            polen.refreshDisplayName();
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
        return InteractionResult.SUCCESS;
    }

    private static void openClientProfile(PolenEntity polen) {
        try {
            Class<?> opener = Class.forName("com.hivesandcolonies.characters.character.polen.client.PolenClientProfileOpener");
            opener.getMethod("open", PolenEntity.class).invoke(null, polen);
        } catch (ReflectiveOperationException ignored) {
            // Client-only profile UI failed to load; keep interaction gameplay-safe.
        }
    }

    private static boolean shouldRevealName(Player player, int affinity) {
        return !PolenStoryFlagsManager.hasFlag(player, PolenStoryFlag.NAME_REVEALED)
                && affinity >= PolenAffinityLevels.NAME_REVEAL;
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
