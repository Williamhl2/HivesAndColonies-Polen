package com.hivesandcolonies.hccharacters.character.polen.progression;

import com.hivesandcolonies.hccharacters.character.polen.progression.world.PolenWorldStorySavedData;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

public final class PolenStoryFlagsManager {

    private PolenStoryFlagsManager() {}

    public static boolean hasFlag(Player player, PolenStoryFlag flag) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        return hasFlag(serverLevel, flag);
    }

    public static boolean hasFlag(ServerLevel level, PolenStoryFlag flag) {
        return PolenWorldStorySavedData.get(level).getData().hasFlag(flag);
    }

    public static void setFlag(Player player, PolenStoryFlag flag) {
        if (player.level() instanceof ServerLevel serverLevel) {
            setFlag(serverLevel, flag);
        }
    }

    public static void setFlag(ServerLevel level, PolenStoryFlag flag) {
        PolenWorldStorySavedData savedData = PolenWorldStorySavedData.get(level);
        if (savedData.getData().setFlag(flag)) {
            savedData.setDirty();
        }
    }

    public static void clearFlag(Player player, PolenStoryFlag flag) {
        if (player.level() instanceof ServerLevel serverLevel) {
            clearFlag(serverLevel, flag);
        }
    }

    public static void clearFlag(ServerLevel level, PolenStoryFlag flag) {
        PolenWorldStorySavedData savedData = PolenWorldStorySavedData.get(level);
        if (savedData.getData().clearFlag(flag)) {
            savedData.setDirty();
        }
    }

    public static void resetFlags(Player player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            resetFlags(serverLevel);
        }
    }

    public static void resetFlags(ServerLevel level) {
        PolenWorldStorySavedData savedData = PolenWorldStorySavedData.get(level);
        if (!savedData.getData().getWorldFlags().isEmpty()) {
            savedData.getData().resetFlags();
            savedData.setDirty();
        }
    }

    public static EnumSet<PolenStoryFlag> getFlags(ServerLevel level) {
        return EnumSet.copyOf(PolenWorldStorySavedData.get(level).getData().getWorldFlags());
    }
}
