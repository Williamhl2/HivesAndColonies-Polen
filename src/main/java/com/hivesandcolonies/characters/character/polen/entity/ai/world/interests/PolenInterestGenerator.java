package com.hivesandcolonies.characters.character.polen.entity.ai.world.interests;

import java.util.Random;

public final class PolenInterestGenerator {
    private PolenInterestGenerator() {
    }

    public static PolenInterestProfile generate(long personalitySeed) {
        Random random = new Random(personalitySeed);
        PolenInterestProfile profile = new PolenInterestProfile();

        for (PolenInterest interest : PolenInterest.values()) {
            profile.set(interest, 35 + random.nextInt(41));
        }

        // Every world gives Polen one strong curiosity and one quiet hesitation.
        PolenInterest favorite = PolenInterest.values()[random.nextInt(PolenInterest.values().length)];
        PolenInterest reserved = PolenInterest.values()[random.nextInt(PolenInterest.values().length)];
        while (reserved == favorite) {
            reserved = PolenInterest.values()[random.nextInt(PolenInterest.values().length)];
        }

        profile.add(favorite, 20 + random.nextInt(16));
        profile.add(reserved, -(10 + random.nextInt(16)));
        return profile;
    }
}
