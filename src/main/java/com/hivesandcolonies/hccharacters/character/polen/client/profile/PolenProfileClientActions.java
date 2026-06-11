package com.hivesandcolonies.hccharacters.character.polen.client.profile;

import com.hivesandcolonies.hccharacters.common.network.ServerboundPolenUiActionPayload;

import net.neoforged.neoforge.network.PacketDistributor;

public final class PolenProfileClientActions {
    public static final String FOLLOW_TOGGLE = "FOLLOW_TOGGLE";
    public static final String RETURN_HOME = "RETURN_HOME";
    public static final String SET_NEARBY_BED_AS_HOME = "SET_NEARBY_BED_AS_HOME";

    private PolenProfileClientActions() {
    }

    public static void send(int entityId, String action) {
        PacketDistributor.sendToServer(new ServerboundPolenUiActionPayload(entityId, action));
    }
}
