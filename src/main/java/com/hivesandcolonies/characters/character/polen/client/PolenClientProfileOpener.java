package com.hivesandcolonies.characters.character.polen.client;

import com.hivesandcolonies.characters.character.polen.client.profile.PolenProfileView;
import com.hivesandcolonies.characters.character.polen.client.screen.PolenProfileScreen;
import com.hivesandcolonies.characters.character.polen.entity.PolenEntity;
import net.minecraft.client.Minecraft;

public final class PolenClientProfileOpener {
    private PolenClientProfileOpener() {
    }

    public static void open(PolenEntity polen) {
        Minecraft.getInstance().setScreen(new PolenProfileScreen(PolenProfileView.from(polen)));
    }
}
