package com.hivesandcolonies.polen.entity.ai.mood;

import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.ai.PolenMood;
import com.hivesandcolonies.polen.entity.ai.memory.PolenMemoryHandler;
import com.hivesandcolonies.polen.progression.PolenStoryFlag;
import com.hivesandcolonies.polen.progression.PolenStoryFlagsManager;
import com.hivesandcolonies.polen.progression.PolenAffinityManager;
import com.hivesandcolonies.polen.progression.PolenAffinityLevels;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public final class PolenMoodController {

    private static final double UNTRUSTED_CLOSE_RANGE = 2.5D;
    private static final double TRUSTED_NEARBY_RANGE = 6.0D;

    private PolenMoodController() {
    }

    public static PolenMood calculateMood(PolenEntity polen) {
        return analyzeMood(polen).mood();
    }

    public static PolenMoodAnalysis analyzeMood(PolenEntity polen) {
        Player closePlayer = polen.level().getNearestPlayer(polen, UNTRUSTED_CLOSE_RANGE);
        Player nearbyPlayer = polen.level().getNearestPlayer(polen, TRUSTED_NEARBY_RANGE);

        if (closePlayer != null && !polen.isComfortableWith(closePlayer)) {
            return new PolenMoodAnalysis(PolenMood.TIMID, "untrusted_player_too_close");
        }

        if (polen.level().isThundering()
                || polen.level().isRaining() && polen.level().canSeeSky(polen.blockPosition())) {
            return new PolenMoodAnalysis(PolenMood.UNSETTLED, "bad_weather_exposed");
        }

        if (nearbyPlayer != null
                && polen.isComfortableWith(nearbyPlayer)) {
            return new PolenMoodAnalysis(PolenMood.JOYFUL, "trusted_player_nearby");
        }

        if (polen.isDoingQuietActivity()) {
            return new PolenMoodAnalysis(PolenMood.INSPIRED, "quiet_activity_active");
        }

        if (PolenMemoryHandler.isNearRememberedInterest(polen)) {
            return new PolenMoodAnalysis(PolenMood.CURIOUS, "near_remembered_interest");
        }

        if (polen.level() instanceof ServerLevel serverLevel
                && PolenStoryFlagsManager.hasFlag(serverLevel, PolenStoryFlag.PLAYER_HAS_SHELTER)) {
            return new PolenMoodAnalysis(PolenMood.CONFIDENT, "world_has_shelter");
        }

        return new PolenMoodAnalysis(PolenMood.CALM, "default_calm");
    }
}
