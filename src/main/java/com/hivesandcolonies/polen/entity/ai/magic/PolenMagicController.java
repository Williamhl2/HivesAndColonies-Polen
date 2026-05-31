package com.hivesandcolonies.polen.entity.ai.magic;

import com.hivesandcolonies.polen.dialogue.PolenDialogueManager;
import com.hivesandcolonies.polen.entity.PolenAmbientDialogueController;
import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.ai.PolenMood;
import com.hivesandcolonies.polen.entity.ai.activity.PolenQuietActivityController;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public final class PolenMagicController {

    private static final int QUIET_MAGIC_INTERVAL = 24;
    private static final int MAGIC_DIALOGUE_CHANCE = 5;

    private PolenMagicController() {
    }

    public static void tickQuietMagic(PolenEntity polen) {
        if (!(polen.level() instanceof ServerLevel serverLevel)
                || !PolenQuietActivityController.isDoingQuietActivity(polen)
                || !canUseSubtleMagic(polen)
                || polen.tickCount % QUIET_MAGIC_INTERVAL != 0) {
            return;
        }

        switch (polen.getQuietActivityType()) {
            case PolenQuietActivityController.QUIET_ACTIVITY_SINGING -> spawnSingingSpell(serverLevel, polen);
            case PolenQuietActivityController.QUIET_ACTIVITY_DRAWING -> spawnDrawingSpell(serverLevel, polen);
            default -> {
                return;
            }
        }

        if (polen.getRandom().nextInt(MAGIC_DIALOGUE_CHANCE) == 0) {
            PolenAmbientDialogueController.tryPlay(polen, PolenDialogueManager.AMBIENT_MAGIC);
        }
    }

    public static boolean blinkToSafety(PolenEntity polen, BlockPos target) {
        if (target == null || !(polen.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        double fromX = polen.getX();
        double fromY = polen.getY();
        double fromZ = polen.getZ();
        double toX = target.getX() + 0.5D;
        double toY = target.getY();
        double toZ = target.getZ() + 0.5D;

        spawnBlinkBurst(serverLevel, fromX, fromY + 0.9D, fromZ);
        serverLevel.playSound(
                null,
                fromX,
                fromY,
                fromZ,
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.NEUTRAL,
                0.55F,
                1.35F
        );

        polen.teleportTo(toX, toY, toZ);

        spawnBlinkBurst(serverLevel, toX, toY + 0.9D, toZ);
        serverLevel.playSound(
                null,
                toX,
                toY,
                toZ,
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.NEUTRAL,
                0.55F,
                1.15F
        );
        PolenAmbientDialogueController.tryPlay(polen, PolenDialogueManager.AMBIENT_MAGIC);
        return true;
    }

    private static boolean canUseSubtleMagic(PolenEntity polen) {
        PolenMood mood = polen.getMood();
        return mood != PolenMood.TIMID && mood != PolenMood.UNSETTLED;
    }

    private static void spawnSingingSpell(ServerLevel serverLevel, PolenEntity polen) {
        double x = polen.getX();
        double y = polen.getEyeY() + 0.15D;
        double z = polen.getZ();
        serverLevel.sendParticles(ParticleTypes.END_ROD, x, y, z, 2, 0.18D, 0.12D, 0.18D, 0.01D);
        serverLevel.sendParticles(ParticleTypes.ENCHANT, x, y + 0.1D, z, 4, 0.25D, 0.08D, 0.25D, 0.02D);
    }

    private static void spawnDrawingSpell(ServerLevel serverLevel, PolenEntity polen) {
        double x = polen.getX();
        double y = polen.getY() + 0.2D;
        double z = polen.getZ();
        serverLevel.sendParticles(ParticleTypes.ENCHANT, x, y, z, 6, 0.28D, 0.06D, 0.28D, 0.03D);
        serverLevel.sendParticles(ParticleTypes.END_ROD, x, y + 0.35D, z, 1, 0.14D, 0.04D, 0.14D, 0.01D);
    }

    private static void spawnBlinkBurst(ServerLevel serverLevel, double x, double y, double z) {
        serverLevel.sendParticles(ParticleTypes.PORTAL, x, y, z, 16, 0.35D, 0.45D, 0.35D, 0.12D);
        serverLevel.sendParticles(ParticleTypes.END_ROD, x, y, z, 10, 0.28D, 0.35D, 0.28D, 0.03D);
        serverLevel.sendParticles(ParticleTypes.ENCHANT, x, y, z, 12, 0.30D, 0.30D, 0.30D, 0.05D);
    }
}
