package com.hivesandcolonies.hccharacters.common.network;

import com.hivesandcolonies.hccharacters.bootstrap.HcCharacters;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClientboundNpcAffinityNotificationPayload(
        String characterName,
        int delta,
        int oldAffinity,
        int newAffinity,
        String rankName,
        boolean levelUp,
        String message
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClientboundNpcAffinityNotificationPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(HcCharacters.MODID, "npc_affinity_notification"));

    public static final StreamCodec<ByteBuf, ClientboundNpcAffinityNotificationPayload> STREAM_CODEC =
            StreamCodec.ofMember(ClientboundNpcAffinityNotificationPayload::write, ClientboundNpcAffinityNotificationPayload::new);

    private ClientboundNpcAffinityNotificationPayload(ByteBuf buffer) {
        this(
                ByteBufCodecs.STRING_UTF8.decode(buffer),
                ByteBufCodecs.VAR_INT.decode(buffer),
                ByteBufCodecs.VAR_INT.decode(buffer),
                ByteBufCodecs.VAR_INT.decode(buffer),
                ByteBufCodecs.STRING_UTF8.decode(buffer),
                ByteBufCodecs.BOOL.decode(buffer),
                ByteBufCodecs.STRING_UTF8.decode(buffer)
        );
    }

    private void write(ByteBuf buffer) {
        ByteBufCodecs.STRING_UTF8.encode(buffer, this.characterName);
        ByteBufCodecs.VAR_INT.encode(buffer, this.delta);
        ByteBufCodecs.VAR_INT.encode(buffer, this.oldAffinity);
        ByteBufCodecs.VAR_INT.encode(buffer, this.newAffinity);
        ByteBufCodecs.STRING_UTF8.encode(buffer, this.rankName);
        ByteBufCodecs.BOOL.encode(buffer, this.levelUp);
        ByteBufCodecs.STRING_UTF8.encode(buffer, this.message);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
