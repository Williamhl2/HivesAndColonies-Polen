package com.hivesandcolonies.polen.dialogue;

import com.hivesandcolonies.polen.Polen;
import com.hivesandcolonies.polen.progression.PolenChapterManager;
import com.hivesandcolonies.polen.progression.PolenStoryFlag;
import com.hivesandcolonies.polen.progression.PolenStoryFlagsManager;

import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Map;

public final class PolenDialogueManager {
    private static final String UNKNOWN_GIRL_KEY = "entity." + Polen.MODID + ".unknown_girl";
    private static final String POLEN_KEY = "entity." + Polen.MODID + ".polen";

    private static final List<String> CHAPTER_0_DIALOGUES = List.of(
            "dialogue.polen.chapter0.line1",
            "dialogue.polen.chapter0.line2",
            "dialogue.polen.chapter0.line3",
            "dialogue.polen.chapter0.line4",
            "dialogue.polen.chapter0.line5"
    );

    private static final List<String> CHAPTER_1_DIALOGUES = List.of(
            "dialogue.polen.chapter1.line1",
            "dialogue.polen.chapter1.line2",
            "dialogue.polen.chapter1.line3",
            "dialogue.polen.chapter1.line4"
    );

    private static final Map<Integer, List<String>> DIALOGUES_BY_CHAPTER = Map.of(
            PolenChapterManager.PROLOGUE, CHAPTER_0_DIALOGUES,
            PolenChapterManager.FOUNDATION, CHAPTER_1_DIALOGUES
    );

    private PolenDialogueManager() {}

    public static Component getDialogue(
            Player player,
            int chapter,
            int affinity,
            RandomSource random
    ) {
        List<String> dialogues = DIALOGUES_BY_CHAPTER.getOrDefault(
                chapter,
                CHAPTER_0_DIALOGUES
        );

        String key = dialogues.get(random.nextInt(dialogues.size()));

        return Component.translatable(getSpeakerKey(player))
                .append(Component.literal(": "))
                .append(Component.translatable(key));
    }

    private static String getSpeakerKey(Player player) {
        if (PolenStoryFlagsManager.hasFlag(player, PolenStoryFlag.NAME_REVEALED)) {
            return POLEN_KEY;
        }

        return UNKNOWN_GIRL_KEY;
    }
}
