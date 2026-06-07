package com.hivesandcolonies.characters.character.polen.progression;

public final class PolenAffinityLevels {

    private PolenAffinityLevels() {}

    public static final int MIN_AFFINITY = 0;
    public static final int MAX_AFFINITY = 100;

    public static final int STRANGER = 0;
    public static final int FIRST_TRUST = 10;
    public static final int NAME_REVEAL = 25;
    public static final int FRIEND = 50;
    public static final int CLOSE_FRIEND = 75;
    public static final int TRUSTED = 100;

    public static int clamp(int value) {
        return Math.max(MIN_AFFINITY, Math.min(MAX_AFFINITY, value));
    }
}