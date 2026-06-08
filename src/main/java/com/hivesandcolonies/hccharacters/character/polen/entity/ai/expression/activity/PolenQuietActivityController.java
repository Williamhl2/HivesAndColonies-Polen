package com.hivesandcolonies.hccharacters.character.polen.entity.ai.expression.activity;

import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.action.PolenAutonomousActionPlan;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.action.PolenAutonomousActionPlanner;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.ability.magic.PolenMagicController;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.routine.PolenRoutinePlanner;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.joml.Vector3f;

public final class PolenQuietActivityController {

    public static final int QUIET_ACTIVITY_NONE = 0;
    public static final int QUIET_ACTIVITY_SINGING = 1;
    public static final int QUIET_ACTIVITY_DRAWING = 2;
    public static final int QUIET_ACTIVITY_ATTUNING = 3;
    public static final int QUIET_ACTIVITY_ILLUMINATING = 4;
    public static final int QUIET_ACTIVITY_REFLECTING = 5;

    private static final Vector3f MAGIC_GREEN = new Vector3f(0.32F, 0.92F, 0.45F);
    private static final Vector3f MAGIC_PURPLE = new Vector3f(0.72F, 0.28F, 0.94F);

    private PolenQuietActivityController() {
    }

    public static boolean isDoingQuietActivity(PolenEntity polen) {
        return polen.getQuietActivityType() != QUIET_ACTIVITY_NONE;
    }

    public static void startQuietActivity(PolenEntity polen, int activityType, int ticks) {
        polen.setQuietActivityState(activityType, ticks);
        if (activityType == QUIET_ACTIVITY_ILLUMINATING) {
            PolenMagicController.tryPlaceManagedLightImmediately(polen);
        }
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

        if (polen.getQuietActivityType() == QUIET_ACTIVITY_ILLUMINATING) {
            PolenMagicController.tryPlaceManagedLightImmediately(polen);
        }
        PolenMagicController.tickQuietMagic(polen);
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
            addMagicDustParticle(polen, x, y, z, 0.75F, true);
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
            addMagicDustParticle(polen, x + offsetX, y - 0.2D, z + offsetZ, 0.65F, false);
            return;
        }

        if (quietActivity == QUIET_ACTIVITY_ATTUNING) {
            double offsetX = (polen.getRandom().nextDouble() - 0.5D) * 0.5D;
            double offsetZ = (polen.getRandom().nextDouble() - 0.5D) * 0.5D;
            addMagicDustParticle(polen, x + offsetX, y - 0.1D, z + offsetZ, 0.90F, polen.getRandom().nextBoolean());
            return;
        }

        if (quietActivity == QUIET_ACTIVITY_ILLUMINATING) {
            double offsetX = (polen.getRandom().nextDouble() - 0.5D) * 0.7D;
            double offsetZ = (polen.getRandom().nextDouble() - 0.5D) * 0.7D;
            addMagicDustParticle(polen, x + offsetX, y + 0.1D, z + offsetZ, 1.0F, polen.tickCount % 24 < 12);
            return;
        }

        if (quietActivity == QUIET_ACTIVITY_REFLECTING) {
            double offsetX = (polen.getRandom().nextDouble() - 0.5D) * 0.3D;
            double offsetZ = (polen.getRandom().nextDouble() - 0.5D) * 0.3D;
            addMagicDustParticle(polen, x + offsetX, y - 0.05D, z + offsetZ, 0.55F, polen.tickCount % 24 < 12);
        }
    }

    public static PolenAutonomousActionPlan pickQuietAction(PolenEntity polen) {
        return PolenAutonomousActionPlanner.pickQuietAction(polen);
    }

    public static int pickQuietActivity(PolenEntity polen) {
        return pickQuietAction(polen).quietActivityType();
    }

    public static String getQuietActivityName(PolenEntity polen) {
        return switch (polen.getQuietActivityType()) {
            case QUIET_ACTIVITY_SINGING -> "singing";
            case QUIET_ACTIVITY_DRAWING -> "drawing";
            case QUIET_ACTIVITY_ATTUNING -> "attuning";
            case QUIET_ACTIVITY_ILLUMINATING -> "illuminating";
            case QUIET_ACTIVITY_REFLECTING -> "reflecting";
            default -> "none";
        };
    }

    private static void addMagicDustParticle(
            PolenEntity polen,
            double x,
            double y,
            double z,
            float scale,
            boolean reverse
    ) {
        DustColorTransitionOptions dust = reverse
                ? new DustColorTransitionOptions(MAGIC_PURPLE, MAGIC_GREEN, scale)
                : new DustColorTransitionOptions(MAGIC_GREEN, MAGIC_PURPLE, scale);
        polen.level().addParticle(dust, x, y, z, 0.0D, 0.02D, 0.0D);
    }
}
