package com.hivesandcolonies.polen.dialogue;

import com.hivesandcolonies.polen.Polen;
import com.hivesandcolonies.polen.progression.PolenStoryFlag;
import com.hivesandcolonies.polen.progression.PolenStoryFlagsManager;
import net.minecraft.world.entity.player.Player;

public final class PolenSpeakerResolver {

    private static final String UNKNOWN_GIRL_KEY = "entity." + Polen.MODID + ".unknown_girl";
    private static final String POLEN_KEY = "entity." + Polen.MODID + ".polen";

    private PolenSpeakerResolver() {
    }

    public static String resolveSpeakerKey(Player player) {
        if (PolenStoryFlagsManager.hasFlag(player, PolenStoryFlag.NAME_REVEALED)) {
            return POLEN_KEY;
        }

        return UNKNOWN_GIRL_KEY;
    }
}
