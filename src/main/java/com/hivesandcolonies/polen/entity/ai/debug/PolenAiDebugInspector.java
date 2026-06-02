package com.hivesandcolonies.polen.entity.ai.debug;

import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.ai.brain.intent.PolenIntentSnapshot;
import com.hivesandcolonies.polen.entity.ai.brain.intent.PolenIntentController;
import com.hivesandcolonies.polen.entity.ai.brain.memory.PolenMemoryHandler;
import com.hivesandcolonies.polen.entity.ai.brain.mood.PolenMoodAnalysis;
import com.hivesandcolonies.polen.entity.ai.brain.mood.PolenMoodController;
import com.hivesandcolonies.polen.entity.ai.brain.need.PolenNeedSnapshot;
import com.hivesandcolonies.polen.entity.ai.brain.need.PolenNeedController;
import com.hivesandcolonies.polen.entity.ai.brain.task.PolenTaskController;
import com.hivesandcolonies.polen.entity.ai.brain.task.PolenTaskSnapshot;
import com.hivesandcolonies.polen.entity.ai.navigation.safety.PolenSafetyNavigator;
import com.hivesandcolonies.polen.entity.ai.world.comfort.PolenComfortEvaluator;
import com.hivesandcolonies.polen.entity.ai.world.comfort.PolenComfortProfile;
import com.hivesandcolonies.polen.entity.ai.world.comfort.PolenComfortReport;
import com.hivesandcolonies.polen.entity.ai.world.identity.PolenAffinity;
import com.hivesandcolonies.polen.entity.ai.world.identity.PolenAffinityFactory;
import com.hivesandcolonies.polen.entity.ai.world.identity.PolenIdentity;
import com.hivesandcolonies.polen.entity.ai.world.interests.PolenInterestProfile;
import com.hivesandcolonies.polen.progression.world.PolenWorldStateManager;
import com.hivesandcolonies.polen.progression.world.PolenWorldStoryData;

import net.minecraft.server.level.ServerLevel;
import com.hivesandcolonies.polen.item.accessory.PolenAccessorySlot;

public final class PolenAiDebugInspector {

    private PolenAiDebugInspector() {
    }

    public static PolenAiDebugSnapshot inspect(PolenEntity polen) {
        PolenMoodAnalysis moodAnalysis = PolenMoodController.analyzeMood(polen);
        PolenNeedSnapshot needSnapshot = PolenNeedController.inspect(polen);
        PolenIntentSnapshot intentSnapshot = PolenIntentController.inspect(polen);
        PolenTaskSnapshot taskSnapshot = PolenTaskController.inspect(polen);
        PolenComfortReport currentComfort = PolenComfortEvaluator.evaluate(
                polen,
                polen.blockPosition(),
                PolenComfortProfile.DEBUG
        );
        PolenComfortReport residenceComfort = polen.getAiState().getResidenceUsePos() == null
                ? PolenComfortReport.empty(null)
                : PolenComfortEvaluator.evaluate(
                        polen,
                        polen.getAiState().getResidenceUsePos(),
                        PolenComfortProfile.RESIDENCE
                );

        PolenWorldStoryData worldData = polen.level() instanceof ServerLevel serverLevel
                ? PolenWorldStateManager.ensureFor(serverLevel, polen)
                : new PolenWorldStoryData();
        PolenIdentity identity = worldData.getIdentity();
        PolenInterestProfile interests = worldData.getInterestProfile();
        PolenAffinity affinity = PolenAffinityFactory.fromProfile(interests);

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
                currentComfort.totalScore(),
                currentComfort.rank(),
                currentComfort.summary(),
                residenceComfort.totalScore(),
                residenceComfort.rank(),
                residenceComfort.summary(),
                identity == null ? null : identity.identityId(),
                worldData.getPolenEntityUuid(),
                worldData.getStoryStage(),
                interests.dominantInterest(),
                affinity,
                String.valueOf(polen.getEquippedAccessory(PolenAccessorySlot.CHARM)),
                interests.summary(),
                worldData.getWorldMemories().toString(),
                polen.getAiState().getRestingPos(),
                polen.getDangerousSpotPos()
        );
    }
}
