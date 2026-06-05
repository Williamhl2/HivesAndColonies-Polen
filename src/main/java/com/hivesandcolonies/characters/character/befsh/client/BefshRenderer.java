package com.hivesandcolonies.characters.character.befsh.client;

import com.hivesandcolonies.characters.bootstrap.Characters;
import com.hivesandcolonies.characters.character.befsh.client.model.BefshModel;
import com.hivesandcolonies.characters.character.befsh.entity.BefshEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BefshRenderer extends MobRenderer<BefshEntity, BefshModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Characters.MODID, "textures/entity/befsh.png");

    public BefshRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new BefshModel(context.bakeLayer(ModelLayers.PLAYER)),
                0.5F
        );
    }

    @Override
    public ResourceLocation getTextureLocation(BefshEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(BefshEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(0.85F, 0.85F, 0.85F);
        super.scale(entity, poseStack, partialTickTime);
    }
}
