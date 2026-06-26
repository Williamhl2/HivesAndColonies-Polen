package com.hivesandcolonies.hccharacters.character.polen.dialogue;

public record PolenDialoguePolicy(
        long cooldownTicks,
        long repeatSituationCooldownTicks,
        int passiveChanceDivisor
) {
    public long normalizedCooldownTicks() {
        return Math.max(0L, this.cooldownTicks);
    }

    public long normalizedRepeatSituationCooldownTicks() {
        return Math.max(0L, this.repeatSituationCooldownTicks);
    }

    public int normalizedPassiveChanceDivisor() {
        return Math.max(1, this.passiveChanceDivisor);
    }
}
