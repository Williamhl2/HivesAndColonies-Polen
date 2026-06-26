package com.hivesandcolonies.hccharacters.character.soa.progression;

import java.util.function.IntFunction;

import com.hivesandcolonies.hccharacters.character.soa.dialogue.SoaMarjorieDialogue;
import com.hivesandcolonies.hccharacters.common.npc.relationship.NpcRelationshipLevels;
import com.hivesandcolonies.hccharacters.common.npc.relationship.NpcRelationshipManager;
import com.hivesandcolonies.hccharacters.common.npc.relationship.NpcRelationshipRecord;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

public final class SoaMarjorieRelationship {
    public static final String CHARACTER_ID = SoaMarjorieDialogue.PROFILE_ID;
    public static final String DISPLAY_NAME = SoaMarjorieDialogue.SPEAKER;
    public static final IntFunction<String> RANK_RESOLVER = SoaMarjorieRelationship::rankName;

    private static final String FLAG_MET_AT_BOARD = "met_at_board";
    private static final String FLAG_RECEIVED_BOARD_GIFT = "received_board_gift";
    private static final String FLAG_FIRST_CAVE_MINING = "first_cave_mining";
    private static final String FLAG_FIRST_SHARED_ORE = "first_shared_ore";
    private static final String FLAG_ATTACKED_SOA = "attacked_soa";

    private static final String COUNTER_BOARD_VISITS = "board_visits";
    private static final String COUNTER_CAVE_ENCOUNTERS = "cave_encounters";
    private static final String COUNTER_SHARED_ORE = "shared_ore";

    private static final String COOLDOWN_ORE_SHARE = "soa_ore_share_affinity";
    private static final String COOLDOWN_ATTACK_WARNING = "soa_attack_warning_affinity";

    private static final long ORE_SHARE_AFFINITY_COOLDOWN = 20L * 90L;
    private static final long ATTACK_AFFINITY_COOLDOWN = 20L * 300L;

    private SoaMarjorieRelationship() {
    }

    public static NpcRelationshipRecord get(ServerPlayer player) {
        return NpcRelationshipManager.get(player, CHARACTER_ID);
    }

    public static int affinity(ServerPlayer player) {
        return get(player).affinity();
    }

    public static String rankName(int affinity) {
        return switch (NpcRelationshipLevels.rankIndex(affinity)) {
            case 4 -> "relationship.soa.rank.4";
            case 3 -> "relationship.soa.rank.3";
            case 2 -> "relationship.soa.rank.2";
            case 1 -> "relationship.soa.rank.1";
            default -> "relationship.soa.rank.0";
        };
    }

    public static void recordBoardVisit(ServerPlayer player) {
        boolean firstMeeting = !NpcRelationshipManager.hasFlag(player, CHARACTER_ID, FLAG_MET_AT_BOARD);
        NpcRelationshipManager.setFlag(player, CHARACTER_ID, FLAG_MET_AT_BOARD);
        int visits = NpcRelationshipManager.incrementCounter(player, CHARACTER_ID, COUNTER_BOARD_VISITS);
        NpcRelationshipManager.addAffinity(
                player,
                CHARACTER_ID,
                DISPLAY_NAME,
                firstMeeting ? 2 : 1,
                "soa_board_visit",
                firstMeeting
                        ? "relationship.soa.reason.board_visit_first"
                        : "relationship.soa.reason.board_visit_repeat",
                RANK_RESOLVER
        );
        if (visits == 3) {
            NpcRelationshipManager.addAffinity(
                    player,
                    CHARACTER_ID,
                    DISPLAY_NAME,
                    1,
                    "soa_repeat_board_visits",
                    "relationship.soa.reason.board_visit_third",
                    RANK_RESOLVER
            );
        }
    }

    public static void recordBoardGift(ServerPlayer player) {
        boolean firstGift = !NpcRelationshipManager.hasFlag(player, CHARACTER_ID, FLAG_RECEIVED_BOARD_GIFT);
        NpcRelationshipManager.setFlag(player, CHARACTER_ID, FLAG_RECEIVED_BOARD_GIFT);
        NpcRelationshipManager.addAffinity(
                player,
                CHARACTER_ID,
                DISPLAY_NAME,
                firstGift ? 2 : 1,
                "soa_board_gift",
                firstGift
                        ? "relationship.soa.reason.board_gift_first"
                        : "relationship.soa.reason.board_gift_repeat",
                RANK_RESOLVER
        );
    }

    public static void recordCaveEncounter(ServerPlayer player) {
        boolean firstCave = !NpcRelationshipManager.hasFlag(player, CHARACTER_ID, FLAG_FIRST_CAVE_MINING);
        NpcRelationshipManager.setFlag(player, CHARACTER_ID, FLAG_FIRST_CAVE_MINING);
        NpcRelationshipManager.incrementCounter(player, CHARACTER_ID, COUNTER_CAVE_ENCOUNTERS);
        NpcRelationshipManager.addAffinity(
                player,
                CHARACTER_ID,
                DISPLAY_NAME,
                firstCave ? 3 : 2,
                "soa_cave_encounter",
                firstCave
                        ? "relationship.soa.reason.cave_first"
                        : "relationship.soa.reason.cave_repeat",
                RANK_RESOLVER
        );
    }

