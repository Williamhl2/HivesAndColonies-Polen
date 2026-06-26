package com.hivesandcolonies.hccharacters.common.network;

import java.lang.reflect.Method;

import com.hivesandcolonies.hccharacters.bootstrap.HcCharacters;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class HcCharactersNetwork {
    private HcCharactersNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                ClientboundNpcAffinityNotificationPayload.TYPE,
                ClientboundNpcAffinityNotificationPayload.STREAM_CODEC,
                HcCharactersNetwork::handleAffinityNotification
        );
        registrar.playToClient(
                ClientboundPolenProfilePayload.TYPE,
                ClientboundPolenProfilePayload.STREAM_CODEC,
                HcCharactersNetwork::handlePolenProfileOpen
        );
        registrar.playToServer(
                ServerboundPolenUiActionPayload.TYPE,
                ServerboundPolenUiActionPayload.STREAM_CODEC,
                PolenUiActionServerHandler::handle
        );
    }

    private static void handleAffinityNotification(ClientboundNpcAffinityNotificationPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                Class<?> clientClass = Class.forName("com.hivesandcolonies.hccharacters.common.client.hud.NpcAffinityOverlayClient");
                Method enqueue = clientClass.getMethod("enqueue", ClientboundNpcAffinityNotificationPayload.class);
                enqueue.invoke(null, payload);
            } catch (ReflectiveOperationException exception) {
                HcCharacters.LOGGER.warn("Could not enqueue NPC affinity notification on the client", exception);
            }
        }).exceptionally(exception -> {
            context.disconnect(Component.literal("Failed to handle NPC affinity notification: " + exception.getMessage()));
            return null;
        });
    }

    private static void handlePolenProfileOpen(ClientboundPolenProfilePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                Class<?> clientClass = Class.forName("com.hivesandcolonies.hccharacters.character.polen.client.profile.PolenClientProfileOpener");
                Method open = clientClass.getMethod("open", ClientboundPolenProfilePayload.class);
                open.invoke(null, payload);
            } catch (ReflectiveOperationException exception) {
                HcCharacters.LOGGER.warn("Could not open Polen profile on the client", exception);
            }
        }).exceptionally(exception -> {
            context.disconnect(Component.literal("Failed to open Polen profile: " + exception.getMessage()));
            return null;
        });
    }
}
