package com.hivesandcolonies.polen.entity;

import com.hivesandcolonies.polen.entity.ai.navigation.goal.PolenApproachTrustedPlayerGoal;
import com.hivesandcolonies.polen.entity.ai.navigation.goal.PolenCuriousInterestGoal;
import com.hivesandcolonies.polen.entity.ai.navigation.goal.PolenIdleHobbyGoal;
import com.hivesandcolonies.polen.entity.ai.navigation.goal.PolenKeepDistanceGoal;
import com.hivesandcolonies.polen.entity.ai.navigation.goal.PolenRoutineGoal;
import com.hivesandcolonies.polen.entity.ai.navigation.goal.PolenSafeStrollGoal;
import com.hivesandcolonies.polen.entity.ai.navigation.goal.PolenSeekSafetyGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;

public final class PolenGoalRegistry {

    private PolenGoalRegistry() {
    }

    public static void register(PolenEntity polen, GoalSelector goalSelector) {
        goalSelector.addGoal(0, new FloatGoal(polen));
        goalSelector.addGoal(1, new PolenSeekSafetyGoal(polen));
        goalSelector.addGoal(2, new PolenKeepDistanceGoal(polen));
        goalSelector.addGoal(3, new PolenApproachTrustedPlayerGoal(polen));
        goalSelector.addGoal(4, new PolenRoutineGoal(polen));
        goalSelector.addGoal(5, new PolenIdleHobbyGoal(polen));
        goalSelector.addGoal(6, new PolenCuriousInterestGoal(polen));
        goalSelector.addGoal(7, new PolenSafeStrollGoal(polen, 0.8D));
        goalSelector.addGoal(8, new LookAtPlayerGoal(polen, Player.class, 8.0F));
        goalSelector.addGoal(9, new RandomLookAroundGoal(polen));
    }
}
