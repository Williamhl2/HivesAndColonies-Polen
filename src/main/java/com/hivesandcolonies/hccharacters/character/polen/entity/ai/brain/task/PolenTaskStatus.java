package com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.task;

public enum PolenTaskStatus {
    IDLE,
    PLANNED,
    ACTIVE,
    COMPLETED,
    FAILED,
    RECOVERING;

    public boolean isExecuting() {
        return this == PLANNED || this == ACTIVE || this == RECOVERING;
    }
}
