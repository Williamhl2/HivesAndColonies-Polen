package com.hivesandcolonies.polen.entity.ai.core;

import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.ai.brain.intent.PolenIntent;
import com.hivesandcolonies.polen.entity.ai.brain.task.PolenTaskController;
import com.hivesandcolonies.polen.entity.ai.brain.task.PolenTaskType;
import com.hivesandcolonies.polen.entity.ai.expression.activity.PolenQuietActivityController;
import com.hivesandcolonies.polen.entity.ai.navigation.safety.PolenSafetyEvaluator;
import com.hivesandcolonies.polen.entity.ai.navigation.search.PolenSearchStatus;
import com.hivesandcolonies.polen.entity.ai.navigation.search.PolenSearchType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

public final class PolenRainRestController {
    private static final long RAIN_REST_LOCK_TICKS = 120L;
    private static final int RAIN_REFLECT_TICKS = 100;
    private static final double HOSTILE_THREAT_RANGE = 6.0D;
    private static final double UNTRUSTED_PLAYER_RANGE = 2.5D;

    private PolenRainRestController() {
    }

    public static void tickServer(PolenEntity polen) {
        if (!(polen.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!shouldRestInRain(polen)) {
            return;
        }

        long gameTime = serverLevel.getGameTime();
        polen.getNavigation().stop();
        polen.getAiState().getIntentState().set(
                PolenIntent.SEEK_REST,
                "rain_resting_under_shelter",
                gameTime + RAIN_REST_LOCK_TICKS
        );
        PolenTaskController.markActive(polen, PolenTaskType.SEEK_REST, "rain_resting_under_shelter");
        polen.getAiState().setSearchState(
                PolenSearchType.REST,
                PolenSearchStatus.ARRIVED,
                polen.blockPosition().immutable(),
                polen.blockPosition().immutable(),
                "waiting_for_rain_to_pass"
        );

        if (!polen.isDoingQuietActivity() && polen.tickCount % 140 == 0) {
            polen.startQuietActivity(PolenQuietActivityController.QUIET_ACTIVITY_REFLECTING, RAIN_REFLECT_TICKS);
        }
    }

    public static boolean shouldRestInRain(PolenEntity polen) {
        return polen.level().isRaining()
                && PolenSafetyEvaluator.isRainShelteredStandingSpot(polen.level(), polen.blockPosition())
                && !hasImmediateThreat(polen)
                && polen.onGround()
                && !polen.isInWaterOrBubble();
    }

    private static boolean hasImmediateThreat(PolenEntity polen) {
        boolean hostileNearby = !polen.level().getEntitiesOfClass(
                Monster.class,
                polen.getBoundingBox().inflate(HOSTILE_THREAT_RANGE),
                monster -> monster.isAlive() && monster.hasLineOfSight(polen)
        ).isEmpty();
        if (hostileNearby) {
            return true;
        }

        Player player = polen.level().getNearestPlayer(polen, UNTRUSTED_PLAYER_RANGE);
        return player != null && player.isAlive() && !polen.isComfortableWith(player);
    }
}
