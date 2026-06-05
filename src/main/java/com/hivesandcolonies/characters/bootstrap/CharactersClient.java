package com.hivesandcolonies.characters.bootstrap;

import com.hivesandcolonies.characters.character.polen.client.PolenRenderer;
import com.hivesandcolonies.characters.character.befsh.client.BefshRenderer;
import com.hivesandcolonies.characters.bootstrap.registry.ModEntities;
import com.hivesandcolonies.characters.common.client.renderer.SimpleCharacterRenderer;

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
        event.registerEntityRenderer(ModEntities.BEFSH.get(), BefshRenderer::new);
        event.registerEntityRenderer(ModEntities.LUNA.get(), context -> new SimpleCharacterRenderer<>(context, "luna"));
        event.registerEntityRenderer(ModEntities.VANILLA.get(), context -> new SimpleCharacterRenderer<>(context, "vanilla"));
        event.registerEntityRenderer(ModEntities.NOIA.get(), context -> new SimpleCharacterRenderer<>(context, "noia"));
        event.registerEntityRenderer(ModEntities.NORIS.get(), context -> new SimpleCharacterRenderer<>(context, "noris"));
    }
}
