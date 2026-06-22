package com.hivesandcolonies.hccharacters.character.polen.dialogue;

import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.task.PolenTaskType;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.environment.PolenEnvironmentSnapshot;

public final class PolenDialoguePolicyResolver {
    private static final PolenDialoguePolicy URGENT_POLICY = new PolenDialoguePolicy(120L, 180L, 2);
    private static final PolenDialoguePolicy NIGHT_POLICY = new PolenDialoguePolicy(160L, 220L, 2);
    private static final PolenDialoguePolicy QUIET_POLICY = new PolenDialoguePolicy(220L, 320L, 3);
    private static final PolenDialoguePolicy OBSERVATION_POLICY = new PolenDialoguePolicy(240L, 340L, 4);
    private static final PolenDialoguePolicy DEFAULT_POLICY = new PolenDialoguePolicy(200L, 280L, 4);

    private PolenDialoguePolicyResolver() {
    }

    public static PolenDialoguePolicy resolveAmbientPolicy(
            PolenEntity polen,
            String situation,
            PolenEnvironmentSnapshot environment
    ) {
        if (polen == null || situation == null || situation.isBlank()) {
            return DEFAULT_POLICY;
        }

        if (environment != null
                && (environment.immediateThreat()
                || environment.exposedToRangedThreat()
                || polen.getCurrentTask() == PolenTaskType.SEEK_SAFETY
                || PolenDialogueManager.AMBIENT_UNSAFE.equals(situation)
                || situation.startsWith("ambient_rain"))) {
            return URGENT_POLICY;
        }

        if (situation.startsWith("ambient_night")) {
            return NIGHT_POLICY;
        }

        if (polen.isDoingQuietActivity()) {
            return QUIET_POLICY;
        }

        if (PolenDialogueManager.AMBIENT_CURIOSITY.equals(situation)
                || PolenDialogueManager.AMBIENT_MAGIC.equals(situation)
                || PolenDialogueManager.AMBIENT_ILLUMINATION.equals(situation)
                || PolenDialogueManager.AMBIENT_REFLECTION.equals(situation)) {
            return OBSERVATION_POLICY;
        }

        return DEFAULT_POLICY;
    }
}
