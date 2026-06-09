package com.hivesandcolonies.hccharacters.character.polen.dialogue;

import com.hivesandcolonies.hccharacters.character.polen.progression.PolenAffinityLevels;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PolenAmbientToneResolverTest {

    @Test
    void resolvesGuardedBelowNameReveal() {
        assertEquals("guarded", PolenAmbientToneResolver.resolveTone(PolenAffinityLevels.NAME_REVEAL - 1));
    }

    @Test
    void resolvesSoftBetweenNameRevealAndFriend() {
        assertEquals("soft", PolenAmbientToneResolver.resolveTone(PolenAffinityLevels.NAME_REVEAL));
    }

    @Test
    void resolvesWarmAtFriendOrAbove() {
        assertEquals("warm", PolenAmbientToneResolver.resolveTone(PolenAffinityLevels.FRIEND));
    }
}
