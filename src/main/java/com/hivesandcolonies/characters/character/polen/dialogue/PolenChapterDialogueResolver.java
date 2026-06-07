package com.hivesandcolonies.characters.character.polen.dialogue;

import com.hivesandcolonies.characters.character.polen.progression.PolenChapterManager;
import net.minecraft.util.RandomSource;

import java.util.List;
import java.util.Map;

public final class PolenChapterDialogueResolver {

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
            "dialogue.polen.chapter1.line4",
            "dialogue.polen.chapter1.line5",
            "dialogue.polen.chapter1.line6",
            "dialogue.polen.chapter1.line7"
    );

    private static final Map<Integer, List<String>> DIALOGUES_BY_CHAPTER = Map.of(
            PolenChapterManager.PROLOGUE, CHAPTER_0_DIALOGUES,
            PolenChapterManager.FOUNDATION, CHAPTER_1_DIALOGUES
    );

    private PolenChapterDialogueResolver() {
    }

    public static String resolveKey(int chapter, RandomSource random) {
        List<String> dialogues = DIALOGUES_BY_CHAPTER.getOrDefault(chapter, CHAPTER_0_DIALOGUES);
        return resolveKeyForRoll(chapter, random.nextInt(dialogues.size()));
    }

    public static String resolveKeyForRoll(int chapter, int roll) {
        List<String> dialogues = DIALOGUES_BY_CHAPTER.getOrDefault(chapter, CHAPTER_0_DIALOGUES);
        return dialogues.get(Math.floorMod(roll, dialogues.size()));
    }
}
