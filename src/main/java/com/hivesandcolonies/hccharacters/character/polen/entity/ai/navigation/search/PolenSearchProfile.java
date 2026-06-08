package com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.search;

public record PolenSearchProfile(
        PolenSearchDomain domain,
        int[] searchRadii,
        int[] verticalOffsets,
        int verticalLimit,
        int blinkDistance,
        boolean requireSafeBlink
) {
}
