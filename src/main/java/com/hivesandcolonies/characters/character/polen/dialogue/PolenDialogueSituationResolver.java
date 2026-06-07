package com.hivesandcolonies.characters.character.polen.dialogue;

import com.hivesandcolonies.characters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.mood.PolenMood;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.task.PolenTaskType;
import com.hivesandcolonies.characters.character.polen.entity.ai.expression.activity.PolenQuietActivityController;
import com.hivesandcolonies.characters.character.polen.entity.ai.navigation.search.shelter.PolenShelterContextResolver;
import com.hivesandcolonies.characters.character.polen.entity.ai.world.affordance.PolenAffordanceType;
import com.hivesandcolonies.characters.character.polen.entity.ai.world.home.PolenHomeManager;
import com.hivesandcolonies.characters.character.polen.entity.ai.world.observation.PolenObservationFocus;
import net.minecraft.world.level.Level;

public final class PolenDialogueSituationResolver {

    private PolenDialogueSituationResolver() {
    }

    public static String resolveSituation(PolenEntity polen) {
        if (polen == null) {
            return null;
        }

        String quietActivitySituation = resolveQuietActivitySituation(polen);
        if (quietActivitySituation != null) {
            return quietActivitySituation;
        }

        String safetySituation = resolveSafetySituation(polen);
        if (safetySituation != null) {
            return safetySituation;
        }

        if (polen.getCurrentTask() == PolenTaskType.KEEP_DISTANCE) {
            return PolenDialogueManager.AMBIENT_TIMID;
        }

        if (polen.getCurrentTask() == PolenTaskType.APPROACH_TRUSTED_PLAYER) {
            return PolenDialogueManager.AMBIENT_APPROACH;
        }

        String observationSituation = resolveObservationSituation(polen);
        if (observationSituation != null) {
            return observationSituation;
        }

        if (polen.getCurrentTask() == PolenTaskType.SEEK_REST || PolenHomeManager.isNearResidence(polen)) {
            return PolenDialogueManager.AMBIENT_REFLECTION;
        }

        if (polen.hasNearbyPlayer(5.0D)
                && (polen.getMood() == PolenMood.TIMID || polen.getMood() == PolenMood.UNSETTLED)) {
            return PolenDialogueManager.AMBIENT_TIMID;
        }

        if (polen.getMood() == PolenMood.CURIOUS) {
            return PolenDialogueManager.AMBIENT_CURIOSITY;
        }

        if (PolenHomeManager.isNearResidence(polen)
                && (polen.getMood() == PolenMood.CALM
                || polen.getMood() == PolenMood.CONFIDENT
                || polen.getMood() == PolenMood.JOYFUL
                || polen.getMood() == PolenMood.INSPIRED)) {
            return PolenDialogueManager.AMBIENT_REFLECTION;
        }

        return null;
    }

    private static String resolveQuietActivitySituation(PolenEntity polen) {
        return switch (polen.getQuietActivityType()) {
            case PolenQuietActivityController.QUIET_ACTIVITY_SINGING -> PolenDialogueManager.AMBIENT_SINGING;
            case PolenQuietActivityController.QUIET_ACTIVITY_DRAWING -> PolenDialogueManager.AMBIENT_DRAWING;
            case PolenQuietActivityController.QUIET_ACTIVITY_ATTUNING -> PolenDialogueManager.AMBIENT_MAGIC;
            case PolenQuietActivityController.QUIET_ACTIVITY_ILLUMINATING -> PolenDialogueManager.AMBIENT_ILLUMINATION;
            case PolenQuietActivityController.QUIET_ACTIVITY_REFLECTING -> PolenDialogueManager.AMBIENT_REFLECTION;
            default -> null;
        };
    }

    private static String resolveSafetySituation(PolenEntity polen) {
        Level level = polen.level();
        boolean weatherPressure = level.isNight() || level.isRaining();
        boolean safetyPressure = polen.getCurrentTask() == PolenTaskType.SEEK_SAFETY
                || polen.getAiState().getObservationFocus() == PolenObservationFocus.SHELTER
                || weatherPressure;
        if (!safetyPressure) {
            return null;
        }

        String shelterSituation = PolenShelterContextResolver.resolveAmbientSituation(level, polen.blockPosition());
        if (shelterSituation != null) {
            return shelterSituation;
        }

        if (polen.getCurrentTask() == PolenTaskType.SEEK_SAFETY || polen.getMood() == PolenMood.UNSETTLED) {
            return PolenDialogueManager.AMBIENT_UNSAFE;
        }

        return null;
    }

    private static String resolveObservationSituation(PolenEntity polen) {
        PolenObservationFocus focus = polen.getAiState().getObservationFocus();
        PolenAffordanceType affordanceType = polen.getAiState().getObservationAffordanceType();
        return switch (focus) {
            case LIGHT -> PolenDialogueManager.AMBIENT_ILLUMINATION;
            case REST -> PolenDialogueManager.AMBIENT_REFLECTION;
            case INTEREST -> resolveInterestSituation(polen, affordanceType);
            case SHELTER -> resolveSafetySituation(polen);
            case NONE -> null;
        };
    }

    private static String resolveInterestSituation(PolenEntity polen, PolenAffordanceType affordanceType) {
        if (affordanceType == PolenAffordanceType.INTEREST_SOURCE
                || affordanceType == PolenAffordanceType.INTEREST_MAGIC) {
            return PolenDialogueManager.AMBIENT_MAGIC;
        }

        String observationContext = polen.getAiState().getObservationContext();
        if (observationContext.contains("source") || observationContext.contains("magic")) {
            return PolenDialogueManager.AMBIENT_MAGIC;
        }

        return PolenDialogueManager.AMBIENT_CURIOSITY;
    }
}
