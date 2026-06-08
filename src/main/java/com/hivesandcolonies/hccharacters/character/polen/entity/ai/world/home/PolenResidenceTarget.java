package com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home;

import net.minecraft.core.BlockPos;

public record PolenResidenceTarget(
        BlockPos anchorPos,
        BlockPos usePos,
        String context,
        PolenResidenceStage stage
) {
}
