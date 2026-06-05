package com.hivesandcolonies.characters.bootstrap.registry;

import com.hivesandcolonies.characters.bootstrap.Characters;
import com.hivesandcolonies.characters.character.befsh.entity.BefshEntity;
import com.hivesandcolonies.characters.character.luna.entity.LunaEntity;
import com.hivesandcolonies.characters.character.noia.entity.NoiaEntity;
import com.hivesandcolonies.characters.character.noris.entity.NorisEntity;
import com.hivesandcolonies.characters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.characters.character.vanilla.entity.VanillaEntity;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = Characters.MODID)
public class ModEntityAttributes {
    private ModEntityAttributes() {}

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.POLEN.get(), PolenEntity.createAttributes().build());
        event.put(ModEntities.BEFSH.get(), BefshEntity.createAttributes().build());
        event.put(ModEntities.LUNA.get(), LunaEntity.createAttributes().build());
        event.put(ModEntities.VANILLA.get(), VanillaEntity.createAttributes().build());
        event.put(ModEntities.NOIA.get(), NoiaEntity.createAttributes().build());
        event.put(ModEntities.NORIS.get(), NorisEntity.createAttributes().build());
    }
}
