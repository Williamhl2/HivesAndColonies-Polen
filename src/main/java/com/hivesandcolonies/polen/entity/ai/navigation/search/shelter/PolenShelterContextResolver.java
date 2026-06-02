package com.hivesandcolonies.polen.entity.ai.navigation.search.shelter;

import com.hivesandcolonies.polen.dialogue.PolenDialogueManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public final class PolenShelterContextResolver {
    private static final int DOOR_RADIUS = 3;
    private static final int LIGHT_RADIUS = 4;
    private static final int BED_RADIUS = 4;

    private PolenShelterContextResolver() {
    }

    public static PolenShelterKind resolveShelterKind(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return PolenShelterKind.NONE;
        }

        if (isTreeShelter(level, pos)) {
            return PolenShelterKind.TREE;
        }

        if (isHouseInterior(level, pos)) {
            return PolenShelterKind.HOUSE;
        }

        if (isRoofShelter(level, pos)) {
            return PolenShelterKind.ROOF;
        }

        return PolenShelterKind.NONE;
    }

    public static String appendShelterContext(String note, Level level, BlockPos pos) {
        String suffix = switch (resolveShelterKind(level, pos)) {
            case TREE -> "tree";
            case HOUSE -> "house";
            case ROOF -> "roof";
            case NONE -> "unknown";
        };
        return (note == null || note.isBlank() ? "shelter" : note) + ":" + suffix;
    }

    public static String resolveAmbientSituation(Level level, BlockPos pos) {
        PolenShelterKind kind = resolveShelterKind(level, pos);
        if (level != null && level.isRaining()) {
            return switch (kind) {
                case TREE -> PolenDialogueManager.AMBIENT_RAIN_TREE;
                case HOUSE -> PolenDialogueManager.AMBIENT_RAIN_HOUSE;
                case ROOF -> PolenDialogueManager.AMBIENT_RAIN_ROOF;
                case NONE -> PolenDialogueManager.AMBIENT_UNSAFE;
            };
        }

        if (level != null && level.isNight()) {
            return switch (kind) {
                case TREE -> PolenDialogueManager.AMBIENT_NIGHT_TREE;
                case HOUSE -> PolenDialogueManager.AMBIENT_NIGHT_HOUSE;
                case ROOF -> PolenDialogueManager.AMBIENT_NIGHT_ROOF;
                case NONE -> PolenDialogueManager.AMBIENT_ILLUMINATION;
            };
        }

        return null;
    }

    public static boolean isHouseInterior(Level level, BlockPos pos) {
        return hasNearbyDoor(level, pos)
                && countProtectiveSides(level, pos) >= 2
                && hasNearbyLight(level, pos);
    }

    public static boolean isStrongHouseInterior(Level level, BlockPos pos) {
        return hasNearbyDoor(level, pos)
                && !level.canSeeSky(pos.above())
                && countProtectiveSides(level, pos) >= 3
                && (hasNearbyLight(level, pos) || hasNearbyBed(level, pos));
    }

    public static boolean isTreeShelter(Level level, BlockPos pos) {
        for (int dy = 2; dy <= 5; dy++) {
            BlockState state = level.getBlockState(pos.above(dy));
            if (state.is(BlockTags.LEAVES)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isRoofShelter(Level level, BlockPos pos) {
        return !isTreeShelter(level, pos);
    }

    public static boolean hasNearbyDoor(Level level, BlockPos origin) {
        for (int dx = -DOOR_RADIUS; dx <= DOOR_RADIUS; dx++) {
            for (int dz = -DOOR_RADIUS; dz <= DOOR_RADIUS; dz++) {
                for (int dy = -1; dy <= 2; dy++) {
                    if (level.getBlockState(origin.offset(dx, dy, dz)).is(BlockTags.DOORS)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean tryOpenNearbyDoor(Level level, BlockPos origin, int radius) {
        if (level == null || origin == null) {
            return false;
        }

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -1; dy <= 2; dy++) {
                    BlockPos candidate = origin.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(candidate);
                    if (!(state.getBlock() instanceof DoorBlock) || !state.hasProperty(DoorBlock.OPEN)) {
                        continue;
                    }

                    BlockPos doorBase = normalizeDoorBase(candidate, state);
                    BlockState baseState = level.getBlockState(doorBase);
                    if (!baseState.getValue(DoorBlock.OPEN)) {
                        level.setBlock(doorBase, baseState.setValue(DoorBlock.OPEN, true), 10);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean hasNearbyLight(Level level, BlockPos origin) {
        for (int dx = -LIGHT_RADIUS; dx <= LIGHT_RADIUS; dx++) {
            for (int dz = -LIGHT_RADIUS; dz <= LIGHT_RADIUS; dz++) {
                for (int dy = -1; dy <= 2; dy++) {
                    if (level.getBlockState(origin.offset(dx, dy, dz)).getLightEmission() >= 10) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean hasNearbyBed(Level level, BlockPos origin) {
        for (int dx = -BED_RADIUS; dx <= BED_RADIUS; dx++) {
            for (int dz = -BED_RADIUS; dz <= BED_RADIUS; dz++) {
                for (int dy = -1; dy <= 2; dy++) {
                    if (level.getBlockState(origin.offset(dx, dy, dz)).is(BlockTags.BEDS)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static int countProtectiveSides(Level level, BlockPos pos) {
        int sides = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos footPos = pos.relative(direction);
            BlockPos headPos = pos.above().relative(direction);
            if (isProtectiveBlock(level, footPos) || isProtectiveBlock(level, headPos)) {
                sides++;
            }
        }
        return sides;
    }

    private static boolean isProtectiveBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.isAir() && (state.canOcclude() || !state.getCollisionShape(level, pos).isEmpty());
    }

    public static BlockPos normalizeDoorBase(BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof DoorBlock
                && state.hasProperty(DoorBlock.HALF)
                && state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
            return pos.below();
        }

        return pos;
    }
}
