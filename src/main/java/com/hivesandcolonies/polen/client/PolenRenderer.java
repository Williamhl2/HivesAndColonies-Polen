package com.hivesandcolonies.polen.client;

import com.hivesandcolonies.polen.Polen;
import com.hivesandcolonies.polen.client.model.PolenModel;
import com.hivesandcolonies.polen.entity.PolenEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class PolenRenderer extends MobRenderer<PolenEntity, PolenModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Polen.MODID, "textures/entity/polen.png");

    public PolenRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new PolenModel(context.bakeLayer(ModelLayers.PLAYER)),
                0.5F
        );
        this.addLayer(new PolenAffinityCharmLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(PolenEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(PolenEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(0.85F, 0.85F, 0.85F);
        super.scale(entity, poseStack, partialTickTime);
    }
}
