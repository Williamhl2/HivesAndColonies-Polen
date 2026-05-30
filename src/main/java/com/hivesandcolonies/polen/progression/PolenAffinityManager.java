package com.hivesandcolonies.polen.progression;

import net.minecraft.world.entity.player.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PolenAffinityManager {

    private static final Map<UUID, Integer> AFFINITY = new HashMap<>();

    private PolenAffinityManager() {}

    public static int getAffinity(Player player) {
        return AFFINITY.getOrDefault(player.getUUID(), PolenAffinityLevels.STRANGER);
    }

    public static void setAffinity(Player player, int value) {
        AFFINITY.put(player.getUUID(), PolenAffinityLevels.clamp(value));
    }

    public static void addAffinity(Player player, int amount) {
        setAffinity(player, getAffinity(player) + amount);
    }

    public static void removeAffinity(Player player, int amount) {
        setAffinity(player, getAffinity(player) - amount);
    }

    public static boolean hasReached(Player player, int level) {
        return getAffinity(player) >= level;
    }

    public static boolean hasRevealedName(Player player) {
        return hasReached(player, PolenAffinityLevels.NAME_REVEAL);
    }

    public static boolean isFriend(Player player) {
        return hasReached(player, PolenAffinityLevels.FRIEND);
    }

    public static boolean isCloseFriend(Player player) {
        return hasReached(player, PolenAffinityLevels.CLOSE_FRIEND);
    }

    public static boolean isTrusted(Player player) {
        return hasReached(player, PolenAffinityLevels.TRUSTED);
    }

    public static void resetAffinity(Player player) {
        AFFINITY.remove(player.getUUID());
    }
}