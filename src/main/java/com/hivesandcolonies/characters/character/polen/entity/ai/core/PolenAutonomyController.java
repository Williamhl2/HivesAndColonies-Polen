package com.hivesandcolonies.characters.character.polen.entity.ai.core;

import com.hivesandcolonies.characters.character.polen.entity.PolenAmbientDialogueController;
import com.hivesandcolonies.characters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.intent.PolenIntentController;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.memory.PolenMemoryHandler;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.mood.PolenMoodController;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.need.PolenNeedController;
import com.hivesandcolonies.characters.character.polen.entity.ai.debug.PolenThoughtDebugController;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.task.PolenTaskController;
import com.hivesandcolonies.characters.character.polen.entity.ai.world.observation.PolenObservationController;

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
