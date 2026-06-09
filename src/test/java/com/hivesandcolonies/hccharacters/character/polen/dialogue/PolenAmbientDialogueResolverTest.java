package com.hivesandcolonies.hccharacters.character.polen.dialogue;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PolenAmbientDialogueResolverTest {

    @Test
    void buildsAmbientKeyWithToneAndVariation() {
        String key = PolenAmbientDialogueResolver.resolveKeyForAffinityAndVariation(0, PolenDialogueManager.AMBIENT_TIMID, 2);
        assertEquals("dialogue.polen.ambient_timid.guarded.line2", key);
    }
}
