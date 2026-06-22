package com.hivesandcolonies.hccharacters.character.polen.entity.ai.core;

import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.intent.PolenIntent;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.task.PolenTaskController;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.task.PolenTaskType;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.expression.activity.PolenQuietActivityController;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.search.PolenSearchStatus;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.search.PolenSearchType;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.environment.PolenEnvironmentResolver;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.environment.PolenEnvironmentSnapshot;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home.PolenHomeManager;
import net.minecraft.server.level.ServerLevel;

public final class PolenRainRestController {
    private static final long RAIN_REST_LOCK_TICKS = 120L;
    private static final int RAIN_REFLECT_TICKS = 100;
    private static final double RAIN_HOME_RETURN_RADIUS = 96.0D;

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
        PolenEnvironmentSnapshot environment = PolenEnvironmentResolver.inspect(polen);
        return environment.raining()
                && !PolenSleepController.shouldPrioritizeBedReturn(polen)
                && environment.rainSheltered()
                && !shouldReturnToKnownHomeForRest(polen)
                && !environment.immediateThreat()
                && polen.onGround()
                && !polen.isInWaterOrBubble();
    }

    private static boolean shouldReturnToKnownHomeForRest(PolenEntity polen) {
        return PolenHomeManager.hasHomeCenter(polen)
                && !PolenHomeManager.isNearHomeCenter(polen, 3.5D)
                && !PolenHomeManager.isFarFromHome(polen, RAIN_HOME_RETURN_RADIUS);
    }
}
