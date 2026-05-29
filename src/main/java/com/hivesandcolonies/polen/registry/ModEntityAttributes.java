package com.hivesandcolonies.polen.registry;

import com.hivesandcolonies.polen.entity.PolenEntity;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

import com.hivesandcolonies.polen.Polen;

@EventBusSubscriber(modid = Polen.MODID)
public class ModEntityAttributes {
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.POLEN.get(), PolenEntity.createAttributes().build());
    }
}