package com.hivesandcolonies.hccharacters.character.polen.client.layer;

import com.hivesandcolonies.hccharacters.bootstrap.HcCharacters;
import com.hivesandcolonies.hccharacters.character.polen.client.model.PolenModel;
import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.identity.PolenWorldAffinity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

/**
 * Visible charm layer for Polen's Curios charm slot.
 *
 * The actual item is registered as a Curios charm and mirrored into Polen's Curios inventory server-side. This layer is
 * only the entity-specific visual anchor, because Curios does not know how Polen's custom model should wear it.
 */
public class PolenAffinityCharmLayer extends RenderLayer<PolenEntity, PolenModel> {
    private static final float LEFT = -1.65F / 16.0F;
    private static final float RIGHT = 1.65F / 16.0F;
    private static final float TOP = 2.2F / 16.0F;
    private static final float BOTTOM = 5.5F / 16.0F;
    private static final float FRONT_Z = -2.08F / 16.0F;

    public PolenAffinityCharmLayer(RenderLayerParent<PolenEntity, PolenModel> renderer) {
        super(renderer);
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            PolenEntity polen,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        PolenWorldAffinity affinity = polen.getEquippedAffinityCharm();
        if (affinity == PolenWorldAffinity.NONE) {
            return;
        }

        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(
                HcCharacters.MODID,
                "textures/entity/accessory/" + affinity.getSerializedName() + "_charm.png"
        );

        poseStack.pushPose();
        this.getParentModel().body.translateAndRotate(poseStack);

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));

        vertex(consumer, matrix, pose, LEFT, BOTTOM, FRONT_Z, 0.0F, 1.0F, packedLight);
        vertex(consumer, matrix, pose, RIGHT, BOTTOM, FRONT_Z, 1.0F, 1.0F, packedLight);
        vertex(consumer, matrix, pose, RIGHT, TOP, FRONT_Z, 1.0F, 0.0F, packedLight);
        vertex(consumer, matrix, pose, LEFT, TOP, FRONT_Z, 0.0F, 0.0F, packedLight);

        poseStack.popPose();
    }

    private static void vertex(
            VertexConsumer consumer,
            Matrix4f matrix,
            PoseStack.Pose pose,
            float x,
            float y,
            float z,
            float u,
            float v,
            int packedLight
    ) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(pose, 0.0F, 0.0F, -1.0F);
    }
}
