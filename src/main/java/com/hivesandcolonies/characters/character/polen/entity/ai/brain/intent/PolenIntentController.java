package com.hivesandcolonies.characters.character.polen.entity.ai.brain.intent;

import com.hivesandcolonies.characters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.interest.PolenInterestLocator;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.interest.PolenInterestType;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.need.PolenNeedController;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.need.PolenNeedSnapshot;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.action.PolenAutonomousActionPlanner;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.mood.PolenMood;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.routine.PolenRoutinePlanner;
import com.hivesandcolonies.characters.character.polen.entity.ai.navigation.safety.PolenSafetyNavigator;
import com.hivesandcolonies.characters.character.polen.entity.ai.world.affordance.PolenAffordanceResolver;
import com.hivesandcolonies.characters.character.polen.entity.ai.world.home.PolenHomeManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class PolenIntentController {

    private static final double TRUSTED_PLAYER_RANGE = 10.0D;
    private static final double UNTRUSTED_CLOSE_RANGE = 2.5D;
    private static final double REST_ARRIVAL_DISTANCE_SQR = 4.0D;

    private PolenIntentController() {
    }

    public static void tick(PolenEntity polen) {
        if (!(polen.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        long gameTime = serverLevel.getGameTime();
        PolenNeedSnapshot needs = PolenNeedController.inspect(polen);
        PolenIntentState state = polen.getAiState().getIntentState();

        if (PolenSafetyNavigator.shouldSeekSafety(polen)) {
            state.set(PolenIntent.SEEK_SAFETY, "unsafe_area_detected", gameTime + 40L);
            return;
        }

        if (hasUntrustedPlayerTooClose(polen)) {
            state.set(PolenIntent.KEEP_DISTANCE, "personal_space_invaded", gameTime + 50L);
            return;
        }

        if (shouldKeepCurrentIntent(polen, needs, state, gameTime)) {
            return;
        }

        PolenIntentSnapshot next = selectIntent(polen, needs, gameTime);
        state.set(next.intent(), next.reason(), next.lockedUntilGameTime());
    }

    public static PolenIntentSnapshot inspect(PolenEntity polen) {
        PolenIntentState state = polen.getAiState().getIntentState();
        return new PolenIntentSnapshot(
                state.currentIntent(),
                state.currentReason(),
                state.lockedUntilGameTime()
        );
    }

    private static boolean shouldKeepCurrentIntent(
            PolenEntity polen,
            PolenNeedSnapshot needs,
            PolenIntentState state,
            long gameTime
    ) {
        if (gameTime >= state.lockedUntilGameTime()) {
            return false;
        }

        return switch (state.currentIntent()) {
            case SEEK_SAFETY -> PolenSafetyNavigator.shouldSeekSafety(polen);
            case KEEP_DISTANCE -> hasUntrustedPlayerTooClose(polen);
            case APPROACH_TRUSTED_PLAYER -> hasApproachableTrustedPlayer(polen);
            case INVESTIGATE_INTEREST -> PolenAffordanceResolver.findBestInterest(polen, true) != null;
            case SEEK_REST -> (needs.rest() >= 35 || polen.level().isNight() || polen.level().isRaining())
                    && canSeekRest(polen);
            case QUIET_CREATION -> needs.magic() >= 20 && canDoIllumination(polen)
                    || needs.magic() >= 35 && canDoQuietCreation(polen);
            case WANDER_SAFE -> canWanderSafely(polen);
        };
    }

    private static PolenIntentSnapshot selectIntent(PolenEntity polen, PolenNeedSnapshot needs, long gameTime) {
        if (needs.social() >= 60 && hasApproachableTrustedPlayer(polen)) {
            return locked(PolenIntent.APPROACH_TRUSTED_PLAYER, "social_need_high", gameTime, 100L);
        }

        if (needs.rest() >= 32 && canDoQuietCreation(polen)
                && PolenAutonomousActionPlanner.shouldReflect(polen, polen.getMood())) {
            return locked(PolenIntent.QUIET_CREATION, "reflective_pause_available", gameTime, 90L);
        }

        if (needs.magic() >= 16 && canDoIllumination(polen) && hasLightMagicTarget(polen)) {
            return locked(PolenIntent.QUIET_CREATION, "darkness_needs_light", gameTime, 100L);
        }

        if (needs.magic() >= 52 && canDoQuietCreation(polen) && hasSourceForQuietCreation(polen)) {
            return locked(PolenIntent.QUIET_CREATION, "source_attunement_needed", gameTime, 120L);
        }

        if ((needs.curiosity() >= 52 || needs.magic() >= 56)
                && PolenAffordanceResolver.findBestInterest(polen, true) != null) {
            return locked(PolenIntent.INVESTIGATE_INTEREST, "interesting_target_available", gameTime, 120L);
        }

        if (needs.rest() >= 55 && canSeekRest(polen)) {
            return locked(PolenIntent.SEEK_REST, "rest_need_high", gameTime, 120L);
        }

        if (needs.magic() >= 48 && canDoQuietCreation(polen)) {
            return locked(PolenIntent.QUIET_CREATION, "magic_need_high", gameTime, 100L);
        }

        if (needs.social() >= 38 && hasApproachableTrustedPlayer(polen)) {
            return locked(PolenIntent.APPROACH_TRUSTED_PLAYER, "social_need_rising", gameTime, 80L);
        }

        if (needs.curiosity() >= 38 && PolenAffordanceResolver.findBestInterest(polen, true) != null) {
            return locked(PolenIntent.INVESTIGATE_INTEREST, "curiosity_need_rising", gameTime, 100L);
        }

        if (needs.magic() >= 12 && canDoIllumination(polen) && hasLightMagicTarget(polen)) {
            return locked(PolenIntent.QUIET_CREATION, "light_magic_available", gameTime, 80L);
        }

        if (needs.magic() >= 38 && canDoQuietCreation(polen) && hasSourceForQuietCreation(polen)) {
            return locked(PolenIntent.QUIET_CREATION, "source_magic_available", gameTime, 90L);
        }

        if (needs.rest() >= 38 && canSeekRest(polen)) {
            return locked(PolenIntent.SEEK_REST, "rest_need_rising", gameTime, 90L);
        }

        if (needs.magic() >= 34 && canDoQuietCreation(polen)) {
            return locked(PolenIntent.QUIET_CREATION, "quiet_magic_possible", gameTime, 80L);
        }

        return locked(PolenIntent.WANDER_SAFE, "default_safe_wander", gameTime, 60L);
    }

    private static boolean canDoQuietCreation(PolenEntity polen) {
        PolenMood mood = polen.getMood();
        return !polen.isDoingQuietActivity()
                && !polen.hasNearbyPlayer(3.0D)
                && !PolenSafetyNavigator.isInUnsafeArea(polen)
                && polen.onGround()
                && !polen.isInWaterOrBubble()
                && mood != PolenMood.TIMID
                && mood != PolenMood.UNSETTLED;
    }

    private static boolean canDoIllumination(PolenEntity polen) {
        return !polen.isDoingQuietActivity()
                && !polen.hasNearbyPlayer(3.0D)
                && !PolenSafetyNavigator.isInUnsafeArea(polen)
                && polen.onGround()
                && !polen.isInWaterOrBubble();
    }

    private static boolean canSeekRest(PolenEntity polen) {
        return (polen.getAiState().getRestingPos() != null || PolenHomeManager.getValidResidenceUsePos(polen) != null)
                && !isAtRestSpot(polen)
                && !PolenSafetyNavigator.isInUnsafeArea(polen);
    }

    private static boolean canWanderSafely(PolenEntity polen) {
        return !PolenSafetyNavigator.isInUnsafeArea(polen)
                && !polen.isDoingQuietActivity()
                && polen.onGround()
                && !polen.isInWaterOrBubble();
    }

    private static boolean hasSourceForQuietCreation(PolenEntity polen) {
        return PolenInterestLocator.findPreferredInterestOfType(polen, PolenInterestType.SOURCE) != null;
    }

    private static boolean hasLightMagicTarget(PolenEntity polen) {
        return PolenRoutinePlanner.findLightMagicTarget(polen) != null;
    }

    private static boolean isAtRestSpot(PolenEntity polen) {
        if (PolenHomeManager.isNearResidence(polen)) {
            return true;
        }

        return polen.getAiState().getRestingPos() != null
                && polen.distanceToSqr(Vec3.atCenterOf(polen.getAiState().getRestingPos())) <= REST_ARRIVAL_DISTANCE_SQR;
    }

    private static boolean hasApproachableTrustedPlayer(PolenEntity polen) {
        Player player = polen.level().getNearestPlayer(polen, TRUSTED_PLAYER_RANGE);
        return player != null
                && player.isAlive()
                && polen.isComfortableWith(player)
                && polen.hasLineOfSight(player)
                && polen.distanceToSqr(player) > 9.0D;
    }

    private static boolean hasUntrustedPlayerTooClose(PolenEntity polen) {
        Player player = polen.level().getNearestPlayer(polen, UNTRUSTED_CLOSE_RANGE);
        return player != null && !polen.isComfortableWith(player);
    }

    private static PolenIntentSnapshot locked(PolenIntent intent, String reason, long gameTime, long lockTicks) {
        return new PolenIntentSnapshot(intent, reason, gameTime + lockTicks);
    }
}
