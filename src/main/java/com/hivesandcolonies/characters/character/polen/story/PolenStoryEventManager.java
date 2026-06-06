package com.hivesandcolonies.characters.character.polen.story;

import com.hivesandcolonies.characters.character.polen.progression.PolenAdvancementManager;
import com.hivesandcolonies.characters.character.polen.progression.PolenChapterManager;
import com.hivesandcolonies.characters.character.polen.progression.PolenStoryFlag;
import com.hivesandcolonies.characters.character.polen.progression.PolenStoryFlagsManager;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public final class PolenStoryEventManager {
    private static final String UNKNOWN_GIRL_KEY = "entity.characters.unknown_girl";
    private static final String POLEN_KEY = "entity.characters.polen";

    private static final List<String> SHELTER_RECOGNITION_LINES = List.of(
            "dialogue.polen.event.shelter.line1",
            "dialogue.polen.event.shelter.line2",
            "dialogue.polen.event.shelter.line3",
            "dialogue.polen.event.shelter.line4"
    );

    private static final List<String> NAME_REVEAL_LINES = List.of(
            "dialogue.polen.event.name_reveal.line1",
            "dialogue.polen.event.name_reveal.line2",
            "dialogue.polen.event.name_reveal.line3",
            "dialogue.polen.event.name_reveal.line4"
    );

    private PolenStoryEventManager() {}

    public static void playShelterRecognition(Player player) {
        if (player.level() instanceof ServerLevel serverLevel
                && PolenStoryFlagsManager.hasFlag(serverLevel, PolenStoryFlag.PLAYER_HAS_SHELTER)) {
            return;
        }

        sendDialogueSequence(player, POLEN_KEY, ChatFormatting.LIGHT_PURPLE, SHELTER_RECOGNITION_LINES);

        if (player.level() instanceof ServerLevel serverLevel) {
            PolenStoryFlagsManager.setFlag(serverLevel, PolenStoryFlag.PLAYER_HAS_SHELTER);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            PolenAdvancementManager.grantPlayerHasShelter(serverPlayer);
        }
    }

    public static void playNameReveal(Player player) {
        sendDialogueSequence(player, UNKNOWN_GIRL_KEY, ChatFormatting.GRAY, NAME_REVEAL_LINES);
        sendDialogueLine(
                player,
                POLEN_KEY,
                ChatFormatting.LIGHT_PURPLE,
                "dialogue.polen.event.name_reveal.line5"
        );
        player.displayClientMessage(
                Component.translatable("dialogue.polen.event.name_reveal.discovered")
                        .withStyle(ChatFormatting.GOLD),
                false
        );

        completeChapter0(player);
    }

    public static void completeChapter0(Player player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            PolenStoryFlagsManager.setFlag(serverLevel, PolenStoryFlag.NAME_REVEALED);
            PolenStoryFlagsManager.setFlag(serverLevel, PolenStoryFlag.CHAPTER_0_COMPLETE);
            PolenChapterManager.advanceToChapter(serverLevel, PolenChapterManager.FOUNDATION);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            PolenAdvancementManager.grantNameReveal(serverPlayer);
            PolenAdvancementManager.grantChapter0Complete(serverPlayer);
        }
    }

    private static void sendDialogueSequence(
            Player player,
            String speakerKey,
            ChatFormatting speakerStyle,
            List<String> dialogueKeys
    ) {
        for (String dialogueKey : dialogueKeys) {
            sendDialogueLine(player, speakerKey, speakerStyle, dialogueKey);
        }
    }

    private static void sendDialogueLine(
            Player player,
            String speakerKey,
            ChatFormatting speakerStyle,
            String dialogueKey
    ) {
        player.displayClientMessage(
                Component.translatable(speakerKey)
                        .withStyle(speakerStyle)
                        .append(Component.literal(": ").withStyle(ChatFormatting.WHITE))
                        .append(Component.translatable(dialogueKey).withStyle(ChatFormatting.WHITE)),
                false
        );
    }
}
