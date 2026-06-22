package com.hivesandcolonies.hccharacters.character.soa.client.layer;

import com.hivesandcolonies.hccharacters.bootstrap.HcCharacters;
import com.hivesandcolonies.hccharacters.character.soa.entity.SoaMarjorieEntity;
import com.hivesandcolonies.hccharacters.common.client.model.SimpleCharacterModel;
import com.hivesandcolonies.hccharacters.integration.curios.PolenCuriosBridge;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Body-anchored equipment for SoaMarjorie.
 *
 * Sophisticated Backpacks is a required dependency, but its player backpack renderer is not automatically applied to
 * custom NPCs.  This layer therefore draws a backpack with player-backpack proportions on Soa's body while Soa still
 * carries a real sophisticatedbackpacks:backpack in her NPC equipment/Curios state.  Tools are anchored to the belt/body,
 * never to the arms; ItemInHandLayer is only responsible for the tool currently drawn in her hand.
 */
public class SoaMarjorieMiningGearLayer extends RenderLayer<SoaMarjorieEntity, SimpleCharacterModel<SoaMarjorieEntity>> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(HcCharacters.MODID, "soa_marjorie_mining_gear"),
            "main"
    );
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HcCharacters.MODID,
            "textures/entity/soa_marjorie_mining_gear.png"
    );
    private static final ItemStack FALLBACK_PICKAXE = new ItemStack(Items.NETHERITE_PICKAXE);
    private static final ItemStack FALLBACK_AXE = new ItemStack(Items.NETHERITE_AXE);
    private static final ResourceLocation SOPHISTICATED_BACKPACK = ResourceLocation.fromNamespaceAndPath("sophisticatedbackpacks", "backpack");

    private final ItemInHandRenderer itemRenderer;
    private final ModelPart belt;

    public SoaMarjorieMiningGearLayer(
            RenderLayerParent<SoaMarjorieEntity, SimpleCharacterModel<SoaMarjorieEntity>> renderer,
            ItemInHandRenderer itemRenderer,
            ModelPart root
    ) {
        super(renderer);
        this.itemRenderer = itemRenderer;
        this.belt = root.getChild("belt");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Body coordinates use vanilla humanoid pixels: x -4..4, y 0..12, z -2..2.  Positive z is the back.
        // The backpack is intentionally full player-backpack size: broad, deep and centered like Sophisticated Backpacks.
        root.addOrReplaceChild(
                "belt",
                CubeListBuilder.create()
                        .texOffs(0, 31).addBox(-4.25F, 7.35F, -2.42F, 8.5F, 1.0F, 0.38F, new CubeDeformation(0.01F))
                        .texOffs(18, 31).addBox(-4.25F, 7.35F, 2.04F, 8.5F, 1.0F, 0.38F, new CubeDeformation(0.01F))
                        .texOffs(0, 33).addBox(-4.55F, 7.35F, -2.05F, 0.38F, 1.0F, 4.1F, new CubeDeformation(0.01F))
                        .texOffs(10, 33).addBox(4.17F, 7.35F, -2.05F, 0.38F, 1.0F, 4.1F, new CubeDeformation(0.01F))
                        .texOffs(20, 33).addBox(-0.85F, 7.18F, -2.62F, 1.7F, 1.34F, 0.3F, new CubeDeformation(0.01F)),
                PartPose.ZERO
        );

        return LayerDefinition.create(mesh, 64, 64);
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
        poseStack.pushPose();
        this.getParentModel().body.translateAndRotate(poseStack);
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        this.belt.render(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        renderBackpackItem(poseStack, buffer, packedLight, soa);

        // These are body/belt tools.  They disappear only while the corresponding real hand item is drawn.
        if (!isHolding(soa, Items.NETHERITE_PICKAXE)) {
            renderBeltTool(poseStack, buffer, packedLight, soa, toolStack(soa, SoaMarjorieEntity.CURIOS_TOOL_RIGHT_SLOT, FALLBACK_PICKAXE), true);
        }
        if (!isHolding(soa, Items.NETHERITE_AXE)) {
            renderBeltTool(poseStack, buffer, packedLight, soa, toolStack(soa, SoaMarjorieEntity.CURIOS_TOOL_LEFT_SLOT, FALLBACK_AXE), false);
        }
    }

    private void renderBackpackItem(PoseStack poseStack, MultiBufferSource buffer, int packedLight, SoaMarjorieEntity soa) {
        ItemStack backpackStack = toolStack(soa, SoaMarjorieEntity.CURIOS_BACKPACK_SLOT, createSophisticatedBackpackFallback());
        if (backpackStack.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        this.getParentModel().body.translateAndRotate(poseStack);

        // Render the actual Sophisticated Backpacks item model, centered on the back at player-backpack scale.
        // This replaces the old fake backpack cubes and keeps SoaMarjorie's backpack visually tied to the real item.
        // Bigger and closer to the player-sized Sophisticated Backpacks silhouette.
        // Y is slightly higher than the previous value; Z pushes it firmly onto SoaMarjorie's back.
        poseStack.translate(0.0D, 0.2D, 0.15D);

        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(0.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));

        poseStack.scale(1.25F, 1.25F, 1.25F);
        this.itemRenderer.renderItem(
                soa,
                backpackStack,
                ItemDisplayContext.FIXED,
                false,
                poseStack,
                buffer,
                packedLight
        );
        poseStack.popPose();
    }

    private static ItemStack createSophisticatedBackpackFallback() {
        Item item = BuiltInRegistries.ITEM.get(SOPHISTICATED_BACKPACK);
        return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }

    private void renderBeltTool(PoseStack poseStack, MultiBufferSource buffer, int packedLight, SoaMarjorieEntity soa, ItemStack stack, boolean rightHip) {
    if (stack.isEmpty()) {
        return;
    }

    poseStack.pushPose();
    this.getParentModel().body.translateAndRotate(poseStack);

    poseStack.translate(
        rightHip ? -0.305D : 0.305D,
        0.625D,
        -0.025D
    );

    poseStack.mulPose(Axis.YP.rotationDegrees(rightHip ? 82.0F : 98.0F));
    poseStack.mulPose(Axis.XP.rotationDegrees(rightHip ? 168.0F : 192.0F));
    poseStack.mulPose(Axis.ZP.rotationDegrees(-88.0F));
    
    poseStack.scale(0.7F, 0.7F, 0.7F);

    this.itemRenderer.renderItem(
            soa,
            stack,
            ItemDisplayContext.FIXED,
            false,
            poseStack,
            buffer,
            packedLight
    );

    poseStack.popPose();
}

    private static ItemStack toolStack(SoaMarjorieEntity soa, String slot, ItemStack fallback) {
        ItemStack stack = PolenCuriosBridge.getCuriosStack(soa, slot, 0);
        return stack.isEmpty() ? fallback : stack;
    }

    private static boolean isHolding(SoaMarjorieEntity soa, Item item) {
        return soa.getMainHandItem().is(item) || soa.getOffhandItem().is(item);
    }
}
