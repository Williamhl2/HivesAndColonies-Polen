package com.hivesandcolonies.characters.character.polen.entity.ai.world.comfort;

public record PolenComfortProfile(
        int scanRadius,
        int verticalRadius,
        boolean includeModdedSignals
) {
    public static final PolenComfortProfile SHELTER = new PolenComfortProfile(4, 2, true);
    public static final PolenComfortProfile RESIDENCE = new PolenComfortProfile(5, 2, true);
    public static final PolenComfortProfile DEBUG = new PolenComfortProfile(5, 2, true);

    public PolenComfortProfile {
        scanRadius = Math.max(1, scanRadius);
        verticalRadius = Math.max(0, verticalRadius);
    }
}
