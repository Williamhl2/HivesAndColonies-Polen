package com.hivesandcolonies.polen.dialogue;

import com.hivesandcolonies.polen.progression.PolenChapterManager;
import com.hivesandcolonies.polen.progression.PolenStoryFlag;
import com.hivesandcolonies.polen.progression.PolenStoryFlagsManager;

import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class PolenDialogueManager {

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

    public static Component getDialogue(
            Player player,
            int chapter,
            int affinity,
            RandomSource random
    ) {
        List<String> dialogues = switch (chapter) {
            case PolenChapterManager.PROLOGUE -> CHAPTER_0_DIALOGUES;
            case PolenChapterManager.FOUNDATION -> CHAPTER_1_DIALOGUES;
            default -> CHAPTER_0_DIALOGUES;
        };

        String key = dialogues.get(random.nextInt(dialogues.size()));

        return Component.translatable(getSpeakerKey(player))
                .append(Component.literal(": "))
                .append(Component.translatable(key));
    }

    private static String getSpeakerKey(Player player) {
        if (PolenStoryFlagsManager.hasFlag(player, PolenStoryFlag.NAME_REVEALED)) {
            return "entity.polen.polen";
        }

        return "entity.polen.unknown_girl";
    }
}