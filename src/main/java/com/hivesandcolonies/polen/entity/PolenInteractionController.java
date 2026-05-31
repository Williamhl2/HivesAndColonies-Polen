package com.hivesandcolonies.polen.entity;

import com.hivesandcolonies.polen.dialogue.PolenDialogueManager;
import com.hivesandcolonies.polen.progression.PolenAdvancementManager;
import com.hivesandcolonies.polen.progression.PolenAffinityLevels;
import com.hivesandcolonies.polen.progression.PolenAffinityManager;
import com.hivesandcolonies.polen.progression.PolenChapterManager;
import com.hivesandcolonies.polen.progression.PolenStoryFlag;
import com.hivesandcolonies.polen.progression.PolenStoryFlagsManager;
import com.hivesandcolonies.polen.progression.player.PolenPlayerRelationshipManager;
import com.hivesandcolonies.polen.story.PolenStoryEventManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public final class PolenInteractionController {

    private PolenInteractionController() {
    }

    public static InteractionResult handleMobInteract(PolenEntity polen, Player player, InteractionHand hand) {
        if (polen.level().isClientSide || hand != InteractionHand.MAIN_HAND) {
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

        player.displayClientMessage(
                PolenDialogueManager.getDialogue(
                        player,
                        currentChapter,
                        polen.getRandom()
                ),
                false
        );

        polen.refreshDisplayName();
        return InteractionResult.SUCCESS;
    }

    private static boolean shouldRevealName(Player player, int affinity) {
        return !PolenStoryFlagsManager.hasFlag(player, PolenStoryFlag.NAME_REVEALED)
                && affinity >= PolenAffinityLevels.NAME_REVEAL;
    }
}
