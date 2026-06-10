package com.hivesandcolonies.hccharacters.character.polen.progression;

import com.hivesandcolonies.hccharacters.character.polen.progression.player.PolenPlayerRelationshipManager;

import net.minecraft.world.entity.player.Player;

public final class PolenRelationshipEvents {
    private static final long FIRST_MEETING_COOLDOWN = 20L * 60L * 60L * 24L;
    private static final long GIFT_CATEGORY_COOLDOWN = 20L * 60L * 10L;
    private static final long HOME_ASSIGNMENT_COOLDOWN = 20L * 60L * 30L;
    private static final long FOCUS_COOLDOWN = 20L * 60L * 5L;
    private static final long RESTING_MARKER_COOLDOWN = 20L * 60L * 20L;
    private static final long TRUST_WALK_COOLDOWN = 20L * 60L * 8L;

    private PolenRelationshipEvents() {
    }

    public static boolean firstMeeting(Player player) {
        return PolenPlayerRelationshipManager.addAffinityWithCooldown(
                player,
                2,
                "polen.first_meeting",
                FIRST_MEETING_COOLDOWN,
                "Polen remembers that you approached gently."
        );
    }

    public static boolean gift(Player player, String category, int amount, String reasonText) {
        return PolenPlayerRelationshipManager.addAffinityWithCooldown(
                player,
                amount,
                "polen.gift." + safe(category),
                GIFT_CATEGORY_COOLDOWN,
                reasonText
        );
    }

    public static boolean homeAssigned(Player player) {
        return PolenPlayerRelationshipManager.addAffinityWithCooldown(
                player,
                5,
                "polen.home_assigned",
                HOME_ASSIGNMENT_COOLDOWN,
                "Polen recognizes a place that can be hers."
        );
    }

    public static boolean focus(Player player, String category, int amount, String reasonText) {
        return PolenPlayerRelationshipManager.addAffinityWithCooldown(
                player,
                amount,
                "polen.focus." + safe(category),
                FOCUS_COOLDOWN,
                reasonText
        );
    }

    public static boolean restingMarker(Player player) {
        return PolenPlayerRelationshipManager.addAffinityWithCooldown(
                player,
                2,
                "polen.resting_marker",
                RESTING_MARKER_COOLDOWN,
                "Polen notices that you are trying to mark a safer place."
        );
    }

    public static boolean trustWalkStarted(Player player) {
        return PolenPlayerRelationshipManager.addAffinityWithCooldown(
                player,
                1,
                "polen.trust_walk_started",
                TRUST_WALK_COOLDOWN,
                "Polen is willing to follow you for a little while."
        );
    }

    private static String safe(String category) {
        return category == null || category.isBlank() ? "unknown" : category;
    }
}
