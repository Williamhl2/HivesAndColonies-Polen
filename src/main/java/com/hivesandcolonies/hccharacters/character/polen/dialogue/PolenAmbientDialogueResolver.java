package com.hivesandcolonies.hccharacters.character.polen.dialogue;

import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.environment.PolenEnvironmentResolver;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.environment.PolenEnvironmentSnapshot;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenAffinityManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public final class PolenAmbientDialogueResolver {

    private PolenAmbientDialogueResolver() {
    }

    public static String resolveKey(Player player, PolenEntity polen, String situation, RandomSource random) {
        return resolveKey(player, polen, situation, PolenEnvironmentResolver.inspect(polen), random);
    }

    public static String resolveKey(
            Player player,
            PolenEntity polen,
            String situation,
            PolenEnvironmentSnapshot environment,
            RandomSource random
    ) {
        int affinity = PolenAffinityManager.getAffinity(player);
        PolenDialogueCue cue = selectCue(polen, PolenDialogueBeatResolver.resolveAmbientCues(polen, situation, environment), random);
        int variation = selectVariation(polen, cue, random);
        String key = resolveKeyForCue(affinity, cue, variation);
        if (polen != null) {
            polen.getAiState().rememberDialogueSelection(cue.family(), key, variation);
        }
        return key;
    }

    public static String resolveKeyForAffinityAndVariation(int affinity, String situation, int variation) {
        String tone = PolenAmbientToneResolver.resolveTone(affinity);
        return "dialogue.polen." + situation + "." + tone + ".line" + variation;
    }

    private static String resolveKeyForCue(int affinity, PolenDialogueCue cue, int variation) {
        if (cue.beat() == null || cue.beat().isBlank()) {
            return resolveKeyForAffinityAndVariation(affinity, cue.situation(), variation);
        }

        return "dialogue.polen." + cue.situation() + "." + cue.beat() + ".line" + variation;
    }

    private static PolenDialogueCue selectCue(PolenEntity polen, List<PolenDialogueCue> cues, RandomSource random) {
        if (cues.isEmpty()) {
            return new PolenDialogueCue(PolenDialogueManager.AMBIENT_REFLECTION, "", 3, 1);
        }

        String lastFamily = polen == null ? "" : polen.getAiState().getLastDialogueFamily();
        int totalWeight = 0;
        int[] effectiveWeights = new int[cues.size()];
        for (int i = 0; i < cues.size(); i++) {
            PolenDialogueCue cue = cues.get(i);
            int weight = Math.max(1, cue.weight());
            if (cues.size() > 1 && cue.family().equals(lastFamily)) {
                weight = 1;
            }
            effectiveWeights[i] = weight;
            totalWeight += weight;
        }

        int roll = random.nextInt(Math.max(1, totalWeight));
        for (int i = 0; i < cues.size(); i++) {
            roll -= effectiveWeights[i];
            if (roll < 0) {
                return cues.get(i);
            }
        }

        return cues.get(cues.size() - 1);
    }

    private static int selectVariation(PolenEntity polen, PolenDialogueCue cue, RandomSource random) {
        int variations = Math.max(1, cue.variations());
        int variation = 1 + random.nextInt(variations);
        if (polen == null) {
            return variation;
        }

        if (cue.family().equals(polen.getAiState().getLastDialogueFamily())
                && variation == polen.getAiState().getLastDialogueVariation()
                && variations > 1) {
            variation = variation % variations + 1;
        }
        return variation;
    }
}
