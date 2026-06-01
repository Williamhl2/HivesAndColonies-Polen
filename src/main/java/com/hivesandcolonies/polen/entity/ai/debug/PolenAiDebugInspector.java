package com.hivesandcolonies.polen.entity.ai.debug;

import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.ai.intent.PolenIntentSnapshot;
import com.hivesandcolonies.polen.entity.ai.intent.PolenIntentController;
import com.hivesandcolonies.polen.entity.ai.memory.PolenMemoryHandler;
import com.hivesandcolonies.polen.entity.ai.mood.PolenMoodAnalysis;
import com.hivesandcolonies.polen.entity.ai.mood.PolenMoodController;
import com.hivesandcolonies.polen.entity.ai.need.PolenNeedSnapshot;
import com.hivesandcolonies.polen.entity.ai.need.PolenNeedController;
import com.hivesandcolonies.polen.entity.ai.safety.PolenSafetyNavigator;

public final class PolenAiDebugInspector {

    private PolenAiDebugInspector() {
    }

    public static PolenAiDebugSnapshot inspect(PolenEntity polen) {
        PolenMoodAnalysis moodAnalysis = PolenMoodController.analyzeMood(polen);
        PolenNeedSnapshot needSnapshot = PolenNeedController.inspect(polen);
        PolenIntentSnapshot intentSnapshot = PolenIntentController.inspect(polen);

        return new PolenAiDebugSnapshot(
                moodAnalysis.mood(),
                moodAnalysis.reason(),
                intentSnapshot.intent(),
                intentSnapshot.reason(),
                polen.getQuietActivityName(),
                needSnapshot.dominantNeed(),
                needSnapshot.safety(),
                needSnapshot.social(),
                needSnapshot.curiosity(),
                needSnapshot.rest(),
                needSnapshot.magic(),
                PolenSafetyNavigator.isInUnsafeArea(polen),
                PolenSafetyNavigator.shouldSeekSafety(polen),
                PolenSafetyNavigator.shouldUseUnsafeDialogue(polen),
                polen.getAiState().getSearchType(),
                polen.getAiState().getSearchStatus(),
                polen.getAiState().getSearchNote(),
                polen.getAiState().getSearchTargetPos(),
                polen.getAiState().getObservedPos(),
                PolenMemoryHandler.isNearRememberedInterest(polen),
                polen.getAiState().getFavoriteFlowerPos(),
                polen.getAiState().getFavoriteHivePos(),
                polen.getAiState().getFavoriteSourcePos(),
                polen.getAiState().getRestingPos(),
                polen.getDangerousSpotPos()
        );
    }
}
