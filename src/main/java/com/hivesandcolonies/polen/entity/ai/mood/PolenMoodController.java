package com.hivesandcolonies.polen.entity.ai.mood;

import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.ai.PolenMood;
import com.hivesandcolonies.polen.entity.ai.memory.PolenMemoryHandler;
import com.hivesandcolonies.polen.progression.PolenAffinityLevels;
import com.hivesandcolonies.polen.progression.PolenAffinityManager;
import com.hivesandcolonies.polen.progression.PolenStoryFlag;
import com.hivesandcolonies.polen.progression.PolenStoryFlagsManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public final class PolenMoodController {

    private PolenMoodController() {
    }

    public static PolenMood calculateMood(PolenEntity polen) {
        Player nearbyPlayer = polen.level().getNearestPlayer(polen, 2.5D);

        if (nearbyPlayer != null && !polen.isComfortableWith(nearbyPlayer)) {
            return PolenMood.TIMID;
        }

        if (polen.level().isThundering()
                || polen.level().isRaining() && polen.level().canSeeSky(polen.blockPosition())) {
            return PolenMood.UNSETTLED;
        }

        if (nearbyPlayer != null
                && PolenAffinityManager.getAffinity(nearbyPlayer) >= PolenAffinityLevels.FRIEND) {
            return PolenMood.JOYFUL;
        }

        if (polen.isDoingQuietActivity()) {
            return PolenMood.INSPIRED;
        }

        if (PolenMemoryHandler.isNearRememberedInterest(polen)) {
            return PolenMood.CURIOUS;
        }

        if (polen.level() instanceof ServerLevel serverLevel
                && PolenStoryFlagsManager.hasFlag(serverLevel, PolenStoryFlag.PLAYER_HAS_SHELTER)) {
            return PolenMood.CONFIDENT;
        }

        return PolenMood.CALM;
    }
}