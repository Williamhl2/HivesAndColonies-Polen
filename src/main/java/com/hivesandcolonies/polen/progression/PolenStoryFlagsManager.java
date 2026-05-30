package com.hivesandcolonies.polen.progression;

import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PolenStoryFlagsManager {

    private static final Map<UUID, EnumSet<PolenStoryFlag>> FLAGS =
            new HashMap<>();

    private PolenStoryFlagsManager() {}

    public static boolean hasFlag(
            Player player,
            PolenStoryFlag flag
    ) {
        return FLAGS
                .getOrDefault(
                        player.getUUID(),
                        EnumSet.noneOf(PolenStoryFlag.class)
                )
                .contains(flag);
    }

    public static void setFlag(
            Player player,
            PolenStoryFlag flag
    ) {
        FLAGS.computeIfAbsent(
                player.getUUID(),
                uuid -> EnumSet.noneOf(PolenStoryFlag.class)
        ).add(flag);
    }

    public static void clearFlag(Player player, PolenStoryFlag flag) {
        FLAGS.computeIfAbsent(
                player.getUUID(),
                uuid -> EnumSet.noneOf(PolenStoryFlag.class)
        ).remove(flag);
    }
    
    public static void resetFlags(Player player) {
        FLAGS.remove(player.getUUID());
    }
}