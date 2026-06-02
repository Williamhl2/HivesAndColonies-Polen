package com.hivesandcolonies.polen.dialogue;

import com.hivesandcolonies.polen.story.PolenMemoryType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

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

    private PolenDialogueManager() {}

    public static Component getDialogue(
            Player player,
            int chapter,
            RandomSource random
    ) {
        String key = PolenChapterDialogueResolver.resolveKey(chapter, random);

        return Component.translatable(PolenSpeakerResolver.resolveSpeakerKey(player))
                .append(Component.literal(": "))
                .append(Component.translatable(key));
    }

    public static Component getAmbientDialogue(
            Player player,
            String situation,
            RandomSource random
    ) {
        String key = PolenAmbientDialogueResolver.resolveKey(player, situation, random);

        return Component.translatable(PolenSpeakerResolver.resolveSpeakerKey(player))
                .append(Component.literal(": "))
                .append(Component.translatable(key));
    }

    public static Component getMemoryDialogue(Player player, PolenMemoryType memory) {
        return Component.translatable(PolenSpeakerResolver.resolveSpeakerKey(player))
                .append(Component.literal(": "))
                .append(Component.translatable(memory.getDialogueKey()));
    }
}
