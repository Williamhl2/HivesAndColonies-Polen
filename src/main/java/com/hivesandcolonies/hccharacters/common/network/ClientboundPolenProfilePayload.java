package com.hivesandcolonies.hccharacters.common.network;

import com.hivesandcolonies.hccharacters.bootstrap.HcCharacters;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClientboundPolenProfilePayload(
        int entityId,
        int affinity,
        int nextThreshold,
        int interactionCount,
        int currentChapter,
        boolean trustWalkUnlocked,
        boolean giftsOnCooldown,
        String relationshipRankText,
        String storyStageKey,
        int beesInterest,
        int magicInterest,
        int coloniesInterest,
        int foodInterest,
        int decorationInterest,
        int explorationInterest,
        boolean firstFlowerMemory,
        boolean firstHiveMemory,
        boolean firstSourceMemory,
        boolean firstColonyMemory,
        boolean firstResidenceMemory
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClientboundPolenProfilePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(HcCharacters.MODID, "polen_profile"));

    public static final StreamCodec<ByteBuf, ClientboundPolenProfilePayload> STREAM_CODEC =
            StreamCodec.ofMember(ClientboundPolenProfilePayload::write, ClientboundPolenProfilePayload::new);

    private ClientboundPolenProfilePayload(ByteBuf buffer) {
        this(
                ByteBufCodecs.VAR_INT.decode(buffer),
                ByteBufCodecs.VAR_INT.decode(buffer),
                ByteBufCodecs.VAR_INT.decode(buffer),
                ByteBufCodecs.VAR_INT.decode(buffer),
                ByteBufCodecs.VAR_INT.decode(buffer),
                ByteBufCodecs.BOOL.decode(buffer),
                ByteBufCodecs.BOOL.decode(buffer),
                ByteBufCodecs.STRING_UTF8.decode(buffer),
                ByteBufCodecs.STRING_UTF8.decode(buffer),
                ByteBufCodecs.VAR_INT.decode(buffer),
                ByteBufCodecs.VAR_INT.decode(buffer),
                ByteBufCodecs.VAR_INT.decode(buffer),
                ByteBufCodecs.VAR_INT.decode(buffer),
                ByteBufCodecs.VAR_INT.decode(buffer),
                ByteBufCodecs.VAR_INT.decode(buffer),
                ByteBufCodecs.BOOL.decode(buffer),
                ByteBufCodecs.BOOL.decode(buffer),
                ByteBufCodecs.BOOL.decode(buffer),
                ByteBufCodecs.BOOL.decode(buffer),
                ByteBufCodecs.BOOL.decode(buffer)
        );
    }

    private void write(ByteBuf buffer) {
        ByteBufCodecs.VAR_INT.encode(buffer, this.entityId);
        ByteBufCodecs.VAR_INT.encode(buffer, this.affinity);
        ByteBufCodecs.VAR_INT.encode(buffer, this.nextThreshold);
        ByteBufCodecs.VAR_INT.encode(buffer, this.interactionCount);
        ByteBufCodecs.VAR_INT.encode(buffer, this.currentChapter);
        ByteBufCodecs.BOOL.encode(buffer, this.trustWalkUnlocked);
        ByteBufCodecs.BOOL.encode(buffer, this.giftsOnCooldown);
        ByteBufCodecs.STRING_UTF8.encode(buffer, this.relationshipRankText == null ? "" : this.relationshipRankText);
        ByteBufCodecs.STRING_UTF8.encode(buffer, this.storyStageKey == null ? "" : this.storyStageKey);
        ByteBufCodecs.VAR_INT.encode(buffer, this.beesInterest);
        ByteBufCodecs.VAR_INT.encode(buffer, this.magicInterest);
        ByteBufCodecs.VAR_INT.encode(buffer, this.coloniesInterest);
        ByteBufCodecs.VAR_INT.encode(buffer, this.foodInterest);
        ByteBufCodecs.VAR_INT.encode(buffer, this.decorationInterest);
        ByteBufCodecs.VAR_INT.encode(buffer, this.explorationInterest);
        ByteBufCodecs.BOOL.encode(buffer, this.firstFlowerMemory);
        ByteBufCodecs.BOOL.encode(buffer, this.firstHiveMemory);
        ByteBufCodecs.BOOL.encode(buffer, this.firstSourceMemory);
        ByteBufCodecs.BOOL.encode(buffer, this.firstColonyMemory);
        ByteBufCodecs.BOOL.encode(buffer, this.firstResidenceMemory);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
