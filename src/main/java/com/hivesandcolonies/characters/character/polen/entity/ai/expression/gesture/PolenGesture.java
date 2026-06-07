package com.hivesandcolonies.characters.character.polen.entity.ai.expression.gesture;

public enum PolenGesture {
    IDLE(0, "idle", true, 0),
    SINGING(1, "singing", true, 0),
    DRAWING(2, "drawing", true, 0),
    ATTUNING(3, "attuning", true, 0),
    ILLUMINATING(4, "illuminating", true, 0),
    REFLECTING(5, "reflecting", true, 0),
    CURIOUS(6, "curious", false, 50),
    APPROACHING(7, "approaching", false, 60),
    WITHDRAWN(8, "withdrawn", false, 55),
    STARTLED(9, "startled", false, 40);

    private final int id;
    private final String animationKey;
    private final boolean looping;
    private final int suggestedDurationTicks;

    PolenGesture(int id, String animationKey, boolean looping, int suggestedDurationTicks) {
        this.id = id;
        this.animationKey = animationKey;
        this.looping = looping;
        this.suggestedDurationTicks = suggestedDurationTicks;
    }

    public int getId() {
        return this.id;
    }

    public String getAnimationKey() {
        return this.animationKey;
    }

    public boolean isLooping() {
        return this.looping;
    }

    public int getSuggestedDurationTicks() {
        return this.suggestedDurationTicks;
    }

    public static PolenGesture fromId(int id) {
        for (PolenGesture gesture : values()) {
            if (gesture.id == id) {
                return gesture;
            }
        }

        return IDLE;
    }
}
