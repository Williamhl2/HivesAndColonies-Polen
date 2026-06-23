package com.hivesandcolonies.hccharacters.character.soa.client.layer;

import com.hivesandcolonies.hccharacters.character.soa.companion.SoaMartaCompanionController;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.AllayModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.allay.Allay;

/**
 * Golden overlay used only for Soa Marjorie's Marta companion.
 *
 * Vanilla Allays keep their normal renderer.  Marta gets a tag and a gold-styled name from
 * {@link SoaMartaCompanionController}; this layer draws a translucent honey-gold pass over the regular Allay model so
 * she reads like a rare variocolor companion.
 */
public class SoaMartaGoldenVariantLayer extends RenderLayer<Allay, AllayModel> {
    private static final ResourceLocation ALLAY_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "minecraft",
            "textures/entity/allay/allay.png"
    );
    private static final int HONEY_GOLD_TINT = 0xD8FFD24A;

    public SoaMartaGoldenVariantLayer(RenderLayerParent<Allay, AllayModel> renderer) {
        super(renderer);
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            Allay allay,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        if (!SoaMartaCompanionController.isGoldenVariantMarta(allay)) {
            return;
        }

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(ALLAY_TEXTURE));
        this.getParentModel().renderToBuffer(
                poseStack,
                vertexConsumer,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                HONEY_GOLD_TINT
        );
    }
}
