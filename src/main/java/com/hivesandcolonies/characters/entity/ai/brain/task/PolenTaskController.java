package com.hivesandcolonies.characters.entity.ai.brain.task;

import com.hivesandcolonies.characters.entity.PolenEntity;
import net.minecraft.server.level.ServerLevel;

import java.util.Locale;

public final class PolenTaskController {
    private PolenTaskController() {
    }

    public static void tick(PolenEntity polen) {
        if (!(polen.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        long gameTime = serverLevel.getGameTime();
        PolenTaskState state = polen.getAiState().getTaskState();
        PolenTaskType desiredTask = PolenTaskType.fromIntent(polen.getCurrentIntent());
        String reason = polen.getCurrentIntentReason();
        state.setDesiredTask(desiredTask, reason);

        if (!desiredTask.isUrgent() && state.shouldRecoverFrom(desiredTask, gameTime)) {
            if (state.getStatus() != PolenTaskStatus.RECOVERING || state.getCurrentTask() != PolenTaskType.WANDER_SAFE) {
                state.startRecoveryWander("recovering_from_" + formatTaskKey(desiredTask), gameTime);
            }
            return;
        }

        if (shouldInterruptCurrentTask(state, desiredTask, gameTime)
                || state.getCurrentTask() != desiredTask
                || !state.getStatus().isExecuting()) {
            state.planTask(desiredTask, reason, "task_selected_from_intent", gameTime);
        }
    }

    public static PolenTaskSnapshot inspect(PolenEntity polen) {
        return polen.getAiState().getTaskState().snapshot();
    }

    public static void markActive(PolenEntity polen, PolenTaskType taskType, String note) {
        polen.getAiState().getTaskState().activateTask(taskType, note, getGameTime(polen));
    }

    public static void markCompleted(PolenEntity polen, PolenTaskType taskType, String note) {
        polen.getAiState().getTaskState().completeTask(taskType, note, getGameTime(polen));
    }

    public static void markFailed(PolenEntity polen, PolenTaskType taskType, String note, long recoveryTicks) {
        long gameTime = getGameTime(polen);
        polen.getAiState().getTaskState().failTask(taskType, note, gameTime, gameTime + Math.max(0L, recoveryTicks));
    }

    private static long getGameTime(PolenEntity polen) {
        return polen.level() instanceof ServerLevel serverLevel ? serverLevel.getGameTime() : 0L;
    }

    private static String formatTaskKey(PolenTaskType taskType) {
        return taskType.name().toLowerCase(Locale.ROOT);
    }

    private static boolean shouldInterruptCurrentTask(PolenTaskState state, PolenTaskType desiredTask, long gameTime) {
        if (desiredTask == null || desiredTask == state.getCurrentTask()) {
            return false;
        }

        if (desiredTask.isUrgent()) {
            return true;
        }

        if (state.isCurrentTaskUrgent()) {
            return false;
        }

        if (desiredTask == PolenTaskType.WANDER_SAFE) {
            return false;
        }

        if (!state.getStatus().isExecuting()) {
            return true;
        }

        return gameTime - state.getTaskSinceGameTime() >= 40L;
    }
}
