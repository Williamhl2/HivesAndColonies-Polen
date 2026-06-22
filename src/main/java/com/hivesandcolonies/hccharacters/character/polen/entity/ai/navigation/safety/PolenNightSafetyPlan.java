package com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.safety;

import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.search.PolenSearchType;
import net.minecraft.core.BlockPos;

public record PolenNightSafetyPlan(
        BlockPos targetPos,
        PolenSearchType searchType,
        String note,
        boolean placeLightImmediately
) {
    public PolenNightSafetyPlan {
        targetPos = targetPos == null ? null : targetPos.immutable();
        searchType = searchType == null ? PolenSearchType.NIGHT_LIGHT : searchType;
        note = note == null ? "" : note;
    }
}
