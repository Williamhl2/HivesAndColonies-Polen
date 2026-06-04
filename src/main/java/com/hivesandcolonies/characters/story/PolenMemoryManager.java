package com.hivesandcolonies.characters.story;

import com.hivesandcolonies.characters.dialogue.PolenDialogueManager;
import com.hivesandcolonies.characters.progression.PolenStoryFlagsManager;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;

public final class PolenMemoryManager {
    private static final double MEMORY_DIALOGUE_RANGE = 8.0D;

    private PolenMemoryManager() {}

    public static void unlockMemory(ServerLevel level, PolenMemoryType memory, double x, double y, double z) {
        if (PolenStoryFlagsManager.hasFlag(level, memory.getFlag())) {
            return;
        }

        PolenStoryFlagsManager.setFlag(level, memory.getFlag());

        for (ServerPlayer player : level.getEntitiesOfClass(
                ServerPlayer.class,
                new AABB(
                        x - MEMORY_DIALOGUE_RANGE,
                        y - MEMORY_DIALOGUE_RANGE,
                        z - MEMORY_DIALOGUE_RANGE,
                        x + MEMORY_DIALOGUE_RANGE,
                        y + MEMORY_DIALOGUE_RANGE,
                        z + MEMORY_DIALOGUE_RANGE
                )
        )) {
            player.displayClientMessage(
                    PolenDialogueManager.getMemoryDialogue(player, memory),
                    false
            );
        }
    }
}