    public static void recordOreShared(ServerPlayer player, int totalItemsShared) {
        if (totalItemsShared <= 0) {
            return;
        }
        boolean firstShare = !NpcRelationshipManager.hasFlag(player, CHARACTER_ID, FLAG_FIRST_SHARED_ORE);
        NpcRelationshipManager.setFlag(player, CHARACTER_ID, FLAG_FIRST_SHARED_ORE);
        NpcRelationshipManager.incrementCounter(player, CHARACTER_ID, COUNTER_SHARED_ORE);
        NpcRelationshipManager.addAffinityWithCooldown(
                player,
                CHARACTER_ID,
                DISPLAY_NAME,
                firstShare ? 2 : 1,
                COOLDOWN_ORE_SHARE,
                firstShare
                        ? "relationship.soa.reason.ore_share_first"
                        : "relationship.soa.reason.ore_share_repeat",
                ORE_SHARE_AFFINITY_COOLDOWN,
                RANK_RESOLVER
        );
    }

    public static void recordAttack(ServerPlayer player) {
        NpcRelationshipManager.setFlag(player, CHARACTER_ID, FLAG_ATTACKED_SOA);
        NpcRelationshipManager.addAffinityWithCooldown(
                player,
                CHARACTER_ID,
                DISPLAY_NAME,
                -8,
                COOLDOWN_ATTACK_WARNING,
                "relationship.soa.reason.attacked",
                ATTACK_AFFINITY_COOLDOWN,
                RANK_RESOLVER
        );
    }

    public static String arrivalBoardLine(ServerPlayer player) {
        return switch (NpcRelationshipLevels.rankIndex(affinity(player))) {
            case 4 -> "dialogue.soa.marjorie.arrival.board.4";
            case 3 -> "dialogue.soa.marjorie.arrival.board.3";
            case 2 -> "dialogue.soa.marjorie.arrival.board.2";
            case 1 -> "dialogue.soa.marjorie.arrival.board.1";
            default -> "dialogue.soa.marjorie.arrival.board.0";
        };
    }

    public static String arrivalCaveLine(ServerPlayer player) {
        return switch (NpcRelationshipLevels.rankIndex(affinity(player))) {
            case 4 -> "dialogue.soa.marjorie.arrival.cave.4";
            case 3 -> "dialogue.soa.marjorie.arrival.cave.3";
            case 2 -> "dialogue.soa.marjorie.arrival.cave.2";
            case 1 -> "dialogue.soa.marjorie.arrival.cave.1";
            default -> "dialogue.soa.marjorie.arrival.cave.0";
        };
    }

    public static String idleBoardLine(ServerPlayer player, RandomSource random) {
        String[] lines = switch (NpcRelationshipLevels.rankIndex(affinity(player))) {
            case 4 -> new String[] {
                    "dialogue.soa.marjorie.idle.board.4.1",
                    "dialogue.soa.marjorie.idle.board.4.2",
                    "dialogue.soa.marjorie.idle.board.4.3"
            };
            case 3 -> new String[] {
                    "dialogue.soa.marjorie.idle.board.3.1",
                    "dialogue.soa.marjorie.idle.board.3.2",
                    "dialogue.soa.marjorie.idle.board.3.3"
            };
            case 2 -> new String[] {
                    "dialogue.soa.marjorie.idle.board.2.1",
                    "dialogue.soa.marjorie.idle.board.2.2",
                    "dialogue.soa.marjorie.idle.board.2.3"
            };
            case 1 -> new String[] {
                    "dialogue.soa.marjorie.idle.board.1.1",
                    "dialogue.soa.marjorie.idle.board.1.2",
                    "dialogue.soa.marjorie.idle.board.1.3"
            };
            default -> new String[] {
                    "dialogue.soa.marjorie.idle.board.0.1",
                    "dialogue.soa.marjorie.idle.board.0.2",
                    "dialogue.soa.marjorie.idle.board.0.3"
            };
        };
        return lines[random.nextInt(lines.length)];
    }

    public static String idleCaveLine(ServerPlayer player, RandomSource random) {
        String[] lines = switch (NpcRelationshipLevels.rankIndex(affinity(player))) {
            case 4 -> new String[] {
                    "dialogue.soa.marjorie.idle.cave.4.1",
                    "dialogue.soa.marjorie.idle.cave.4.2",
                    "dialogue.soa.marjorie.idle.cave.4.3"
            };
            case 3 -> new String[] {
                    "dialogue.soa.marjorie.idle.cave.3.1",
                    "dialogue.soa.marjorie.idle.cave.3.2",
                    "dialogue.soa.marjorie.idle.cave.3.3"
            };
            case 2 -> new String[] {
                    "dialogue.soa.marjorie.idle.cave.2.1",
                    "dialogue.soa.marjorie.idle.cave.2.2",
                    "dialogue.soa.marjorie.idle.cave.2.3"
            };
            case 1 -> new String[] {
                    "dialogue.soa.marjorie.idle.cave.1.1",
                    "dialogue.soa.marjorie.idle.cave.1.2",
                    "dialogue.soa.marjorie.idle.cave.1.3"
            };
            default -> new String[] {
                    "dialogue.soa.marjorie.idle.cave.0.1",
                    "dialogue.soa.marjorie.idle.cave.0.2",
                    "dialogue.soa.marjorie.idle.cave.0.3"
            };
        };
        return lines[random.nextInt(lines.length)];
    }
}
