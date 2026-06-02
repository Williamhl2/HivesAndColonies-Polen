package com.hivesandcolonies.polen.entity.ai.brain.intent;

public record PolenIntentSnapshot(
        PolenIntent intent,
        String reason,
        long lockedUntilGameTime
) {
}
