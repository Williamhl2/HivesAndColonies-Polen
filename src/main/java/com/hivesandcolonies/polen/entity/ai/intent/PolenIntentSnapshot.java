package com.hivesandcolonies.polen.entity.ai.intent;

public record PolenIntentSnapshot(
        PolenIntent intent,
        String reason,
        long lockedUntilGameTime
) {
}
