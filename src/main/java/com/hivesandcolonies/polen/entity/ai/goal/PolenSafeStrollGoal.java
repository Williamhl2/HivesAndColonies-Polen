package com.hivesandcolonies.polen.entity.ai.goal;

import com.hivesandcolonies.polen.entity.PolenEntity;

import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.phys.Vec3;

public class PolenSafeStrollGoal extends RandomStrollGoal {
    private final PolenEntity polen;

    public PolenSafeStrollGoal(PolenEntity polen, double speedModifier) {
        super(polen, speedModifier);
        this.polen = polen;
    }

    @Override
    protected Vec3 getPosition() {
        for (int i = 0; i < 12; i++) {
            Vec3 candidate = super.getPosition();
            if (candidate == null) {
                continue;
            }

            if (this.polen.isSafeStandingSpot(net.minecraft.core.BlockPos.containing(candidate))
                    && !this.polen.isDangerousMemorySpot(net.minecraft.core.BlockPos.containing(candidate))) {
                return candidate;
            }
        }

        return this.polen.getNearestSafeSpotCenter(16);
    }
}
