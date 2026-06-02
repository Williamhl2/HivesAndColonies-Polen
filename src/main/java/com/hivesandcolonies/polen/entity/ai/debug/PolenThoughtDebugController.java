package com.hivesandcolonies.polen.entity.ai.debug;

import com.hivesandcolonies.polen.entity.PolenEntity;
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
                        + ", search=" + snapshot.searchType() + "/" + snapshot.searchStatus()
                        + ", note=" + snapshot.searchNote()
        );

        boolean sentAny = false;
        for (ServerPlayer player : serverLevel.getEntitiesOfClass(
                ServerPlayer.class,
                polen.getBoundingBox().inflate(THOUGHT_DEBUG_RANGE)
        )) {
            player.displayClientMessage(
                    Component.translatable("entity.polen.polen")
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
                + "|" + snapshot.quietActivity();
    }
}
