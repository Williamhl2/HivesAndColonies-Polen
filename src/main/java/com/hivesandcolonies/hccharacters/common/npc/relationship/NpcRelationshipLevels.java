package com.hivesandcolonies.hccharacters.common.npc.relationship;

public final class NpcRelationshipLevels {
    public static final int MIN_AFFINITY = 0;
    public static final int MAX_AFFINITY = 100;

    private NpcRelationshipLevels() {
    }

    public static int clamp(int affinity) {
        return Math.max(MIN_AFFINITY, Math.min(MAX_AFFINITY, affinity));
    }

    public static int rankIndex(int affinity) {
        int value = clamp(affinity);
        if (value >= 75) {
            return 4;
        }
        if (value >= 50) {
            return 3;
        }
        if (value >= 25) {
            return 2;
        }
        if (value >= 10) {
            return 1;
        }
        return 0;
    }

    public static String defaultRankName(int affinity) {
        return switch (rankIndex(affinity)) {
            case 4 -> "Confianza alta";
            case 3 -> "Confiable";
            case 2 -> "Cercano";
            case 1 -> "Conocido";
            default -> "Desconocido";
        };
    }
}
