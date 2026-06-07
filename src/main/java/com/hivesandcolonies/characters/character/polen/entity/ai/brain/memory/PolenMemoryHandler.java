package com.hivesandcolonies.characters.character.polen.entity.ai.brain.memory;

import com.hivesandcolonies.characters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.characters.character.polen.entity.PolenDangerMemoryTracker;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.interest.PolenInterestLocator;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.routine.PolenRoutinePlanner;
import com.hivesandcolonies.characters.character.polen.entity.ai.navigation.safety.PolenSafetyEvaluator;
import com.hivesandcolonies.characters.character.polen.progression.PolenStoryFlag;
import com.hivesandcolonies.characters.character.polen.progression.PolenStoryFlagsManager;
import com.hivesandcolonies.characters.character.polen.story.PolenMemoryManager;
import com.hivesandcolonies.characters.character.polen.story.PolenMemoryType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;

public final class PolenMemoryHandler {

    private static final int MEMORY_SCAN_RADIUS = 6;

    private PolenMemoryHandler() {
    }

    public static void rememberInterestingSpot(PolenEntity polen, BlockPos pos) {
        if (polen == null || pos == null || !(polen.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (polen.level().getBlockState(pos).is(BlockTags.FLOWERS)) {
            polen.getAiState().setFavoriteFlowerPos(pos.immutable());
            PolenStoryFlagsManager.setFlag(serverLevel, PolenStoryFlag.POLEN_FOUND_FLOWER_SPOT);
            PolenMemoryManager.unlockMemory(
                    serverLevel,
                    PolenMemoryType.FIRST_FLOWER,
                    pos.getX() + 0.5D,
                    pos.getY() + 0.5D,
                    pos.getZ() + 0.5D
            );
            return;
        }

        if (isHive(polen, pos)) {
            polen.getAiState().setFavoriteHivePos(pos.immutable());
            PolenStoryFlagsManager.setFlag(serverLevel, PolenStoryFlag.POLEN_FOUND_HIVE_SPOT);
            PolenMemoryManager.unlockMemory(
                    serverLevel,
                    PolenMemoryType.FIRST_HIVE,
                    pos.getX() + 0.5D,
                    pos.getY() + 0.5D,
                    pos.getZ() + 0.5D
            );
            return;
        }

        if (PolenInterestLocator.isSourceLike(polen.level().getBlockState(pos))) {
            polen.getAiState().setFavoriteSourcePos(pos.immutable());
            PolenMemoryManager.unlockMemory(
                    serverLevel,
                    PolenMemoryType.FIRST_SOURCE,
                    pos.getX() + 0.5D,
                    pos.getY() + 0.5D,
                    pos.getZ() + 0.5D
            );
        }
    }

    public static void rememberRestingSpot(PolenEntity polen, BlockPos pos) {
        if (polen == null || pos == null) {
            return;
        }

        BlockPos normalizedRestingPos = PolenRoutinePlanner.normalizeRestingAnchor(polen, pos);
        if (normalizedRestingPos != null
                && PolenSafetyEvaluator.isSafeStandingSpot(polen, normalizedRestingPos)
                && !PolenDangerMemoryTracker.isDangerousMemorySpot(polen, normalizedRestingPos)) {
            polen.getAiState().setRestingPos(normalizedRestingPos);

            if (polen.level() instanceof ServerLevel serverLevel) {
                PolenStoryFlagsManager.setFlag(serverLevel, PolenStoryFlag.POLEN_FOUND_RESTING_SPOT);
            }
        }
    }

    public static boolean isNearRememberedInterest(PolenEntity polen) {
        return polen.getAiState().getFavoriteFlowerPos() != null
                && polen.getAiState().getFavoriteFlowerPos().closerToCenterThan(polen.position(), 3.5D)
                || polen.getAiState().getFavoriteHivePos() != null
                && polen.getAiState().getFavoriteHivePos().closerToCenterThan(polen.position(), 3.5D)
                || polen.getAiState().getFavoriteSourcePos() != null
                && polen.getAiState().getFavoriteSourcePos().closerToCenterThan(polen.position(), 3.5D);
    }

    public static void seedMemoriesFromNearbyEnvironment(PolenEntity polen) {
        if (polen == null) {
            return;
        }

        if (polen.getAiState().getRestingPos() == null) {
            BlockPos normalizedRestingPos = PolenRoutinePlanner.normalizeRestingAnchor(polen, polen.blockPosition());
            if (normalizedRestingPos != null) {
                polen.getAiState().setRestingPos(normalizedRestingPos);
            }
        }

        if (polen.getAiState().getFavoriteFlowerPos() != null
                && polen.getAiState().getFavoriteHivePos() != null
                && polen.getAiState().getFavoriteSourcePos() != null) {
            return;
        }

        BlockPos origin = polen.blockPosition();

        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-MEMORY_SCAN_RADIUS, -2, -MEMORY_SCAN_RADIUS),
                origin.offset(MEMORY_SCAN_RADIUS, 2, MEMORY_SCAN_RADIUS)
        )) {
            if (polen.getAiState().getFavoriteFlowerPos() == null
                    && polen.level().getBlockState(pos).is(BlockTags.FLOWERS)) {
                rememberInterestingSpot(polen, pos);
            }

            if (polen.getAiState().getFavoriteHivePos() == null && isHive(polen, pos)) {
                rememberInterestingSpot(polen, pos);
            }

            if (polen.getAiState().getFavoriteSourcePos() == null
                    && PolenInterestLocator.isSourceLike(polen.level().getBlockState(pos))) {
                rememberInterestingSpot(polen, pos);
            }

            if (polen.getAiState().getFavoriteFlowerPos() != null
                    && polen.getAiState().getFavoriteHivePos() != null
                    && polen.getAiState().getFavoriteSourcePos() != null) {
                return;
            }
        }
    }

    private static boolean isHive(PolenEntity polen, BlockPos pos) {
        return polen.level().getBlockState(pos).is(Blocks.BEE_NEST)
                || polen.level().getBlockState(pos).is(Blocks.BEEHIVE);
    }
}
