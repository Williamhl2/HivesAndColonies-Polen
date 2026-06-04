package com.hivesandcolonies.characters.registry;

import com.hivesandcolonies.characters.entity.PolenEntity;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

import com.hivesandcolonies.characters.Characters;

@EventBusSubscriber(modid = Characters.MODID)
public class ModEntityAttributes {
    private ModEntityAttributes() {}

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.POLEN.get(), PolenEntity.createAttributes().build());
    }
}
