package com.hivesandcolonies.characters.character.polen.entity.ai.world.observation;

import com.hivesandcolonies.characters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.intent.PolenIntent;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.need.PolenNeedController;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.need.PolenNeedSnapshot;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.task.PolenTaskType;
import com.hivesandcolonies.characters.character.polen.entity.ai.navigation.safety.PolenSafetyNavigator;
import com.hivesandcolonies.characters.character.polen.entity.ai.world.affordance.PolenAffordanceResolver;
import com.hivesandcolonies.characters.character.polen.entity.ai.world.affordance.PolenAffordanceTarget;
import com.hivesandcolonies.characters.character.polen.entity.ai.world.identity.PolenWorldAffinity;

public final class PolenObservationController {
    private PolenObservationController() {
    }

    public static void tick(PolenEntity polen) {
        if (polen == null) {
            return;
        }

        PolenAffordanceTarget target = null;
        PolenObservationFocus focus = PolenObservationFocus.NONE;
        PolenObservationDisposition disposition = PolenObservationDisposition.IDLE;
        String note = "";

        if (PolenSafetyNavigator.shouldSeekRainShelter(polen)) {
            target = PolenAffordanceResolver.findBestRainShelter(polen, 18);
            focus = PolenObservationFocus.SHELTER;
            disposition = target == null ? PolenObservationDisposition.REJECTED : resolveDispositionForCurrentState(polen, focus);
            note = target == null ? "no_rain_shelter_visible" : "rain_shelter_visible";
        } else if (PolenSafetyNavigator.shouldSeekNightLight(polen)) {
            target = PolenAffordanceResolver.findBestNightLight(polen, 14);
            focus = PolenObservationFocus.LIGHT;
            disposition = target == null ? PolenObservationDisposition.REJECTED : resolveDispositionForCurrentState(polen, focus);
            note = target == null ? "no_light_solution_visible" : target.contextKey();
        } else if (shouldObserveRest(polen)) {
            target = PolenAffordanceResolver.findBestRestSpot(polen, 12);
            focus = PolenObservationFocus.REST;
            disposition = target == null ? PolenObservationDisposition.REJECTED : resolveDispositionForCurrentState(polen, focus);
            note = target == null ? "no_rest_spot_visible" : target.contextKey();
        } else if (shouldObserveInterest(polen)) {
            target = PolenAffordanceResolver.findBestInterest(polen, true);
            focus = PolenObservationFocus.INTEREST;
            disposition = target == null ? PolenObservationDisposition.REJECTED : resolveDispositionForCurrentState(polen, focus);
            note = target == null ? "no_interest_visible" : target.contextKey();
        }

        if (target == null) {
            polen.getAiState().setObservationState(
                    focus,
                    disposition,
                    null,
                    null,
                    null,
                    "",
                    note
            );
            return;
        }

        polen.getAiState().setObservationState(
                focus,
                disposition,
                target.type(),
                target.focusPos(),
                target.usePos(),
                target.contextKey(),
                note
        );
    }

    private static boolean shouldObserveRest(PolenEntity polen) {
        PolenNeedSnapshot needs = PolenNeedController.inspect(polen);
        return polen.getCurrentTask() == PolenTaskType.SEEK_REST
                || polen.getCurrentIntent() == PolenIntent.SEEK_REST
                || needs.rest() >= 36;
    }

    private static boolean shouldObserveInterest(PolenEntity polen) {
        PolenNeedSnapshot needs = PolenNeedController.inspect(polen);
        boolean hasAffinityCharm = polen.getEquippedAffinityCharm() != PolenWorldAffinity.NONE;
        return polen.getCurrentTask() == PolenTaskType.INVESTIGATE_INTEREST
                || polen.getCurrentIntent() == PolenIntent.INVESTIGATE_INTEREST
                || needs.curiosity() >= (hasAffinityCharm ? 24 : 30)
                || needs.magic() >= 42;
    }

    private static PolenObservationDisposition resolveDispositionForCurrentState(
            PolenEntity polen,
            PolenObservationFocus focus
    ) {
        return switch (focus) {
            case SHELTER -> polen.getCurrentTask() == PolenTaskType.SEEK_SAFETY
                    ? PolenObservationDisposition.USING
                    : PolenObservationDisposition.NOTICED;
            case LIGHT -> polen.getCurrentTask() == PolenTaskType.SEEK_SAFETY
                    || polen.getCurrentIntent() == PolenIntent.QUIET_CREATION
                    ? PolenObservationDisposition.EVALUATING
                    : PolenObservationDisposition.NOTICED;
            case REST -> polen.getCurrentTask() == PolenTaskType.SEEK_REST
                    ? PolenObservationDisposition.USING
                    : PolenObservationDisposition.NOTICED;
            case INTEREST -> polen.getCurrentTask() == PolenTaskType.INVESTIGATE_INTEREST
                    ? PolenObservationDisposition.USING
                    : PolenObservationDisposition.NOTICED;
            case NONE -> PolenObservationDisposition.IDLE;
        };
    }
}
