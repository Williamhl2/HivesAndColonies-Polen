package com.hivesandcolonies.polen.entity.ai.brain.action;

import com.hivesandcolonies.polen.dialogue.PolenDialogueManager;
import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.ai.expression.activity.PolenQuietActivityController;
import com.hivesandcolonies.polen.entity.ai.ability.magic.PolenMagicController;
import com.hivesandcolonies.polen.entity.ai.brain.mood.PolenMood;
import com.hivesandcolonies.polen.entity.ai.brain.routine.PolenRoutinePlanner;

public final class PolenAutonomousActionPlanner {
    private static final PolenAutonomousActionPlan ILLUMINATE_PLAN = new PolenAutonomousActionPlan(
            PolenAutonomousActionType.ILLUMINATE_AREA,
            PolenQuietActivityController.QUIET_ACTIVITY_ILLUMINATING,
            PolenDialogueManager.AMBIENT_ILLUMINATION,
            90,
            150
    );
    private static final PolenAutonomousActionPlan ATTUNE_PLAN = new PolenAutonomousActionPlan(
            PolenAutonomousActionType.ATTUNE_SOURCE,
            PolenQuietActivityController.QUIET_ACTIVITY_ATTUNING,
            PolenDialogueManager.AMBIENT_MAGIC,
            100,
            170
    );
    private static final PolenAutonomousActionPlan REFLECT_PLAN = new PolenAutonomousActionPlan(
            PolenAutonomousActionType.REFLECT,
            PolenQuietActivityController.QUIET_ACTIVITY_REFLECTING,
            PolenDialogueManager.AMBIENT_REFLECTION,
            110,
            190
    );
    private static final PolenAutonomousActionPlan SING_PLAN = new PolenAutonomousActionPlan(
            PolenAutonomousActionType.SING,
            PolenQuietActivityController.QUIET_ACTIVITY_SINGING,
            PolenDialogueManager.AMBIENT_SINGING,
            80,
            160
    );
    private static final PolenAutonomousActionPlan DRAW_PLAN = new PolenAutonomousActionPlan(
            PolenAutonomousActionType.DRAW,
            PolenQuietActivityController.QUIET_ACTIVITY_DRAWING,
            PolenDialogueManager.AMBIENT_DRAWING,
            80,
            160
    );

    private PolenAutonomousActionPlanner() {
    }

    public static PolenAutonomousActionPlan pickQuietAction(PolenEntity polen) {
        PolenMood mood = polen.getMood();

        if (PolenRoutinePlanner.isDarkEnoughForLightMagic(polen)
                && PolenRoutinePlanner.findLightMagicTarget(polen) != null) {
            return ILLUMINATE_PLAN;
        }

        if (shouldReflect(polen, mood)) {
            return REFLECT_PLAN;
        }

        if (PolenMagicController.hasNearbySourceLikeInterest(polen)
                && polen.getAiState().getNeedState().magic() >= 40) {
            return ATTUNE_PLAN;
        }

        if (mood == PolenMood.JOYFUL) {
            return polen.getRandom().nextInt(4) == 0 ? DRAW_PLAN : SING_PLAN;
        }

        if (mood == PolenMood.CONFIDENT || mood == PolenMood.INSPIRED || mood == PolenMood.CURIOUS) {
            return polen.getRandom().nextBoolean() ? SING_PLAN : DRAW_PLAN;
        }

        return polen.getRandom().nextInt(3) == 0 ? SING_PLAN : DRAW_PLAN;
    }

    public static boolean shouldReflect(PolenEntity polen, PolenMood mood) {
        return (mood == PolenMood.CALM || mood == PolenMood.CONFIDENT || mood == PolenMood.INSPIRED)
                && (PolenMagicController.hasNearbyManagedLight(polen) || PolenRoutinePlanner.isNearRestingSpot(polen));
    }
}
