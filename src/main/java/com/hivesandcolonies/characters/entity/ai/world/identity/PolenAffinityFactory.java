package com.hivesandcolonies.characters.entity.ai.world.identity;

import com.hivesandcolonies.characters.entity.PolenEntity;
import com.hivesandcolonies.characters.entity.ai.world.interests.PolenInterest;
import com.hivesandcolonies.characters.entity.ai.world.interests.PolenInterestProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;

public final class PolenAffinityFactory {
    private PolenAffinityFactory() {
    }

    public static PolenWorldAffinity fromProfile(PolenInterestProfile profile) {
        if (profile == null) {
            return PolenWorldAffinity.WAYFARER;
        }

        PolenInterest dominant = profile.dominantInterest();
        return switch (dominant) {
            case BEES -> PolenWorldAffinity.APIARIST;
            case MAGIC -> PolenWorldAffinity.ARCANE;
            case COLONIES -> PolenWorldAffinity.COLONIAL;
            case FOOD -> PolenWorldAffinity.HARVEST;
            case DECORATION -> PolenWorldAffinity.ARTISAN;
            case EXPLORATION -> PolenWorldAffinity.WAYFARER;
        };
    }

    public static PolenWorldAffinity createInitialAffinity(PolenEntity polen) {
        long seed = polen.getUUID().getMostSignificantBits() ^ polen.getUUID().getLeastSignificantBits();
        if (polen.level() instanceof ServerLevel serverLevel) {
            seed ^= serverLevel.getSeed();
        }

        RandomSource random = RandomSource.create(seed);
        PolenWorldAffinity[] candidates = {
                PolenWorldAffinity.APIARIST,
                PolenWorldAffinity.ARCANE,
                PolenWorldAffinity.COLONIAL,
                PolenWorldAffinity.HARVEST,
                PolenWorldAffinity.ARTISAN,
                PolenWorldAffinity.WAYFARER
        };

        return candidates[random.nextInt(candidates.length)];
    }
}
