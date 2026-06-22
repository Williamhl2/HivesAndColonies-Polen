package com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home;

import net.minecraft.core.BlockPos;

public record PolenHomeSnapshot(
        PolenResidenceTarget residence,
        PolenBedTarget homeBed,
        BlockPos restingPos
) {
    public PolenHomeSnapshot {
        restingPos = restingPos == null ? null : restingPos.immutable();
    }

    public BlockPos residenceUsePos() {
        return this.residence == null || this.residence.usePos() == null
                ? null
                : this.residence.usePos().immutable();
    }

    public BlockPos homeCenterPos() {
        if (this.homeBed != null && this.homeBed.accessPos() != null) {
            return this.homeBed.accessPos().immutable();
        }
        if (this.residence != null && this.residence.usePos() != null) {
            return this.residence.usePos().immutable();
        }
        return this.restingPos == null ? null : this.restingPos.immutable();
    }
}
