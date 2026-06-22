package com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home;

import net.minecraft.core.BlockPos;

public record PolenBedTarget(
        BlockPos anchorPos,
        BlockPos bedPos,
        BlockPos accessPos,
        boolean beeBed
) {
    public PolenBedTarget {
        anchorPos = anchorPos == null ? null : anchorPos.immutable();
        bedPos = bedPos == null ? null : bedPos.immutable();
        accessPos = accessPos == null ? null : accessPos.immutable();
    }
}
