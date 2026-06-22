package com.hivesandcolonies.hccharacters.character.polen.entity.ai.core;

import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.mood.PolenMood;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.environment.PolenEnvironmentSnapshot;

public final class PolenBehaviorReadiness {
    private PolenBehaviorReadiness() {
    }

    public static boolean isGroundedAndDryFooted(PolenEntity polen) {
        return polen != null && polen.onGround() && !polen.isInWaterOrBubble();
    }

    public static boolean isSafeOnFoot(PolenEntity polen, PolenEnvironmentSnapshot environment) {
        return polen != null
                && environment != null
                && !polen.isSleeping()
                && !environment.isInUnsafeArea()
                && isGroundedAndDryFooted(polen);
    }

    public static boolean isCalmDaytime(PolenEnvironmentSnapshot environment) {
        return environment != null && !environment.night() && !environment.raining();
    }

    public static boolean canStartQuietActivity(
            PolenEntity polen,
            PolenEnvironmentSnapshot environment,
            boolean settledNightMode
    ) {
        if (!isSafeOnFoot(polen, environment) || polen.isDoingQuietActivity()) {
            return false;
        }

        if (settledNightMode) {
            return true;
        }

        PolenMood mood = polen.getMood();
        return !polen.hasNearbyPlayer(3.0D)
                && mood != PolenMood.TIMID
                && mood != PolenMood.UNSETTLED;
    }

    public static boolean canContinueQuietActivity(PolenEntity polen, PolenEnvironmentSnapshot environment) {
        return polen != null
                && environment != null
                && polen.isDoingQuietActivity()
                && !polen.isSleeping()
                && !PolenSleepController.shouldPrioritizeBedReturn(polen)
                && !environment.isInUnsafeArea()
                && !polen.hasNearbyPlayer(2.5D)
                && isGroundedAndDryFooted(polen);
    }

    public static boolean canSeekRest(
            PolenEntity polen,
            PolenEnvironmentSnapshot environment,
            boolean hasRestAnchor,
            boolean atRestSpot,
            boolean honoringReturnHome
    ) {
        boolean adverseRestTime = environment != null && (environment.night() || environment.raining());
        return hasRestAnchor
                && isSafeOnFoot(polen, environment)
                && (adverseRestTime || !atRestSpot || honoringReturnHome);
    }

    public static boolean canWanderSafely(
            PolenEntity polen,
            PolenEnvironmentSnapshot environment,
            boolean settledNightMode,
            boolean farFromHome
    ) {
        return isSafeOnFoot(polen, environment)
                && !settledNightMode
                && !farFromHome
                && !polen.isDoingQuietActivity();
    }

    public static boolean canStartTrustedApproach(PolenEntity polen, PolenEnvironmentSnapshot environment) {
        return isSafeOnFoot(polen, environment)
                && isCalmDaytime(environment)
                && !polen.isDoingQuietActivity();
    }

    public static boolean canContinueTrustedApproach(PolenEntity polen, PolenEnvironmentSnapshot environment) {
        return polen != null
                && environment != null
                && !polen.isSleeping()
                && isCalmDaytime(environment)
                && !environment.isInUnsafeArea();
    }

    public static boolean canStartInterestInvestigation(PolenEntity polen, PolenEnvironmentSnapshot environment) {
        return isSafeOnFoot(polen, environment)
                && isCalmDaytime(environment)
                && !polen.isDoingQuietActivity()
                && !polen.hasNearbyPlayer(2.5D);
    }

    public static boolean canContinueInterestInvestigation(PolenEntity polen, PolenEnvironmentSnapshot environment) {
        return polen != null
                && environment != null
                && !polen.isSleeping()
                && isCalmDaytime(environment)
                && !environment.isInUnsafeArea()
                && !polen.hasNearbyPlayer(2.0D);
    }
}
