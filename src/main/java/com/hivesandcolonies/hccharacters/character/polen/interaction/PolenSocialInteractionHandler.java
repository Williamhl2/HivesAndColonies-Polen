package com.hivesandcolonies.hccharacters.character.polen.interaction;

import com.hivesandcolonies.hccharacters.character.polen.dialogue.PolenDialogueManager;
import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home.PolenShelterValidator;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenAdvancementManager;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenAffinityLevels;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenAffinityManager;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenChapterManager;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenRelationshipEvents;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenStoryFlag;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenStoryFlagsManager;
import com.hivesandcolonies.hccharacters.character.polen.progression.player.PolenPlayerRelationshipManager;
import com.hivesandcolonies.hccharacters.character.polen.story.PolenStoryEventManager;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public final class PolenSocialInteractionHandler {
    private PolenSocialInteractionHandler() {
    }

    public static InteractionResult handleEmptyHandInteraction(PolenEntity polen, Player player) {
        polen.refreshDisplayName();
        PolenPlayerRelationshipManager.recordInteraction(player);
        PolenRelationshipEvents.firstMeeting(player);

        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            PolenAdvancementManager.grantFirstMeeting(serverPlayer);
        }

        int currentChapter = PolenChapterManager.getCurrentChapter(player);
        int affinity = PolenAffinityManager.getAffinity(player);

        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
                && affinity >= PolenAffinityLevels.FIRST_TRUST) {
            PolenAdvancementManager.grantFirstTrust(serverPlayer);
        }

        if (shouldRevealName(player, affinity)) {
            PolenStoryEventManager.playNameReveal(player);
            polen.refreshDisplayName();
            PolenProfileSync.sendToClient(polen, player);
            return InteractionResult.SUCCESS;
        }

        if (shouldRecognizeShelter(polen, player, currentChapter)) {
            PolenStoryEventManager.playShelterRecognition(player);
            polen.refreshDisplayName();
            PolenProfileSync.sendToClient(polen, player);
            return InteractionResult.SUCCESS;
        }

        player.displayClientMessage(
                PolenDialogueManager.getInteractionDialogue(player, polen, currentChapter, polen.getRandom()),
                false
        );
        polen.refreshDisplayName();
        PolenProfileSync.sendToClient(polen, player);
        return InteractionResult.SUCCESS;
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
