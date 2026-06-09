package com.hivesandcolonies.hccharacters.bootstrap;

import com.hivesandcolonies.hccharacters.bootstrap.registry.ModEntities;
import com.hivesandcolonies.hccharacters.bootstrap.registry.ModItems;
import com.hivesandcolonies.hccharacters.character.befsh.client.BefshRenderer;
import com.hivesandcolonies.hccharacters.character.polen.client.PolenRenderer;
import com.hivesandcolonies.hccharacters.character.polen.item.focus.HiveheartCharmItem;
import com.hivesandcolonies.hccharacters.common.client.renderer.SimpleCharacterRenderer;
import com.hivesandcolonies.hccharacters.character.soa.client.SoaMarjorieRenderer;

import net.minecraft.client.renderer.item.CompassItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = HcCharacters.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = HcCharacters.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class HcCharactersClient {

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(
                ModItems.HIVEHEART_CHARM.get(),
                ResourceLocation.withDefaultNamespace("angle"),
                new CompassItemPropertyFunction((level, stack, entity) -> HiveheartCharmItem.getCompassTarget(stack))
        ));
    }

    @SubscribeEvent
    static void onEntityRenderersSetup(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.POLEN.get(), PolenRenderer::new);
        event.registerEntityRenderer(ModEntities.BEFSH.get(), BefshRenderer::new);
        event.registerEntityRenderer(ModEntities.LUNA.get(), context -> new SimpleCharacterRenderer<>(context, "luna"));
        event.registerEntityRenderer(ModEntities.VANILLA.get(), context -> new SimpleCharacterRenderer<>(context, "vanilla"));
        event.registerEntityRenderer(ModEntities.NOIA.get(), context -> new SimpleCharacterRenderer<>(context, "noia"));
        event.registerEntityRenderer(ModEntities.NORIS.get(), context -> new SimpleCharacterRenderer<>(context, "noris"));
        event.registerEntityRenderer(ModEntities.SOA_MARJORIE.get(), SoaMarjorieRenderer::new);
    }
}
