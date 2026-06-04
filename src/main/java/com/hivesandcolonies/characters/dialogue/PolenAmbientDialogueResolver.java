package com.hivesandcolonies.characters.dialogue;

import com.hivesandcolonies.characters.progression.PolenAffinityManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

public final class PolenAmbientDialogueResolver {

    private PolenAmbientDialogueResolver() {
    }

    public static String resolveKey(Player player, String situation, RandomSource random) {
        int affinity = PolenAffinityManager.getAffinity(player);
        return resolveKeyForAffinityAndVariation(affinity, situation, 1 + random.nextInt(3));
    }

    public static String resolveKeyForAffinityAndVariation(int affinity, String situation, int variation) {
        String tone = PolenAmbientToneResolver.resolveTone(affinity);
        return "dialogue.polen." + situation + "." + tone + ".line" + variation;
    }
}
