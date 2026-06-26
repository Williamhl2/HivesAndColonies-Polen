package com.hivesandcolonies.hccharacters.character.soa.client.layer;

import com.hivesandcolonies.hccharacters.bootstrap.HcCharacters;
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
 * she reads like a rare variocolor companion.  The layer uses a dedicated gold texture instead of tinting the
 * vanilla blue Allay texture; tinting cyan pixels with gold makes Marta look green.
 */
public class SoaMartaGoldenVariantLayer extends RenderLayer<Allay, AllayModel> {
    private static final ResourceLocation MARTA_GOLDEN_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HcCharacters.MODID,
            "textures/entity/marta/golden_allay.png"
    );
    private static final int FULL_WHITE_TINT = 0xFFFFFFFF;

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

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(MARTA_GOLDEN_TEXTURE));
        poseStack.pushPose();
        poseStack.scale(1.0125F, 1.0125F, 1.0125F);
        this.getParentModel().renderToBuffer(
                poseStack,
                vertexConsumer,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                FULL_WHITE_TINT
        );
        poseStack.popPose();
    }
}
