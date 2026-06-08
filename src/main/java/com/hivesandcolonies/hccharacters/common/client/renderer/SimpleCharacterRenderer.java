package com.hivesandcolonies.hccharacters.common.client.renderer;

import com.hivesandcolonies.hccharacters.bootstrap.HcCharacters;
import com.hivesandcolonies.hccharacters.common.client.model.SimpleCharacterModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.PathfinderMob;

public class SimpleCharacterRenderer<T extends PathfinderMob> extends MobRenderer<T, SimpleCharacterModel<T>> {
    private final ResourceLocation texture;

    public SimpleCharacterRenderer(EntityRendererProvider.Context context, String textureName) {
        super(
                context,
                new SimpleCharacterModel<>(context.bakeLayer(ModelLayers.PLAYER)),
                0.5F
        );
        this.texture = ResourceLocation.fromNamespaceAndPath(
                HcCharacters.MODID,
                "textures/entity/" + textureName + ".png"
        );
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return this.texture;
    }

    @Override
    protected void scale(T entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(0.85F, 0.85F, 0.85F);
        super.scale(entity, poseStack, partialTickTime);
    }
}
