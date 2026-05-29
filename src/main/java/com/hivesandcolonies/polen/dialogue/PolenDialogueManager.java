package com.hivesandcolonies.polen.dialogue;
import com.hivesandcolonies.polen.progression.PolenAffinityManager;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;

public class PolenDialogueManager {
    private static final List<String> CHAPTER_1_DIALOGUES = List.of(
            "dialogue.polen.chapter1.line1",
            "dialogue.polen.chapter1.line2",
            "dialogue.polen.chapter1.line3",
            "dialogue.polen.chapter1.line4"
    );

    public static Component getDialogue(int chapter, RandomSource random) {
        List<String> dialogues = switch (chapter) {
            case 1 -> CHAPTER_1_DIALOGUES;
            default -> CHAPTER_1_DIALOGUES;
        };

        String key = dialogues.get(random.nextInt(dialogues.size()));
        return Component.translatable(key);
    }

    public static Component getDialogue(
        int chapter,
        int affinity,
        RandomSource random
    ) {
        return getDialogue(chapter, random);
    }
}