package com.hivesandcolonies.hccharacters.common.network;

import com.hivesandcolonies.hccharacters.bootstrap.HcCharacters;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ServerboundPolenUiActionPayload(
        int entityId,
        String action
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ServerboundPolenUiActionPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(HcCharacters.MODID, "polen_ui_action"));

    public static final StreamCodec<ByteBuf, ServerboundPolenUiActionPayload> STREAM_CODEC =
            StreamCodec.ofMember(ServerboundPolenUiActionPayload::write, ServerboundPolenUiActionPayload::new);

    private ServerboundPolenUiActionPayload(ByteBuf buffer) {
        this(
                ByteBufCodecs.VAR_INT.decode(buffer),
                ByteBufCodecs.STRING_UTF8.decode(buffer)
        );
    }

    private void write(ByteBuf buffer) {
        ByteBufCodecs.VAR_INT.encode(buffer, this.entityId);
        ByteBufCodecs.STRING_UTF8.encode(buffer, this.action == null ? "" : this.action);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
