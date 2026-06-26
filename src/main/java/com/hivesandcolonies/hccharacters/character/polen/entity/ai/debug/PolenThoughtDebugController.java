package com.hivesandcolonies.hccharacters.character.polen.entity.ai.debug;

import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class PolenThoughtDebugController {
    private static final long THOUGHT_DEBUG_COOLDOWN = 20L;
    private static final double THOUGHT_DEBUG_RANGE = 16.0D;

    private PolenThoughtDebugController() {
    }

    public static void tick(PolenEntity polen) {
        if (!(polen.level() instanceof ServerLevel serverLevel) || !polen.getAiState().isDebugThoughtsEnabled()) {
            return;
        }

        PolenAiDebugSnapshot snapshot = PolenAiDebugInspector.inspect(polen);
        String signature = buildSignature(snapshot);
        long gameTime = serverLevel.getGameTime();
        if (signature.equals(polen.getAiState().getLastThoughtDebugSignature())
                || gameTime - polen.getAiState().getLastThoughtDebugGameTime() < THOUGHT_DEBUG_COOLDOWN) {
            return;
        }

        Component line = Component.literal(
                "[thought] task=" + snapshot.task()
                        + ", status=" + snapshot.taskStatus()
                        + ", safety=" + snapshot.shouldSeekSafety() + "/" + snapshot.unsafeArea()
                        + ", shelter=" + snapshot.shelterKind()
                        + ", search=" + snapshot.searchType() + "/" + snapshot.searchStatus()
                        + ", note=" + snapshot.searchNote()
                        + ", obs=" + snapshot.observationFocus() + "/" + snapshot.observationDisposition()
                        + "/" + snapshot.observationContext()
        );

        boolean sentAny = false;
        for (ServerPlayer player : serverLevel.getEntitiesOfClass(
                ServerPlayer.class,
                polen.getBoundingBox().inflate(THOUGHT_DEBUG_RANGE)
        )) {
            player.displayClientMessage(
                    Component.translatable("entity.hc_characters.polen")
                            .append(Component.literal(": "))
                            .append(line),
                    false
            );
            sentAny = true;
        }

        if (sentAny) {
            polen.getAiState().setLastThoughtDebugSignature(signature);
            polen.getAiState().setLastThoughtDebugGameTime(gameTime);
        }
    }

    private static String buildSignature(PolenAiDebugSnapshot snapshot) {
        return snapshot.task()
                + "|" + snapshot.taskStatus()
                + "|" + snapshot.searchType()
                + "|" + snapshot.searchStatus()
                + "|" + snapshot.searchNote()
                + "|" + snapshot.shouldSeekSafety()
                + "|" + snapshot.unsafeArea()
                + "|" + snapshot.shelterKind()
                + "|" + snapshot.observationFocus()
                + "|" + snapshot.observationDisposition()
                + "|" + snapshot.observationContext()
                + "|" + snapshot.observationNote()
                + "|" + snapshot.quietActivity();
    }
}
