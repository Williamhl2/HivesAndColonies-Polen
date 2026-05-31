package com.hivesandcolonies.polen.entity.ai.autonomy;

import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.ai.intent.PolenIntentController;
import com.hivesandcolonies.polen.entity.ai.memory.PolenMemoryHandler;
import com.hivesandcolonies.polen.entity.ai.mood.PolenMoodController;
import com.hivesandcolonies.polen.entity.ai.need.PolenNeedController;

public final class PolenAutonomyController {

    private PolenAutonomyController() {
    }

    public static void tickServer(PolenEntity polen) {
        if (polen.tickCount % 20 == 0) {
            polen.refreshDisplayName();
            PolenNeedController.tick(polen);
            PolenIntentController.tick(polen);
            polen.setMood(PolenMoodController.calculateMood(polen));
        }

        if (polen.tickCount % 100 == 0) {
            PolenMemoryHandler.seedMemoriesFromNearbyEnvironment(polen);
        }
    }
}
