package com.hivesandcolonies.polen.registry;

import com.hivesandcolonies.polen.Polen;
import com.hivesandcolonies.polen.entity.PolenEntity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, Polen.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<PolenEntity>> POLEN =
            ENTITY_TYPES.register(
                    "polen",
                    () -> EntityType.Builder.of(PolenEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(8)
                            .build("polen:polen")
            );
}