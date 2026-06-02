package com.hivesandcolonies.polen.client;

import com.hivesandcolonies.polen.Polen;
import com.hivesandcolonies.polen.client.model.PolenModel;
import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.item.accessory.PolenAccessorySlot;
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
 * Renders Polen's innate affinity charm as a small visible badge on her chest.
 *
 * The charm is still an internal Polen accessory, not a Nest Core and not a
 * vanilla armor slot. This layer only makes the already-equipped charm visible
 * to the player from the first moment of the world.
 */
public class PolenAffinityCharmLayer extends RenderLayer<PolenEntity, PolenModel> {
    private static final float MIN = -3.0F / 16.0F;
    private static final float MAX = 3.0F / 16.0F;
    private static final float TOP = 2.0F / 16.0F;
    private static final float BOTTOM = 8.0F / 16.0F;
    private static final float FRONT_Z = -2.35F / 16.0F;

    public PolenAffinityCharmLayer(RenderLayerParent<PolenEntity, PolenModel> parent) {
        super(parent);
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
        ResourceLocation charmId = polen.getEquippedAccessory(PolenAccessorySlot.CHARM);
        if (charmId == null) {
            return;
        }

        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(
                Polen.MODID,
                "textures/entity/accessory/" + charmId.getPath() + ".png"
        );

        poseStack.pushPose();
        this.getParentModel().body.translateAndRotate(poseStack);

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));

        vertex(consumer, matrix, pose, MIN, BOTTOM, FRONT_Z, 0.0F, 1.0F, packedLight);
        vertex(consumer, matrix, pose, MAX, BOTTOM, FRONT_Z, 1.0F, 1.0F, packedLight);
        vertex(consumer, matrix, pose, MAX, TOP, FRONT_Z, 1.0F, 0.0F, packedLight);
        vertex(consumer, matrix, pose, MIN, TOP, FRONT_Z, 0.0F, 0.0F, packedLight);

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
