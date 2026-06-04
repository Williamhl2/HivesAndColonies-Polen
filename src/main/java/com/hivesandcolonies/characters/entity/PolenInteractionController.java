package com.hivesandcolonies.characters.entity;

import com.hivesandcolonies.characters.dialogue.PolenDialogueManager;
import com.hivesandcolonies.characters.progression.PolenAdvancementManager;
import com.hivesandcolonies.characters.progression.PolenAffinityLevels;
import com.hivesandcolonies.characters.progression.PolenAffinityManager;
import com.hivesandcolonies.characters.progression.PolenChapterManager;
import com.hivesandcolonies.characters.progression.PolenStoryFlag;
import com.hivesandcolonies.characters.progression.PolenStoryFlagsManager;
import com.hivesandcolonies.characters.progression.player.PolenPlayerRelationshipManager;
import com.hivesandcolonies.characters.story.PolenStoryEventManager;
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
            Class<?> opener = Class.forName("com.hivesandcolonies.characters.client.PolenClientProfileOpener");
            opener.getMethod("open", PolenEntity.class).invoke(null, polen);
        } catch (ReflectiveOperationException ignored) {
            // Client-only profile UI failed to load; keep interaction gameplay-safe.
        }
    }

    private static boolean shouldRevealName(Player player, int affinity) {
        return !PolenStoryFlagsManager.hasFlag(player, PolenStoryFlag.NAME_REVEALED)
                && affinity >= PolenAffinityLevels.NAME_REVEAL;
    }
}
