package com.hivesandcolonies.polen.client.profile;

public enum ProfileInterest {
    BEES("Bees"),
    MAGIC("Magic"),
    COLONIES("Colonies"),
    FOOD("Food"),
    DECORATION("Decoration"),
    EXPLORATION("Exploration"),
    NATURE("Nature");

    private final String displayName;

    ProfileInterest(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
