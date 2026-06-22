package com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.goal;

import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.PolenDangerMemoryTracker;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.routine.PolenRoutinePlanner;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.task.PolenTaskController;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.task.PolenTaskType;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.core.PolenBehaviorReadiness;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.safety.PolenSafetyEvaluator;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.core.PolenRainRestController;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.environment.PolenEnvironmentSnapshot;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.environment.PolenEnvironmentResolver;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home.PolenHomeManager;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.phys.Vec3;

public class PolenSafeStrollGoal extends RandomStrollGoal {
    private static final int HOME_STROLL_RADIUS = 18;
    private static final double HOME_STROLL_HARD_LIMIT_RADIUS = 28.0D;

    private final PolenEntity polen;

    public PolenSafeStrollGoal(PolenEntity polen, double speedModifier) {
        super(polen, speedModifier);
        this.polen = polen;
    }

    @Override
    public boolean canUse() {
        return canStrollNow()
                && !PolenRainRestController.shouldRestInRain(this.polen)
                && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return canStrollNow() && super.canContinueToUse();
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

            BlockPos candidatePos = BlockPos.containing(candidate);
            if (isSafeHomeBoundedCandidate(candidatePos)) {
                return candidate;
            }
        }

        BlockPos homeTarget = PolenRoutinePlanner.findHomeAnchoredSafeWanderTarget(this.polen, HOME_STROLL_RADIUS);
        return homeTarget == null ? null : Vec3.atCenterOf(homeTarget);
    }

    private boolean canStrollNow() {
        PolenEnvironmentSnapshot environment = PolenEnvironmentResolver.inspect(this.polen);
        return PolenBehaviorReadiness.isSafeOnFoot(this.polen, environment)
                && this.polen.getCurrentTask() == PolenTaskType.WANDER_SAFE
                && PolenBehaviorReadiness.isCalmDaytime(environment)
                && !PolenHomeManager.isFarFromHome(this.polen, HOME_STROLL_HARD_LIMIT_RADIUS)
                && !this.polen.isDoingQuietActivity();
    }

    private boolean isSafeHomeBoundedCandidate(BlockPos candidatePos) {
        return PolenSafetyEvaluator.isSafeStandingSpot(this.polen, candidatePos)
                && !PolenDangerMemoryTracker.isDangerousMemorySpot(this.polen, candidatePos)
                && PolenHomeManager.isPositionWithinHomeRadius(this.polen, candidatePos, HOME_STROLL_RADIUS);
    }
}
