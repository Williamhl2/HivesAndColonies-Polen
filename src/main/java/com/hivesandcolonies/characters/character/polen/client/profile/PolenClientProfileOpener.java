package com.hivesandcolonies.characters.character.polen.client.profile;

import com.hivesandcolonies.characters.character.polen.client.screen.PolenProfileScreen;
import com.hivesandcolonies.characters.character.polen.entity.PolenEntity;
import net.minecraft.client.Minecraft;

public final class PolenClientProfileOpener {
    private PolenClientProfileOpener() {
    }

    public static void open(PolenEntity polen) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || polen == null) {
            return;
        }

        minecraft.setScreen(new PolenProfileScreen(PolenProfileView.from(polen)));
    }
}
