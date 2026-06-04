package com.hivesandcolonies.characters.bootstrap.registry;

import com.hivesandcolonies.characters.character.polen.entity.PolenEntity;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

import com.hivesandcolonies.characters.bootstrap.Characters;

@EventBusSubscriber(modid = Characters.MODID)
public class ModEntityAttributes {
    private ModEntityAttributes() {}

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.POLEN.get(), PolenEntity.createAttributes().build());
    }
}
