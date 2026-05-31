package com.hivesandcolonies.polen.entity.ai.debug;

import com.hivesandcolonies.polen.entity.ai.PolenMood;
import net.minecraft.core.BlockPos;

public record PolenAiDebugSnapshot(
        PolenMood mood,
        String moodReason,
        String quietActivity,
        boolean unsafeArea,
        boolean shouldSeekSafety,
        boolean shouldUseUnsafeDialogue,
        boolean nearRememberedInterest,
        BlockPos flowerSpot,
        BlockPos hiveSpot,
        BlockPos restingSpot,
        BlockPos dangerousSpot
) {
}
