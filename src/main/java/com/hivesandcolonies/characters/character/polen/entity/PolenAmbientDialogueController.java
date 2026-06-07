package com.hivesandcolonies.characters.character.polen.entity;

import com.hivesandcolonies.characters.character.polen.dialogue.PolenDialogueManager;
import com.hivesandcolonies.characters.character.polen.dialogue.PolenDialogueSituationResolver;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public final class PolenAmbientDialogueController {

    private static final long AMBIENT_DIALOGUE_COOLDOWN = 160L;
    private static final double AMBIENT_DIALOGUE_RANGE = 8.0D;
    private static final int CONTEXTUAL_DIALOGUE_CHECK_INTERVAL = 20;

    private PolenAmbientDialogueController() {
    }

    public static void tickContextualDialogue(PolenEntity polen) {
        if (polen == null || polen.tickCount % CONTEXTUAL_DIALOGUE_CHECK_INTERVAL != 0) {
            return;
        }

        String situation = PolenDialogueSituationResolver.resolveSituation(polen);
        if (situation == null || !shouldSpeakPassivelyThisTick(polen, situation)) {
            return;
        }

        tryPlay(polen, situation);
    }

    public static void tryPlay(PolenEntity polen, String situation) {
        tryPlay(polen, situation, false);
    }

    public static void tryPlayImmediate(PolenEntity polen, String situation) {
        tryPlay(polen, situation, true);
    }

    private static void tryPlay(PolenEntity polen, String situation, boolean ignoreCooldown) {
        if (!(polen.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        long gameTime = serverLevel.getGameTime();
        if (!ignoreCooldown && gameTime - polen.getAiState().getLastAmbientDialogueGameTime() < AMBIENT_DIALOGUE_COOLDOWN) {
            return;
        }

        List<ServerPlayer> nearbyPlayers = serverLevel.getEntitiesOfClass(
                ServerPlayer.class,
                polen.getBoundingBox().inflate(AMBIENT_DIALOGUE_RANGE)
        );
        if (nearbyPlayers.isEmpty()) {
            return;
        }

        boolean sentAny = false;
        for (ServerPlayer player : nearbyPlayers) {
            player.displayClientMessage(
                    PolenDialogueManager.getAmbientDialogue(player, situation, polen.getRandom()),
                    false
            );
            sentAny = true;
        }

        if (sentAny) {
            polen.getAiState().setLastAmbientDialogueGameTime(gameTime);
        }
    }

    private static boolean shouldSpeakPassivelyThisTick(PolenEntity polen, String situation) {
        if (PolenDialogueManager.AMBIENT_UNSAFE.equals(situation)
                || situation.startsWith("ambient_rain")
                || situation.startsWith("ambient_night")) {
            return polen.getRandom().nextInt(2) == 0;
        }

        if (polen.isDoingQuietActivity()) {
            return polen.getRandom().nextInt(3) == 0;
        }

        return polen.getRandom().nextInt(4) == 0;
    }
}
