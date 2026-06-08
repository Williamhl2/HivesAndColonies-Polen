package com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.task;

import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.intent.PolenIntent;

public enum PolenTaskType {
    SEEK_SAFETY(true),
    KEEP_DISTANCE(true),
    APPROACH_TRUSTED_PLAYER(false),
    INVESTIGATE_INTEREST(false),
    SEEK_REST(false),
    QUIET_CREATION(false),
    WANDER_SAFE(false);

    private final boolean urgent;

    PolenTaskType(boolean urgent) {
        this.urgent = urgent;
    }

    public boolean isUrgent() {
        return this.urgent;
    }

    public static PolenTaskType fromIntent(PolenIntent intent) {
        return switch (intent) {
            case SEEK_SAFETY -> SEEK_SAFETY;
            case KEEP_DISTANCE -> KEEP_DISTANCE;
            case APPROACH_TRUSTED_PLAYER -> APPROACH_TRUSTED_PLAYER;
            case INVESTIGATE_INTEREST -> INVESTIGATE_INTEREST;
            case SEEK_REST -> SEEK_REST;
            case QUIET_CREATION -> QUIET_CREATION;
            case WANDER_SAFE -> WANDER_SAFE;
        };
    }
}
