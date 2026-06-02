package com.hivesandcolonies.polen.entity.ai.brain.task;

public record PolenTaskSnapshot(
        PolenTaskType currentTask,
        PolenTaskType desiredTask,
        PolenTaskStatus status,
        String reason,
        String note,
        PolenTaskType recentFailedTask,
        int recentFailureCount,
        long recoverUntilGameTime
) {
}
