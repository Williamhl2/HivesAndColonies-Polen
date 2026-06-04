package com.hivesandcolonies.characters.bootstrap;

import com.hivesandcolonies.characters.character.polen.client.PolenRenderer;
import com.hivesandcolonies.characters.bootstrap.registry.ModEntities;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod(value = Characters.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Characters.MODID, value = Dist.CLIENT)
public class CharactersClient {

    @SubscribeEvent
    static void onEntityRenderersSetup(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.POLEN.get(), PolenRenderer::new);
    }
}
