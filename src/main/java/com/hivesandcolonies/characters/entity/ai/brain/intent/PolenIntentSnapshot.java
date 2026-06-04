package com.hivesandcolonies.characters.entity.ai.brain.intent;

public record PolenIntentSnapshot(
        PolenIntent intent,
        String reason,
        long lockedUntilGameTime
) {
}
