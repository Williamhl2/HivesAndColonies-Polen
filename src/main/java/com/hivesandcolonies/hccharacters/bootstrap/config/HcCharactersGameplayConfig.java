package com.hivesandcolonies.hccharacters.bootstrap.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class HcCharactersGameplayConfig {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.BooleanValue ENABLE_SECONDARY_CHARACTER_SPAWN_EGGS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("early_access");
        ENABLE_SECONDARY_CHARACTER_SPAWN_EGGS = builder
                .comment(
                        "Enables spawn eggs and related creative-tab content for non-Polen NPCs.",
                        "Set this to true only when you explicitly want early testing access to Befsh, Luna, Vanilla, Noia and Noris."
                )
                .define("enable_secondary_character_spawn_eggs", false);
        builder.pop();
        SPEC = builder.build();
    }

    private HcCharactersGameplayConfig() {
    }

    public static boolean secondaryCharacterSpawnEggsEnabled() {
        return ENABLE_SECONDARY_CHARACTER_SPAWN_EGGS.get();
    }
}
