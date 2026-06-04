package com.hivesandcolonies.characters.client;

import com.hivesandcolonies.characters.client.profile.PolenProfileView;
import com.hivesandcolonies.characters.client.screen.PolenProfileScreen;
import com.hivesandcolonies.characters.entity.PolenEntity;
import net.minecraft.client.Minecraft;

public final class PolenClientProfileOpener {
    private PolenClientProfileOpener() {
    }

    public static void open(PolenEntity polen) {
        Minecraft.getInstance().setScreen(new PolenProfileScreen(PolenProfileView.from(polen)));
    }
}
