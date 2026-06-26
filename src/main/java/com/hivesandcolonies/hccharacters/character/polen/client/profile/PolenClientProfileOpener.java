package com.hivesandcolonies.hccharacters.character.polen.client.profile;

import com.hivesandcolonies.hccharacters.character.polen.client.screen.PolenProfileScreen;
import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.common.network.ClientboundPolenProfilePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

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

    public static void open(ClientboundPolenProfilePayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.level == null || payload == null) {
            return;
        }

        Entity entity = minecraft.level.getEntity(payload.entityId());
        if (!(entity instanceof PolenEntity polen)) {
            return;
        }

        minecraft.setScreen(new PolenProfileScreen(PolenProfileView.from(polen, payload)));
    }
}
