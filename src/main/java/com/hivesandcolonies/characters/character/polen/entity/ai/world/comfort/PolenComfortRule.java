package com.hivesandcolonies.characters.character.polen.entity.ai.world.comfort;

import com.hivesandcolonies.characters.character.polen.entity.PolenEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.List;

public interface PolenComfortRule {
    void collect(PolenEntity polen, Level level, BlockPos origin, PolenComfortProfile profile, List<PolenComfortSignal> signals);
}
