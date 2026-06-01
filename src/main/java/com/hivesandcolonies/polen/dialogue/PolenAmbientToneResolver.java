package com.hivesandcolonies.polen.dialogue;

import com.hivesandcolonies.polen.progression.PolenAffinityLevels;

public final class PolenAmbientToneResolver {

    private PolenAmbientToneResolver() {
    }

    public static String resolveTone(int affinity) {
        if (affinity >= PolenAffinityLevels.FRIEND) {
            return "warm";
        }

        if (affinity >= PolenAffinityLevels.NAME_REVEAL) {
            return "soft";
        }

        return "guarded";
    }
}
