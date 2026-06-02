package com.hivesandcolonies.polen.entity.ai.brain.mood;

public enum PolenMood {
    CALM(0),
    TIMID(1),
    CURIOUS(2),
    INSPIRED(3),
    UNSETTLED(4),
    CONFIDENT(5),
    JOYFUL(6);

    private final int id;

    PolenMood(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static PolenMood fromId(int id) {
        for (PolenMood mood : values()) {
            if (mood.id == id) {
                return mood;
            }
        }

        return CALM;
    }
}
