package com.hivesandcolonies.polen.entity.ai.debug;

import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.ai.memory.PolenMemoryHandler;
import com.hivesandcolonies.polen.entity.ai.mood.PolenMoodAnalysis;
import com.hivesandcolonies.polen.entity.ai.mood.PolenMoodController;
import com.hivesandcolonies.polen.entity.ai.safety.PolenSafetyNavigator;

public final class PolenAiDebugInspector {

    private PolenAiDebugInspector() {
    }

    public static PolenAiDebugSnapshot inspect(PolenEntity polen) {
        PolenMoodAnalysis moodAnalysis = PolenMoodController.analyzeMood(polen);

        return new PolenAiDebugSnapshot(
                moodAnalysis.mood(),
                moodAnalysis.reason(),
                polen.getQuietActivityName(),
                PolenSafetyNavigator.isInUnsafeArea(polen),
                PolenSafetyNavigator.shouldSeekSafety(polen),
                PolenSafetyNavigator.shouldUseUnsafeDialogue(polen),
                PolenMemoryHandler.isNearRememberedInterest(polen),
                polen.getFavoriteFlowerPos(),
                polen.getFavoriteHivePos(),
                polen.getRestingPos(),
                polen.getDangerousSpotPos()
        );
    }
}
