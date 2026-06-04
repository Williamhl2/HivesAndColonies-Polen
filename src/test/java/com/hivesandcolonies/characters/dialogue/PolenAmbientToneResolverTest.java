package com.hivesandcolonies.characters.dialogue;

import com.hivesandcolonies.characters.progression.PolenAffinityLevels;
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
