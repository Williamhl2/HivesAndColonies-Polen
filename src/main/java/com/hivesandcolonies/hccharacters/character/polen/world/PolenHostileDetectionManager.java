package com.hivesandcolonies.hccharacters.character.polen.world;

import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

public final class PolenHostileDetectionManager {
    private static final String POLEN_TARGET_GOAL_TAG = "hc_characters.polen_target_goal";

    private PolenHostileDetectionManager() {
    }

    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Monster monster) || event.getLevel().isClientSide()) {
            return;
        }

        if (monster.getPersistentData().getBoolean(POLEN_TARGET_GOAL_TAG)) {
            return;
        }

        monster.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(monster, PolenEntity.class, true));
        monster.getPersistentData().putBoolean(POLEN_TARGET_GOAL_TAG, true);
    }
}
