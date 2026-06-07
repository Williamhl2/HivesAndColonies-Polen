package com.hivesandcolonies.characters.character.polen.entity.ai.brain.task;

public final class PolenTaskState {
    private PolenTaskType currentTask = PolenTaskType.WANDER_SAFE;
    private PolenTaskType desiredTask = PolenTaskType.WANDER_SAFE;
    private PolenTaskStatus status = PolenTaskStatus.IDLE;
    private String reason = "initial_wander";
    private String note = "";
    private PolenTaskType recentFailedTask;
    private int recentFailureCount;
    private long taskSinceGameTime;
    private long statusSinceGameTime;
    private long recoverUntilGameTime;

    public PolenTaskType getCurrentTask() {
        return this.currentTask;
    }

    public PolenTaskType getDesiredTask() {
        return this.desiredTask;
    }

    public PolenTaskStatus getStatus() {
        return this.status;
    }

    public String getReason() {
        return this.reason;
    }

    public String getNote() {
        return this.note;
    }

    public PolenTaskType getRecentFailedTask() {
        return this.recentFailedTask;
    }

    public int getRecentFailureCount() {
        return this.recentFailureCount;
    }

    public long getTaskSinceGameTime() {
        return this.taskSinceGameTime;
    }

    public long getStatusSinceGameTime() {
        return this.statusSinceGameTime;
    }

    public long getRecoverUntilGameTime() {
        return this.recoverUntilGameTime;
    }

    public void setDesiredTask(PolenTaskType desiredTask, String reason) {
        this.desiredTask = desiredTask == null ? PolenTaskType.WANDER_SAFE : desiredTask;
        this.reason = reason == null || reason.isBlank() ? "unknown_task_reason" : reason;
    }

    public boolean shouldRecoverFrom(PolenTaskType taskType, long gameTime) {
        return this.recentFailedTask == taskType && gameTime < this.recoverUntilGameTime;
    }

    public void planTask(PolenTaskType taskType, String reason, String note, long gameTime) {
        setTaskState(taskType, PolenTaskStatus.PLANNED, reason, note, gameTime);
    }

    public void activateTask(PolenTaskType taskType, String note, long gameTime) {
        setTaskState(taskType, PolenTaskStatus.ACTIVE, this.reason, note, gameTime);
    }

    public void completeTask(PolenTaskType taskType, String note, long gameTime) {
        setTaskState(taskType, PolenTaskStatus.COMPLETED, this.reason, note, gameTime);
    }

    public void failTask(PolenTaskType taskType, String note, long gameTime, long recoverUntilGameTime) {
        if (this.recentFailedTask == taskType && gameTime <= this.recoverUntilGameTime + 200L) {
            this.recentFailureCount++;
        } else {
            this.recentFailureCount = 1;
        }

        this.recentFailedTask = taskType;
        this.recoverUntilGameTime = Math.max(gameTime, recoverUntilGameTime);
        setTaskState(taskType, PolenTaskStatus.FAILED, this.reason, note, gameTime);
    }

    public void startRecoveryWander(String note, long gameTime) {
        setTaskState(
                PolenTaskType.WANDER_SAFE,
                PolenTaskStatus.RECOVERING,
                "recovering_task_flow",
                note,
                gameTime
        );
    }

    public boolean isCurrentTaskUrgent() {
        return this.currentTask != null && this.currentTask.isUrgent();
    }

    public PolenTaskSnapshot snapshot() {
        return new PolenTaskSnapshot(
                this.currentTask,
                this.desiredTask,
                this.status,
                this.reason,
                this.note,
                this.recentFailedTask,
                this.recentFailureCount,
                this.recoverUntilGameTime
        );
    }

    private void setTaskState(
            PolenTaskType taskType,
            PolenTaskStatus status,
            String reason,
            String note,
            long gameTime
    ) {
        PolenTaskType resolvedTask = taskType == null ? PolenTaskType.WANDER_SAFE : taskType;
        if (this.currentTask != resolvedTask) {
            this.taskSinceGameTime = gameTime;
        }

        this.currentTask = resolvedTask;
        this.status = status == null ? PolenTaskStatus.IDLE : status;
        this.reason = reason == null || reason.isBlank() ? this.reason : reason;
        this.note = note == null ? "" : note;
        this.statusSinceGameTime = gameTime;
    }
}
