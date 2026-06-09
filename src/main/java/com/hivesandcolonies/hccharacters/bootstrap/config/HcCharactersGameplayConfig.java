package com.hivesandcolonies.hccharacters.bootstrap.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class HcCharactersGameplayConfig {
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.BooleanValue ENABLE_SECONDARY_CHARACTER_SPAWN_EGGS;

    private static final ModConfigSpec.BooleanValue SHOW_AFFINITY_NOTIFICATIONS;
    private static final ModConfigSpec.BooleanValue SHOW_AFFINITY_DEBUG_CHAT;
    private static final ModConfigSpec.IntValue AFFINITY_NOTIFICATION_DURATION_TICKS;

    private static final ModConfigSpec.BooleanValue ENABLE_SOA_MARJORIE_ENCOUNTERS;
    private static final ModConfigSpec.BooleanValue ENABLE_SOA_MARJORIE_BOUNTIFUL_BOARD_VISITS;
    private static final ModConfigSpec.BooleanValue ENABLE_SOA_MARJORIE_CAVE_MINING_ENCOUNTERS;
    private static final ModConfigSpec.BooleanValue SOA_MARJORIE_CAN_MINE_BLOCKS;
    private static final ModConfigSpec.BooleanValue SOA_MARJORIE_CAN_PLACE_TORCHES;
    private static final ModConfigSpec.IntValue SOA_MARJORIE_BOARD_VISIT_DURATION_SECONDS;
    private static final ModConfigSpec.IntValue SOA_MARJORIE_CAVE_ENCOUNTER_DURATION_SECONDS;
    private static final ModConfigSpec.IntValue SOA_MARJORIE_BOARD_PLAYER_COOLDOWN_SECONDS;
    private static final ModConfigSpec.IntValue SOA_MARJORIE_BOARD_POSITION_COOLDOWN_SECONDS;
    private static final ModConfigSpec.IntValue SOA_MARJORIE_CAVE_PLAYER_COOLDOWN_SECONDS;
    private static final ModConfigSpec.IntValue SOA_MARJORIE_CAVE_MAX_Y;
    private static final ModConfigSpec.IntValue SOA_MARJORIE_BOARD_SPAWN_CHANCE_DIVISOR;
    private static final ModConfigSpec.IntValue SOA_MARJORIE_CAVE_SPAWN_CHANCE_DIVISOR;
    private static final ModConfigSpec.IntValue SOA_MARJORIE_MAX_BLOCKS_PER_CAVE_ENCOUNTER;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("early_access");
        ENABLE_SECONDARY_CHARACTER_SPAWN_EGGS = builder
                .comment(
                        "Enables spawn eggs and related creative-tab content for non-Polen NPCs.",
                        "Set this to true only when you explicitly want early testing access to Befsh, Luna, Vanilla, Noia, Noris and SoaMarjorie."
                )
                .define("enable_secondary_character_spawn_eggs", false);
        builder.pop();

        builder.push("relationship_feedback");
        SHOW_AFFINITY_NOTIFICATIONS = builder
                .comment("Shows a small client-side HUD notification when a character gains or loses affinity with the player.")
                .define("show_affinity_notifications", true);
        SHOW_AFFINITY_DEBUG_CHAT = builder
                .comment("Also prints affinity changes to chat for debugging. Leave disabled in normal gameplay.")
                .define("show_affinity_debug_chat", false);
        AFFINITY_NOTIFICATION_DURATION_TICKS = builder
                .comment("How long affinity HUD notifications remain visible.")
                .defineInRange("affinity_notification_duration_ticks", 90, 20, 240);
        builder.pop();

        builder.push("soa_marjorie");
        ENABLE_SOA_MARJORIE_ENCOUNTERS = builder
                .comment("Master switch for temporary SoaMarjorie world encounters.")
                .define("enable_encounters", true);
        ENABLE_SOA_MARJORIE_BOUNTIFUL_BOARD_VISITS = builder
                .comment("Allows SoaMarjorie to appear near Bountiful bounty boards for short dialogue/gift visits.")
                .define("enable_bountiful_board_visits", true);
        ENABLE_SOA_MARJORIE_CAVE_MINING_ENCOUNTERS = builder
                .comment("Allows SoaMarjorie to appear temporarily in caves and mine exposed ores near players.")
                .define("enable_cave_mining_encounters", true);
        SOA_MARJORIE_CAN_MINE_BLOCKS = builder
                .comment(
                        "Allows SoaMarjorie to actually break exposed ore blocks during cave encounters.",
                        "She only mines visible vanilla ores, respects mobGriefing, has a per-encounter block cap and does not use Fortune/Silk Touch."
                )
                .define("can_mine_blocks", true);
        SOA_MARJORIE_CAN_PLACE_TORCHES = builder
                .comment("Allows SoaMarjorie to place torches in dark cave encounters. This also respects mobGriefing.")
                .define("can_place_torches", true);
        SOA_MARJORIE_BOARD_VISIT_DURATION_SECONDS = builder
                .comment("How long a SoaMarjorie board visit lasts before she despawns.")
                .defineInRange("board_visit_duration_seconds", 180, 30, 900);
        SOA_MARJORIE_CAVE_ENCOUNTER_DURATION_SECONDS = builder
                .comment("How long a SoaMarjorie cave mining encounter lasts before she despawns.")
                .defineInRange("cave_encounter_duration_seconds", 300, 45, 1200);
        SOA_MARJORIE_BOARD_PLAYER_COOLDOWN_SECONDS = builder
                .comment("Per-player cooldown after a Bountiful board visit spawn attempt succeeds.")
                .defineInRange("board_player_cooldown_seconds", 1800, 60, 21600);
        SOA_MARJORIE_BOARD_POSITION_COOLDOWN_SECONDS = builder
                .comment("Cooldown for each individual Bountiful board after SoaMarjorie appears there.")
                .defineInRange("board_position_cooldown_seconds", 3600, 300, 43200);
        SOA_MARJORIE_CAVE_PLAYER_COOLDOWN_SECONDS = builder
                .comment("Per-player cooldown after a cave mining encounter spawn attempt succeeds.")
                .defineInRange("cave_player_cooldown_seconds", 2400, 60, 21600);
        SOA_MARJORIE_CAVE_MAX_Y = builder
                .comment("Highest Y level where SoaMarjorie may start a cave mining encounter. Default 0 keeps her in deeper caves.")
                .defineInRange("cave_max_y", 0, -64, 64);
        SOA_MARJORIE_BOARD_SPAWN_CHANCE_DIVISOR = builder
                .comment(
                        "Spawn chance divisor checked every manager interval for board visits after cooldowns pass.",
                        "8 means roughly one successful roll every 40 seconds while a valid player waits near a valid board. Cooldowns still apply."
                )
                .defineInRange("board_spawn_chance_divisor", 8, 1, 240);
        SOA_MARJORIE_CAVE_SPAWN_CHANCE_DIVISOR = builder
                .comment(
                        "Spawn chance divisor checked every manager interval for cave encounters after cooldowns pass.",
                        "12 means roughly one successful roll every 60 seconds while the player is in a valid deep cave. Cooldowns still apply."
                )
                .defineInRange("cave_spawn_chance_divisor", 12, 1, 240);
        SOA_MARJORIE_MAX_BLOCKS_PER_CAVE_ENCOUNTER = builder
                .comment("Maximum exposed ore blocks SoaMarjorie may break during one cave encounter.")
                .defineInRange("max_blocks_per_cave_encounter", 8, 0, 64);
        builder.pop();

        SPEC = builder.build();
    }

    private HcCharactersGameplayConfig() {
    }

    public static boolean secondaryCharacterSpawnEggsEnabled() {
        return ENABLE_SECONDARY_CHARACTER_SPAWN_EGGS.get();
    }

    public static boolean showAffinityNotifications() {
        return SHOW_AFFINITY_NOTIFICATIONS.get();
    }

    public static boolean showAffinityDebugChat() {
        return SHOW_AFFINITY_DEBUG_CHAT.get();
    }

    public static int affinityNotificationDurationTicks() {
        return AFFINITY_NOTIFICATION_DURATION_TICKS.get();
    }

    public static boolean soaMarjorieEncountersEnabled() {
        return ENABLE_SOA_MARJORIE_ENCOUNTERS.get();
    }

    public static boolean soaMarjorieBoardVisitsEnabled() {
        return ENABLE_SOA_MARJORIE_BOUNTIFUL_BOARD_VISITS.get();
    }

    public static boolean soaMarjorieCaveMiningEncountersEnabled() {
        return ENABLE_SOA_MARJORIE_CAVE_MINING_ENCOUNTERS.get();
    }

    public static boolean soaMarjorieCanMineBlocks() {
        return SOA_MARJORIE_CAN_MINE_BLOCKS.get();
    }

    public static boolean soaMarjorieCanPlaceTorches() {
        return SOA_MARJORIE_CAN_PLACE_TORCHES.get();
    }

    public static int soaMarjorieBoardVisitDurationTicks() {
        return SOA_MARJORIE_BOARD_VISIT_DURATION_SECONDS.get() * 20;
    }

    public static int soaMarjorieCaveEncounterDurationTicks() {
        return SOA_MARJORIE_CAVE_ENCOUNTER_DURATION_SECONDS.get() * 20;
    }

    public static int soaMarjorieBoardPlayerCooldownTicks() {
        return SOA_MARJORIE_BOARD_PLAYER_COOLDOWN_SECONDS.get() * 20;
    }

    public static int soaMarjorieCavePlayerCooldownTicks() {
        return SOA_MARJORIE_CAVE_PLAYER_COOLDOWN_SECONDS.get() * 20;
    }

    public static int soaMarjorieCaveMaxY() {
        return SOA_MARJORIE_CAVE_MAX_Y.get();
    }

    public static int soaMarjorieBoardSpawnChanceDivisor() {
        return SOA_MARJORIE_BOARD_SPAWN_CHANCE_DIVISOR.get();
    }

    public static int soaMarjorieCaveSpawnChanceDivisor() {
        return SOA_MARJORIE_CAVE_SPAWN_CHANCE_DIVISOR.get();
    }

    public static int soaMarjorieMaxBlocksPerCaveEncounter() {
        return SOA_MARJORIE_MAX_BLOCKS_PER_CAVE_ENCOUNTER.get();
    }

    public static int soaMarjorieBoardPositionCooldownTicks() {
        return SOA_MARJORIE_BOARD_POSITION_COOLDOWN_SECONDS.get() * 20;
    }
}

