package com.hivesandcolonies.hccharacters.character.soa.client;

import com.hivesandcolonies.hccharacters.bootstrap.HcCharacters;
import com.hivesandcolonies.hccharacters.character.soa.companion.SoaMartaCompanionController;

import net.minecraft.client.renderer.entity.AllayRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.allay.Allay;

/**
 * Allay renderer that swaps only Marta's texture.
 *
 * A render layer cannot remove the vanilla blue Allay base pass, so tinting or overlaying gold
 * still leaves cyan pixels visible.  Replacing the base texture for tagged Marta entities makes
 * the companion read as a true golden variant while every normal Allay stays vanilla.
 */
public class MartaAllayRenderer extends AllayRenderer {
    private static final ResourceLocation VANILLA_ALLAY_TEXTURE = ResourceLocation.withDefaultNamespace(
            "textures/entity/allay/allay.png"
    );
    private static final ResourceLocation MARTA_GOLDEN_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HcCharacters.MODID,
            "textures/entity/marta/golden_allay.png"
    );

    public MartaAllayRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(Allay allay) {
        if (SoaMartaCompanionController.isGoldenVariantMarta(allay)) {
            return MARTA_GOLDEN_TEXTURE;
        }
        return VANILLA_ALLAY_TEXTURE;
    }
}
