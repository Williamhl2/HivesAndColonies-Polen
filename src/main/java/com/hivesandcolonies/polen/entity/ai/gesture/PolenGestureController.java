package com.hivesandcolonies.polen.entity.ai.gesture;

import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.ai.activity.PolenQuietActivityController;
import com.hivesandcolonies.polen.entity.ai.intent.PolenIntent;
import com.hivesandcolonies.polen.entity.ai.mood.PolenMood;

public final class PolenGestureController {
    private PolenGestureController() {
    }

    public static void tickServer(PolenEntity polen) {
        int remainingTicks = polen.getGestureTicks();
        if (remainingTicks > 1) {
            polen.setGestureState(polen.getGesture(), remainingTicks - 1);
            return;
        }

        polen.setGestureState(resolvePassiveGesture(polen), 0);
    }

    public static void triggerGesture(PolenEntity polen, PolenGesture gesture) {
        triggerGesture(polen, gesture, gesture.getSuggestedDurationTicks());
    }

    public static void triggerGesture(PolenEntity polen, PolenGesture gesture, int ticks) {
        polen.setGestureState(gesture, Math.max(0, ticks));
    }

    private static PolenGesture resolvePassiveGesture(PolenEntity polen) {
        int quietActivity = polen.getQuietActivityType();
        if (quietActivity == PolenQuietActivityController.QUIET_ACTIVITY_SINGING) {
            return PolenGesture.SINGING;
        }
        if (quietActivity == PolenQuietActivityController.QUIET_ACTIVITY_DRAWING) {
            return PolenGesture.DRAWING;
        }
        if (quietActivity == PolenQuietActivityController.QUIET_ACTIVITY_ATTUNING) {
            return PolenGesture.ATTUNING;
        }
        if (quietActivity == PolenQuietActivityController.QUIET_ACTIVITY_ILLUMINATING) {
            return PolenGesture.ILLUMINATING;
        }
        if (quietActivity == PolenQuietActivityController.QUIET_ACTIVITY_REFLECTING) {
            return PolenGesture.REFLECTING;
        }

        PolenIntent intent = polen.getCurrentIntent();
        if (intent == PolenIntent.KEEP_DISTANCE) {
            return PolenGesture.WITHDRAWN;
        }
        if (intent == PolenIntent.APPROACH_TRUSTED_PLAYER) {
            return PolenGesture.APPROACHING;
        }
        if (intent == PolenIntent.INVESTIGATE_INTEREST) {
            return PolenGesture.CURIOUS;
        }
        if (intent == PolenIntent.SEEK_SAFETY) {
            return PolenGesture.STARTLED;
        }

        PolenMood mood = polen.getMood();
        if (mood == PolenMood.TIMID || mood == PolenMood.UNSETTLED) {
            return PolenGesture.WITHDRAWN;
        }
        if (mood == PolenMood.CURIOUS) {
            return PolenGesture.CURIOUS;
        }

        return PolenGesture.IDLE;
    }
}
