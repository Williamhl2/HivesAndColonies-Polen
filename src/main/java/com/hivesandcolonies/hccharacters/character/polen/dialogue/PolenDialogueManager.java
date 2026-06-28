package com.hivesandcolonies.hccharacters.character.polen.dialogue;

import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.task.PolenTaskType;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.environment.PolenEnvironmentResolver;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.environment.PolenEnvironmentSnapshot;
import com.hivesandcolonies.hccharacters.character.polen.story.PolenMemoryType;
import com.hivesandcolonies.hccharacters.common.util.LocalizedText;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;

public final class PolenDialogueManager {
    public static final String AMBIENT_SINGING = "ambient_singing";
    public static final String AMBIENT_DRAWING = "ambient_drawing";
    public static final String AMBIENT_CURIOSITY = "ambient_curiosity";
    public static final String AMBIENT_TIMID = "ambient_timid";
    public static final String AMBIENT_UNSAFE = "ambient_unsafe";
    public static final String AMBIENT_APPROACH = "ambient_approach";
    public static final String AMBIENT_MAGIC = "ambient_magic";
    public static final String AMBIENT_ILLUMINATION = "ambient_illumination";
    public static final String AMBIENT_REFLECTION = "ambient_reflection";
    public static final String AMBIENT_RAIN_TREE = "ambient_rain_tree";
    public static final String AMBIENT_RAIN_HOUSE = "ambient_rain_house";
    public static final String AMBIENT_RAIN_ROOF = "ambient_rain_roof";
    public static final String AMBIENT_NIGHT_TREE = "ambient_night_tree";
    public static final String AMBIENT_NIGHT_HOUSE = "ambient_night_house";
    public static final String AMBIENT_NIGHT_ROOF = "ambient_night_roof";
    public static final String AMBIENT_BEDTIME = "ambient_bedtime";

    private PolenDialogueManager() {}

    public static Component getDialogue(
            Player player,
            PolenEntity polen,
            int chapter,
            RandomSource random
    ) {
        String key = PolenChapterDialogueResolver.resolveKey(
                chapter,
                polen == null ? "" : polen.getAiState().getLastDialogueKey(),
                random
        );
        if (polen != null) {
            polen.getAiState().rememberDialogueSelection("chapter:" + chapter, key, 0);
        }
        return formatDialogue(player, key);
    }

    public static Component getInteractionDialogue(
            Player player,
            PolenEntity polen,
            int chapter,
            RandomSource random
    ) {
        PolenEnvironmentSnapshot environment = PolenEnvironmentResolver.inspect(polen);
        String situation = PolenDialogueSituationResolver.resolveSituation(polen, environment);
        if (situation != null && shouldPreferContextualDialogue(polen, random)) {
            return getAmbientDialogue(player, polen, situation, environment, random);
        }

        return getDialogue(player, polen, chapter, random);
    }

    public static Component getAmbientDialogue(
            Player player,
            PolenEntity polen,
            String situation,
            RandomSource random
    ) {
        return getAmbientDialogue(player, polen, situation, PolenEnvironmentResolver.inspect(polen), random);
    }

    public static Component getAmbientDialogue(
            Player player,
            PolenEntity polen,
            String situation,
            PolenEnvironmentSnapshot environment,
            RandomSource random
    ) {
        return formatDialogueForPlayer(player, resolveAmbientDialogueKey(player, polen, situation, environment, random));
    }

    public static Component getMemoryDialogue(Player player, PolenMemoryType memory) {
        return formatDialogueForPlayer(player, memory.getDialogueKey());
    }

    public static String resolveAmbientDialogueKey(
            Player player,
            PolenEntity polen,
            String situation,
            PolenEnvironmentSnapshot environment,
            RandomSource random
    ) {
        return PolenAmbientDialogueResolver.resolveKey(player, polen, situation, environment, random);
    }

    public static Component formatDialogueForPlayer(Player player, String key) {
        return LocalizedText.dialogue(
                PolenSpeakerResolver.resolveSpeakerKey(player),
                net.minecraft.ChatFormatting.LIGHT_PURPLE,
                key
        );
    }

    private static boolean shouldPreferContextualDialogue(PolenEntity polen, RandomSource random) {
        if (polen == null) {
            return false;
        }

        PolenTaskType currentTask = polen.getCurrentTask();
        if (polen.isDoingQuietActivity()) {
            return true;
        }

        if (currentTask == null) {
            return random.nextInt(3) == 0;
        }

        if (currentTask.isUrgent()
                || currentTask == PolenTaskType.APPROACH_TRUSTED_PLAYER
                || currentTask == PolenTaskType.INVESTIGATE_INTEREST
                || currentTask == PolenTaskType.SEEK_REST) {
            return true;
        }

        return random.nextInt(3) == 0;
    }

    private static Component formatDialogue(Player player, String key) {
        return formatDialogueForPlayer(player, key);
    }
}
