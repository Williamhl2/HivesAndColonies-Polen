package com.hivesandcolonies.polen.entity.ai.activity;

import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.ai.PolenMood;
import net.minecraft.core.particles.ParticleTypes;

public final class PolenQuietActivityController {

    public static final int QUIET_ACTIVITY_NONE = 0;
    public static final int QUIET_ACTIVITY_SINGING = 1;
    public static final int QUIET_ACTIVITY_DRAWING = 2;

    private PolenQuietActivityController() {
    }

    public static boolean isDoingQuietActivity(PolenEntity polen) {
        return polen.getQuietActivityType() != QUIET_ACTIVITY_NONE;
    }

    public static void startQuietActivity(PolenEntity polen, int activityType, int ticks) {
        polen.setQuietActivityState(activityType, ticks);
    }

    public static void stopQuietActivity(PolenEntity polen) {
        if (polen.getQuietActivityType() != QUIET_ACTIVITY_NONE || polen.getQuietActivityTicks() != 0) {
            polen.setQuietActivityState(QUIET_ACTIVITY_NONE, 0);
        }
    }

    public static void tickServer(PolenEntity polen) {
        int quietActivityTicks = polen.getQuietActivityTicks();
        if (quietActivityTicks <= 0) {
            return;
        }

        if (quietActivityTicks == 1) {
            stopQuietActivity(polen);
            return;
        }

        polen.setQuietActivityState(polen.getQuietActivityType(), quietActivityTicks - 1);
    }

    public static void tickClientParticles(PolenEntity polen) {
        int quietActivity = polen.getQuietActivityType();
        if (quietActivity == QUIET_ACTIVITY_NONE || polen.tickCount % 12 != 0) {
            return;
        }

        double x = polen.getX();
        double y = polen.getEyeY() + 0.2D;
        double z = polen.getZ();

        if (quietActivity == QUIET_ACTIVITY_SINGING) {
            polen.level().addParticle(
                    ParticleTypes.NOTE,
                    x,
                    y + 0.2D,
                    z,
                    polen.getRandom().nextDouble(),
                    0.0D,
                    0.0D
            );
            return;
        }

        if (quietActivity == QUIET_ACTIVITY_DRAWING) {
            double offsetX = (polen.getRandom().nextDouble() - 0.5D) * 0.4D;
            double offsetZ = (polen.getRandom().nextDouble() - 0.5D) * 0.4D;
            polen.level().addParticle(
                    ParticleTypes.ENCHANT,
                    x + offsetX,
                    y - 0.5D,
                    z + offsetZ,
                    0.0D,
                    0.02D,
                    0.0D
            );
        }
    }

    public static int pickQuietActivity(PolenEntity polen) {
        PolenMood mood = polen.getMood();

        if (mood == PolenMood.JOYFUL) {
            return polen.getRandom().nextInt(4) == 0
                    ? QUIET_ACTIVITY_DRAWING
                    : QUIET_ACTIVITY_SINGING;
        }

        if (mood == PolenMood.CONFIDENT || mood == PolenMood.INSPIRED || mood == PolenMood.CURIOUS) {
            return polen.getRandom().nextBoolean()
                    ? QUIET_ACTIVITY_SINGING
                    : QUIET_ACTIVITY_DRAWING;
        }

        return polen.getRandom().nextInt(3) == 0
                ? QUIET_ACTIVITY_SINGING
                : QUIET_ACTIVITY_DRAWING;
    }

    public static String getQuietActivityName(PolenEntity polen) {
        return switch (polen.getQuietActivityType()) {
            case QUIET_ACTIVITY_SINGING -> "singing";
            case QUIET_ACTIVITY_DRAWING -> "drawing";
            default -> "none";
        };
    }
}
