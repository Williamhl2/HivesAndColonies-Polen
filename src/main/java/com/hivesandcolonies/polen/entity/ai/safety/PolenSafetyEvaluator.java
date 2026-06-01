package com.hivesandcolonies.polen.entity.ai.safety;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

public final class PolenSafetyEvaluator {

    private static final int MIN_SAFE_BRIGHTNESS = 8;
    private static final int MIN_TRUE_DANGER_BRIGHTNESS = 4;

    private PolenSafetyEvaluator() {
    }

    public static boolean isSafeStandingSpot(Entity entity, BlockPos pos) {
        if (entity == null || pos == null) {
            return false;
        }

        Level level = entity.level();

        if (!canPhysicallyStandAt(level, pos)) {
            return false;
        }

        if (isNearOutdoorSurface(level, pos)) {
            return true;
        }

        if (level.getMaxLocalRawBrightness(pos) < MIN_SAFE_BRIGHTNESS) {
            return false;
        }

        return isShelteredStandingSpot(level, pos);
    }

    public static boolean isClaustrophobicStandingSpot(Entity entity, BlockPos pos) {
        if (entity == null || pos == null) {
            return false;
        }

        Level level = entity.level();
        return canPhysicallyStandAt(level, pos)
                && level.getMaxLocalRawBrightness(pos) < MIN_SAFE_BRIGHTNESS
                && isUndergroundEnclosedSpot(level, pos);
    }

    public static boolean isTrulyDangerousStandingSpot(Entity entity, BlockPos pos) {
        if (entity == null || pos == null) {
            return false;
        }

        Level level = entity.level();

        if (!level.getFluidState(pos).isEmpty() || !level.getFluidState(pos.above()).isEmpty()) {
            return true;
        }

        if (!level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) {
            return true;
        }

        return level.getMaxLocalRawBrightness(pos) < MIN_TRUE_DANGER_BRIGHTNESS
                && isUndergroundEnclosedSpot(level, pos);
    }

    public static boolean isDangerousMemorySpot(BlockPos dangerousSpotPos, BlockPos pos, double radius) {
        if (dangerousSpotPos == null || pos == null) {
            return false;
        }

        return PolenDangerMemoryMath.isDangerousMemorySpot(
                dangerousSpotPos.getX(),
                dangerousSpotPos.getY(),
                dangerousSpotPos.getZ(),
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                radius
        );
    }

    public static boolean isStandableSpot(Entity entity, BlockPos pos) {
        if (entity == null || pos == null) {
            return false;
        }

        return canPhysicallyStandAt(entity.level(), pos);
    }

    private static boolean canPhysicallyStandAt(Level level, BlockPos pos) {
        if (!level.getFluidState(pos).isEmpty() || !level.getFluidState(pos.above()).isEmpty()) {
            return false;
        }

        if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
            return false;
        }

        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    public static boolean isNearOutdoorSurface(Level level, BlockPos pos) {
        int surfaceY = level.getHeight(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                pos.getX(),
                pos.getZ()
        );

        return pos.getY() >= surfaceY - 2;
    }

    public static boolean isShelteredStandingSpot(Level level, BlockPos pos) {
        if (level.canSeeSky(pos) || !isNearOutdoorSurface(level, pos)) {
            return false;
        }

        return hasOverheadCover(level, pos);
    }

    public static boolean isExposedToRain(Level level, BlockPos pos) {
        return level.isRaining() && level.isRainingAt(pos.above()) && level.canSeeSky(pos.above());
    }

    private static boolean isUndergroundEnclosedSpot(Level level, BlockPos pos) {
        return !level.canSeeSky(pos)
                && !isNearOutdoorSurface(level, pos)
                && hasOverheadCover(level, pos);
    }

    public static boolean hasOverheadCover(Level level, BlockPos pos) {
        for (int dy = 2; dy <= 6; dy++) {
            BlockPos roofPos = pos.above(dy);

            if (level.getBlockState(roofPos).isFaceSturdy(level, roofPos, Direction.DOWN)) {
                return true;
            }
        }

        return false;
    }
}
