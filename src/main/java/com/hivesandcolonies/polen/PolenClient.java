package com.hivesandcolonies.polen;

import com.hivesandcolonies.polen.client.PolenRenderer;
import com.hivesandcolonies.polen.registry.ModEntities;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod(value = Polen.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Polen.MODID, value = Dist.CLIENT)
public class PolenClient {

    @SubscribeEvent
    static void onEntityRenderersSetup(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.POLEN.get(), PolenRenderer::new);
    }
}
