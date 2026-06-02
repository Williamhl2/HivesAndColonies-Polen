package com.hivesandcolonies.polen.entity.ai.world.home;

public enum PolenResidenceStage {
    NONE,
    BORROWED_SHELTER,
    OWN_SPACE,
    INTEGRATED_RESIDENCE,
    LIVING_ARCHIVE;

    public static PolenResidenceStage fromName(String name) {
        if (name == null || name.isBlank()) {
            return NONE;
        }

        try {
            return PolenResidenceStage.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return NONE;
        }
    }
}
