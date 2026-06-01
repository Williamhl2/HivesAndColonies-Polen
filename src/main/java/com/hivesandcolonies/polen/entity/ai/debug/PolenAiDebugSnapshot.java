package com.hivesandcolonies.polen.entity.ai.debug;

import com.hivesandcolonies.polen.entity.ai.mood.PolenMood;
import com.hivesandcolonies.polen.entity.ai.intent.PolenIntent;
import com.hivesandcolonies.polen.entity.ai.navigation.PolenSearchStatus;
import com.hivesandcolonies.polen.entity.ai.navigation.PolenSearchType;
import com.hivesandcolonies.polen.entity.ai.need.PolenNeed;
import net.minecraft.core.BlockPos;

public record PolenAiDebugSnapshot(
        PolenMood mood,
        String moodReason,
        PolenIntent intent,
        String intentReason,
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
