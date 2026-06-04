package com.hivesandcolonies.characters.dialogue;

import com.hivesandcolonies.characters.progression.PolenChapterManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PolenChapterDialogueResolverTest {

    @Test
    void resolvesPrologueDialogueKey() {
        String key = PolenChapterDialogueResolver.resolveKeyForRoll(PolenChapterManager.PROLOGUE, 0);
        assertTrue(key.startsWith("dialogue.polen.chapter0."));
    }

    @Test
    void fallsBackToPrologueForUnknownChapter() {
        String key = PolenChapterDialogueResolver.resolveKeyForRoll(9999, 0);
        assertTrue(key.startsWith("dialogue.polen.chapter0."));
    }

    @Test
    void wrapsRollIntoAvailableDialogueList() {
        String key = PolenChapterDialogueResolver.resolveKeyForRoll(PolenChapterManager.FOUNDATION, 9);
        assertEquals("dialogue.polen.chapter1.line2", key);
    }
}
