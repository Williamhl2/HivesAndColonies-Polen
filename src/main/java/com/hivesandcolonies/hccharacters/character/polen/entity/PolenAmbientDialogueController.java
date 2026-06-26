package com.hivesandcolonies.hccharacters.character.polen.entity;

import com.hivesandcolonies.hccharacters.character.polen.dialogue.PolenDialogueManager;
import com.hivesandcolonies.hccharacters.character.polen.dialogue.PolenDialoguePolicy;
import com.hivesandcolonies.hccharacters.character.polen.dialogue.PolenDialoguePolicyResolver;
import com.hivesandcolonies.hccharacters.character.polen.dialogue.PolenDialogueSituationResolver;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.environment.PolenEnvironmentResolver;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.environment.PolenEnvironmentSnapshot;
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

        PolenEnvironmentSnapshot environment = PolenEnvironmentResolver.inspect(polen);
        String situation = PolenDialogueSituationResolver.resolveSituation(polen, environment);
        PolenDialoguePolicy policy = PolenDialoguePolicyResolver.resolveAmbientPolicy(polen, situation, environment);
        if (situation == null || !shouldSpeakPassivelyThisTick(polen, policy)) {
            return;
        }

        tryPlay(polen, situation, environment, policy, false);
    }

    public static void tryPlay(PolenEntity polen, String situation) {
        PolenEnvironmentSnapshot environment = PolenEnvironmentResolver.inspect(polen);
        tryPlay(
                polen,
                situation,
                environment,
                PolenDialoguePolicyResolver.resolveAmbientPolicy(polen, situation, environment),
                false
        );
    }

    public static void tryPlayImmediate(PolenEntity polen, String situation) {
        PolenEnvironmentSnapshot environment = PolenEnvironmentResolver.inspect(polen);
        tryPlay(
                polen,
                situation,
                environment,
                PolenDialoguePolicyResolver.resolveAmbientPolicy(polen, situation, environment),
                true
        );
    }

    private static void tryPlay(
            PolenEntity polen,
            String situation,
            PolenEnvironmentSnapshot environment,
            PolenDialoguePolicy policy,
            boolean ignoreCooldown
    ) {
        if (!(polen.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        long gameTime = serverLevel.getGameTime();
        long effectiveCooldown = policy.normalizedCooldownTicks() > 0L
                ? policy.normalizedCooldownTicks()
                : AMBIENT_DIALOGUE_COOLDOWN;
        if (!ignoreCooldown
                && gameTime - polen.getAiState().getLastAmbientDialogueGameTime() < effectiveCooldown) {
            return;
        }

        if (!ignoreCooldown && polen.getAiState().hasRecentAmbientDialogue(
                situation,
                gameTime,
                policy.normalizedRepeatSituationCooldownTicks()
        )) {
            return;
        }

        List<ServerPlayer> nearbyPlayers = serverLevel.getEntitiesOfClass(
                ServerPlayer.class,
                polen.getBoundingBox().inflate(AMBIENT_DIALOGUE_RANGE)
        );
        if (nearbyPlayers.isEmpty()) {
            return;
        }

        ServerPlayer referencePlayer = resolveReferencePlayer(polen, nearbyPlayers);
        String dialogueKey = PolenDialogueManager.resolveAmbientDialogueKey(
                referencePlayer,
                polen,
                situation,
                environment,
                polen.getRandom()
        );
        boolean sentAny = false;
        for (ServerPlayer player : nearbyPlayers) {
            player.displayClientMessage(
                    PolenDialogueManager.formatDialogueForPlayer(player, dialogueKey),
                    false
            );
            sentAny = true;
        }

        if (sentAny) {
            polen.getAiState().rememberAmbientDialogueBroadcast(situation, gameTime);
        }
    }

    private static boolean shouldSpeakPassivelyThisTick(PolenEntity polen, PolenDialoguePolicy policy) {
        return polen.getRandom().nextInt(policy.normalizedPassiveChanceDivisor()) == 0;
    }

    private static ServerPlayer resolveReferencePlayer(PolenEntity polen, List<ServerPlayer> nearbyPlayers) {
        ServerPlayer preferred = null;
        double bestDistanceSqr = Double.MAX_VALUE;
        for (ServerPlayer player : nearbyPlayers) {
            double distanceSqr = player.distanceToSqr(polen);
            if (polen.isComfortableWith(player) && distanceSqr < bestDistanceSqr) {
                preferred = player;
                bestDistanceSqr = distanceSqr;
            }
        }

        if (preferred != null) {
            return preferred;
        }

        return nearbyPlayers.stream()
                .min((left, right) -> Double.compare(left.distanceToSqr(polen), right.distanceToSqr(polen)))
                .orElse(nearbyPlayers.get(0));
    }
}
