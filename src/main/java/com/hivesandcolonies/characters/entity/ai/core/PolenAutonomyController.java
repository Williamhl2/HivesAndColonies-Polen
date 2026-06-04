package com.hivesandcolonies.characters.entity.ai.core;

import com.hivesandcolonies.characters.entity.PolenAmbientDialogueController;
import com.hivesandcolonies.characters.entity.PolenEntity;
import com.hivesandcolonies.characters.entity.ai.brain.intent.PolenIntentController;
import com.hivesandcolonies.characters.entity.ai.brain.memory.PolenMemoryHandler;
import com.hivesandcolonies.characters.entity.ai.brain.mood.PolenMoodController;
import com.hivesandcolonies.characters.entity.ai.brain.need.PolenNeedController;
import com.hivesandcolonies.characters.entity.ai.debug.PolenThoughtDebugController;
import com.hivesandcolonies.characters.entity.ai.brain.task.PolenTaskController;
import com.hivesandcolonies.characters.entity.ai.world.observation.PolenObservationController;

public final class PolenAutonomyController {

    private PolenAutonomyController() {
    }

    public static void tickServer(PolenEntity polen) {
        if (polen.tickCount % 20 == 0) {
            polen.refreshDisplayName();
            PolenNeedController.tick(polen);
            PolenIntentController.tick(polen);
            PolenTaskController.tick(polen);
            polen.setMood(PolenMoodController.calculateMood(polen));
            PolenObservationController.tick(polen);
            PolenThoughtDebugController.tick(polen);
            PolenAmbientDialogueController.tickContextualDialogue(polen);
        }

        if (polen.tickCount % 100 == 0) {
            PolenMemoryHandler.seedMemoriesFromNearbyEnvironment(polen);
        }
    }
}
