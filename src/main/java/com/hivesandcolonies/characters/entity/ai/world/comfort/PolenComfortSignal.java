package com.hivesandcolonies.characters.entity.ai.world.comfort;

public record PolenComfortSignal(
        PolenComfortCategory category,
        String key,
        int value
) {
    public PolenComfortSignal {
        if (category == null) {
            category = PolenComfortCategory.PENALTY;
        }
        if (key == null) {
            key = "unknown";
        }
    }
}
