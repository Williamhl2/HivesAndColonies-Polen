package com.hivesandcolonies.hccharacters.bootstrap.registry;

import com.hivesandcolonies.hccharacters.bootstrap.HcCharacters;
import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.befsh.entity.BefshEntity;
import com.hivesandcolonies.hccharacters.character.luna.entity.LunaEntity;
import com.hivesandcolonies.hccharacters.character.noia.entity.NoiaEntity;
import com.hivesandcolonies.hccharacters.character.noris.entity.NorisEntity;
import com.hivesandcolonies.hccharacters.character.vanilla.entity.VanillaEntity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, HcCharacters.MODID);

    private ModEntities() {}

    public static final DeferredHolder<EntityType<?>, EntityType<PolenEntity>> POLEN =
            ENTITY_TYPES.register(
                    "polen",
                    () -> EntityType.Builder.of(PolenEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(8)
                            .build(HcCharacters.MODID + ":polen")
            );
    public static final DeferredHolder<EntityType<?>, EntityType<BefshEntity>> BEFSH =
            ENTITY_TYPES.register(
                    "befsh",
                    () -> EntityType.Builder.of(BefshEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(8)
                            .build(HcCharacters.MODID + ":befsh")
            );
    public static final DeferredHolder<EntityType<?>, EntityType<LunaEntity>> LUNA =
            ENTITY_TYPES.register(
                    "luna",
                    () -> EntityType.Builder.of(LunaEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(8)
                            .build(HcCharacters.MODID + ":luna")
            );
    public static final DeferredHolder<EntityType<?>, EntityType<VanillaEntity>> VANILLA =
            ENTITY_TYPES.register(
                    "vanilla",
                    () -> EntityType.Builder.of(VanillaEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(8)
                            .build(HcCharacters.MODID + ":vanilla")
            );
    public static final DeferredHolder<EntityType<?>, EntityType<NoiaEntity>> NOIA =
            ENTITY_TYPES.register(
                    "noia",
                    () -> EntityType.Builder.of(NoiaEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(8)
                            .build(HcCharacters.MODID + ":noia")
            );
    public static final DeferredHolder<EntityType<?>, EntityType<NorisEntity>> NORIS =
            ENTITY_TYPES.register(
                    "noris",
                    () -> EntityType.Builder.of(NorisEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(8)
                            .build(HcCharacters.MODID + ":noris")
            );
}
