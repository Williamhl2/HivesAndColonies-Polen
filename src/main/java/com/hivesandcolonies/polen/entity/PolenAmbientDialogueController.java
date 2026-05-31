package com.hivesandcolonies.polen.entity;

import com.hivesandcolonies.polen.dialogue.PolenDialogueManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class PolenAmbientDialogueController {

    private static final long AMBIENT_DIALOGUE_COOLDOWN = 160L;
    private static final double AMBIENT_DIALOGUE_RANGE = 8.0D;

    private PolenAmbientDialogueController() {
    }

    public static void tryPlay(PolenEntity polen, String situation) {
        if (!(polen.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        long gameTime = serverLevel.getGameTime();
        if (gameTime - polen.getLastAmbientDialogueGameTime() < AMBIENT_DIALOGUE_COOLDOWN) {
            return;
        }

        boolean sentAny = false;
        for (ServerPlayer player : serverLevel.getEntitiesOfClass(
                ServerPlayer.class,
                polen.getBoundingBox().inflate(AMBIENT_DIALOGUE_RANGE)
        )) {
            player.displayClientMessage(
                    PolenDialogueManager.getAmbientDialogue(player, situation, polen.getRandom()),
                    false
            );
            sentAny = true;
        }

        if (sentAny) {
            polen.setLastAmbientDialogueGameTime(gameTime);
        }
    }
}
