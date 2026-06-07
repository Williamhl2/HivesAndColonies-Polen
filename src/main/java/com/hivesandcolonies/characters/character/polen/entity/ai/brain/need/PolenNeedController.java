package com.hivesandcolonies.characters.character.polen.entity.ai.brain.need;

import com.hivesandcolonies.characters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.interest.PolenInterestTarget;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.interest.PolenInterestType;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.interest.PolenInterestLocator;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.memory.PolenMemoryHandler;
import com.hivesandcolonies.characters.character.polen.entity.ai.world.home.PolenHomeManager;
import com.hivesandcolonies.characters.character.polen.entity.ai.navigation.safety.PolenSafetyNavigator;
import com.hivesandcolonies.characters.character.polen.progression.PolenStoryFlag;
import com.hivesandcolonies.characters.character.polen.progression.PolenStoryFlagsManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public final class PolenNeedController {

    private static final double TRUSTED_NEARBY_RANGE = 6.0D;
    private static final double UNTRUSTED_NEARBY_RANGE = 4.0D;

    private PolenNeedController() {
    }

    public static void tick(PolenEntity polen) {
        PolenNeedState state = polen.getAiState().getNeedState();

        boolean shouldSeekSafety = PolenSafetyNavigator.shouldSeekSafety(polen);
        boolean unsafeArea = PolenSafetyNavigator.isInUnsafeArea(polen);
        boolean trustedNearby = hasTrustedPlayerNearby(polen);
        boolean untrustedNearby = hasUntrustedPlayerNearby(polen);
        boolean nearInterest = PolenMemoryHandler.isNearRememberedInterest(polen);
        PolenInterestTarget localInterest = PolenInterestLocator.findNearestLocalInterest(
                polen,
                4,
                2,
                true,
                polen.level().getGameTime()
        );
        boolean nearSource = polen.getAiState().getFavoriteSourcePos() != null
                && polen.getAiState().getFavoriteSourcePos().closerToCenterThan(polen.position(), 4.0D)
                || localInterest != null && localInterest.type() == PolenInterestType.SOURCE;
        boolean atRest = PolenHomeManager.isNearResidence(polen)
                || polen.getAiState().getRestingPos() != null
                && polen.getAiState().getRestingPos().closerToCenterThan(polen.position(), 2.0D);
        boolean badWeather = polen.level().isThundering()
                || polen.level().isRaining() && polen.level().canSeeSky(polen.blockPosition());
        boolean shelterKnown = polen.level() instanceof ServerLevel serverLevel
                && PolenStoryFlagsManager.hasFlag(serverLevel, PolenStoryFlag.PLAYER_HAS_SHELTER);
        boolean homeKnown = shelterKnown
                && (polen.getAiState().getRestingPos() != null || PolenHomeManager.getValidResidenceUsePos(polen) != null);

        state.adjustSafety(shouldSeekSafety ? 26 : unsafeArea ? 12 : atRest ? -10 : -5);
        if (badWeather) {
            state.adjustSafety(atRest ? -4 : homeKnown ? 12 : 8);
        }

        state.adjustSocial(trustedNearby ? -14 : shelterKnown && !unsafeArea ? 5 : 2);
        if (untrustedNearby) {
            state.adjustSocial(-4);
        }

        state.adjustCuriosity(nearInterest ? -10 : !unsafeArea && !polen.level().isNight() ? 7 : 1);
        if (trustedNearby) {
            state.adjustCuriosity(2);
        }

        state.adjustRest(polen.level().isNight() || badWeather ? homeKnown && !atRest ? 14 : 10 : atRest ? -12 : 3);
        if (polen.isDoingQuietActivity()) {
            state.adjustRest(-8);
        }
        if (atRest) {
            state.adjustRest(-6);
        }

        state.adjustMagic(nearSource ? -14 : polen.isDoingQuietActivity() ? -10 : shelterKnown ? 2 : 1);
        if (unsafeArea) {
            state.adjustMagic(-3);
        }
    }

    public static PolenNeedSnapshot inspect(PolenEntity polen) {
        PolenNeedState state = polen.getAiState().getNeedState();
        return new PolenNeedSnapshot(
                state.safety(),
                state.social(),
                state.curiosity(),
                state.rest(),
                state.magic(),
                dominantNeed(state)
        );
    }

    private static PolenNeed dominantNeed(PolenNeedState state) {
        PolenNeed best = PolenNeed.SAFETY;
        int bestValue = state.safety();

        if (state.social() > bestValue) {
            best = PolenNeed.SOCIAL;
            bestValue = state.social();
        }

        if (state.curiosity() > bestValue) {
            best = PolenNeed.CURIOSITY;
            bestValue = state.curiosity();
        }

        if (state.rest() > bestValue) {
            best = PolenNeed.REST;
            bestValue = state.rest();
        }

        if (state.magic() > bestValue) {
            best = PolenNeed.MAGIC;
        }

        return best;
    }

    private static boolean hasTrustedPlayerNearby(PolenEntity polen) {
        Player player = polen.level().getNearestPlayer(polen, TRUSTED_NEARBY_RANGE);
        return player != null && polen.isComfortableWith(player);
    }

    private static boolean hasUntrustedPlayerNearby(PolenEntity polen) {
        Player player = polen.level().getNearestPlayer(polen, UNTRUSTED_NEARBY_RANGE);
        return player != null && !polen.isComfortableWith(player);
    }
}
