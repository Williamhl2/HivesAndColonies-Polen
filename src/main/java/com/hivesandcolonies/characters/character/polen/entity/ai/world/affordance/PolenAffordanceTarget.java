package com.hivesandcolonies.characters.character.polen.entity.ai.world.affordance;

import net.minecraft.core.BlockPos;

public record PolenAffordanceTarget(
        BlockPos focusPos,
        BlockPos usePos,
        PolenAffordanceType type,
        String contextKey
) {
}
