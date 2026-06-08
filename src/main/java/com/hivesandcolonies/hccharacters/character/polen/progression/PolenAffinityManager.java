package com.hivesandcolonies.hccharacters.character.polen.progression;

import com.hivesandcolonies.hccharacters.character.polen.progression.player.PolenPlayerRelationshipManager;

import net.minecraft.world.entity.player.Player;

public final class PolenAffinityManager {

    private PolenAffinityManager() {}

    public static int getAffinity(Player player) {
        return PolenPlayerRelationshipManager.getAffinity(player);
    }

    public static void setAffinity(Player player, int value) {
        PolenPlayerRelationshipManager.setAffinity(player, value);
    }

    public static void addAffinity(Player player, int amount) {
        PolenPlayerRelationshipManager.addAffinity(player, amount);
    }

    public static void removeAffinity(Player player, int amount) {
        PolenPlayerRelationshipManager.removeAffinity(player, amount);
    }

    public static boolean hasReached(Player player, int level) {
        return getAffinity(player) >= level;
    }

    public static boolean hasRevealedName(Player player) {
        return PolenStoryFlagsManager.hasFlag(player, PolenStoryFlag.NAME_REVEALED);
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
        PolenPlayerRelationshipManager.resetAffinity(player);
    }
}
