package com.hivesandcolonies.hccharacters.character.soa.client;

import com.hivesandcolonies.hccharacters.character.soa.client.layer.SoaMarjorieMiningGearLayer;
import com.hivesandcolonies.hccharacters.character.soa.entity.SoaMarjorieEntity;
import com.hivesandcolonies.hccharacters.common.client.renderer.SimpleCharacterRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;

public class SoaMarjorieRenderer extends SimpleCharacterRenderer<SoaMarjorieEntity> {
    public SoaMarjorieRenderer(EntityRendererProvider.Context context) {
        super(context, "soa");
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new SoaMarjorieMiningGearLayer(this, context.getItemInHandRenderer(), context.bakeLayer(SoaMarjorieMiningGearLayer.LAYER_LOCATION)));
    }
}
