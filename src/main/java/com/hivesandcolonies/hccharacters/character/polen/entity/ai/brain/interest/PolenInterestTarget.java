package com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.interest;

import net.minecraft.core.BlockPos;

public record PolenInterestTarget(BlockPos pos, BlockPos observePos, PolenInterestType type) {
    public PolenInterestTarget(BlockPos pos, PolenInterestType type) {
        this(pos, pos, type);
    }
}
