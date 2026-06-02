package com.hivesandcolonies.polen.entity.ai.navigation.goal;

import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.PolenDangerMemoryTracker;
import com.hivesandcolonies.polen.entity.ai.brain.task.PolenTaskController;
import com.hivesandcolonies.polen.entity.ai.brain.task.PolenTaskType;
import com.hivesandcolonies.polen.entity.ai.navigation.safety.PolenSafetyEvaluator;
import com.hivesandcolonies.polen.entity.ai.navigation.safety.PolenSafetyNavigator;

import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.phys.Vec3;

public class PolenSafeStrollGoal extends RandomStrollGoal {
    private final PolenEntity polen;

    public PolenSafeStrollGoal(PolenEntity polen, double speedModifier) {
        super(polen, speedModifier);
        this.polen = polen;
    }

    @Override
    public boolean canUse() {
        return this.polen.getCurrentTask() == PolenTaskType.WANDER_SAFE
                && !PolenSafetyNavigator.isInUnsafeArea(this.polen)
                && super.canUse();
    }

    @Override
    public void start() {
        PolenTaskController.markActive(this.polen, PolenTaskType.WANDER_SAFE, "safe_stroll_started");
        super.start();
    }

    @Override
    public void stop() {
        PolenTaskController.markCompleted(this.polen, PolenTaskType.WANDER_SAFE, "safe_stroll_finished");
        super.stop();
    }

    @Override
    protected Vec3 getPosition() {
        for (int i = 0; i < 12; i++) {
            Vec3 candidate = super.getPosition();
            if (candidate == null) {
                continue;
            }

            if (PolenSafetyEvaluator.isSafeStandingSpot(this.polen, net.minecraft.core.BlockPos.containing(candidate))
                    && !PolenDangerMemoryTracker.isDangerousMemorySpot(
                            this.polen,
                            net.minecraft.core.BlockPos.containing(candidate)
                    )) {
                return candidate;
            }
        }

        return PolenSafetyNavigator.getNearestSafeSpotCenter(this.polen, 16);
    }
}
