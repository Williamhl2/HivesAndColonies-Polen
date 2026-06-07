package com.hivesandcolonies.characters.character.polen.story;

import com.hivesandcolonies.characters.character.polen.progression.PolenStoryFlag;

public enum PolenMemoryType {
    FIRST_FLOWER(
            PolenStoryFlag.POLEN_MEMORY_FIRST_FLOWER,
            "dialogue.polen.memory.first_flower"
    ),
    FIRST_HIVE(
            PolenStoryFlag.POLEN_MEMORY_FIRST_HIVE,
            "dialogue.polen.memory.first_hive"
    ),
    FIRST_SOURCE(
            PolenStoryFlag.POLEN_MEMORY_FIRST_SOURCE,
            "dialogue.polen.memory.first_source"
    ),
    FIRST_COLONY(
            PolenStoryFlag.POLEN_MEMORY_FIRST_COLONY,
            "dialogue.polen.memory.first_colony"
    ),
    FIRST_RESIDENCE(
            PolenStoryFlag.POLEN_MEMORY_FIRST_RESIDENCE,
            "dialogue.polen.memory.first_residence"
    );

    private final PolenStoryFlag flag;
    private final String dialogueKey;

    PolenMemoryType(PolenStoryFlag flag, String dialogueKey) {
        this.flag = flag;
        this.dialogueKey = dialogueKey;
    }

    public PolenStoryFlag getFlag() {
        return flag;
    }

    public String getDialogueKey() {
        return dialogueKey;
    }
}
