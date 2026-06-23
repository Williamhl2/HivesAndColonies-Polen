package com.hivesandcolonies.hccharacters.bootstrap.registry;

import com.hivesandcolonies.hccharacters.bootstrap.HcCharacters;
import com.hivesandcolonies.hccharacters.character.befsh.entity.BefshEntity;
import com.hivesandcolonies.hccharacters.character.lucy.entity.LucyEntity;
import com.hivesandcolonies.hccharacters.character.luna.entity.LunaEntity;
import com.hivesandcolonies.hccharacters.character.noia.entity.NoiaEntity;
import com.hivesandcolonies.hccharacters.character.noris.entity.NorisEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.soa.entity.SoaMarjorieEntity;
import com.hivesandcolonies.hccharacters.character.vanilla.entity.VanillaEntity;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = HcCharacters.MODID, bus = EventBusSubscriber.Bus.MOD)
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
        event.put(ModEntities.SOA_MARJORIE.get(), SoaMarjorieEntity.createAttributes().build());
        event.put(ModEntities.LUCY.get(), LucyEntity.createAttributes().build());
    }
}
