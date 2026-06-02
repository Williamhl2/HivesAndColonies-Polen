package com.hivesandcolonies.polen.entity.ai.expression.gesture;

import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.ai.expression.activity.PolenQuietActivityController;
import com.hivesandcolonies.polen.entity.ai.brain.mood.PolenMood;
import com.hivesandcolonies.polen.entity.ai.brain.task.PolenTaskType;

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

        PolenTaskType taskType = polen.getCurrentTask();
        if (taskType == PolenTaskType.KEEP_DISTANCE) {
            return PolenGesture.WITHDRAWN;
        }
        if (taskType == PolenTaskType.APPROACH_TRUSTED_PLAYER) {
            return PolenGesture.APPROACHING;
        }
        if (taskType == PolenTaskType.INVESTIGATE_INTEREST) {
            return PolenGesture.CURIOUS;
        }
        if (taskType == PolenTaskType.SEEK_SAFETY) {
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
