package com.hivesandcolonies.polen.entity.ai.routine;

import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.ai.safety.PolenSafetyEvaluator;
import com.hivesandcolonies.polen.entity.ai.safety.PolenSafetyNavigator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;

public final class PolenRoutinePlanner {

    private static final int DEFAULT_SAFE_SPOT_RADIUS = 10;
    private static final int MIN_INTEREST_BRIGHTNESS = 8;

    private PolenRoutinePlanner() {
    }

    public static BlockPos getRoutineTarget(PolenEntity polen) {
        if (polen.level().isRaining() || polen.level().isThundering() || polen.level().isNight()) {
            if (isRememberedSpotStillValid(polen, polen.getRestingPos())
                    && PolenSafetyEvaluator.isSafeStandingSpot(polen, polen.getRestingPos())) {
                return polen.getRestingPos();
            }

            return PolenSafetyNavigator.findNearbySafeSurfaceSpot(polen, DEFAULT_SAFE_SPOT_RADIUS);
        }

        long dayTime = polen.level().getDayTime() % 24000L;
        if (dayTime < 6000L
                && isRememberedSpotStillValid(polen, polen.getFavoriteFlowerPos())
                && isSafeInterestSpot(polen, polen.getFavoriteFlowerPos())) {
            return polen.getFavoriteFlowerPos();
        }

        if (dayTime < 12000L
                && isRememberedSpotStillValid(polen, polen.getFavoriteHivePos())
                && isSafeInterestSpot(polen, polen.getFavoriteHivePos())) {
            return polen.getFavoriteHivePos();
        }

        if (isRememberedSpotStillValid(polen, polen.getFavoriteFlowerPos())
                && isSafeInterestSpot(polen, polen.getFavoriteFlowerPos())) {
            return polen.getFavoriteFlowerPos();
        }

        if (isRememberedSpotStillValid(polen, polen.getFavoriteHivePos())
                && isSafeInterestSpot(polen, polen.getFavoriteHivePos())) {
            return polen.getFavoriteHivePos();
        }

        if (isRememberedSpotStillValid(polen, polen.getRestingPos())
                && PolenSafetyEvaluator.isSafeStandingSpot(polen, polen.getRestingPos())) {
            return polen.getRestingPos();
        }

        return PolenSafetyNavigator.findNearbySafeSurfaceSpot(polen, DEFAULT_SAFE_SPOT_RADIUS);
    }

    public static boolean isRememberedSpotStillValid(PolenEntity polen, BlockPos pos) {
        if (pos == null) {
            return false;
        }

        return (!polen.level().getBlockState(pos).isAir() || pos.closerToCenterThan(polen.position(), 2.0D))
                && !polen.isDangerousMemorySpot(pos);
    }

    public static boolean isSafeInterestSpot(PolenEntity polen, BlockPos pos) {
        if (pos == null || polen.isDangerousMemorySpot(pos)) {
            return false;
        }

        int surfaceY = polen.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        boolean nearSurface = pos.getY() >= surfaceY - 2;
        boolean brightEnough = polen.level().getMaxLocalRawBrightness(pos.above()) >= MIN_INTEREST_BRIGHTNESS;

        return nearSurface && brightEnough;
    }
}
