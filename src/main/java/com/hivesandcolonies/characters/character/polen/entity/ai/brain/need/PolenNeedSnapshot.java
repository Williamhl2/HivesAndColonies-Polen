package com.hivesandcolonies.characters.character.polen.entity.ai.brain.need;

public record PolenNeedSnapshot(
        int safety,
        int social,
        int curiosity,
        int rest,
        int magic,
        PolenNeed dominantNeed
) {
}
