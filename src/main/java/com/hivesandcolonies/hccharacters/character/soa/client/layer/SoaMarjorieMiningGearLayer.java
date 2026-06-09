package com.hivesandcolonies.hccharacters.character.soa.client.layer;

import com.hivesandcolonies.hccharacters.character.soa.entity.SoaMarjorieEntity;
import com.hivesandcolonies.hccharacters.common.client.model.SimpleCharacterModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Built-in Curios-like mining gear layer for SoaMarjorie.
 *
 * Curios is still useful as a data convention for slots, but NPC render support is not as reliable as player render
 * support. This layer gives Hives & Colonies its own character accessory renderer while keeping the same idea: named
 * slots such as belt/tool_right/tool_left/backpack that can be reused by future characters.
 */
public class SoaMarjorieMiningGearLayer extends RenderLayer<SoaMarjorieEntity, SimpleCharacterModel<SoaMarjorieEntity>> {
    private static final ItemStack WAIST_PICKAXE = new ItemStack(Items.NETHERITE_PICKAXE);
    private static final ItemStack WAIST_AXE = new ItemStack(Items.NETHERITE_AXE);
    private static final ResourceLocation SOPHISTICATED_BACKPACK_ID =
            ResourceLocation.fromNamespaceAndPath("sophisticatedbackpacks", "backpack");
    private static final ItemStack FALLBACK_BACKPACK = new ItemStack(Items.BUNDLE);

    private final ItemInHandRenderer itemRenderer;

    public SoaMarjorieMiningGearLayer(
            RenderLayerParent<SoaMarjorieEntity, SimpleCharacterModel<SoaMarjorieEntity>> renderer,
            ItemInHandRenderer itemRenderer
    ) {
        super(renderer);
        this.itemRenderer = itemRenderer;
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            SoaMarjorieEntity soa,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        renderBackpack(poseStack, buffer, packedLight, soa);
        if (!isHolding(soa, Items.NETHERITE_PICKAXE)) {
            renderWaistPickaxe(poseStack, buffer, packedLight, soa);
        }
        if (!isHolding(soa, Items.NETHERITE_AXE)) {
            renderWaistAxe(poseStack, buffer, packedLight, soa);
        }
    }

    private void renderBackpack(PoseStack poseStack, MultiBufferSource buffer, int packedLight, SoaMarjorieEntity soa) {
        poseStack.pushPose();
        this.getParentModel().body.translateAndRotate(poseStack);

        // Compact, player-like backpack: centered on the upper back and attached to the torso.
        // The body model uses positive Z as the back side; avoid arm transforms and avoid the upside-down X flip.
        poseStack.translate(0.0D, 0.245D, 0.255D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(0.34F, 0.34F, 0.34F);
        this.itemRenderer.renderItem(soa, getBackpackStack(), ItemDisplayContext.FIXED, false, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    private static ItemStack getBackpackStack() {
        Item backpack = BuiltInRegistries.ITEM.getOptional(SOPHISTICATED_BACKPACK_ID).orElse(Items.BUNDLE);
        if (backpack == Items.AIR) {
            return FALLBACK_BACKPACK.copy();
        }
        return new ItemStack(backpack);
    }

    private void renderWaistPickaxe(PoseStack poseStack, MultiBufferSource buffer, int packedLight, SoaMarjorieEntity soa) {
        poseStack.pushPose();
        this.getParentModel().body.translateAndRotate(poseStack);

        // Belt tools are anchored to the torso, not the arms. Keep them small, vertical, and close to the back-hip line.
        poseStack.translate(-0.185D, 0.525D, 0.205D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-28.0F));
        poseStack.scale(0.155F, 0.155F, 0.155F);
        this.itemRenderer.renderItem(soa, WAIST_PICKAXE, ItemDisplayContext.FIXED, false, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    private void renderWaistAxe(PoseStack poseStack, MultiBufferSource buffer, int packedLight, SoaMarjorieEntity soa) {
        poseStack.pushPose();
        this.getParentModel().body.translateAndRotate(poseStack);

        poseStack.translate(0.185D, 0.525D, 0.205D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(28.0F));
        poseStack.scale(0.165F, 0.165F, 0.165F);
        this.itemRenderer.renderItem(soa, WAIST_AXE, ItemDisplayContext.FIXED, false, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    private static boolean isHolding(SoaMarjorieEntity soa, Item item) {
        return soa.getMainHandItem().is(item) || soa.getOffhandItem().is(item);
    }
}
