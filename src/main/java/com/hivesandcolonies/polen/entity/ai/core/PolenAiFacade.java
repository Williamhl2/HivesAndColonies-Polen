package com.hivesandcolonies.polen.entity.ai.core;

import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.PolenGoalRegistry;
import com.hivesandcolonies.polen.entity.ai.expression.activity.PolenQuietActivityController;
import com.hivesandcolonies.polen.entity.ai.brain.action.PolenAutonomousActionPlan;
import com.hivesandcolonies.polen.entity.ai.core.PolenAutonomyController;
import com.hivesandcolonies.polen.entity.ai.expression.gesture.PolenGestureController;
import com.hivesandcolonies.polen.entity.ai.ability.magic.PolenMagicController;
import com.hivesandcolonies.polen.entity.ai.brain.memory.PolenMemoryHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.GoalSelector;

public final class PolenAiFacade {

    private PolenAiFacade() {
    }

    public static void tickClient(PolenEntity polen) {
        PolenQuietActivityController.tickClientParticles(polen);
    }

    public static void tickServer(PolenEntity polen) {
        PolenGestureController.tickServer(polen);
        PolenAutonomyController.tickServer(polen);
        PolenQuietActivityController.tickServer(polen);
        PolenMagicController.tickPersistentMagic(polen);
    }

    public static void registerGoals(PolenEntity polen, GoalSelector goalSelector) {
        PolenGoalRegistry.register(polen, goalSelector);
    }

    public static boolean isDoingQuietActivity(PolenEntity polen) {
        return PolenQuietActivityController.isDoingQuietActivity(polen);
    }

    public static void startQuietActivity(PolenEntity polen, int activityType, int ticks) {
        PolenQuietActivityController.startQuietActivity(polen, activityType, ticks);
    }

    public static void stopQuietActivity(PolenEntity polen) {
        PolenQuietActivityController.stopQuietActivity(polen);
    }

    public static int pickQuietActivity(PolenEntity polen) {
        return PolenQuietActivityController.pickQuietActivity(polen);
    }

    public static PolenAutonomousActionPlan pickQuietActionPlan(PolenEntity polen) {
        return PolenQuietActivityController.pickQuietAction(polen);
    }

    public static String getQuietActivityName(PolenEntity polen) {
        return PolenQuietActivityController.getQuietActivityName(polen);
    }

    public static void rememberInterestingSpot(PolenEntity polen, BlockPos pos) {
        PolenMemoryHandler.rememberInterestingSpot(polen, pos);
    }

    public static void rememberRestingSpot(PolenEntity polen, BlockPos pos) {
        PolenMemoryHandler.rememberRestingSpot(polen, pos);
    }
}
