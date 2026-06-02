package com.hivesandcolonies.polen.entity.ai.debug;

import com.hivesandcolonies.polen.entity.ai.brain.mood.PolenMood;
import com.hivesandcolonies.polen.entity.ai.brain.intent.PolenIntent;
import com.hivesandcolonies.polen.entity.ai.navigation.search.PolenSearchStatus;
import com.hivesandcolonies.polen.entity.ai.navigation.search.PolenSearchType;
import com.hivesandcolonies.polen.entity.ai.brain.need.PolenNeed;
import com.hivesandcolonies.polen.entity.ai.brain.task.PolenTaskStatus;
import com.hivesandcolonies.polen.entity.ai.brain.task.PolenTaskType;
import net.minecraft.core.BlockPos;

public record PolenAiDebugSnapshot(
        PolenMood mood,
        String moodReason,
        PolenIntent intent,
        String intentReason,
        PolenTaskType task,
        PolenTaskType desiredTask,
        PolenTaskStatus taskStatus,
        String taskReason,
        String taskNote,
        PolenTaskType recentFailedTask,
        int recentFailureCount,
        long taskRecoverUntil,
        String quietActivity,
        PolenNeed dominantNeed,
        int safetyNeed,
        int socialNeed,
        int curiosityNeed,
        int restNeed,
        int magicNeed,
        boolean unsafeArea,
        boolean shouldSeekSafety,
        boolean shouldUseUnsafeDialogue,
        PolenSearchType searchType,
        PolenSearchStatus searchStatus,
        String searchNote,
        BlockPos searchTarget,
        BlockPos observedPos,
        boolean nearRememberedInterest,
        BlockPos flowerSpot,
        BlockPos hiveSpot,
        BlockPos sourceSpot,
        BlockPos restingSpot,
        BlockPos dangerousSpot
) {
}
