package com.hivesandcolonies.characters.character.polen.dialogue;

import com.hivesandcolonies.characters.bootstrap.Characters;
import com.hivesandcolonies.characters.character.polen.progression.PolenStoryFlag;
import com.hivesandcolonies.characters.character.polen.progression.PolenStoryFlagsManager;
import net.minecraft.world.entity.player.Player;

public final class PolenSpeakerResolver {

    private static final String UNKNOWN_GIRL_KEY = "entity." + Characters.MODID + ".unknown_girl";
    private static final String POLEN_KEY = "entity." + Characters.MODID + ".polen";

    private PolenSpeakerResolver() {
    }

    public static String resolveSpeakerKey(Player player) {
        if (PolenStoryFlagsManager.hasFlag(player, PolenStoryFlag.NAME_REVEALED)) {
            return POLEN_KEY;
        }

        return UNKNOWN_GIRL_KEY;
    }
}
