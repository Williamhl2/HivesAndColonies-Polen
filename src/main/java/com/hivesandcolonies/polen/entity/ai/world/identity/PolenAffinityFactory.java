package com.hivesandcolonies.polen.entity.ai.world.identity;

import com.hivesandcolonies.polen.entity.ai.world.interests.PolenInterestProfile;

/**
 * Converts persistent interest data into a visible starting affinity.
 */
public final class PolenAffinityFactory {
    private PolenAffinityFactory() {
    }

    public static PolenAffinity fromProfile(PolenInterestProfile profile) {
        if (profile == null) {
            return PolenAffinity.WAYFARER;
        }
        return PolenAffinity.fromInterest(profile.dominantInterest());
    }
}
