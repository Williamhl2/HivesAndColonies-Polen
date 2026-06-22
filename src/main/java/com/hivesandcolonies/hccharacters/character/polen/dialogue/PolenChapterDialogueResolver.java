package com.hivesandcolonies.hccharacters.character.polen.dialogue;

import com.hivesandcolonies.hccharacters.character.polen.progression.PolenChapterManager;
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

    public static String resolveKey(int chapter, String lastDialogueKey, RandomSource random) {
        List<String> dialogues = DIALOGUES_BY_CHAPTER.getOrDefault(chapter, CHAPTER_0_DIALOGUES);
        int roll = random.nextInt(dialogues.size());
        String key = resolveKeyForRoll(chapter, roll);
        if (dialogues.size() > 1 && key.equals(lastDialogueKey)) {
            key = resolveKeyForRoll(chapter, roll + 1);
        }
        return key;
    }

    public static String resolveKeyForRoll(int chapter, int roll) {
        List<String> dialogues = DIALOGUES_BY_CHAPTER.getOrDefault(chapter, CHAPTER_0_DIALOGUES);
        return dialogues.get(Math.floorMod(roll, dialogues.size()));
    }
}
