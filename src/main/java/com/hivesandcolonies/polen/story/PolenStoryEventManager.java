package com.hivesandcolonies.polen.story;

import com.hivesandcolonies.polen.progression.PolenAdvancementManager;
import com.hivesandcolonies.polen.progression.PolenChapterManager;
import com.hivesandcolonies.polen.progression.PolenStoryFlag;
import com.hivesandcolonies.polen.progression.PolenStoryFlagsManager;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class PolenStoryEventManager {

    private PolenStoryEventManager() {}

    public static void playShelterRecognition(Player player) {
        sendPolenLine(player, "dialogue.polen.event.shelter.line1");
        sendPolenLine(player, "dialogue.polen.event.shelter.line2");
        sendPolenLine(player, "dialogue.polen.event.shelter.line3");
        sendPolenLine(player, "dialogue.polen.event.shelter.line4");

        PolenStoryFlagsManager.setFlag(
                player,
                PolenStoryFlag.PLAYER_HAS_SHELTER
        );

        if (player instanceof ServerPlayer serverPlayer) {
            PolenAdvancementManager.grantPlayerHasShelter(serverPlayer);
        }
    }

    public static void completeChapter0(Player player) {
        PolenStoryFlagsManager.setFlag(
                player,
                PolenStoryFlag.CHAPTER_0_COMPLETE
        );

        PolenChapterManager.advanceToChapter(
                player,
                PolenChapterManager.FOUNDATION
        );
    }

    private static void sendPolenLine(Player player, String dialogueKey) {
        player.displayClientMessage(
                Component.translatable("entity.polen.polen")
                        .withStyle(ChatFormatting.LIGHT_PURPLE)
                        .append(Component.literal(": ").withStyle(ChatFormatting.WHITE))
                        .append(Component.translatable(dialogueKey).withStyle(ChatFormatting.WHITE)),
                false
        );
    }
}