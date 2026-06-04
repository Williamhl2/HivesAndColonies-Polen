package com.hivesandcolonies.characters.entity.ai.world.comfort;

public enum PolenComfortRank {
    DANGEROUS,
    POOR,
    BASIC,
    COMFORTABLE,
    COZY,
    HOME_LIKE;

    public static PolenComfortRank fromScore(int score) {
        if (score < 0) {
            return DANGEROUS;
        }
        if (score < 25) {
            return POOR;
        }
        if (score < 50) {
            return BASIC;
        }
        if (score < 80) {
            return COMFORTABLE;
        }
        if (score < 120) {
            return COZY;
        }
        return HOME_LIKE;
    }
}
