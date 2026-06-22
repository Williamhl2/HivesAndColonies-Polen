package com.hivesandcolonies.hccharacters.character.polen.dialogue;

public record PolenDialogueCue(
        String situation,
        String beat,
        int variations,
        int weight
) {
    public String family() {
        return this.beat == null || this.beat.isBlank()
                ? this.situation
                : this.situation + "#" + this.beat;
    }
}
