package com.hivesandcolonies.hccharacters.character.polen.dialogue;

import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.task.PolenTaskType;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.expression.activity.PolenQuietActivityController;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.affordance.PolenAffordanceType;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.environment.PolenEnvironmentResolver;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.environment.PolenEnvironmentSnapshot;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home.PolenHomeManager;

import java.util.ArrayList;
import java.util.List;

public final class PolenDialogueBeatResolver {
    private static final int DEFAULT_VARIATIONS = 3;

    private PolenDialogueBeatResolver() {
    }

    public static List<PolenDialogueCue> resolveAmbientCues(PolenEntity polen, String situation) {
        return resolveAmbientCues(polen, situation, PolenEnvironmentResolver.inspect(polen));
    }

    public static List<PolenDialogueCue> resolveAmbientCues(
            PolenEntity polen,
            String situation,
            PolenEnvironmentSnapshot environment
    ) {
        List<PolenDialogueCue> cues = new ArrayList<>();
        if (polen == null || situation == null || situation.isBlank()) {
            return cues;
        }

        switch (situation) {
            case PolenDialogueManager.AMBIENT_UNSAFE -> addUnsafeCues(cues, environment, polen);
            case PolenDialogueManager.AMBIENT_ILLUMINATION -> addIlluminationCues(cues, environment, polen);
            case PolenDialogueManager.AMBIENT_MAGIC -> addMagicCues(cues, polen);
            case PolenDialogueManager.AMBIENT_REFLECTION -> addReflectionCues(cues, environment, polen);
            case PolenDialogueManager.AMBIENT_CURIOSITY -> addCuriosityCues(cues, polen);
            default -> {
            }
        }

        cues.add(new PolenDialogueCue(situation, "", DEFAULT_VARIATIONS, 2));
        return cues;
    }

    private static void addUnsafeCues(List<PolenDialogueCue> cues, PolenEnvironmentSnapshot environment, PolenEntity polen) {
        if (environment.exposedToRangedThreat()) {
            cues.add(new PolenDialogueCue(PolenDialogueManager.AMBIENT_UNSAFE, "ranged", DEFAULT_VARIATIONS, 8));
        }
        if (environment.rainExposed()) {
            cues.add(new PolenDialogueCue(PolenDialogueManager.AMBIENT_UNSAFE, "rain", DEFAULT_VARIATIONS, 5));
        }
        if (environment.claustrophobicStandingSpot()) {
            cues.add(new PolenDialogueCue(PolenDialogueManager.AMBIENT_UNSAFE, "claustrophobic", DEFAULT_VARIATIONS, 6));
        }
        if (environment.night() && !environment.nearbyLight()) {
            cues.add(new PolenDialogueCue(PolenDialogueManager.AMBIENT_UNSAFE, "night", DEFAULT_VARIATIONS, 4));
        }
        if (polen.getCurrentTask() == PolenTaskType.SEEK_SAFETY) {
            cues.add(new PolenDialogueCue(PolenDialogueManager.AMBIENT_UNSAFE, "escaping", DEFAULT_VARIATIONS, 5));
        }
    }

    private static void addIlluminationCues(List<PolenDialogueCue> cues, PolenEnvironmentSnapshot environment, PolenEntity polen) {
        if (environment.exposedToRangedThreat() || polen.getCurrentTask() == PolenTaskType.SEEK_SAFETY) {
            cues.add(new PolenDialogueCue(PolenDialogueManager.AMBIENT_ILLUMINATION, "emergency", DEFAULT_VARIATIONS, 7));
        }
        if (environment.activeManagedLight() || environment.nearbyManagedLight()) {
            cues.add(new PolenDialogueCue(PolenDialogueManager.AMBIENT_ILLUMINATION, "settled", DEFAULT_VARIATIONS, 4));
        }
    }

    private static void addMagicCues(List<PolenDialogueCue> cues, PolenEntity polen) {
        PolenAffordanceType affordanceType = polen.getAiState().getObservationAffordanceType();
        String observationContext = polen.getAiState().getObservationContext();
        if (affordanceType == PolenAffordanceType.INTEREST_SOURCE
                || affordanceType == PolenAffordanceType.INTEREST_MAGIC
                || observationContext.contains("source")
                || observationContext.contains("magic")) {
            cues.add(new PolenDialogueCue(PolenDialogueManager.AMBIENT_MAGIC, "source", DEFAULT_VARIATIONS, 6));
        }
        if (polen.getQuietActivityType() == PolenQuietActivityController.QUIET_ACTIVITY_ATTUNING) {
            cues.add(new PolenDialogueCue(PolenDialogueManager.AMBIENT_MAGIC, "attuning", DEFAULT_VARIATIONS, 5));
        }
    }

    private static void addReflectionCues(
            List<PolenDialogueCue> cues,
            PolenEnvironmentSnapshot environment,
            PolenEntity polen
    ) {
        if (PolenHomeManager.isNearResidence(polen) || environment != null && environment.nearbyBed()) {
            cues.add(new PolenDialogueCue(PolenDialogueManager.AMBIENT_REFLECTION, "home", DEFAULT_VARIATIONS, 6));
        }
        if (polen.hasPendingReturnHomeRequest()) {
            cues.add(new PolenDialogueCue(PolenDialogueManager.AMBIENT_REFLECTION, "returning", DEFAULT_VARIATIONS, 5));
        }
    }

    private static void addCuriosityCues(List<PolenDialogueCue> cues, PolenEntity polen) {
        PolenAffordanceType affordanceType = polen.getAiState().getObservationAffordanceType();
        String observationContext = polen.getAiState().getObservationContext();
        if (affordanceType == PolenAffordanceType.INTEREST_HIVE || observationContext.contains("hive")) {
            cues.add(new PolenDialogueCue(PolenDialogueManager.AMBIENT_CURIOSITY, "hive", DEFAULT_VARIATIONS, 5));
        }
        if (affordanceType == PolenAffordanceType.INTEREST_FLOWER || observationContext.contains("flower")) {
            cues.add(new PolenDialogueCue(PolenDialogueManager.AMBIENT_CURIOSITY, "flower", DEFAULT_VARIATIONS, 5));
        }
    }
}
