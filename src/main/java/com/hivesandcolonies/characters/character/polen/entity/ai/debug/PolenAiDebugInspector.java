package com.hivesandcolonies.characters.character.polen.entity.ai.debug;

import com.hivesandcolonies.characters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.intent.PolenIntentSnapshot;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.intent.PolenIntentController;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.memory.PolenMemoryHandler;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.mood.PolenMoodAnalysis;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.mood.PolenMoodController;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.need.PolenNeedSnapshot;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.need.PolenNeedController;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.task.PolenTaskController;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.task.PolenTaskSnapshot;
import com.hivesandcolonies.characters.character.polen.entity.ai.navigation.safety.PolenSafetyNavigator;
import com.hivesandcolonies.characters.character.polen.entity.ai.world.interests.PolenAffinityBehaviorHooks;

public final class PolenAiDebugInspector {

    private PolenAiDebugInspector() {
    }

    public static PolenAiDebugSnapshot inspect(PolenEntity polen) {
        PolenMoodAnalysis moodAnalysis = PolenMoodController.analyzeMood(polen);
        PolenNeedSnapshot needSnapshot = PolenNeedController.inspect(polen);
        PolenIntentSnapshot intentSnapshot = PolenIntentController.inspect(polen);
        PolenTaskSnapshot taskSnapshot = PolenTaskController.inspect(polen);

        return new PolenAiDebugSnapshot(
                moodAnalysis.mood(),
                moodAnalysis.reason(),
                intentSnapshot.intent(),
                intentSnapshot.reason(),
                taskSnapshot.currentTask(),
                taskSnapshot.desiredTask(),
                taskSnapshot.status(),
                taskSnapshot.reason(),
                taskSnapshot.note(),
                taskSnapshot.recentFailedTask(),
                taskSnapshot.recentFailureCount(),
                taskSnapshot.recoverUntilGameTime(),
                polen.getQuietActivityName(),
                polen.getEquippedAffinityCharm(),
                PolenAffinityBehaviorHooks.affinityReason(polen.getEquippedAffinityCharm()),
                PolenAffinityBehaviorHooks.describeActiveTarget(
                        polen.getAiState().getObservationAffordanceType(),
                        polen.getAiState().getObservationContext()
                ),
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
                polen.getAiState().getObservationFocus(),
                polen.getAiState().getObservationDisposition(),
                polen.getAiState().getObservationAffordanceType(),
                polen.getAiState().getObservationFocusPos(),
                polen.getAiState().getObservationUsePos(),
                polen.getAiState().getObservationContext(),
                polen.getAiState().getObservationNote(),
                PolenMemoryHandler.isNearRememberedInterest(polen),
                polen.getAiState().getFavoriteFlowerPos(),
                polen.getAiState().getFavoriteHivePos(),
                polen.getAiState().getFavoriteSourcePos(),
                polen.getAiState().getResidenceAnchorPos(),
                polen.getAiState().getResidenceUsePos(),
                polen.getAiState().getResidenceContext(),
                polen.getAiState().getResidenceStage(),
                polen.getAiState().getRestingPos(),
                polen.getDangerousSpotPos()
        );
    }
}
